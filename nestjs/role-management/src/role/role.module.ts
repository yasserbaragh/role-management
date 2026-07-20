import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { RoleService } from './role.service';
import { RoleController } from './role.controller';
import { Role } from './entities/role.entity';
import { Permission } from 'src/permission/entities/permission.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Role, Permission, OrganisationMembership])],
  controllers: [RoleController],
  providers: [RoleService],
  exports: [TypeOrmModule],
})
export class RoleModule {}
