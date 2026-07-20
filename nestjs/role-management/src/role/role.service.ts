import { ConflictException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { CreateRoleDto } from './dto/create-role.dto';
import { UpdateRoleDto } from './dto/update-role.dto';
import { Role } from './entities/role.entity';
import { Permission } from 'src/permission/entities/permission.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';

@Injectable()
export class RoleService {
  constructor(
    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,

    @InjectRepository(Permission)
    private readonly permissionRepository: Repository<Permission>,

    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,
  ) {}

  private resolvePermissions(permissionKeys?: string[]) {
    if (!permissionKeys?.length) return [];
    return this.permissionRepository.find({ where: { key: In(permissionKeys) } });
  }

  private async findScoped(id: number, organisationId: number) {
    const role = await this.roleRepository.findOne({
      where: { id, organisation: { id: organisationId } },
      relations: ['permissions'],
    });
    if (!role) {
      throw new NotFoundException('Role not found');
    }
    return role;
  }

  async create(createRoleDto: CreateRoleDto, organisationId: number) {
    const permissions = await this.resolvePermissions(createRoleDto.permissionKeys);

    const role = this.roleRepository.create({
      name: createRoleDto.name,
      isSystemRole: false,
      organisation: { id: organisationId },
      permissions,
    });

    return this.roleRepository.save(role);
  }

  findAll(organisationId: number) {
    return this.roleRepository.find({
      where: { organisation: { id: organisationId } },
      relations: ['permissions'],
    });
  }

  findOne(id: number, organisationId: number) {
    return this.findScoped(id, organisationId);
  }

  async update(id: number, updateRoleDto: UpdateRoleDto, organisationId: number) {
    const role = await this.findScoped(id, organisationId);

    if (updateRoleDto.name !== undefined) {
      role.name = updateRoleDto.name;
    }
    if (updateRoleDto.permissionKeys !== undefined) {
      role.permissions = await this.resolvePermissions(updateRoleDto.permissionKeys);
    }

    return this.roleRepository.save(role);
  }

  async remove(id: number, organisationId: number) {
    const role = await this.findScoped(id, organisationId);
    if (role.isSystemRole) {
      throw new ForbiddenException('System roles cannot be deleted');
    }

    const membershipCount = await this.membershipRepository.count({
      where: { role: { id } },
    });
    if (membershipCount > 0) {
      throw new ConflictException('Role is still assigned to one or more members and cannot be deleted');
    }

    return this.roleRepository.remove(role);
  }
}
