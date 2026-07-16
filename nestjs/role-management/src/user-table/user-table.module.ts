import { Module } from '@nestjs/common';
import { UserTableService } from './user-table.service';
import { UserTableController } from './user-table.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserTable } from './entities/user-table.entity';
import { AuthModule } from '../auth/auth.module';

@Module({
  imports: [TypeOrmModule.forFeature([UserTable]), AuthModule],
  controllers: [UserTableController],
  providers: [UserTableService],
  exports: [UserTableService],
})
export class UserTableModule {}
