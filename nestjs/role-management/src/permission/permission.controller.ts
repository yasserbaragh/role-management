import { Controller, Get, Param, ParseIntPipe } from '@nestjs/common';
import { PermissionService } from './permission.service';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';

@Controller('/api/permission')
export class PermissionController {
  constructor(private readonly permissionService: PermissionService) {}

  @Permissions('VIEW-PERMISSION')
  @Get()
  findAll() {
    return this.permissionService.findAll();
  }

  @Permissions('VIEW-PERMISSION')
  @Get(':id')
  findOne(@Param('id', ParseIntPipe) id: number) {
    return this.permissionService.findOne(id);
  }
}
