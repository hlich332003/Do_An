import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IVe } from '../ve.model';
import { VeService } from '../service/ve.service';

@Component({
  templateUrl: './ve-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class VeDeleteDialogComponent {
  ve?: IVe;

  protected veService = inject(VeService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.veService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
