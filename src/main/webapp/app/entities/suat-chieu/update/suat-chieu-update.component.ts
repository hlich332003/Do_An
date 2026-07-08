import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, ParamMap, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPhim } from 'app/entities/phim/phim.model';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';
import { SuatChieuService } from '../service/suat-chieu.service';
import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuFormGroup, SuatChieuFormService } from './suat-chieu-form.service';

@Component({
  selector: 'jhi-suat-chieu-update',
  templateUrl: './suat-chieu-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule],
})
export class SuatChieuUpdateComponent implements OnInit {
  isSaving = false;
  overlapError = false;
  saveErrorMessage = '';
  suatChieu: ISuatChieu | null = null;
  preselectedRoomId: number | null = null;

  phimsSharedCollection: IPhim[] = [];
  phongChieusSharedCollection: IPhongChieu[] = [];

  protected suatChieuService = inject(SuatChieuService);
  protected suatChieuFormService = inject(SuatChieuFormService);
  protected phimService = inject(PhimService);
  protected phongChieuService = inject(PhongChieuService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SuatChieuFormGroup = this.suatChieuFormService.createSuatChieuFormGroup();

  comparePhim = (o1: IPhim | null, o2: IPhim | null): boolean => this.phimService.comparePhim(o1, o2);

  comparePhongChieu = (o1: IPhongChieu | null, o2: IPhongChieu | null): boolean => this.phongChieuService.comparePhongChieu(o1, o2);

  get isEditMode(): boolean {
    return this.editForm.controls.id.value !== null;
  }

  get selectedPhim(): IPhim | null {
    return (this.editForm.get('phim')?.value as IPhim) ?? null;
  }

  get selectedPhongChieu(): IPhongChieu | null {
    return (this.editForm.get('phongChieu')?.value as IPhongChieu) ?? null;
  }

  get hasMovieSetup(): boolean {
    return this.phimsSharedCollection.length > 0;
  }

  get hasRoomSetup(): boolean {
    return this.phongChieusSharedCollection.length > 0;
  }

  get estimatedDurationText(): string {
    const movieDuration = this.selectedPhim?.thoiLuong;
    if (!movieDuration) {
      return 'Chọn phim để hệ thống tự tính giờ kết thúc.';
    }
    return `${movieDuration} phút phim + 15 phút dọn phòng.`;
  }

  get couplePrice(): number {
    const vipPrice = Number(this.editForm.get('giaVip')?.value ?? 0);
    return vipPrice > 0 ? vipPrice * 2 : 0;
  }

  ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe((params: ParamMap) => {
      const roomId = Number(params.get('roomId'));
      this.preselectedRoomId = Number.isFinite(roomId) && roomId > 0 ? roomId : null;
      this.applyPreselectedRoom();
    });

    this.activatedRoute.data.subscribe(({ suatChieu }) => {
      this.suatChieu = suatChieu;
      if (suatChieu) {
        this.updateForm(suatChieu);
      }

      this.loadRelationshipsOptions();
    });

    this.editForm.get('thoiGianBatDau')?.valueChanges.subscribe(() => this.calculateEndTime());
    this.editForm.get('phim')?.valueChanges.subscribe(() => this.calculateEndTime());
  }

  calculateEndTime(): void {
    const thoiGianBatDau = this.editForm.get('thoiGianBatDau')?.value;
    const phim = this.editForm.get('phim')?.value as IPhim;
    if (thoiGianBatDau && phim && phim.thoiLuong) {
      const start = dayjs(thoiGianBatDau);
      if (start.isValid()) {
        const end = start.add(phim.thoiLuong + 15, 'minute');
        this.editForm.patchValue({ thoiGianKetThuc: end.format('YYYY-MM-DDTHH:mm') }, { emitEvent: false });
      }
    }
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    this.overlapError = false;
    this.saveErrorMessage = '';
    const suatChieu = this.suatChieuFormService.getSuatChieu(this.editForm);
    if (suatChieu.id !== null) {
      this.subscribeToSaveResponse(this.suatChieuService.update(suatChieu));
    } else {
      this.subscribeToSaveResponse(this.suatChieuService.create(suatChieu));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISuatChieu>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: err => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(err?: any): void {
    if (err && err.error && err.error.errorKey === 'overlapping') {
      this.overlapError = true;
      this.saveErrorMessage = '';
      return;
    }

    this.saveErrorMessage =
      err?.error?.detail || err?.error?.title || 'Không thể tạo suất chiếu. Vui lòng kiểm tra lại phim, phòng và thời gian.';
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(suatChieu: ISuatChieu): void {
    this.suatChieu = suatChieu;
    this.suatChieuFormService.resetForm(this.editForm, suatChieu);

    this.phimsSharedCollection = this.phimService.addPhimToCollectionIfMissing<IPhim>(this.phimsSharedCollection, suatChieu.phim);
    this.phongChieusSharedCollection = this.phongChieuService.addPhongChieuToCollectionIfMissing<IPhongChieu>(
      this.phongChieusSharedCollection,
      suatChieu.phongChieu,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.phimService
      .query({ page: 0, size: 1000, sort: ['id,desc'] })
      .pipe(map((res: HttpResponse<IPhim[]>) => res.body ?? []))
      .pipe(map((phims: IPhim[]) => this.phimService.addPhimToCollectionIfMissing<IPhim>(phims, this.suatChieu?.phim)))
      .subscribe((phims: IPhim[]) => (this.phimsSharedCollection = phims));

    this.phongChieuService
      .query({ page: 0, size: 1000, sort: ['id,desc'] })
      .pipe(map((res: HttpResponse<IPhongChieu[]>) => res.body ?? []))
      .pipe(
        map((phongChieus: IPhongChieu[]) =>
          this.phongChieuService.addPhongChieuToCollectionIfMissing<IPhongChieu>(phongChieus, this.suatChieu?.phongChieu),
        ),
      )
      .subscribe((phongChieus: IPhongChieu[]) => {
        this.phongChieusSharedCollection = phongChieus;
        this.applyPreselectedRoom();
      });
  }

  private applyPreselectedRoom(): void {
    if (this.isEditMode || !this.preselectedRoomId || this.phongChieusSharedCollection.length === 0) {
      return;
    }

    const selectedRoom = this.phongChieusSharedCollection.find(room => room.id === this.preselectedRoomId);
    if (selectedRoom) {
      this.editForm.patchValue({ phongChieu: selectedRoom }, { emitEvent: false });
    }
  }
}
