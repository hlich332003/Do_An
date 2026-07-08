import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IHoaDon } from '../hoa-don.model';
import { BookingService, IHoaDonLichSu } from 'app/dat-ve/service/booking.service';
import { formatTicketStatusLabel } from 'app/shared/util/display-text.util';

@Component({
  selector: 'jhi-hoa-don-detail',
  templateUrl: './hoa-don-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class HoaDonDetailComponent {
  hoaDon = input<IHoaDon | null>(null);

  formatStatusLabel(value?: string | null): string {
    return formatTicketStatusLabel(value);
  }
  hoaDonLichSu = signal<IHoaDonLichSu | null>(null);

  private readonly bookingService = inject(BookingService);
  private loadedHoaDonId: number | null = null;

  constructor() {
    effect(() => {
      const hoaDon = this.hoaDon();
      if (!hoaDon?.id || hoaDon.id === this.loadedHoaDonId) {
        return;
      }
      this.loadedHoaDonId = hoaDon.id;
      this.bookingService.getAdminLichSuById(hoaDon.id).subscribe({
        next: data => {
          this.hoaDonLichSu.set(data);
        },
        error: () => {
          this.hoaDonLichSu.set(null);
        },
      });
    });
  }

  previousState(): void {
    window.history.back();
  }

  getVeCount(): number {
    return this.hoaDonLichSu()?.veIds?.length ?? 0;
  }
}
