import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MemberhsipInvitationService } from './memberhsip-invitation.service';
import { MemberhsipInvitationController } from './memberhsip-invitation.controller';
import { MembershipInvitation } from './entities/membership-invitation.entity';
import { Role } from 'src/role/entities/role.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';

@Module({
  imports: [TypeOrmModule.forFeature([MembershipInvitation, Role, UserTable, OrganisationMembership])],
  controllers: [MemberhsipInvitationController],
  providers: [MemberhsipInvitationService],
  exports: [TypeOrmModule],
})
export class MemberhsipInvitationModule {}
