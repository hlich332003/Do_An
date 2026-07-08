import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { formatGenreLabel, normalizeForMatch, repairVietnameseText } from 'app/shared/util/display-text.util';
import { IPhim } from '../phim.model';
import { PhimService } from '../service/phim.service';
import { ISuatChieu } from 'app/entities/suat-chieu/suat-chieu.model';
import { SuatChieuService } from 'app/entities/suat-chieu/service/suat-chieu.service';
import { AccountService } from 'app/core/auth/account.service';
import { DanhGiaService } from 'app/entities/danh-gia/service/danh-gia.service';
import { IDanhGia, NewDanhGia } from 'app/entities/danh-gia/danh-gia.model';
import { BookingService } from 'app/dat-ve/service/booking.service';
import dayjs from 'dayjs/esm';

interface SuatChieuGroup {
  dateStr: string;
  dateObj: dayjs.Dayjs;
  suatChieus: ISuatChieu[];
}

@Component({
  selector: 'jhi-phim-chi-tiet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SharedModule],
  templateUrl: './phim-chi-tiet.component.html',
  styleUrls: ['./phim-chi-tiet.component.scss'],
})
export class PhimChiTietComponent implements OnInit {
  phim: IPhim | null = null;
  suatChieuGroups: SuatChieuGroup[] = [];
  selectedDateStr = '';
  danhGias: IDanhGia[] = [];
  averageRating = 0;
  reviewCount = 0;
  reviewForm = {
    soSao: 10,
    noiDung: '',
  };
  reviewMessage = '';
  reviewError = '';
  isReviewLoading = false;
  isSubmittingReview = false;
  isAuthenticated = false;
  currentUserEmail = '';
  currentUserReview: IDanhGia | null = null;
  canEditReview = false;
  editWindowClosed = false;
  isEligibleToReview = false;
  eligibilityMessage = '';
  readonly ratingOptions = [10, 9, 8, 7, 6, 5, 4, 3, 2, 1];

  protected activatedRoute = inject(ActivatedRoute);
  protected phimService = inject(PhimService);
  protected suatChieuService = inject(SuatChieuService);
  protected accountService = inject(AccountService);
  protected danhGiaService = inject(DanhGiaService);
  protected bookingService = inject(BookingService);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.isAuthenticated = !!account;
      this.currentUserEmail = account?.email || '';
      this.checkUserReviewStatus();
      this.checkReviewEligibility();
    });

    const phimId = Number(this.activatedRoute.snapshot.paramMap.get('id'));
    if (!phimId) {
      return;
    }

    this.phimService.find(phimId).subscribe({
      next: res => {
        this.phim = res.body ?? null;
        this.checkReviewEligibility();
      },
    });
    this.suatChieuService.search({ phimId }).subscribe(res => {
      const now = dayjs();
      const limit = now.add(7, 'day');
      const filtered = (res.body ?? []).filter(sc => {
        if (!sc.thoiGianBatDau) return false;
        return sc.thoiGianBatDau.isAfter(now) && sc.thoiGianBatDau.isBefore(limit);
      });
      this.groupSuatChieus(filtered);
    });
    this.loadDanhGias(phimId);
  }

  previousState(): void {
    window.history.back();
  }

  getAgeRating(phim: IPhim): string {
    const theLoai = normalizeForMatch(phim.theLoai);
    if (theLoai.includes('kinh di') || theLoai.includes('18')) {
      return 'T18';
    }
    if (theLoai.includes('hanh dong') || theLoai.includes('16')) {
      return 'T16';
    }
    if (theLoai.includes('13')) {
      return 'T13';
    }
    return 'P';
  }

  formatGenre(value?: string | null): string {
    return formatGenreLabel(value);
  }

  repairText(value?: string | null): string {
    return repairVietnameseText(value);
  }

  onPosterError(event: Event): void {
    const img = event.target as HTMLImageElement;
    const fallback = 'content/images/default-poster.png';
    if (img.dataset.fallbackApplied === '1' || img.src.endsWith(fallback)) {
      return;
    }
    img.dataset.fallbackApplied = '1';
    img.src = fallback;
  }

  private groupSuatChieus(suatChieus: ISuatChieu[]): void {
    const groupsMap = new Map<string, ISuatChieu[]>();

    suatChieus.forEach(sc => {
      if (sc.thoiGianBatDau) {
        const dateStr = sc.thoiGianBatDau.format('YYYY-MM-DD');
        if (!groupsMap.has(dateStr)) {
          groupsMap.set(dateStr, []);
        }
        groupsMap.get(dateStr)?.push(sc);
      }
    });

    this.suatChieuGroups = Array.from(groupsMap.entries())
      .map(([dateStr, scs]) => {
        scs.sort((a, b) => a.thoiGianBatDau!.valueOf() - b.thoiGianBatDau!.valueOf());
        return {
          dateStr,
          dateObj: dayjs(dateStr),
          suatChieus: scs,
        };
      })
      .sort((a, b) => a.dateObj.valueOf() - b.dateObj.valueOf());

    if (this.suatChieuGroups.length > 0) {
      this.selectedDateStr = this.suatChieuGroups[0].dateStr;
    }
  }

  selectDate(dateStr: string): void {
    this.selectedDateStr = dateStr;
  }

  getActiveSuatChieus(): ISuatChieu[] {
    const group = this.suatChieuGroups.find(g => g.dateStr === this.selectedDateStr);
    return group ? group.suatChieus : [];
  }

  getGroupedActiveShowtimes(): { roomName: string; showtimes: ISuatChieu[] }[] {
    const active = this.getActiveSuatChieus();
    const map = new Map<string, ISuatChieu[]>();
    active.forEach(sc => {
      const roomName = sc.phongChieu?.tenPhong || 'Rạp Khác';
      if (!map.has(roomName)) {
        map.set(roomName, []);
      }
      map.get(roomName)?.push(sc);
    });
    return Array.from(map.entries()).map(([roomName, scs]) => ({ roomName, showtimes: scs }));
  }

  loadDanhGias(phimId: number): void {
    this.isReviewLoading = true;
    this.danhGiaService.queryByPhim(phimId).subscribe({
      next: res => {
        this.danhGias = res.body ?? [];
        this.reviewCount = this.danhGias.length;
        this.averageRating = this.reviewCount
          ? Number((this.danhGias.reduce((sum, item) => sum + (item.soSao ?? 0), 0) / this.reviewCount).toFixed(1))
          : 0;
        this.isReviewLoading = false;
        this.checkUserReviewStatus();
      },
      error: () => {
        this.danhGias = [];
        this.reviewCount = 0;
        this.averageRating = 0;
        this.isReviewLoading = false;
        this.checkUserReviewStatus();
      },
    });
  }

  submitReview(): void {
    if (!this.phim?.id) {
      return;
    }
    if (!this.isAuthenticated) {
      this.reviewError = 'Vui lòng đăng nhập để đánh giá phim.';
      return;
    }
    const noiDung = this.reviewForm.noiDung.trim();
    if (!noiDung) {
      this.reviewError = 'Vui lòng nhập nhận xét trước khi gửi.';
      return;
    }

    this.isSubmittingReview = true;
    this.reviewError = '';
    this.reviewMessage = '';

    if (this.currentUserReview) {
      if (!this.canEditReview) {
        this.reviewError = 'Thời gian 7 ngày chỉnh sửa đã hết. Bạn không thể sửa đổi đánh giá này.';
        this.isSubmittingReview = false;
        return;
      }

      const payload = {
        ...this.currentUserReview,
        soSao: this.reviewForm.soSao,
        noiDung,
      };

      this.danhGiaService.update(payload).subscribe({
        next: () => {
          this.reviewMessage = 'Cập nhật đánh giá thành công.';
          this.isSubmittingReview = false;
          this.loadDanhGias(this.phim!.id);
        },
        error: err => {
          this.reviewError = err?.error?.detail || err?.error?.title || err?.error?.message || 'Không thể cập nhật đánh giá lúc này.';
          this.isSubmittingReview = false;
        },
      });
    } else {
      const payload: NewDanhGia = {
        id: null,
        phimId: this.phim.id,
        soSao: this.reviewForm.soSao,
        noiDung,
        createdAt: null,
        phimTen: null,
        nguoiDungId: null,
        nguoiDungHoTen: null,
        nguoiDungEmail: null,
      };

      this.danhGiaService.create(payload).subscribe({
        next: () => {
          this.reviewForm.noiDung = '';
          this.reviewForm.soSao = 10;
          this.reviewMessage = 'Cảm ơn bạn đã gửi đánh giá.';
          this.isSubmittingReview = false;
          this.loadDanhGias(this.phim!.id);
        },
        error: err => {
          this.reviewError = err?.error?.detail || err?.error?.title || err?.error?.message || 'Không thể gửi đánh giá lúc này.';
          this.isSubmittingReview = false;
        },
      });
    }
  }

  checkUserReviewStatus(): void {
    if (!this.isAuthenticated || !this.currentUserEmail || this.danhGias.length === 0) {
      this.currentUserReview = null;
      this.canEditReview = false;
      this.editWindowClosed = false;
      return;
    }

    const review = this.danhGias.find(r => r.nguoiDungEmail?.toLowerCase() === this.currentUserEmail.toLowerCase());
    if (review) {
      this.currentUserReview = review;
      if (review.createdAt) {
        const createdDate = dayjs(review.createdAt);
        const limitDate = createdDate.add(7, 'day');
        const now = dayjs();
        if (now.isBefore(limitDate)) {
          this.canEditReview = true;
          this.editWindowClosed = false;
          this.reviewForm.soSao = review.soSao ?? 10;
          this.reviewForm.noiDung = review.noiDung ?? '';
        } else {
          this.canEditReview = false;
          this.editWindowClosed = true;
        }
      } else {
        this.canEditReview = false;
        this.editWindowClosed = false;
      }
    } else {
      this.currentUserReview = null;
      this.canEditReview = false;
      this.editWindowClosed = false;
    }
  }

  checkReviewEligibility(): void {
    if (!this.isAuthenticated || !this.phim) {
      this.isEligibleToReview = false;
      this.eligibilityMessage = '';
      return;
    }

    this.accountService.identity().subscribe(account => {
      const isAdmin = !!account?.authorities?.includes('ROLE_ADMIN');
      if (isAdmin) {
        this.isEligibleToReview = true;
        this.eligibilityMessage = '';
        return;
      }

      this.bookingService.getLichSu().subscribe({
        next: history => {
          const matchingBookings = history.filter(h => {
            if (!h.tenPhim || !h.gioChieu) return false;
            const nameMatch = normalizeForMatch(h.tenPhim) === normalizeForMatch(this.phim!.tenPhim);
            const isPaid = h.trangThai === '2' || h.trangThai === 'SUCCESS' || h.trangThai === 'PAID' || h.trangThai === 'DA_THANH_TOAN';
            return nameMatch && isPaid;
          });

          if (matchingBookings.length === 0) {
            this.isEligibleToReview = false;
            this.eligibilityMessage = 'Bạn chỉ có thể đánh giá sau khi đã mua vé xem phim này.';
            return;
          }

          const now = dayjs();
          const fourteenDaysAgo = now.subtract(14, 'day');
          const hasRecentBooking = matchingBookings.some(h => {
            const showtimeDate = dayjs(h.gioChieu);
            return showtimeDate.isBefore(now) && showtimeDate.isAfter(fourteenDaysAgo);
          });

          if (hasRecentBooking) {
            this.isEligibleToReview = true;
            this.eligibilityMessage = '';
          } else {
            this.isEligibleToReview = false;
            this.eligibilityMessage = 'Đã quá thời hạn 14 ngày kể từ suất chiếu để gửi đánh giá phim.';
          }
        },
        error: () => {
          this.isEligibleToReview = false;
          this.eligibilityMessage = 'Không thể xác thực quyền đánh giá phim.';
        },
      });
    });
  }

  getStarText(rating?: number | null): string {
    const value = Math.max(0, Math.min(10, rating ?? 0));
    return `${value}/10`;
  }

  formatReviewDate(review: IDanhGia): string {
    return review.createdAt ? review.createdAt.format('HH:mm - DD/MM/YYYY') : 'Vừa xong';
  }

  getRoundedAverageRating(): number {
    return Math.round(this.averageRating);
  }
}
