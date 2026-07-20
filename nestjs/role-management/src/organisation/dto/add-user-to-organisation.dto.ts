import { IsEmail, IsInt } from 'class-validator';

export class AddUserToOrganisationDto {
    @IsEmail()
    email!: string;

    @IsInt()
    roleId!: number;
}
