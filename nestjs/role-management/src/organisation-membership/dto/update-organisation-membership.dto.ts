import { PartialType } from '@nestjs/mapped-types';
import { CreateOrganisationMembershipDto } from './create-organisation-membership.dto';

export class UpdateOrganisationMembershipDto extends PartialType(CreateOrganisationMembershipDto) {}
