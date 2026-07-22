import { Module } from '@nestjs/common';
import { UserTableService } from './user-table.service';
import { UserTableController } from './user-table.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserTable } from './entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import { AuthModule } from '../auth/auth.module';
import { EmailModule } from 'src/email/email.module';

@Module({
  imports: [TypeOrmModule.forFeature([UserTable, OrganisationMembership]), AuthModule, EmailModule],
  controllers: [UserTableController],
  providers: [UserTableService],
  exports: [UserTableService],
})
export class UserTableModule {}
