import { ConflictException, Injectable, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { CreateUserTableDto } from './dto/create-user-table.dto';
import { UpdateUserTableDto } from './dto/update-user-table.dto';
import { ChangePasswordDto } from './dto/change-password.dto';
import { ForgotPasswordDto } from './dto/forgot-password.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { UserTable } from './entities/user-table.entity';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { randomBytes } from 'crypto';
import { LoginUserDto } from './dto/login-user.dto';
import { AuthService } from 'src/auth/auth.service';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import { EmailService } from 'src/email/email.service';

const RESET_TOKEN_TTL_MS = 60 * 60 * 1000;

@Injectable()
export class UserTableService {
  constructor(
    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,
    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,
    private readonly authService: AuthService,
    private readonly emailService: EmailService,
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

  async forgotPassword(forgotPasswordDto: ForgotPasswordDto) {
    const user = await this.userRepository.findOne({ where: { email: forgotPasswordDto.email } });
    if (user) {
      user.resetPasswordToken = randomBytes(32).toString('hex');
      user.resetPasswordExpiresAt = new Date(Date.now() + RESET_TOKEN_TTL_MS);
      await this.userRepository.save(user);

      await this.emailService.send({
        to: user.email,
        subject: 'Reset your password',
        text: `Use this token to reset your password: ${user.resetPasswordToken}. It expires in 1 hour.`,
      });
    }

    return { message: 'If an account exists for this email, a password reset link has been sent' };
  }

  async resetPassword(resetPasswordDto: ResetPasswordDto) {
    const user = await this.userRepository.findOne({ where: { resetPasswordToken: resetPasswordDto.token } });
    if (!user || !user.resetPasswordExpiresAt || user.resetPasswordExpiresAt.getTime() < Date.now()) {
      throw new UnauthorizedException('Invalid or expired reset token');
    }

    user.password = await bcrypt.hash(resetPasswordDto.newPassword, 10);
    user.resetPasswordToken = null;
    user.resetPasswordExpiresAt = null;
    await this.userRepository.save(user);

    return { message: 'Password has been reset successfully' };
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
