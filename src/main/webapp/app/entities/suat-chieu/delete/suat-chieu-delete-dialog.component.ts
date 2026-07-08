import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuService } from '../service/suat-chieu.service';

@Component({
  templateUrl: './suat-chieu-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SuatChieuDeleteDialogComponent {
  suatChieu?: ISuatChieu;

  protected suatChieuService = inject(SuatChieuService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.suatChieuService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
