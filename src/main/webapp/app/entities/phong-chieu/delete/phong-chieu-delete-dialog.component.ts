import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPhongChieu } from '../phong-chieu.model';
import { PhongChieuService } from '../service/phong-chieu.service';

@Component({
  templateUrl: './phong-chieu-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PhongChieuDeleteDialogComponent {
  phongChieu?: IPhongChieu;

  protected phongChieuService = inject(PhongChieuService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.phongChieuService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
