import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export const CurrentOrganisation = createParamDecorator(
  (data: unknown, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    const organisationId = request.cookies?.organisationId;
    return organisationId ? Number(organisationId) : undefined;
  },
);
