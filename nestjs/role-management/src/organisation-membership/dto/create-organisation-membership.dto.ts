import { IsInt } from 'class-validator';

export class CreateOrganisationMembershipDto {
    @IsInt()
    userId!: number;

    @IsInt()
    roleId!: number;
}
