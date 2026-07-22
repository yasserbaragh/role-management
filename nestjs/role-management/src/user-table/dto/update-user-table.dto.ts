import { OmitType, PartialType } from '@nestjs/mapped-types';
import { CreateUserTableDto } from './create-user-table.dto';

export class UpdateUserTableDto extends PartialType(OmitType(CreateUserTableDto, ['password'] as const)) {}
