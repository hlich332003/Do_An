import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IChiTietFB } from '../chi-tiet-fb.model';

@Component({
  selector: 'jhi-chi-tiet-fb-detail',
  templateUrl: './chi-tiet-fb-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class ChiTietFBDetailComponent {
  chiTietFB = input<IChiTietFB | null>(null);

  previousState(): void {
    window.history.back();
  }
}
