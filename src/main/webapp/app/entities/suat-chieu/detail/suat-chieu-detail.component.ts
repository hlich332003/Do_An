import { Component, inject, input } from '@angular/core';
import { filter, tap } from 'rxjs';
import { RouterModule } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuDeleteDialogComponent } from '../delete/suat-chieu-delete-dialog.component';

@Component({
  selector: 'jhi-suat-chieu-detail',
  templateUrl: './suat-chieu-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class SuatChieuDetailComponent {
  suatChieu = input<ISuatChieu | null>(null);
  protected modalService = inject(NgbModal);

  previousState(): void {
    window.history.back();
  }

  deleteSuatChieu(): void {
    const suatChieuRef = this.suatChieu();
    if (!suatChieuRef?.id) {
      return;
    }

    const modalRef = this.modalService.open(SuatChieuDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.suatChieu = suatChieuRef;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.previousState()),
      )
      .subscribe();
  }
}
