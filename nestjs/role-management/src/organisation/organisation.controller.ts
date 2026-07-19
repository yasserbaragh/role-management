import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { OrganisationService } from './organisation.service';
import { CreateOrganisationDto } from './dto/create-organisation.dto';
import { UpdateOrganisationDto } from './dto/update-organisation.dto';
import { CurrentUser } from 'src/common/decorator/current-user/current-user.decorator';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';

@Controller('organisation')
export class OrganisationController {
  constructor(private readonly organisationService: OrganisationService) {}

  @Post()
  create(@CurrentUser() user: JwtPayload,
    @Body() createOrganisationDto: CreateOrganisationDto) {
    return this.organisationService.create(createOrganisationDto, user);
  }

 

 
}
