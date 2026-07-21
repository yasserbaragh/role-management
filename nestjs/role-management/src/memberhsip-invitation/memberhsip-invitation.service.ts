import { ConflictException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { randomBytes } from 'crypto';
import { MembershipInvitation } from './entities/membership-invitation.entity';
import { CreateInvitationLinkDto } from './dto/create-invitation-link.dto';
import { Role } from 'src/role/entities/role.entity';
import { UserTable } from 'src/user-table/entities/user-table.entity';
import { OrganisationMembership } from 'src/organisation-membership/entities/organisation-membership.entity';
import type { JwtPayload } from 'src/auth/interfaces/jwt-payload.interface';

@Injectable()
export class MemberhsipInvitationService {
  constructor(
    @InjectRepository(MembershipInvitation)
    private readonly invitationRepository: Repository<MembershipInvitation>,

    @InjectRepository(Role)
    private readonly roleRepository: Repository<Role>,

    @InjectRepository(UserTable)
    private readonly userRepository: Repository<UserTable>,

    @InjectRepository(OrganisationMembership)
    private readonly membershipRepository: Repository<OrganisationMembership>,
  ) {}

  private async findScoped(id: number, organisationId: number) {
    const invitation = await this.invitationRepository.findOne({
      where: { id, organisation: { id: organisationId } },
      relations: ['role'],
    });
    if (!invitation) {
      throw new NotFoundException('Invitation link not found');
    }
    return invitation;
  }

  async createLink(createInvitationLinkDto: CreateInvitationLinkDto, organisationId: number, inviterId: number) {
    const role = await this.roleRepository.findOne({
      where: { id: createInvitationLinkDto.roleId, organisation: { id: organisationId } },
    });
    if (!role) {
      throw new NotFoundException('Role not found in this organisation');
    }

    const token = randomBytes(32).toString('hex');
    const expiresAt = new Date(Date.now() + createInvitationLinkDto.durationInHours * 60 * 60 * 1000);

    const invitation = this.invitationRepository.create({
      organisation: { id: organisationId },
      role,
      invitedBy: { id: inviterId },
      token,
      maxUses: createInvitationLinkDto.maxUses ?? null,
      expiresAt,
    });

    return this.invitationRepository.save(invitation);
  }

  findAll(organisationId: number) {
    return this.invitationRepository.find({
      where: { organisation: { id: organisationId } },
      relations: ['role'],
    });
  }

  async revoke(id: number, organisationId: number) {
    const invitation = await this.findScoped(id, organisationId);
    invitation.revoked = true;
    return this.invitationRepository.save(invitation);
  }

  async join(token: string, user: JwtPayload) {
    const invitation = await this.invitationRepository.findOne({
      where: { token },
      relations: ['organisation', 'role'],
    });
    if (!invitation) {
      throw new NotFoundException('Invalid invitation link');
    }

    if (invitation.revoked) {
      throw new ForbiddenException('This invitation link has been revoked');
    }

    if (invitation.expiresAt.getTime() < Date.now()) {
      throw new ForbiddenException('This invitation link has expired');
    }

    if (invitation.maxUses !== null && invitation.usesCount >= invitation.maxUses) {
      throw new ForbiddenException('This invitation link has reached its maximum uses');
    }

    const currentUser = await this.userRepository.findOne({ where: { id: user.sub } });
    if (!currentUser) {
      throw new NotFoundException('User not found');
    }

    const existing = await this.membershipRepository.findOne({
      where: { user: { id: currentUser.id }, organisation: { id: invitation.organisation.id } },
    });
    if (existing) {
      throw new ConflictException('You are already a member of this organisation');
    }

    const membership = this.membershipRepository.create({
      user: currentUser,
      role: invitation.role,
      organisation: invitation.organisation,
      isOwner: false,
    });
    await this.membershipRepository.save(membership);

    invitation.usesCount += 1;
    await this.invitationRepository.save(invitation);

    return membership;
  }
}
