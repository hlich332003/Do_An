import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IDichVuFB } from '../dich-vu-fb.model';

@Component({
  selector: 'jhi-dich-vu-fb-detail',
  templateUrl: './dich-vu-fb-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class DichVuFBDetailComponent {
  dichVuFB = input<IDichVuFB | null>(null);

  formatStatusLabel(value?: string | null): string {
    if (value === '1') {
      return 'Đang bán';
    }
    if (value === '0') {
      return 'Ngừng bán';
    }
    return value?.trim() || 'Chưa cập nhật trạng thái';
  }

  previousState(): void {
    window.history.back();
  }
}
