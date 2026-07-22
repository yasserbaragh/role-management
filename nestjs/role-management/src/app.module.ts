import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { UserTableModule } from './user-table/user-table.module';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { AuthModule } from './auth/auth.module';
import { OrganisationModule } from './organisation/organisation.module';
import { OrganisationMembershipModule } from './organisation-membership/organisation-membership.module';
import { RoleModule } from './role/role.module';
import { PermissionModule } from './permission/permission.module';
import { APP_GUARD } from '@nestjs/core';
import { RolesGuard } from './common/guards/roles/roles.guard';
import { MemberhsipInvitationModule } from './memberhsip-invitation/memberhsip-invitation.module';
import { ExampleModule } from './example/example.module';
import { CacheModule } from "@nestjs/cache-manager"
import Keyv from 'keyv';
import KeyvRedis from '@keyv/redis';

@Module({
  imports: [
     ConfigModule.forRoot({
      isGlobal: true,
    }),
    CacheModule.registerAsync({
      isGlobal: true,
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: async(configService: ConfigService) => ({
        stores: [
          new Keyv({
            store: new KeyvRedis(
              `redis://${configService.get<string>('REDIS_HOST')}:${configService.get<number>('REDIS_PORT')}`,
            )
          })
        ],
         ttl: 60_000
      })
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get<string>('DB_HOST'),
        port: configService.get<number>('DB_PORT'),
        username: configService.get<string>('DB_USERNAME'),
        password: configService.get<string>('DB_PASSWORD'),
        database: configService.get<string>('DB_NAME'),
        entities: [__dirname + '/**/*.entity{.ts,.js}'],
        synchronize: true, 
      }),
    }),
    UserTableModule,
    AuthModule,
    OrganisationModule,
    OrganisationMembershipModule,
    RoleModule,
    PermissionModule,
    MemberhsipInvitationModule,
    ExampleModule
  ],
  controllers: [AppController],
  providers: [
    AppService, 
    { provide: APP_GUARD, useClass: RolesGuard }
  ],
})
export class AppModule {}
