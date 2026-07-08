import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';
import { GheService } from '../service/ghe.service';
import { IGhe } from '../ghe.model';
import { GheFormGroup, GheFormService } from './ghe-form.service';

@Component({
  selector: 'jhi-ghe-update',
  templateUrl: './ghe-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class GheUpdateComponent implements OnInit {
  isSaving = false;
  ghe: IGhe | null = null;

  phongChieusSharedCollection: IPhongChieu[] = [];

  protected gheService = inject(GheService);
  protected gheFormService = inject(GheFormService);
  protected phongChieuService = inject(PhongChieuService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: GheFormGroup = this.gheFormService.createGheFormGroup();

  comparePhongChieu = (o1: IPhongChieu | null, o2: IPhongChieu | null): boolean => this.phongChieuService.comparePhongChieu(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ ghe }) => {
      this.ghe = ghe;
      if (ghe) {
        this.updateForm(ghe);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const ghe = this.gheFormService.getGhe(this.editForm);
    if (ghe.id !== null) {
      this.subscribeToSaveResponse(this.gheService.update(ghe));
    } else {
      this.subscribeToSaveResponse(this.gheService.create(ghe));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGhe>>): void {
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

  protected updateForm(ghe: IGhe): void {
    this.ghe = ghe;
    this.gheFormService.resetForm(this.editForm, ghe);

    this.phongChieusSharedCollection = this.phongChieuService.addPhongChieuToCollectionIfMissing<IPhongChieu>(
      this.phongChieusSharedCollection,
      ghe.phongChieu,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.phongChieuService
      .query()
      .pipe(map((res: HttpResponse<IPhongChieu[]>) => res.body ?? []))
      .pipe(
        map((phongChieus: IPhongChieu[]) =>
          this.phongChieuService.addPhongChieuToCollectionIfMissing<IPhongChieu>(phongChieus, this.ghe?.phongChieu),
        ),
      )
      .subscribe((phongChieus: IPhongChieu[]) => (this.phongChieusSharedCollection = phongChieus));
  }
}
