import { Column, Entity, PrimaryGeneratedColumn, Unique } from 'typeorm'

@Entity()
export class UserTable {
    @PrimaryGeneratedColumn()
    id!: number

    @Column({unique: true})
    email!: string;

    @Column()
    password!: string

    @Column()
    fullName!: string
}
