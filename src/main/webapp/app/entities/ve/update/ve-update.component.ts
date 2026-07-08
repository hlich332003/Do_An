import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';
import { HoaDonService } from 'app/entities/hoa-don/service/hoa-don.service';
import { ISuatChieu } from 'app/entities/suat-chieu/suat-chieu.model';
import { SuatChieuService } from 'app/entities/suat-chieu/service/suat-chieu.service';
import { VeService } from '../service/ve.service';
import { IVe } from '../ve.model';
import { VeFormGroup, VeFormService } from './ve-form.service';

@Component({
  selector: 'jhi-ve-update',
  templateUrl: './ve-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class VeUpdateComponent implements OnInit {
  isSaving = false;
  ve: IVe | null = null;

  hoaDonsSharedCollection: IHoaDon[] = [];
  suatChieusSharedCollection: ISuatChieu[] = [];

  protected veService = inject(VeService);
  protected veFormService = inject(VeFormService);
  protected hoaDonService = inject(HoaDonService);
  protected suatChieuService = inject(SuatChieuService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VeFormGroup = this.veFormService.createVeFormGroup();

  compareHoaDon = (o1: IHoaDon | null, o2: IHoaDon | null): boolean => this.hoaDonService.compareHoaDon(o1, o2);

  compareSuatChieu = (o1: ISuatChieu | null, o2: ISuatChieu | null): boolean => this.suatChieuService.compareSuatChieu(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ ve }) => {
      this.ve = ve;
      if (ve) {
        this.updateForm(ve);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const ve = this.veFormService.getVe(this.editForm);
    if (ve.id !== null) {
      this.subscribeToSaveResponse(this.veService.update(ve));
    } else {
      this.subscribeToSaveResponse(this.veService.create(ve));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVe>>): void {
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

  protected updateForm(ve: IVe): void {
    this.ve = ve;
    this.veFormService.resetForm(this.editForm, ve);

    this.hoaDonsSharedCollection = this.hoaDonService.addHoaDonToCollectionIfMissing<IHoaDon>(this.hoaDonsSharedCollection, ve.hoaDon);
    this.suatChieusSharedCollection = this.suatChieuService.addSuatChieuToCollectionIfMissing<ISuatChieu>(
      this.suatChieusSharedCollection,
      ve.suatChieu,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.hoaDonService
      .query()
      .pipe(map((res: HttpResponse<IHoaDon[]>) => res.body ?? []))
      .pipe(map((hoaDons: IHoaDon[]) => this.hoaDonService.addHoaDonToCollectionIfMissing<IHoaDon>(hoaDons, this.ve?.hoaDon)))
      .subscribe((hoaDons: IHoaDon[]) => (this.hoaDonsSharedCollection = hoaDons));

    this.suatChieuService
      .query()
      .pipe(map((res: HttpResponse<ISuatChieu[]>) => res.body ?? []))
      .pipe(
        map((suatChieus: ISuatChieu[]) =>
          this.suatChieuService.addSuatChieuToCollectionIfMissing<ISuatChieu>(suatChieus, this.ve?.suatChieu),
        ),
      )
      .subscribe((suatChieus: ISuatChieu[]) => (this.suatChieusSharedCollection = suatChieus));
  }
}
