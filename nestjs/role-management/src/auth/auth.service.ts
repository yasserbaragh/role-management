import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { UserTable } from 'src/user-table/entities/user-table.entity';

@Injectable()
export class AuthService {
    constructor(
    private readonly jwtService: JwtService,
    
  ) {}

    generateToken(user: UserTable): string {
    const payload = {
      sub: user.id,     
      email: user.email,
    };
    return this.jwtService.sign(payload);
  }
}
