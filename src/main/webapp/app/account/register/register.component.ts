import { AfterViewInit, Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { EMAIL_ALREADY_USED_TYPE, LOGIN_ALREADY_USED_TYPE, PHONE_NUMBER_ALREADY_USED_TYPE } from 'app/config/error.constants';
import SharedModule from 'app/shared/shared.module';
import { LoginService } from 'app/login/login.service';
import PasswordStrengthBarComponent from '../password/password-strength-bar/password-strength-bar.component';
import { RegisterService } from './register.service';

@Component({
  selector: 'jhi-register',
  imports: [SharedModule, RouterModule, FormsModule, ReactiveFormsModule, PasswordStrengthBarComponent],
  templateUrl: './register.component.html',
})
export default class RegisterComponent implements AfterViewInit {
  login = viewChild.required<ElementRef>('login');

  doNotMatch = signal(false);
  error = signal(false);
  errorEmailExists = signal(false);
  errorPhoneExists = signal(false);
  errorUserExists = signal(false);
  success = signal(false);
  autoLoggedIn = signal(false);

  showPassword = signal(false);
  showConfirmPassword = signal(false);

  togglePassword(): void {
    this.showPassword.update(s => !s);
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword.update(s => !s);
  }

  registerForm = new FormGroup({
    login: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50),
        Validators.pattern('^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$'),
      ],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
    }),
    soDienThoai: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern('^(0|\\+84)[0-9]{9,10}$')],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
    confirmPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
  });

  private readonly registerService = inject(RegisterService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);

  ngAfterViewInit(): void {
    this.login().nativeElement.focus();
  }

  register(): void {
    this.doNotMatch.set(false);
    this.error.set(false);
    this.errorEmailExists.set(false);
    this.errorPhoneExists.set(false);
    this.errorUserExists.set(false);
    this.autoLoggedIn.set(false);

    const { password, confirmPassword } = this.registerForm.getRawValue();
    if (password !== confirmPassword) {
      this.doNotMatch.set(true);
    } else {
      const { login, email, password, soDienThoai } = this.registerForm.getRawValue();
      const phoneToSend = soDienThoai && soDienThoai.trim() !== '' ? soDienThoai.trim() : undefined;
      this.registerService.save({ login, email, password, langKey: 'en', soDienThoai: phoneToSend }).subscribe({
        next: () => {
          this.loginService.login({ username: email, password, rememberMe: true }).subscribe({
            next: () => {
              this.autoLoggedIn.set(true);
              this.success.set(true);
            },
            error: () => {
              this.autoLoggedIn.set(false);
              this.success.set(true);
            },
          });
        },
        error: response => this.processError(response),
      });
    }
  }

  private processError(response: HttpErrorResponse): void {
    if (response.status === 400 && response.error.type === LOGIN_ALREADY_USED_TYPE) {
      this.errorUserExists.set(true);
    } else if (response.status === 400 && response.error.type === EMAIL_ALREADY_USED_TYPE) {
      this.errorEmailExists.set(true);
    } else if (response.status === 400 && response.error.type === PHONE_NUMBER_ALREADY_USED_TYPE) {
      this.errorPhoneExists.set(true);
    } else {
      this.error.set(true);
    }
  }

  closeModal(): void {
    this.success.set(false);
  }

  goToLogin(): void {
    this.success.set(false);
    if (this.autoLoggedIn()) {
      void this.router.navigate(['/phim-danh-sach']);
    } else {
      void this.router.navigate(['/login']);
    }
  }
}
