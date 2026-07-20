import { ConflictException, Injectable, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { CreateUserTableDto } from './dto/create-user-table.dto';
import { UpdateUserTableDto } from './dto/update-user-table.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { UserTable } from './entities/user-table.entity';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { LoginUserDto } from './dto/login-user.dto';
import { AuthService } from 'src/auth/auth.service';

@Injectable()
export class UserTableService {
  constructor(
    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,
    private readonly authService: AuthService
  ) { }

  async create(createUserTableDto: CreateUserTableDto) {
    const existing = await this.userRepository.findOne({ where: { email: createUserTableDto.email } })
    if (existing) {
      throw new ConflictException('Email is already in use');
    }

    const hashedPass = await bcrypt.hash(createUserTableDto.password, 10)

    const user = this.userRepository.create({
      ...createUserTableDto,
      password: hashedPass
    })

    return this.userRepository.save(user)
  }

  async login(loginUserDto: LoginUserDto) {
    const existing = await this.userRepository.findOne({ where: { email: loginUserDto.email } })
    if (!existing) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const passwordMatches = await bcrypt.compare(loginUserDto.password, existing.password);
    if (!passwordMatches) {
      throw new UnauthorizedException('Invalid credentials');
    }

    return this.authService.generateToken(existing)
  }

  async update(id: number, updateUserTableDto: UpdateUserTableDto) {
    const user = await this.userRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (updateUserTableDto.email && updateUserTableDto.email !== user.email) {
      const existing = await this.userRepository.findOne({ where: { email: updateUserTableDto.email } });
      if (existing) {
        throw new ConflictException('Email is already in use');
      }
      user.email = updateUserTableDto.email;
    }

    if (updateUserTableDto.fullName !== undefined) {
      user.fullName = updateUserTableDto.fullName;
    }

    if (updateUserTableDto.password) {
      user.password = await bcrypt.hash(updateUserTableDto.password, 10);
    }

    return this.userRepository.save(user);
  }

  async remove(id: number) {
    const user = await this.userRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return this.userRepository.remove(user);
  }
}
