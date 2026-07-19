import { Column, Entity, JoinTable, ManyToMany, ManyToOne, PrimaryGeneratedColumn } from 'typeorm'
import { Organisation } from '../../organisation/entities/organisation.entity'
import { Permission } from '../../permission/entities/permission.entity'

@Entity()
export class Role {
    @PrimaryGeneratedColumn()
    id!: number

    @Column()
    name!: string

    @Column({ default: false })
    isSystemRole!: boolean

    @ManyToOne(() => Organisation)
    organisation!: Organisation

    @ManyToMany(() => Permission)
    @JoinTable()
    permissions!: Permission[]
}
