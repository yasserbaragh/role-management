import { Controller, Post, Body, Patch, Delete, Res } from '@nestjs/common';
import { UserTableService } from './user-table.service';
import { CreateUserTableDto } from './dto/create-user-table.dto';
import { UpdateUserTableDto } from './dto/update-user-table.dto';
import { ChangePasswordDto } from './dto/change-password.dto';
import { ForgotPasswordDto } from './dto/forgot-password.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import * as express from "express"
import { LoginUserDto } from './dto/login-user.dto';
import { Public } from 'src/common/decorator/public/public.decorator';
import { CurrentUser } from 'src/common/decorator/current-user/current-user.decorator';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';

@Controller('/api/auth')
export class UserTableController {
  constructor(private readonly userTableService: UserTableService) {}

  @Public()
  @Post('/register')
  create(@Body() createUserTableDto: CreateUserTableDto) {
    return this.userTableService.create(createUserTableDto);
  }

  @Public()
  @Post('/login')
  async login(@Body() loginUserDto: LoginUserDto, @Res({ passthrough: true }) res: express.Response) {
    const token = await this.userTableService.login(loginUserDto);

     res.cookie('token', token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 60 * 60 * 1000,
    });

    return { message: `Logged in successfully` };
  }

  @Public()
  @Post('/forgot-password')
  forgotPassword(@Body() forgotPasswordDto: ForgotPasswordDto) {
    return this.userTableService.forgotPassword(forgotPasswordDto);
  }

  @Public()
  @Post('/reset-password')
  resetPassword(@Body() resetPasswordDto: ResetPasswordDto) {
    return this.userTableService.resetPassword(resetPasswordDto);
  }

  @Patch('/me')
  update(@CurrentUser() user: JwtPayload, @Body() updateUserTableDto: UpdateUserTableDto) {
    return this.userTableService.update(user.sub, updateUserTableDto);
  }

  @Patch('/me/password')
  changePassword(@CurrentUser() user: JwtPayload, @Body() changePasswordDto: ChangePasswordDto) {
    return this.userTableService.changePassword(user.sub, changePasswordDto);
  }

  @Delete('/me')
  remove(@CurrentUser() user: JwtPayload) {
    return this.userTableService.remove(user.sub);
  }
}
