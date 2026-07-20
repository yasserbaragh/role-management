import { Controller, Get, Post, Body, Patch, Param, Delete, ParseIntPipe, Res } from '@nestjs/common';
import { OrganisationService } from './organisation.service';
import { CreateOrganisationDto } from './dto/create-organisation.dto';
import { UpdateOrganisationDto } from './dto/update-organisation.dto';
import { CurrentUser } from 'src/common/decorator/current-user/current-user.decorator';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';
import * as express from 'express';
import { CurrentOrganisation } from 'src/common/decorator/current-organisation/current-organisation.decorator';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';
import { AddUserToOrganisationDto } from './dto/add-user-to-organisation.dto';

@Controller('/api/organisation')
export class OrganisationController {
  constructor(private readonly organisationService: OrganisationService) {}

  @Post()
  create(@CurrentUser() user: JwtPayload,
    @Body() createOrganisationDto: CreateOrganisationDto) {
    return this.organisationService.create(createOrganisationDto, user);
  }

  @Post('/:id/select')
  async selectOrg(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Res({ passthrough: true }) res: express.Response,
  ) {
    const organisation = await this.organisationService.selectOrganisation(id, user);

    res.cookie('organisationId', organisation.id, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 60 * 60 * 1000,
    });

    return organisation;
  }

  @Permissions('CREATE-MEMBERSHIP')
  @Post('/:id/add-user')
  addUserToOrg(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
    @Body() addUserToOrganisationDto: AddUserToOrganisationDto,
  ) {
    return this.organisationService.addUserToOrganisation(id, organisationId, addUserToOrganisationDto);
  }

  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateOrganisationDto: UpdateOrganisationDto,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.organisationService.update(id, updateOrganisationDto, user);
  }

  @Delete(':id')
  remove(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.organisationService.remove(id, user);
  }
}
