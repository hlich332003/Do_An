import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IPhongChieu } from '../phong-chieu.model';

@Component({
  selector: 'jhi-phong-chieu-detail',
  templateUrl: './phong-chieu-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class PhongChieuDetailComponent {
  phongChieu = input<IPhongChieu | null>(null);

  getRoomStatusLabel(status?: string | number | null): string {
    const normalized = String(status ?? '').toLowerCase();
    if (normalized.includes('bao') || normalized.includes('tri')) {
      return 'Bảo trì';
    }
    if (normalized.includes('khong')) {
      return 'Ngừng khai thác';
    }
    return 'Hoạt động';
  }

  previousState(): void {
    window.history.back();
  }
}
