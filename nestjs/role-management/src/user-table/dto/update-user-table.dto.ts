import { PartialType } from '@nestjs/mapped-types';
import { CreateUserTableDto } from './create-user-table.dto';

export class UpdateUserTableDto extends PartialType(CreateUserTableDto) {}
