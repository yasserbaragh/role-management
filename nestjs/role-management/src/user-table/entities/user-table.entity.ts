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

    @Column({ type: 'varchar', nullable: true })
    resetPasswordToken!: string | null

    @Column({ type: 'timestamptz', nullable: true })
    resetPasswordExpiresAt!: Date | null
}
