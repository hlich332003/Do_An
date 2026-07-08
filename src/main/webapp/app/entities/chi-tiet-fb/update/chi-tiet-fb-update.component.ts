import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDichVuFB } from 'app/entities/dich-vu-fb/dich-vu-fb.model';
import { DichVuFBService } from 'app/entities/dich-vu-fb/service/dich-vu-fb.service';
import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';
import { HoaDonService } from 'app/entities/hoa-don/service/hoa-don.service';
import { ChiTietFBService } from '../service/chi-tiet-fb.service';
import { IChiTietFB } from '../chi-tiet-fb.model';
import { ChiTietFBFormGroup, ChiTietFBFormService } from './chi-tiet-fb-form.service';

@Component({
  selector: 'jhi-chi-tiet-fb-update',
  templateUrl: './chi-tiet-fb-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ChiTietFBUpdateComponent implements OnInit {
  isSaving = false;
  chiTietFB: IChiTietFB | null = null;

  dichVuFBSSharedCollection: IDichVuFB[] = [];
  hoaDonsSharedCollection: IHoaDon[] = [];

  protected chiTietFBService = inject(ChiTietFBService);
  protected chiTietFBFormService = inject(ChiTietFBFormService);
  protected dichVuFBService = inject(DichVuFBService);
  protected hoaDonService = inject(HoaDonService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ChiTietFBFormGroup = this.chiTietFBFormService.createChiTietFBFormGroup();

  compareDichVuFB = (o1: IDichVuFB | null, o2: IDichVuFB | null): boolean => this.dichVuFBService.compareDichVuFB(o1, o2);

  compareHoaDon = (o1: IHoaDon | null, o2: IHoaDon | null): boolean => this.hoaDonService.compareHoaDon(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ chiTietFB }) => {
      this.chiTietFB = chiTietFB;
      if (chiTietFB) {
        this.updateForm(chiTietFB);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const chiTietFB = this.chiTietFBFormService.getChiTietFB(this.editForm);
    if (chiTietFB.id !== null) {
      this.subscribeToSaveResponse(this.chiTietFBService.update(chiTietFB));
    } else {
      this.subscribeToSaveResponse(this.chiTietFBService.create(chiTietFB));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IChiTietFB>>): void {
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

  protected updateForm(chiTietFB: IChiTietFB): void {
    this.chiTietFB = chiTietFB;
    this.chiTietFBFormService.resetForm(this.editForm, chiTietFB);

    this.dichVuFBSSharedCollection = this.dichVuFBService.addDichVuFBToCollectionIfMissing<IDichVuFB>(
      this.dichVuFBSSharedCollection,
      chiTietFB.dichVuFB,
    );
    this.hoaDonsSharedCollection = this.hoaDonService.addHoaDonToCollectionIfMissing<IHoaDon>(
      this.hoaDonsSharedCollection,
      chiTietFB.hoaDon,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.dichVuFBService
      .query()
      .pipe(map((res: HttpResponse<IDichVuFB[]>) => res.body ?? []))
      .pipe(
        map((dichVuFBS: IDichVuFB[]) =>
          this.dichVuFBService.addDichVuFBToCollectionIfMissing<IDichVuFB>(dichVuFBS, this.chiTietFB?.dichVuFB),
        ),
      )
      .subscribe((dichVuFBS: IDichVuFB[]) => (this.dichVuFBSSharedCollection = dichVuFBS));

    this.hoaDonService
      .query()
      .pipe(map((res: HttpResponse<IHoaDon[]>) => res.body ?? []))
      .pipe(map((hoaDons: IHoaDon[]) => this.hoaDonService.addHoaDonToCollectionIfMissing<IHoaDon>(hoaDons, this.chiTietFB?.hoaDon)))
      .subscribe((hoaDons: IHoaDon[]) => (this.hoaDonsSharedCollection = hoaDons));
  }
}
