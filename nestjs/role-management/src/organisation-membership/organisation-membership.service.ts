import { ConflictException, ForbiddenException, Inject, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import type { Cache } from 'cache-manager';
import { CreateOrganisationMembershipDto } from './dto/create-organisation-membership.dto';
import { UpdateOrganisationMembershipDto } from './dto/update-organisation-membership.dto';
import { OrganisationMembership } from './entities/organisation-membership.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { Role } from 'src/role/entities/role.entity';
import { membershipCacheKey } from 'src/common/guards/roles/roles.guard';
import { EmailService } from 'src/email/email.service';

@Injectable()
export class OrganisationMembershipService {
  constructor(
    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,

    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,

    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,

    @Inject(CACHE_MANAGER)
    private readonly cache: Cache,

    private readonly emailService: EmailService,
  ) {}

  private async findScoped(id: number, organisationId: number) {
    const membership = await this.membershipRepository.findOne({
      where: { id, organisation: { id: organisationId } },
      relations: ['user', 'role'],
    });
    if (!membership) {
      throw new NotFoundException('Membership not found');
    }
    return membership;
  }

  private async resolveRole(roleId: number, organisationId: number) {
    const role = await this.roleRepository.findOne({
      where: { id: roleId, organisation: { id: organisationId } },
    });
    if (!role) {
      throw new NotFoundException('Role not found in this organisation');
    }
    return role;
  }

  async create(createOrganisationMembershipDto: CreateOrganisationMembershipDto, organisationId: number) {
    const user = await this.userRepository.findOne({ where: { id: createOrganisationMembershipDto.userId } });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const role = await this.resolveRole(createOrganisationMembershipDto.roleId, organisationId);

    const existing = await this.membershipRepository.findOne({
      where: { user: { id: user.id }, organisation: { id: organisationId } },
    });
    if (existing) {
      throw new ConflictException('User is already a member of this organisation');
    }

    const membership = this.membershipRepository.create({
      user,
      role,
      organisation: { id: organisationId },
    });

    const saved = await this.membershipRepository.save(membership);

    await this.emailService.send({
      to: user.email,
      subject: 'You were added to an organisation',
      text: `You have been added to an organisation with the role "${role.name}".`,
    });

    return saved;
  }

  findAll(organisationId: number) {
    return this.membershipRepository.find({
      where: { organisation: { id: organisationId } },
      relations: ['user', 'role'],
    });
  }

  findOne(id: number, organisationId: number) {
    return this.findScoped(id, organisationId);
  }

  async update(id: number, updateOrganisationMembershipDto: UpdateOrganisationMembershipDto, organisationId: number) {
    const membership = await this.findScoped(id, organisationId);
    if (membership.isOwner) {
      throw new ForbiddenException('The organisation owner\'s membership cannot be changed');
    }

    if (updateOrganisationMembershipDto.roleId !== undefined) {
      membership.role = await this.resolveRole(updateOrganisationMembershipDto.roleId, organisationId);
    }

    const saved = await this.membershipRepository.save(membership);
    await this.cache.del(membershipCacheKey(membership.user.id, organisationId));
    return saved;
  }

  async remove(id: number, organisationId: number) {
    const membership = await this.findScoped(id, organisationId);
    if (membership.isOwner) {
      throw new ForbiddenException('The organisation owner cannot be removed');
    }

    const removed = await this.membershipRepository.remove(membership);
    await this.cache.del(membershipCacheKey(membership.user.id, organisationId));
    return removed;
  }
}
