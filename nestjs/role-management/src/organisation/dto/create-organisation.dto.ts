import { IsString, IsNotEmpty } from 'class-validator';

export class CreateOrganisationDto {
    @IsString()
    @IsNotEmpty()
    name!: string;

    @IsString()
    @IsNotEmpty()
    type!: string;
}
