import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { IPhim } from '../phim.model';

@Component({
  selector: 'jhi-phim-detail',
  templateUrl: './phim-detail.component.html',
  styleUrls: ['./phim-detail.component.scss'],
  imports: [SharedModule, RouterModule, FormatMediumDatePipe],
})
export class PhimDetailComponent {
  phim = input<IPhim | null>(null);

  previousState(): void {
    window.history.back();
  }
}
