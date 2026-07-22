import { Controller, Get, Post, Body, Param, Delete, ParseIntPipe } from '@nestjs/common';
import { MemberhsipInvitationService } from './memberhsip-invitation.service';
import { CreateInvitationLinkDto } from './dto/create-invitation-link.dto';
import { Permissions } from 'src/common/decorator/permissions/permissions.decorator';
import { CurrentOrganisation } from 'src/common/decorator/current-organisation/current-organisation.decorator';
import { CurrentUser } from 'src/common/decorator/current-user/current-user.decorator';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';

@Controller('/api/memberhsip-invitation')
export class MemberhsipInvitationController {
  constructor(private readonly memberhsipInvitationService: MemberhsipInvitationService) {}

  @Permissions('EDIT-MEMBERSHIP')
  @Post('/link')
  createLink(
    @Body() createInvitationLinkDto: CreateInvitationLinkDto,
    @CurrentOrganisation() organisationId: number,
    @CurrentUser() user: JwtPayload,
  ) {
    return this.memberhsipInvitationService.createLink(createInvitationLinkDto, organisationId, user.sub);
  }

  @Permissions('VIEW-MEMBERSHIP')
  @Get()
  findAll(@CurrentOrganisation() organisationId: number) {
    return this.memberhsipInvitationService.findAll(organisationId);
  }

  @Permissions('EDIT-MEMBERSHIP')
  @Delete(':id')
  revoke(
    @Param('id', ParseIntPipe) id: number,
    @CurrentOrganisation() organisationId: number,
  ) {
    return this.memberhsipInvitationService.revoke(id, organisationId);
  }

  @Post('/join/:token')
  join(@Param('token') token: string, @CurrentUser() user: JwtPayload) {
    return this.memberhsipInvitationService.join(token, user);
  }
}
