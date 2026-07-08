import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { Router } from '@angular/router';
import { ChatbotComponent } from '../chatbot/chatbot.component';

@Component({
  selector: 'jhi-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, FontAwesomeModule, RouterLink, RouterLinkActive, ChatbotComponent],
})
export default class AdminLayoutComponent {
  account = this.accountService.trackCurrentAccount();
  isAccountMenuOpen = false;

  constructor(
    private accountService: AccountService,
    private loginService: LoginService,
    private router: Router,
  ) {}

  toggleAccountMenu(): void {
    this.isAccountMenuOpen = !this.isAccountMenuOpen;
  }

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['']);
  }
}
