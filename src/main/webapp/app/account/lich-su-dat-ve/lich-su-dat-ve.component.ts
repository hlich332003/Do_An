import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { BookingService, IHoaDonLichSu } from 'app/dat-ve/service/booking.service';

@Component({
  selector: 'jhi-lich-su-dat-ve',
  standalone: true,
  imports: [CommonModule, RouterModule, FaIconComponent],
  templateUrl: './lich-su-dat-ve.component.html',
})
export class LichSuDatVeComponent implements OnInit {
  danhSachVe: IHoaDonLichSu[] = [];
  isLoading = false;

  protected bookingService = inject(BookingService);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.bookingService.getLichSu().subscribe({
      next: data => {
        this.danhSachVe = data ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.danhSachVe = [];
        this.isLoading = false;
      },
    });
  }

  getTrangThaiLabel(trangThai?: string): string {
    switch (trangThai) {
      case 'PAID':
        return 'Đã thanh toán';
      case 'PENDING':
        return 'Chờ thanh toán';
      case 'CANCELLED':
        return 'Đã hủy';
      case 'REFUNDED':
        return 'Đã hoàn tiền';
      default:
        return trangThai ?? '';
    }
  }

  getTrangThaiClass(trangThai?: string): string {
    switch (trangThai) {
      case 'PAID':
        return 'bg-success';
      case 'PENDING':
        return 'bg-warning text-dark';
      case 'CANCELLED':
      case 'REFUNDED':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }

  getTongSoVe(): number {
    return this.danhSachVe.reduce((total, ve) => total + (ve.veIds?.length ?? 0), 0);
  }
}
