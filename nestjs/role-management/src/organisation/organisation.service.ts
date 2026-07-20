import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { CreateOrganisationDto } from './dto/create-organisation.dto';
import { UpdateOrganisationDto } from './dto/update-organisation.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Organisation } from './entities/organisation.entity';
import { Repository } from 'typeorm';
import { Role } from 'src/role/entities/role.entity';
import { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';

@Injectable()
export class OrganisationService {
  constructor(
    @InjectRepository(Organisation)
    private readonly organisationRepository: Repository<Organisation>,

    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,

    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,

    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>
  ) {}

  private async ensureOwner(organisationId: number, userId: number) {
    const membership = await this.membershipRepository.findOne({
      where: {
        organisation: { id: organisationId },
        user: { id: userId },
      },
      relations: ['organisation'],
    })

    if (!membership || !membership.isOwner) {
      throw new ForbiddenException('Only the organisation owner can perform this action')
    }

    return membership.organisation
  }

  async create(createOrganisationDto: CreateOrganisationDto, user: JwtPayload) {
    const organisation = this.organisationRepository.create(createOrganisationDto);
    await this.organisationRepository.save(organisation)
    const savedRole = this.roleRepository.create({
      name: 'Admin',
      isSystemRole: true,
      organisation,
    })
    await this.roleRepository.save(savedRole)
    const currUser = await this.userRepository.findOne({ where: {
      email: user.email
    }})
    if (!currUser) {
      throw new NotFoundException('User not found')
    }

    const newMembership = this.membershipRepository.create({
      user: currUser,
      organisation,
      role: savedRole,
      isOwner: true,
    })
    await this.membershipRepository.save(newMembership)
    return this.organisationRepository.save(organisation);
  }

  async selectOrganisation(id: number, user: JwtPayload) {
    const membership = await this.membershipRepository.findOne({
      where: {
        organisation: { id },
        user: { id: user.sub },
      },
      relations: ['organisation'],
    })

    if (!membership) {
      throw new ForbiddenException('You are not a member of this organisation')
    }

    return membership.organisation
  }

  async update(id: number, updateOrganisationDto: UpdateOrganisationDto, user: JwtPayload) {
    const organisation = await this.ensureOwner(id, user.sub)
    Object.assign(organisation, updateOrganisationDto)
    return this.organisationRepository.save(organisation)
  }

  async remove(id: number, user: JwtPayload) {
    const organisation = await this.ensureOwner(id, user.sub)

    const memberships = await this.membershipRepository.find({ where: { organisation: { id } } })
    await this.membershipRepository.remove(memberships)

    const roles = await this.roleRepository.find({ where: { organisation: { id } } })
    await this.roleRepository.remove(roles)

    return this.organisationRepository.remove(organisation)
  }

}
