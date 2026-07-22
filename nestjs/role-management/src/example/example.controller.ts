import { Controller, Get, Post, Body, Patch, Param, Delete, ParseIntPipe } from '@nestjs/common';
import { ExampleService } from './example.service';
import { CreateExampleDto } from './dto/create-example.dto';
import { UpdateExampleDto } from './dto/update-example.dto';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';
import { CurrentOrganisation } from 'src/common/decorator/current-organisation/current-organisation.decorator';

@Controller('/api/example')
export class ExampleController {
  constructor(private readonly exampleService: ExampleService) {}

  @Permissions('EDIT-EXAMPLE')
  @Post()
  create(
    @Body() createExampleDto: CreateExampleDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.exampleService.create(createExampleDto, organisationId);
  }

  @Permissions('VIEW-EXAMPLE')
  @Get()
  findAll(@CurrentOrganisation() organisationId: number) {
    return this.exampleService.findAll(organisationId);
  }

  @Permissions('VIEW-EXAMPLE')
  @Get(':id')
  findOne(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.exampleService.findOne(id, organisationId);
  }

  @Permissions('EDIT-EXAMPLE')
  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateExampleDto: UpdateExampleDto,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.exampleService.update(id, updateExampleDto, organisationId);
  }

  @Permissions('EDIT-EXAMPLE')
  @Delete(':id')
  remove(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.exampleService.remove(id, organisationId);
  }
}
