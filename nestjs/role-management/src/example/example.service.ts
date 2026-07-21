import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CreateExampleDto } from './dto/create-example.dto';
import { UpdateExampleDto } from './dto/update-example.dto';
import { Example } from './entities/example.entity';

@Injectable()
export class ExampleService {
  constructor(
    @InjectRepository(Example)
    private readonly exampleRepository: Repository<Example>,
  ) {}

  private async findScoped(id: number, organisationId: number) {
    const example = await this.exampleRepository.findOne({
      where: { id, organisation: { id: organisationId } },
    });
    if (!example) {
      throw new NotFoundException('Example not found');
    }
    return example;
  }

  create(createExampleDto: CreateExampleDto, organisationId: number) {
    const example = this.exampleRepository.create({
      ...createExampleDto,
      organisation: { id: organisationId },
    });

    return this.exampleRepository.save(example);
  }

  findAll(organisationId: number) {
    return this.exampleRepository.find({
      where: { organisation: { id: organisationId } },
    });
  }

  findOne(id: number, organisationId: number) {
    return this.findScoped(id, organisationId);
  }

  async update(id: number, updateExampleDto: UpdateExampleDto, organisationId: number) {
    const example = await this.findScoped(id, organisationId);
    Object.assign(example, updateExampleDto);
    return this.exampleRepository.save(example);
  }

  async remove(id: number, organisationId: number) {
    const example = await this.findScoped(id, organisationId);
    return this.exampleRepository.remove(example);
  }
}
