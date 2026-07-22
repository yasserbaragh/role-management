import { ConflictException, Injectable, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { CreateUserTableDto } from './dto/create-user-table.dto';
import { UpdateUserTableDto } from './dto/update-user-table.dto';
import { ChangePasswordDto } from './dto/change-password.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { UserTable } from './entities/user-table.entity';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { LoginUserDto } from './dto/login-user.dto';
import { AuthService } from 'src/auth/auth.service';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';

@Injectable()
export class UserTableService {
  constructor(
    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,
    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,
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

    return this.userRepository.save(user);
  }

  async changePassword(id: number, changePasswordDto: ChangePasswordDto) {
    const user = await this.userRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const passwordMatches = await bcrypt.compare(changePasswordDto.currentPassword, user.password);
    if (!passwordMatches) {
      throw new UnauthorizedException('Current password is incorrect');
    }

    user.password = await bcrypt.hash(changePasswordDto.newPassword, 10);
    await this.userRepository.save(user);
    return { message: 'Password updated successfully' };
  }

  async remove(id: number) {
    const user = await this.userRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const memberships = await this.membershipRepository.find({ where: { user: { id } } });
    if (memberships.some((membership) => membership.isOwner)) {
      throw new ConflictException('Delete or transfer ownership of your organisations before deleting your account');
    }

    await this.membershipRepository.remove(memberships);
    return this.userRepository.remove(user);
  }
}
