import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IChiTietFB } from '../chi-tiet-fb.model';
import { ChiTietFBService } from '../service/chi-tiet-fb.service';

@Component({
  templateUrl: './chi-tiet-fb-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ChiTietFBDeleteDialogComponent {
  chiTietFB?: IChiTietFB;

  protected chiTietFBService = inject(ChiTietFBService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.chiTietFBService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
