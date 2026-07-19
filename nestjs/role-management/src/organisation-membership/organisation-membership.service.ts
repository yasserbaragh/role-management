import { Injectable } from '@nestjs/common';
import { CreateOrganisationMembershipDto } from './dto/create-organisation-membership.dto';
import { UpdateOrganisationMembershipDto } from './dto/update-organisation-membership.dto';

@Injectable()
export class OrganisationMembershipService {
  create(createOrganisationMembershipDto: CreateOrganisationMembershipDto) {
    return 'This action adds a new organisationMembership';
  }

  findAll() {
    return `This action returns all organisationMembership`;
  }

  findOne(id: number) {
    return `This action returns a #${id} organisationMembership`;
  }

  update(id: number, updateOrganisationMembershipDto: UpdateOrganisationMembershipDto) {
    return `This action updates a #${id} organisationMembership`;
  }

  remove(id: number) {
    return `This action removes a #${id} organisationMembership`;
  }
}
