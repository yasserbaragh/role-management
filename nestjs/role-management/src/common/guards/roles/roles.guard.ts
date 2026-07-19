import { CanActivate, ExecutionContext, Injectable, UnauthorizedException } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { IS_PUBLIC_KEY } from 'src/common/decorator/public/public.decorator';


@Injectable()
export class RolesGuard implements CanActivate {
  constructor(
    private readonly jwtService: JwtService,
    private readonly reflector: Reflector
  ) {}

  canActivate(
    context: ExecutionContext,
  ): boolean {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) return true;

    const request = context.switchToHttp().getRequest();
    let token: string | undefined;

    if (request.cookies && request.cookies.token) {
      token = request.cookies.token;
    } else if (request.headers?.authorization) {
      const [scheme, credentials] = request.headers.authorization.split(' ');
      if (scheme?.toLowerCase() === 'bearer' && credentials) {
        token = credentials;
      }
    }

    if (!token) {
      throw new UnauthorizedException('Token not found');
    }


    try {
      const payload = this.jwtService.verify(token);
      request.user = payload;
      return true;
    } catch (error) {
      throw new UnauthorizedException('Invalid token');
    }
  }
}
