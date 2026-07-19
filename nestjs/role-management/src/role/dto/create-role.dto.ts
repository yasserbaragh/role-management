import { IsString, IsNotEmpty, IsBoolean, IsOptional, IsInt, IsArray } from 'class-validator';

export class CreateRoleDto {
    @IsString()
    @IsNotEmpty()
    name!: string;

    @IsBoolean()
    @IsOptional()
    isSystemRole?: boolean;

    @IsInt()
    organisationId!: number;

    @IsArray()
    @IsInt({ each: true })
    @IsOptional()
    permissionIds?: number[];
}
