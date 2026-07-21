import { Controller, Get, Post, Body, Patch, Param, Delete, ParseIntPipe } from '@nestjs/common';
import { OrganisationMembershipService } from './organisation-membership.service';
import { CreateOrganisationMembershipDto } from './dto/create-organisation-membership.dto';
import { UpdateOrganisationMembershipDto } from './dto/update-organisation-membership.dto';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';
import { CurrentOrganisation } from 'src/common/decorator/current-organisation/current-organisation.decorator';

@Controller('/api/organisation-membership')
export class OrganisationMembershipController {
  constructor(private readonly organisationMembershipService: OrganisationMembershipService) {}

  @Permissions('EDIT-MEMBERSHIP')
  @Post()
  create(
    @Body() createOrganisationMembershipDto: CreateOrganisationMembershipDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.organisationMembershipService.create(createOrganisationMembershipDto, organisationId);
  }

  @Permissions('READ-MEMBERSHIP')
  @Get()
  findAll(@CurrentOrganisation() organisationId: number) {
    return this.organisationMembershipService.findAll(organisationId);
  }

  @Permissions('READ-MEMBERSHIP')
  @Get(':id')
  findOne(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.organisationMembershipService.findOne(id, organisationId);
  }

  @Permissions('EDIT-MEMBERSHIP')
  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateOrganisationMembershipDto: UpdateOrganisationMembershipDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.organisationMembershipService.update(id, updateOrganisationMembershipDto, organisationId);
  }

  @Permissions('EDIT-MEMBERSHIP')
  @Delete(':id')
  remove(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.organisationMembershipService.remove(id, organisationId);
  }
}
