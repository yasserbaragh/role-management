import { Column, CreateDateColumn, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm'
import { Organisation } from '../../organisation/entities/organisation.entity'
import { Role } from '../../role/entities/role.entity'
import { UserTable } from '../../user-table/entities/user-table.entity'

@Entity()
export class MembershipInvitation {
    @PrimaryGeneratedColumn()
    id!: number

    @ManyToOne(() => Organisation)
    organisation!: Organisation

    @ManyToOne(() => Role)
    role!: Role

    @ManyToOne(() => UserTable)
    invitedBy!: UserTable

    @Column({ unique: true })
    token!: string

    @Column({ type: 'int', nullable: true })
    maxUses!: number | null

    @Column({ default: 0 })
    usesCount!: number

    @Column()
    expiresAt!: Date

    @Column({ default: false })
    revoked!: boolean

    @CreateDateColumn()
    createdAt!: Date
}
