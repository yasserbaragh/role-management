import { IsString, IsNotEmpty, IsOptional, IsArray } from 'class-validator';

export class CreateRoleDto {
    @IsString()
    @IsNotEmpty()
    name!: string;

    @IsArray()
    @IsString({ each: true })
    @IsOptional()
    permissionKeys?: string[];
}
