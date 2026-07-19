import { Module } from '@nestjs/common';
import { OrganisationMembershipService } from './organisation-membership.service';
import { OrganisationMembershipController } from './organisation-membership.controller';

@Module({
  controllers: [OrganisationMembershipController],
  providers: [OrganisationMembershipService],
})
export class OrganisationMembershipModule {}
