import { ConflictException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CreateOrganisationMembershipDto } from './dto/create-organisation-membership.dto';
import { UpdateOrganisationMembershipDto } from './dto/update-organisation-membership.dto';
import { OrganisationMembership } from './entities/organisation-membership.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { Role } from 'src/role/entities/role.entity';

@Injectable()
export class OrganisationMembershipService {
  constructor(
    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,

    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,

    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,
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

    return this.membershipRepository.save(membership);
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

    if (updateOrganisationMembershipDto.roleId !== undefined) {
      membership.role = await this.resolveRole(updateOrganisationMembershipDto.roleId, organisationId);
    }

    return this.membershipRepository.save(membership);
  }

  async remove(id: number, organisationId: number) {
    const membership = await this.findScoped(id, organisationId);
    return this.membershipRepository.remove(membership);
  }
}
