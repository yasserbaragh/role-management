import { Module } from '@nestjs/common';
import { OrganisationService } from './organisation.service';
import { OrganisationController } from './organisation.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Organisation } from './entities/organisation.entity';
import { RoleModule } from 'src/role/role.module';
import { UserTableModule } from 'src/user-table/user-table.module';
import { AuthModule } from 'src/auth/auth.module';
import { OrganisationMembershipModule } from 'src/organisation-membership/organisation-membership.module';
import { Role } from 'src/role/entities/role.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import { MembershipInvitation } from 'src/memberhsip-invitation/entities/membership-invitation.entity';
import { EmailModule } from 'src/email/email.module';

@Module({
  imports: [TypeOrmModule.forFeature([Organisation, Role, UserTable, OrganisationMembership, MembershipInvitation]), RoleModule, UserTableModule, AuthModule, OrganisationMembershipModule, EmailModule],
  controllers: [OrganisationController],
  providers: [OrganisationService],
  exports: [OrganisationService]
})
export class OrganisationModule {}
