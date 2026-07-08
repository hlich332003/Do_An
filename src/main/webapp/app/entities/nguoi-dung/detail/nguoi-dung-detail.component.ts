import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { INguoiDung } from '../nguoi-dung.model';

@Component({
  selector: 'jhi-nguoi-dung-detail',
  templateUrl: './nguoi-dung-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class NguoiDungDetailComponent {
  nguoiDung = input<INguoiDung | null>(null);

  previousState(): void {
    window.history.back();
  }
}
