import { IsString, IsNotEmpty } from 'class-validator';

export class CreatePermissionDto {
    @IsString()
    @IsNotEmpty()
    key!: string;

    @IsString()
    @IsNotEmpty()
    label!: string;
}
