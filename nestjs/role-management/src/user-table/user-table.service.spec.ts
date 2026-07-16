import { Test, TestingModule } from '@nestjs/testing';
import { UserTableService } from './user-table.service';

describe('UserTableService', () => {
  let service: UserTableService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [UserTableService],
    }).compile();

    service = module.get<UserTableService>(UserTableService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
