import { AfterViewInit, Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';

@Component({
  selector: 'jhi-login',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
})
export default class LoginComponent implements OnInit, AfterViewInit {
  username = viewChild.required<ElementRef>('username');

  authenticationError = signal(false);
  accountLocked = signal(false);
  showPassword = signal(false);

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
  });

  togglePassword(): void {
    this.showPassword.update(show => !show);
  }

  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly stateStorageService = inject(StateStorageService);

  private navigateAfterLogin(): void {
    const queryRedirect = this.route.snapshot.queryParamMap.get('redirectUrl');
    const previousUrl = queryRedirect || this.stateStorageService.getUrl();

    if (previousUrl) {
      this.stateStorageService.clearUrl();
      void this.router.navigateByUrl(previousUrl, { replaceUrl: true });
      return;
    }

    if (this.accountService.hasAnyAuthority('ROLE_ADMIN')) {
      void this.router.navigate(['/admin/dashboard'], { replaceUrl: true });
    } else {
      void this.router.navigate(['/phim-danh-sach'], { replaceUrl: true });
    }
  }

  ngOnInit(): void {
    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        this.navigateAfterLogin();
      }
    });
  }

  ngAfterViewInit(): void {
    this.username().nativeElement.focus();
  }

  login(): void {
    this.authenticationError.set(false);
    this.accountLocked.set(false);

    this.loginService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.navigateAfterLogin();
      },
      error: (response: HttpErrorResponse) => {
        if (response.status === 423) {
          this.accountLocked.set(true);
        } else {
          this.authenticationError.set(true);
        }
      },
    });
  }
}
