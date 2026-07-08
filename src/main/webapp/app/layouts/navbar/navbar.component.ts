import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { environment } from 'environments/environment';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { IPhim } from 'app/entities/phim/phim.model';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  imports: [RouterModule, SharedModule, HasAnyAuthorityDirective, FormsModule],
})
export default class NavbarComponent implements OnInit, OnDestroy {
  inProduction?: boolean;
  isNavbarCollapsed = signal(true);
  openAPIEnabled?: boolean;
  version = '';
  account = inject(AccountService).trackCurrentAccount();
  searchKeyword = '';
  isSearching = false;
  searchSuggestions: string[] = [];

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  private readonly loginService = inject(LoginService);
  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);
  private readonly phimService = inject(PhimService);

  constructor() {
    const { VERSION } = environment;
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    }

    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(term => {
      this.isSearching = true;
      this.collapseNavbar();
      this.router.navigate(['/phim-danh-sach'], { queryParams: { search: term.trim() || null } }).finally(() => {
        this.isSearching = false;
      });
    });
  }

  ngOnInit(): void {
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.openAPIEnabled = profileInfo.openAPIEnabled;
    });

    void this.loadSearchSuggestions();
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.router.navigate(['']);
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed.update(isNavbarCollapsed => !isNavbarCollapsed);
  }

  searchMovies(): void {
    this.searchSubject.next(this.searchKeyword);
  }

  onSearchInput(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.searchSubject.next(term);
  }

  private async loadSearchSuggestions(): Promise<void> {
    try {
      const res = await firstValueFrom(this.phimService.query({ page: 0, size: 1000, sort: ['tenPhim,asc'] }));
      this.searchSuggestions = this.buildSearchSuggestions(
        (res.body ?? []).filter((phim: any): phim is IPhim => !!phim && phim.id != null),
      );
    } catch {
      this.searchSuggestions = [];
    }
  }

  private buildSearchSuggestions(phims: IPhim[]): string[] {
    return Array.from(
      new Set(
        phims
          .flatMap(phim => [phim.tenPhim, phim.theLoai, phim.daoDien])
          .map(value => value?.trim())
          .filter((value): value is string => !!value),
      ),
    ).slice(0, 15);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
