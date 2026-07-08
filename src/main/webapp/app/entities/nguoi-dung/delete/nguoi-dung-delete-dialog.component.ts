import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { INguoiDung } from '../nguoi-dung.model';
import { NguoiDungService } from '../service/nguoi-dung.service';

@Component({
  templateUrl: './nguoi-dung-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class NguoiDungDeleteDialogComponent {
  nguoiDung?: INguoiDung;

  protected nguoiDungService = inject(NguoiDungService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.nguoiDungService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
