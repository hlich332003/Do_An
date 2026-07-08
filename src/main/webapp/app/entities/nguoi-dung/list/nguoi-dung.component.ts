import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { INguoiDung } from '../nguoi-dung.model';
import { NguoiDungService } from '../service/nguoi-dung.service';
import { NguoiDungDeleteDialogComponent } from '../delete/nguoi-dung-delete-dialog.component';
import { formatRoleLabel, formatUserStatusLabel, normalizeForMatch } from 'app/shared/util/display-text.util';

@Component({
  selector: 'jhi-nguoi-dung',
  templateUrl: './nguoi-dung.component.html',
  styleUrls: ['./nguoi-dung.component.scss'],
  imports: [FormsModule, RouterModule, SharedModule],
})
export class NguoiDungComponent implements OnInit, OnDestroy {
  nguoiDungs: INguoiDung[] = [];
  isLoading = false;
  searchTerm = '';
  searchSuggestions: string[] = [];
  selectedRole = '';
  selectedStatus = '';
  subscription: Subscription | null = null;

  protected readonly nguoiDungService = inject(NguoiDungService);
  protected readonly modalService = inject(NgbModal);
  protected readonly router = inject(Router);
  protected readonly activatedRoute = inject(ActivatedRoute);

  ngOnInit(): void {
    this.subscription = this.activatedRoute.queryParamMap.subscribe(params => {
      this.searchTerm = params.get('search') ?? '';
      this.load();
    });
  }

  load(): void {
    this.isLoading = true;
    const request = this.searchTerm.trim()
      ? this.nguoiDungService.search({ page: 0, size: 1000, sort: ['id,asc'], query: this.searchTerm.trim(), cacheBuster: Date.now() })
      : this.nguoiDungService.query({ page: 0, size: 1000, sort: ['id,asc'], cacheBuster: Date.now() });

    request.subscribe({
      next: res => {
        this.nguoiDungs = this.applyLocalFilters(this.normalizeUsers(res.body ?? []));
        this.searchSuggestions = this.buildSearchSuggestions(this.nguoiDungs);
        this.isLoading = false;
      },
      error: () => {
        this.nguoiDungs = [];
        this.searchSuggestions = [];
        this.isLoading = false;
      },
    });
  }

  delete(nguoiDung: INguoiDung): void {
    const modalRef = this.modalService.open(NguoiDungDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.nguoiDung = nguoiDung;
    modalRef.closed.subscribe(reason => {
      if (reason === ITEM_DELETED_EVENT) {
        this.load();
      }
    });
  }

  onSearch(): void {
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: this.searchTerm.trim() ? { search: this.searchTerm.trim() } : {},
    });
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: {},
    });
  }

  onFilterChange(): void {
    this.load();
  }

  clearRoleFilter(): void {
    this.selectedRole = '';
    this.load();
  }

  clearStatusFilter(): void {
    this.selectedStatus = '';
    this.load();
  }

  clearAllFilters(): void {
    this.searchTerm = '';
    this.selectedRole = '';
    this.selectedStatus = '';
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: {},
    });
  }

  getRoleLabel(value?: string | null): string {
    return formatRoleLabel(value);
  }

  getRoleBadgeClass(value?: string | null): string {
    const role = normalizeForMatch(value);
    if (role === 'role_admin') {
      return 'bg-primary';
    }
    if (role === 'role_user') {
      return 'bg-info';
    }
    return 'bg-secondary';
  }

  getStatusLabel(value?: string | null): string {
    return formatUserStatusLabel(value);
  }

  getStatusBadgeClass(value?: string | null): string {
    const status = normalizeForMatch(value);
    if (status === 'active' || status === 'hoat dong') {
      return 'bg-success';
    }
    return 'bg-danger';
  }

  getUserDisplayName(nguoiDung: INguoiDung): string {
    const hoTen = nguoiDung.hoTen?.trim();
    if (hoTen) {
      return hoTen;
    }

    const email = nguoiDung.email?.trim();
    if (email) {
      return email.split('@')[0];
    }

    return `Người dùng #${nguoiDung.id ?? ''}`;
  }

  getUserAvatarLetter(nguoiDung: INguoiDung): string {
    return this.getUserDisplayName(nguoiDung).charAt(0).toUpperCase() || 'U';
  }

  private normalizeUsers(users: INguoiDung[]): INguoiDung[] {
    return users
      .filter(user => user && user.id != null)
      .map(user => ({
        ...user,
        hoTen: user.hoTen?.trim() || '',
        email: user.email?.trim() || '',
        soDienThoai: user.soDienThoai?.trim() || '',
        diaChi: user.diaChi?.trim() || '',
        vaiTro: user.vaiTro?.trim() || '',
        trangThai: user.trangThai?.trim() || '',
        diemTichLuy: user.diemTichLuy ?? 0,
      }));
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private buildSearchSuggestions(users: INguoiDung[]): string[] {
    return Array.from(
      new Set(
        users
          .flatMap(user => [this.getUserDisplayName(user), user.email, user.soDienThoai, user.vaiTro, user.trangThai])
          .map(value => value?.trim())
          .filter((value): value is string => !!value),
      ),
    ).slice(0, 15);
  }

  private applyLocalFilters(users: INguoiDung[]): INguoiDung[] {
    return users.filter(user => {
      if (this.selectedRole && user.vaiTro !== this.selectedRole) {
        return false;
      }
      if (this.selectedStatus && user.trangThai !== this.selectedStatus) {
        return false;
      }
      return true;
    });
  }
}
