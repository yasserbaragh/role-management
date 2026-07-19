import { Entity, ManyToOne, PrimaryGeneratedColumn, Unique } from 'typeorm'
import { UserTable } from '../../user-table/entities/user-table.entity'
import { Organisation } from '../../organisation/entities/organisation.entity'
import { Role } from '../../role/entities/role.entity'

@Entity()
@Unique(['user', 'organisation'])
export class OrganisationMembership {
    @PrimaryGeneratedColumn()
    id!: number

    @ManyToOne(() => UserTable)
    user!: UserTable

    @ManyToOne(() => Organisation)
    organisation!: Organisation

    @ManyToOne(() => Role)
    role!: Role
}
