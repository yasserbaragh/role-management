import { IsEmail, IsString, MinLength, Matches } from 'class-validator';

export class CreateUserTableDto {
    @IsEmail({}, { message: 'Please provide a valid email address' })
    email!: string;

    @IsString()
    @MinLength(8, { message: 'Password must be at least 8 characters' })
    @Matches(/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_])/, {
        message: 'Password must contain uppercase, lowercase, a number, and a special character',
    })
    password!: string;

    @IsString()
    fullName!: string;
}
