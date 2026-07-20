import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { OrganisationMembershipService } from './organisation-membership.service';
import { OrganisationMembershipController } from './organisation-membership.controller';
import { OrganisationMembership } from './entities/organisation-membership.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { Role } from 'src/role/entities/role.entity';

@Module({
  imports: [TypeOrmModule.forFeature([OrganisationMembership, UserTable, Role])],
  controllers: [OrganisationMembershipController],
  providers: [OrganisationMembershipService],
  exports: [TypeOrmModule],
})
export class OrganisationMembershipModule {}
