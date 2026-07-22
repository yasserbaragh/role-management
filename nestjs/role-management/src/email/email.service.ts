import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as nodemailer from 'nodemailer';

export interface SendEmailOptions {
  to: string;
  subject: string;
  text: string;
}

@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);
  private readonly enabled: boolean;
  private readonly transporter: nodemailer.Transporter | null;

  constructor(private readonly configService: ConfigService) {
    this.enabled = this.configService.get<string>('EMAIL_ENABLED') === 'true';

    this.transporter = this.enabled
      ? nodemailer.createTransport({
          host: this.configService.get<string>('SMTP_HOST'),
          port: Number(this.configService.get<string>('SMTP_PORT')),
          secure: this.configService.get<string>('SMTP_SECURE') === 'true',
          auth: {
            user: this.configService.get<string>('SMTP_USER'),
            pass: this.configService.get<string>('SMTP_PASSWORD'),
          },
        })
      : null;
  }

  async send(options: SendEmailOptions) {
    if (!this.enabled || !this.transporter) {
      return;
    }

    try {
      await this.transporter.sendMail({
        from: this.configService.get<string>('SMTP_FROM'),
        to: options.to,
        subject: options.subject,
        text: options.text,
      });
    } catch (error) {
      this.logger.warn(`Failed to send email to ${options.to}: ${error instanceof Error ? error.message : error}`);
    }
  }
}
