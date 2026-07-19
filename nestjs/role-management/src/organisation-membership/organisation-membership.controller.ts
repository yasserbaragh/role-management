import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { OrganisationMembershipService } from './organisation-membership.service';
import { CreateOrganisationMembershipDto } from './dto/create-organisation-membership.dto';
import { UpdateOrganisationMembershipDto } from './dto/update-organisation-membership.dto';

@Controller('organisation-membership')
export class OrganisationMembershipController {
  constructor(private readonly organisationMembershipService: OrganisationMembershipService) {}

  @Post()
  create(@Body() createOrganisationMembershipDto: CreateOrganisationMembershipDto) {
    return this.organisationMembershipService.create(createOrganisationMembershipDto);
  }

  @Get()
  findAll() {
    return this.organisationMembershipService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.organisationMembershipService.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateOrganisationMembershipDto: UpdateOrganisationMembershipDto) {
    return this.organisationMembershipService.update(+id, updateOrganisationMembershipDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.organisationMembershipService.remove(+id);
  }
}
