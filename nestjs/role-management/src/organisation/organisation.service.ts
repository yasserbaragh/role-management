import { Injectable } from '@nestjs/common';
import { CreateOrganisationDto } from './dto/create-organisation.dto';
import { UpdateOrganisationDto } from './dto/update-organisation.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Organisation } from './entities/organisation.entity';
import { Repository } from 'typeorm';
import { Role } from 'src/role/entities/role.entity';
import { CreateRoleDto } from 'src/role/dto/create-role.dto';
import { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import { CreateOrganisationMembershipDto } from 'src/organisation-membership/dto/create-organisation-membership.dto';

@Injectable()
export class OrganisationService {
  constructor(
    @InjectRepository(Organisation)
    @InjectRepository(Role)
    @InjectRepository(UserTable)
    @InjectRepository(OrganisationMembership)
    private readonly organisationRepository: Repository<Organisation>,
    private readonly roleRepository: Repository<Role>,
    private readonly userRepository: Repository<UserTable>,
    private readonly membershipRepository: Repository<OrganisationMembership>

  ) {}

  async create(createOrganisationDto: CreateOrganisationDto, user: JwtPayload) {
    const newRole = new CreateRoleDto()
    const organisation = this.organisationRepository.create(createOrganisationDto);
    await this.organisationRepository.save(organisation)
    newRole.organisationId = organisation.id
    newRole.isSystemRole = true
    newRole.name = "Admin"
    const savedRole = this.roleRepository.create(newRole)
    await this.roleRepository.save(savedRole)
    const currUser = await this.userRepository.findOne({ where: {
      email: user.email
    }})
    const membership = new CreateOrganisationMembershipDto()
    membership.userId = user.sub
    membership.organisationId = organisation.id
    membership.roleId = savedRole.id
    const newMembership = this.membershipRepository.create()
    await this.membershipRepository.save(newMembership)
    return this.organisationRepository.save(organisation);
  }


}
