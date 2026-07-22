import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import type { Cache } from 'cache-manager';
import { Permission } from './entities/permission.entity';

@Injectable()
export class PermissionService {
  constructor(
    @InjectRepository(Permission)
    private readonly permissionRepository: Repository<Permission>,

    @Inject(CACHE_MANAGER)
    private readonly cache: Cache,
  ) {}

  async findAll() {
    const key = 'permissions:all';
    const cached = await this.cache.get<Permission[]>(key);
    if (cached) {
      return cached;
    }

    const permissions = await this.permissionRepository.find();
    await this.cache.set(key, permissions);
    return permissions;
  }

  async findOne(id: number) {
    const key = `permissions:id:${id}`;
    const cached = await this.cache.get<Permission>(key);
    if (cached) {
      return cached;
    }

    const permission = await this.permissionRepository.findOne({ where: { id } });
    if (!permission) {
      throw new NotFoundException('Permission not found');
    }
    await this.cache.set(key, permission);
    return permission;
  }
}
