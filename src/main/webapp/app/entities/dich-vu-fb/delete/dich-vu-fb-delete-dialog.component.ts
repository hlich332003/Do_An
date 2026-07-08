import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IDichVuFB } from '../dich-vu-fb.model';
import { DichVuFBService } from '../service/dich-vu-fb.service';

@Component({
  templateUrl: './dich-vu-fb-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class DichVuFBDeleteDialogComponent {
  dichVuFB?: IDichVuFB;

  protected dichVuFBService = inject(DichVuFBService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.dichVuFBService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
