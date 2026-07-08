import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AccountService } from 'app/core/auth/account.service';
import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import FooterComponent from '../footer/footer.component';
import PageRibbonComponent from '../profiles/page-ribbon.component';
import { ChatbotComponent } from '../chatbot/chatbot.component';

import { CommonModule } from '@angular/common';

@Component({
  selector: 'jhi-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  imports: [CommonModule, RouterOutlet, FooterComponent, PageRibbonComponent, ChatbotComponent],
})
export default class MainComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);
  account = this.accountService.trackCurrentAccount();

  ngOnInit(): void {
    // try to log in automatically
    this.accountService.identity().subscribe(account => {
      if (account?.authorities?.includes('ROLE_ADMIN') && (this.router.url === '/' || this.router.url === '')) {
        this.router.navigate(['/admin']);
      }
    });
  }
}
