import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuService } from '../service/suat-chieu.service';
import { IPhim } from 'app/entities/phim/phim.model';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';
import dayjs from 'dayjs/esm';
import 'dayjs/esm/locale/vi';

dayjs.locale('vi');

import { SuatChieuPhimComponent } from '../suat-chieu-phim/suat-chieu-phim.component';
import { AccountService } from 'app/core/auth/account.service';

@Component({
  selector: 'jhi-lich-chieu',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SuatChieuPhimComponent, SharedModule],
  templateUrl: './lich-chieu.component.html',
  styleUrls: ['./lich-chieu.component.scss'],
})
export class LichChieuComponent implements OnInit {
  suatChieus: ISuatChieu[] = [];
  phims: IPhim[] = [];
  phongChieus: IPhongChieu[] = [];
  isLoading = false;
  isAdmin = false;
  isCashierMode = false;
  searchTerm = '';
  searchSuggestions: string[] = [];
  phimFilterText = '';

  dateList: dayjs.Dayjs[] = [];
  selectedDate!: dayjs.Dayjs;
  selectedPhimIds: number[] = [];
  phongChieuId: number | null = null;
  selectedTimeFrom: string | null = null;
  selectedTimeTo: string | null = null;

  private readonly suatChieuService = inject(SuatChieuService);
  private readonly phimService = inject(PhimService);
  private readonly phongChieuService = inject(PhongChieuService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);

  async ngOnInit(): Promise<void> {
    this.isCashierMode = this.router.url.includes('/admin');
    this.generateDateList();
    this.selectedDate = this.dateList[0];

    this.accountService.identity().subscribe(account => {
      this.isAdmin = !!account?.authorities?.includes('ROLE_ADMIN');
    });

    this.route.queryParams.subscribe(params => {
      const phimIds = params['phimIds'];
      const singlePhimId = params['phimId'];
      const rawIds = phimIds ? (Array.isArray(phimIds) ? phimIds : [phimIds]) : singlePhimId ? [singlePhimId] : [];
      this.selectedPhimIds = rawIds.map(value => Number(value)).filter(value => !Number.isNaN(value));

      const phongChieuId = params['phongChieuId'];
      this.phongChieuId = phongChieuId ? Number(phongChieuId) : null;

      const search = params['search'];
      this.searchTerm = search || '';

      const gioBatDau = params['gioBatDau'] || params['gioChieu'];
      this.selectedTimeFrom = gioBatDau || null;

      const gioKetThuc = params['gioKetThuc'];
      this.selectedTimeTo = gioKetThuc || null;

      const ngayChieu = params['ngayChieu'];
      if (ngayChieu) {
        const parsedDate = dayjs(ngayChieu);
        if (parsedDate.isValid()) {
          this.selectedDate = parsedDate;
        } else if (this.dateList.length > 0) {
          this.selectedDate = this.dateList[0];
        }
      } else if (this.dateList.length > 0) {
        this.selectedDate = this.dateList[0];
      }

      this.syncPhimFilterText();
      void this.loadSuatChieus();
    });

    try {
      const [phimsRes, phongChieusRes] = await Promise.all([
        firstValueFrom(this.phimService.query({ size: 1000, sort: ['id,desc'] })),
        firstValueFrom(this.phongChieuService.query({ size: 1000 })),
      ]);
      this.phims = phimsRes.body ?? [];
      this.phongChieus = phongChieusRes.body ?? [];
      this.refreshSearchSuggestions();
      this.syncPhimFilterText();
    } catch {
      this.phims = [];
      this.phongChieus = [];
    }
  }

  generateDateList(): void {
    const today = dayjs();
    for (let i = 0; i < 7; i++) {
      this.dateList.push(today.add(i, 'day'));
    }
  }

  syncPhimFilterText(): void {
    if (this.selectedPhimIds.length > 0) {
      const phimId = this.selectedPhimIds[0];
      const phim = this.phims.find(p => p.id === phimId);
      if (phim) {
        this.phimFilterText = phim.tenPhim ?? '';
      } else {
        this.phimFilterText = '';
      }
    } else {
      this.phimFilterText = '';
    }
  }

  updateUrlQueryParams(): void {
    const queryParams: Record<string, any> = {
      phimId: this.selectedPhimIds[0] || null,
      phimIds: null, // clear old array parameter if any
      phongChieuId: this.phongChieuId || null,
      search: this.searchTerm.trim() || null,
      gioBatDau: this.selectedTimeFrom || null,
      gioKetThuc: this.selectedTimeTo || null,
    };
    if (this.selectedDate) {
      queryParams['ngayChieu'] = this.selectedDate.format('YYYY-MM-DD');
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  onPhimFilterChange(): void {
    const term = this.phimFilterText.trim().toLowerCase();
    if (!term) {
      this.selectedPhimIds = [];
    } else {
      const match = this.phims.find(p => p.tenPhim?.trim().toLowerCase() === term);
      if (match?.id != null) {
        this.selectedPhimIds = [match.id];
      } else {
        this.selectedPhimIds = [];
      }
    }
    this.updateUrlQueryParams();
  }

  selectDate(date: dayjs.Dayjs): void {
    this.selectedDate = date;
    this.updateUrlQueryParams();
  }

  onFilterChange(): void {
    this.updateUrlQueryParams();
  }

  selectRoom(roomId: number | null): void {
    this.phongChieuId = roomId;
    this.updateUrlQueryParams();
  }

  clearPhimFilter(): void {
    this.selectedPhimIds = [];
    this.phimFilterText = '';
    this.updateUrlQueryParams();
  }

  async loadSuatChieus(): Promise<void> {
    this.isLoading = true;
    try {
      const res = await firstValueFrom(
        this.suatChieuService.search({
          ngayChieu: this.selectedDate ? this.selectedDate.format('YYYY-MM-DD') : undefined,
          phimIds: this.selectedPhimIds.length > 0 ? this.selectedPhimIds : undefined,
          phongChieuId: this.phongChieuId ?? undefined,
          gioBatDau: this.selectedTimeFrom ?? undefined,
          gioKetThuc: this.selectedTimeTo ?? undefined,
          gioChieu: this.selectedTimeFrom ?? undefined,
          query: this.searchTerm.trim() || undefined,
        }),
      );
      const all = res.body ?? [];
      this.suatChieus = this.isAdmin ? all : all.filter(sc => this.isUpcomingShowtime(sc));
    } catch {
      this.suatChieus = [];
    } finally {
      this.isLoading = false;
    }
  }

  clearFilters(): void {
    this.selectedPhimIds = [];
    this.phimFilterText = '';
    this.phongChieuId = null;
    this.searchTerm = '';
    this.selectedTimeFrom = null;
    this.selectedTimeTo = null;
    if (this.dateList.length > 0) {
      this.selectedDate = this.dateList[0];
    }
    this.updateUrlQueryParams();
  }

  private isUpcomingShowtime(suatChieu: ISuatChieu): boolean {
    const now = dayjs();
    return !!suatChieu.thoiGianBatDau && suatChieu.thoiGianBatDau.isAfter(now);
  }

  private refreshSearchSuggestions(): void {
    this.searchSuggestions = Array.from(
      new Set([
        ...this.phims.map(phim => phim.tenPhim?.trim()).filter((value): value is string => !!value),
        ...this.phongChieus.map(phong => phong.tenPhong?.trim()).filter((value): value is string => !!value),
      ]),
    ).slice(0, 20);
  }
}
