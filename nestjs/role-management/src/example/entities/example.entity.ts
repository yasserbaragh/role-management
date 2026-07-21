import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm'
import { Organisation } from '../../organisation/entities/organisation.entity'

@Entity()
export class Example {
    @PrimaryGeneratedColumn()
    id!: number

    @Column()
    title!: string

    @Column({ nullable: true })
    description!: string

    @ManyToOne(() => Organisation)
    organisation!: Organisation
}
