import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPhim } from '../phim.model';
import { PhimService } from '../service/phim.service';

@Component({
  templateUrl: './phim-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PhimDeleteDialogComponent {
  phim?: IPhim;

  protected phimService = inject(PhimService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.phimService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
