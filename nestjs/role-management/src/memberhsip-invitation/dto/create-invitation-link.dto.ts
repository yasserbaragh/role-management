import { IsInt, IsOptional, IsPositive, Min } from 'class-validator';

export class CreateInvitationLinkDto {
  @IsInt()
  roleId!: number;

  @IsOptional()
  @IsInt()
  @IsPositive()
  maxUses?: number;

  @IsInt()
  @Min(1)
  durationInHours!: number;
}
