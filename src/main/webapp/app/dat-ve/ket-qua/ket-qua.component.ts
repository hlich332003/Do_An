import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { BookingService, IHoaDonLichSu } from '../service/booking.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'jhi-ket-qua',
  standalone: true,
  imports: [CommonModule, RouterModule, FaIconComponent],
  templateUrl: './ket-qua.component.html',
  styleUrls: ['./ket-qua.component.scss'],
})
export class KetQuaComponent implements OnInit {
  hoaDonId: number | null = null;
  chiTiet: IHoaDonLichSu | null = null;
  qrCodeUrl: SafeUrl | null = null;
  isLoading = true;
  paymentStatus: 'success' | 'failed' = 'success';
  paymentMessage = '';
  bookingNotFound = false;
  isCashierMode = false;

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly bookingService = inject(BookingService);
  private readonly sanitizer = inject(DomSanitizer);

  ngOnInit(): void {
    this.isCashierMode = this.router.url.includes('/admin');
    const params = this.route.snapshot.queryParamMap;
    this.hoaDonId = params.get('hoaDonId') ? Number(params.get('hoaDonId')) : null;
    this.paymentStatus = params.get('status') === 'failed' ? 'failed' : 'success';
    this.paymentMessage = params.get('message') ?? '';
    this.loadChiTiet();
  }

  loadChiTiet(): void {
    this.bookingService.getLichSu().subscribe({
      next: data => {
        this.chiTiet = data.find(h => h.id === this.hoaDonId) ?? null;
        this.bookingNotFound = !this.chiTiet;
        if (this.chiTiet?.veIds?.length) {
          this.bookingService.getQrCode(this.chiTiet.veIds[0]).subscribe({
            next: qr => {
              this.qrCodeUrl = this.sanitizer.bypassSecurityTrustUrl(`data:image/png;base64,${qr}`);
              this.isLoading = false;
            },
            error: () => {
              this.isLoading = false;
            },
          });
        } else {
          this.isLoading = false;
        }
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }
}
