import { ConflictException, ForbiddenException, Inject, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import type { Cache } from 'cache-manager';
import { CreateRoleDto } from './dto/create-role.dto';
import { UpdateRoleDto } from './dto/update-role.dto';
import { Role } from './entities/role.entity';
import { Permission } from 'src/permission/entities/permission.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import { MembershipInvitation } from 'src/memberhsip-invitation/entities/membership-invitation.entity';
import { membershipCacheKey } from 'src/common/guards/roles/roles.guard';

@Injectable()
export class RoleService {
  constructor(
    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,

    @InjectRepository(Permission)
    private readonly permissionRepository: Repository<Permission>,

    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,

    @InjectRepository(MembershipInvitation)
    private readonly invitationRepository: Repository<MembershipInvitation>,

    @Inject(CACHE_MANAGER)
    private readonly cache: Cache,
  ) {}

  private listKey(organisationId: number) {
    return `roles:org:${organisationId}`;
  }

  private itemKey(id: number, organisationId: number) {
    return `roles:org:${organisationId}:id:${id}`;
  }

  private async invalidate(organisationId: number, id?: number) {
    await this.cache.del(this.listKey(organisationId));
    if (id !== undefined) {
      await this.cache.del(this.itemKey(id, organisationId));
    }
  }

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

    const saved = await this.roleRepository.save(role);
    await this.invalidate(organisationId);
    return saved;
  }

  async findAll(organisationId: number) {
    const key = this.listKey(organisationId);
    const cached = await this.cache.get<Role[]>(key);
    if (cached) {
      return cached;
    }

    const roles = await this.roleRepository.find({
      where: { organisation: { id: organisationId } },
      relations: ['permissions'],
    });
    await this.cache.set(key, roles);
    return roles;
  }

  async findOne(id: number, organisationId: number) {
    const key = this.itemKey(id, organisationId);
    const cached = await this.cache.get<Role>(key);
    if (cached) {
      return cached;
    }

    const role = await this.findScoped(id, organisationId);
    await this.cache.set(key, role);
    return role;
  }

  async update(id: number, updateRoleDto: UpdateRoleDto, organisationId: number) {
    const role = await this.findScoped(id, organisationId);

    if (updateRoleDto.name !== undefined) {
      role.name = updateRoleDto.name;
    }
    if (updateRoleDto.permissionKeys !== undefined) {
      role.permissions = await this.resolvePermissions(updateRoleDto.permissionKeys);
    }

    const saved = await this.roleRepository.save(role);
    await this.invalidate(organisationId, id);

    if (updateRoleDto.permissionKeys !== undefined) {
      const affected = await this.membershipRepository.find({
        where: { role: { id } },
        relations: ['user'],
      });
      await Promise.all(
        affected.map((membership) =>
          this.cache.del(membershipCacheKey(membership.user.id, organisationId)),
        ),
      );
    }

    return saved;
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

    const invitationCount = await this.invitationRepository.count({
      where: { role: { id } },
    });
    if (invitationCount > 0) {
      throw new ConflictException('Role is still referenced by one or more invitation links and cannot be deleted');
    }

    const removed = await this.roleRepository.remove(role);
    await this.invalidate(organisationId, id);
    return removed;
  }
}
