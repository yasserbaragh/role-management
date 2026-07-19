import { IsInt } from 'class-validator';

export class CreateOrganisationMembershipDto {
    @IsInt()
    userId!: number;

    @IsInt()
    organisationId!: number;

    @IsInt()
    roleId!: number;
}
