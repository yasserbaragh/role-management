import { Controller, Get, Post, Body, Patch, Param, Delete, Res } from '@nestjs/common';
import { UserTableService } from './user-table.service';
import { CreateUserTableDto } from './dto/create-user-table.dto';
import { UpdateUserTableDto } from './dto/update-user-table.dto';
import * as express from "express"
import { LoginUserDto } from './dto/login-user.dto';

@Controller('/api/auth')
export class UserTableController {
  constructor(private readonly userTableService: UserTableService) {}

  @Post('/register')
  create(@Body() createUserTableDto: CreateUserTableDto) {
    return this.userTableService.create(createUserTableDto);
  }

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

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateUserTableDto: UpdateUserTableDto) {
    return this.userTableService.update(+id, updateUserTableDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.userTableService.remove(+id);
  }
}
