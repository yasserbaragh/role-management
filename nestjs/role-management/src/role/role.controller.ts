import { Controller, Get, Post, Body, Patch, Param, Delete, ParseIntPipe } from '@nestjs/common';
import { RoleService } from './role.service';
import { CreateRoleDto } from './dto/create-role.dto';
import { UpdateRoleDto } from './dto/update-role.dto';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';
import { CurrentOrganisation } from 'src/common/decorator/current-organisation/current-organisation.decorator';

@Controller('/api/role')
export class RoleController {
  constructor(private readonly roleService: RoleService) {}

  @Permissions('EDIT-ROLE')
  @Post()
  create(
    @Body() createRoleDto: CreateRoleDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.roleService.create(createRoleDto, organisationId);
  }

  @Permissions('VIEW-ROLE')
  @Get()
  findAll(@CurrentOrganisation() organisationId: number) {
    return this.roleService.findAll(organisationId);
  }

  @Permissions('VIEW-ROLE')
  @Get(':id')
  findOne(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.roleService.findOne(id, organisationId);
  }

  @Permissions('EDIT-ROLE')
  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateRoleDto: UpdateRoleDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.roleService.update(id, updateRoleDto, organisationId);
  }

  @Permissions('EDIT-ROLE')
  @Delete(':id')
  remove(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.roleService.remove(id, organisationId);
  }
}
