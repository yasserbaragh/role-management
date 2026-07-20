import { CanActivate, ExecutionContext, ForbiddenException, Injectable, Logger, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { IS_PUBLIC_KEY } from 'src/common/decorator/public/public.decorator';
import { PERMISSIONS_KEY } from 'src/common/decorator/permissions/permissions.decorator';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';


@Injectable()
export class RolesGuard implements CanActivate {
  private readonly logger = new Logger(RolesGuard.name);

  constructor(
    private readonly jwtService: JwtService,
    private readonly reflector: Reflector,
    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,
  ) {}

  async canActivate(
    context: ExecutionContext,
  ): Promise<boolean> {
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

    let payload: JwtPayload;
    try {
      payload = this.jwtService.verify(token);
    } catch (error) {
      if (error instanceof Error && error.name === 'TokenExpiredError') {
        throw new UnauthorizedException('Token expired');
      }
      this.logger.warn(`Token verification failed: ${error instanceof Error ? error.message : error}`);
      throw new UnauthorizedException('Invalid token');
    }
    request.user = payload;

    const requiredPermissions = this.reflector.getAllAndOverride<string[]>(PERMISSIONS_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (!requiredPermissions || requiredPermissions.length === 0) {
      return true;
    }

    const organisationId = Number(request.cookies?.organisationId);
    if (!organisationId) {
      throw new ForbiddenException('No organisation selected');
    }

    const membership = await this.membershipRepository.findOne({
      where: {
        user: { id: payload.sub },
        organisation: { id: organisationId },
      },
      relations: ['role', 'role.permissions'],
    });
    if (!membership) {
      throw new ForbiddenException('You are not a member of this organisation');
    }
    request.role = membership.role;

    if (membership.role.isSystemRole) {
      return true;
    }

    const grantedPermissions = membership.role.permissions.map((permission) => permission.key);
    const hasAllPermissions = requiredPermissions.every((permission) => grantedPermissions.includes(permission));
    if (!hasAllPermissions) {
      throw new ForbiddenException('Insufficient permissions');
    }

    return true;
  }
}
