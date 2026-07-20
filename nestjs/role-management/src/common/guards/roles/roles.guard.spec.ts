import { RolesGuard } from './roles.guard';

describe('RolesGuard', () => {
  it('should be defined', () => {
    const jwtService = {} as any;
    const reflector = {} as any;
    const membershipRepository = {} as any;
    expect(new RolesGuard(jwtService, reflector, membershipRepository)).toBeDefined();
  });
});
