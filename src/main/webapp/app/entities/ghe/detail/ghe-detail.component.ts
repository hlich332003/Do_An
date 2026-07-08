import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IGhe } from '../ghe.model';

@Component({
  selector: 'jhi-ghe-detail',
  templateUrl: './ghe-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class GheDetailComponent {
  ghe = input<IGhe | null>(null);

  previousState(): void {
    window.history.back();
  }
}
