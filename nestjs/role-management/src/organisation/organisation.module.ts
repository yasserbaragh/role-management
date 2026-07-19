import { Module } from '@nestjs/common';
import { OrganisationService } from './organisation.service';
import { OrganisationController } from './organisation.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Organisation } from './entities/organisation.entity';
import { RoleModule } from 'src/role/role.module';
import { UserTableModule } from 'src/user-table/user-table.module';
import { AuthModule } from 'src/auth/auth.module';

@Module({
  imports: [TypeOrmModule.forFeature([Organisation]), RoleModule, UserTableModule, AuthModule],
  controllers: [OrganisationController],
  providers: [OrganisationService],
  exports: [OrganisationService]
})
export class OrganisationModule {}
