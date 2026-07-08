import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SoDoGheComponent, IGheVM } from './so-do-ghe/so-do-ghe.component';
import { ChonDichVuComponent, IDichVuFBVM } from './chon-dich-vu/chon-dich-vu.component';
import { ISuatChieu } from 'app/entities/suat-chieu/suat-chieu.model';
import SharedModule from 'app/shared/shared.module';
import { BookingService } from './service/booking.service';

import { AccountService } from 'app/core/auth/account.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { NguoiDungService } from 'app/entities/nguoi-dung/service/nguoi-dung.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'jhi-dat-ve',
  standalone: true,
  imports: [CommonModule, FormsModule, SharedModule, SoDoGheComponent, ChonDichVuComponent],
  templateUrl: './dat-ve.component.html',
})
export class DatVeComponent implements OnInit, OnDestroy {
  suatChieu: ISuatChieu | null = null;
  gheDaChonBanDau: IGheVM[] = [];
  gheDaChon: IGheVM[] = [];
  combosDaChon: IDichVuFBVM[] = [];

  pingInterval: any;

  tongTienGhe = 0;
  tongTienCombo = 0;
  giamGia = 0;
  maGiamGia = '';
  isSaving = false;
  nguoiDungId: number | null = null;

  phuongThucThanhToan = 'VNPAY';
  showMockGateway = false;
  isSavingPayment = false;
  createdHoaDon: any = null;

  protected route = inject(ActivatedRoute);
  protected router = inject(Router);
  protected bookingService = inject(BookingService);

  protected accountService = inject(AccountService);
  protected stateStorageService = inject(StateStorageService);
  protected nguoiDungService = inject(NguoiDungService);

  ngOnInit(): void {
    this.route.data.subscribe(({ suatChieu }) => {
      this.suatChieu = suatChieu;
      if (suatChieu) {
        const savedState = sessionStorage.getItem(`booking_state_${suatChieu.id}`);
        if (savedState) {
          try {
            const parsed = JSON.parse(savedState);
            this.gheDaChonBanDau = parsed.gheDaChon || [];
            this.gheDaChon = [...this.gheDaChonBanDau];
            this.combosDaChon = parsed.combosDaChon || [];
            this.tinhTongTienGhe();
            this.tinhTongTienCombo();
          } catch (e) {
            console.error('Error parsing booking state', e);
          }
        }
      }
    });

    this.accountService.identity().subscribe(account => {
      if (account?.email) {
        this.nguoiDungService.query({ 'email.equals': account.email, size: 1 }).subscribe(res => {
          const nguoiDung = res.body?.[0];
          if (nguoiDung) {
            this.nguoiDungId = nguoiDung.id;
          }
        });
      } else {
        if (this.gheDaChon.length > 0) {
          const suatChieuId = this.suatChieu?.id;
          if (suatChieuId) {
            this.gheDaChon.forEach(g => {
              if (g.maGhe) {
                this.bookingService.releaseSeat(suatChieuId, g.maGhe).subscribe();
              }
            });
          }
          this.gheDaChon = [];
          this.gheDaChonBanDau = [];
          this.combosDaChon = [];
          this.tinhTongTienGhe();
          this.tinhTongTienCombo();
          this.stopPing();
          if (this.suatChieu) {
            sessionStorage.removeItem(`booking_state_${this.suatChieu.id}`);
          }
        }
        this.nguoiDungId = null;
      }
    });
  }

  isAdmin(): boolean {
    return this.accountService.hasAnyAuthority('ROLE_ADMIN');
  }

  ngOnDestroy(): void {
    this.stopPing();
    if (this.gheDaChon.length > 0 && !this.createdHoaDon) {
      const maGhes = this.gheDaChon.map(g => g.maGhe!);
      if (this.suatChieu) {
        this.bookingService.releaseSeats(this.suatChieu.id, maGhes).subscribe();
      }
      this.clearSavedState();
    } else {
      this.saveState();
    }
  }

  private clearSavedState(): void {
    if (this.suatChieu) {
      sessionStorage.removeItem(`booking_state_${this.suatChieu.id}`);
    }
  }

  saveState(): void {
    if (this.suatChieu && (this.gheDaChon.length > 0 || this.combosDaChon.length > 0)) {
      sessionStorage.setItem(
        `booking_state_${this.suatChieu.id}`,
        JSON.stringify({
          gheDaChon: this.gheDaChon,
          combosDaChon: this.combosDaChon,
        }),
      );
    } else if (this.suatChieu) {
      sessionStorage.removeItem(`booking_state_${this.suatChieu.id}`);
    }
  }

  onGheDaChonChange(ghes: IGheVM[]): void {
    this.gheDaChon = ghes;
    this.tinhTongTienGhe();
    this.saveState();

    if (this.gheDaChon.length > 0) {
      this.startPing();
    } else {
      this.stopPing();
    }
  }

  startPing(): void {
    if (!this.pingInterval) {
      // Bắn Ping lên server mỗi 10 giây để gia hạn ghế thêm 15 giây
      this.pingInterval = setInterval(() => {
        if (this.suatChieu && this.gheDaChon.length > 0) {
          const maGhes = this.gheDaChon.map(g => g.maGhe!);
          this.bookingService.extendLock(this.suatChieu.id, maGhes).subscribe();
        }
      }, 10000);
    }
  }

  stopPing(): void {
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
  }

  onCombosDaChonChange(combos: IDichVuFBVM[]): void {
    this.combosDaChon = combos;
    this.tinhTongTienCombo();
    this.saveState();
  }

  tinhTongTienGhe(): void {
    this.tongTienGhe = this.gheDaChon.reduce((sum, ghe) => sum + this.getGiaGhe(ghe), 0);
  }

  tinhTongTienCombo(): void {
    this.tongTienCombo = this.combosDaChon.reduce((sum, combo) => sum + (combo.gia ?? 0) * (combo.soLuong ?? 0), 0);
  }

  getGiaGhe(ghe: IGheVM): number {
    const giaThuong = Number(this.suatChieu?.giaThuong ?? 50000);
    const giaVip = Number(this.suatChieu?.giaVip ?? 120000);

    if (ghe.loaiGhe === 3) return giaVip * 2;
    if (ghe.loaiGhe === 2) return giaVip;
    return giaThuong;
  }

  get tongTienThanhToan(): number {
    const total = this.tongTienGhe + this.tongTienCombo - this.giamGia;
    return total > 0 ? total : 0;
  }

  getDanhSachTenGhe(): string {
    return this.gheDaChon.map(g => g.maGhe).join(', ');
  }

  apDungMaGiamGia(): void {
    if (!this.maGiamGia.trim()) {
      return;
    }
    // GiamGia has been removed from the backend.
    // This is just a placeholder, or we alert the user it's disabled.
    alert('Tính năng mã giảm giá tạm thời không khả dụng.');
    this.giamGia = 0;
  }

  thanhToan(): void {
    if (this.gheDaChon.length === 0) {
      alert('Vui lòng chọn ít nhất 1 ghế!');
      return;
    }
    if (!this.suatChieu) {
      return;
    }

    if (!this.accountService.isAuthenticated()) {
      alert('Vui lòng đăng nhập để tiếp tục đặt vé!');
      if (this.router.url) {
        this.stateStorageService.storeUrl(this.router.url);
      }
      this.router.navigate(['/login']);
      return;
    }

    // Cắt Ping ngay khi bấm xác nhận thanh toán, phó thác cho RabbitMQ
    this.stopPing();
    this.isSaving = true;
    this.bookingService
      .createBooking({
        suatChieuId: this.suatChieu.id,
        gheIds: this.gheDaChon.map(g => g.id),
        combos: this.combosDaChon.map(c => ({ dichVuFBId: c.id, soLuong: c.soLuong ?? 0 })),
        maGiamGia: this.maGiamGia || undefined,
        phuongThucThanhToan: this.phuongThucThanhToan,
        nguoiDungId: this.nguoiDungId ?? undefined,
      })
      .subscribe({
        next: hoaDon => {
          this.createdHoaDon = hoaDon;
          if (this.phuongThucThanhToan === 'VNPAY') {
            this.bookingService.createVnpayPaymentUrl(hoaDon.id, false).subscribe({
              next: response => {
                this.clearSavedState();
                window.location.href = response.paymentUrl;
              },
              error: () => {
                this.isSaving = false;
                this.startPing();
                alert('Không xác nhận được thanh toán VNPay. Vui lòng thử lại.');
              },
            });
          } else if (this.phuongThucThanhToan === 'CASH') {
            this.isSaving = false;
            this.onConfirmPayment();
          } else {
            this.isSaving = false;
          }
        },
        error: () => {
          this.isSaving = false;
          alert('Tạo hóa đơn thất bại. Vui lòng thử lại.');
          // Nếu có lỗi mạng hoặc backend từ chối đơn, nối lại Ping để giữ ghế tiếp
          this.startPing();
        },
      });
  }

  onConfirmPayment(): void {
    if (!this.createdHoaDon) {
      return;
    }
    this.isSavingPayment = true;
    this.bookingService
      .confirmPayment(this.createdHoaDon.id, 'SUCCESS')
      .pipe(finalize(() => (this.isSavingPayment = false)))
      .subscribe({
        next: () => {
          this.gheDaChon = [];
          this.gheDaChonBanDau = [];
          this.combosDaChon = [];
          this.tinhTongTienGhe();
          this.tinhTongTienCombo();
          this.clearSavedState();
          if (this.router.url.includes('/admin')) {
            void this.router.navigate(['/admin/dat-ve/ket-qua'], { queryParams: { hoaDonId: this.createdHoaDon.id } });
          } else {
            void this.router.navigate(['/dat-ve/ket-qua'], { queryParams: { hoaDonId: this.createdHoaDon.id } });
          }
        },
        error: () => {
          alert('Thanh toán thất bại. Vui lòng kiểm tra lại.');
        },
      });
  }

  onCancelPayment(): void {
    if (!this.createdHoaDon) {
      return;
    }
    this.isSavingPayment = true;
    this.bookingService
      .cancelBooking(this.createdHoaDon.id)
      .pipe(finalize(() => (this.isSavingPayment = false)))
      .subscribe({
        next: () => {
          alert('Giao dịch thanh toán đã bị hủy. Ghế của bạn đã được giải phóng.');
          this.gheDaChon = [];
          this.gheDaChonBanDau = [];
          this.combosDaChon = [];
          this.tinhTongTienGhe();
          this.tinhTongTienCombo();
          this.clearSavedState();
        },
        error: () => {
          this.gheDaChon = [];
          this.gheDaChonBanDau = [];
          this.combosDaChon = [];
          this.tinhTongTienGhe();
          this.tinhTongTienCombo();
          this.clearSavedState();
        },
      });
  }
}
