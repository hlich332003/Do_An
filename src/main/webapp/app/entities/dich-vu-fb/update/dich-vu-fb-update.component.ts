import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDichVuFB } from '../dich-vu-fb.model';
import { DichVuFBService } from '../service/dich-vu-fb.service';
import { DichVuFBFormGroup, DichVuFBFormService } from './dich-vu-fb-form.service';

@Component({
  selector: 'jhi-dich-vu-fb-update',
  templateUrl: './dich-vu-fb-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class DichVuFBUpdateComponent implements OnInit {
  isSaving = false;
  dichVuFB: IDichVuFB | null = null;
  imagePreview: string | null = null;

  protected dichVuFBService = inject(DichVuFBService);
  protected dichVuFBFormService = inject(DichVuFBFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: DichVuFBFormGroup = this.dichVuFBFormService.createDichVuFBFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ dichVuFB }) => {
      this.dichVuFB = dichVuFB;
      if (dichVuFB) {
        this.updateForm(dichVuFB);
      }
    });
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : null;
      this.imagePreview = result;
      this.editForm.controls.hinhAnh.setValue(result);
      this.editForm.controls.hinhAnh.markAsDirty();
    };
    reader.readAsDataURL(file);
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const dichVuFB = this.dichVuFBFormService.getDichVuFB(this.editForm);
    if (dichVuFB.id !== null) {
      this.subscribeToSaveResponse(this.dichVuFBService.update(dichVuFB));
    } else {
      this.subscribeToSaveResponse(this.dichVuFBService.create(dichVuFB));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IDichVuFB>>): void {
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

  protected updateForm(dichVuFB: IDichVuFB): void {
    this.dichVuFB = dichVuFB;
    this.dichVuFBFormService.resetForm(this.editForm, dichVuFB);
    this.imagePreview = dichVuFB.hinhAnh ?? null;
  }
}
