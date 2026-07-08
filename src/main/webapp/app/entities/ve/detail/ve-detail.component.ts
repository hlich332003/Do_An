import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IVe } from '../ve.model';
import { formatTicketStatusLabel } from 'app/shared/util/display-text.util';

@Component({
  selector: 'jhi-ve-detail',
  templateUrl: './ve-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class VeDetailComponent {
  ve = input<IVe | null>(null);

  formatStatusLabel(value?: string | null): string {
    return formatTicketStatusLabel(value);
  }

  previousState(): void {
    window.history.back();
  }
}
