import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { INguoiDung } from 'app/entities/nguoi-dung/nguoi-dung.model';
import { NguoiDungService } from 'app/entities/nguoi-dung/service/nguoi-dung.service';
import { IHoaDon } from '../hoa-don.model';
import { HoaDonService } from '../service/hoa-don.service';
import { HoaDonFormGroup, HoaDonFormService } from './hoa-don-form.service';

@Component({
  selector: 'jhi-hoa-don-update',
  templateUrl: './hoa-don-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class HoaDonUpdateComponent implements OnInit {
  isSaving = false;
  hoaDon: IHoaDon | null = null;

  nguoiDungsSharedCollection: INguoiDung[] = [];

  protected hoaDonService = inject(HoaDonService);
  protected hoaDonFormService = inject(HoaDonFormService);
  protected nguoiDungService = inject(NguoiDungService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: HoaDonFormGroup = this.hoaDonFormService.createHoaDonFormGroup();

  compareNguoiDung = (o1: INguoiDung | null, o2: INguoiDung | null): boolean => this.nguoiDungService.compareNguoiDung(o1, o2);

  getTrangThaiLabel(status?: string | null): string {
    if (!status) return 'Chưa Thanh Toán';
    const s = status.toUpperCase();
    if (s === 'DA_THANH_TOAN' || s === '2' || s === 'SUCCESS' || s === 'PAID' || s === 'DONE') {
      return 'Đã Thanh Toán';
    }
    if (s === 'DA_HUY' || s === '0' || s === 'CANCELLED') {
      return 'Đã Hủy';
    }
    return 'Chưa Thanh Toán';
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ hoaDon }) => {
      this.hoaDon = hoaDon;
      if (hoaDon) {
        this.updateForm(hoaDon);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const hoaDon = this.hoaDonFormService.getHoaDon(this.editForm);
    if (hoaDon.id !== null) {
      this.subscribeToSaveResponse(this.hoaDonService.update(hoaDon));
    } else {
      this.subscribeToSaveResponse(this.hoaDonService.create(hoaDon));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IHoaDon>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(hoaDon: IHoaDon): void {
    this.hoaDon = hoaDon;
    this.hoaDonFormService.resetForm(this.editForm, hoaDon);

    this.nguoiDungsSharedCollection = this.nguoiDungService.addNguoiDungToCollectionIfMissing<INguoiDung>(
      this.nguoiDungsSharedCollection,
      hoaDon.nguoiDung,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.nguoiDungService
      .query()
      .pipe(map((res: HttpResponse<INguoiDung[]>) => res.body ?? []))
      .pipe(
        map((nguoiDungs: INguoiDung[]) =>
          this.nguoiDungService.addNguoiDungToCollectionIfMissing<INguoiDung>(nguoiDungs, this.hoaDon?.nguoiDung),
        ),
      )
      .subscribe((nguoiDungs: INguoiDung[]) => (this.nguoiDungsSharedCollection = nguoiDungs));
  }
}
