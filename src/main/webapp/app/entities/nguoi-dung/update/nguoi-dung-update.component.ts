import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { INguoiDung } from '../nguoi-dung.model';
import { NguoiDungService } from '../service/nguoi-dung.service';
import { NguoiDungFormGroup, NguoiDungFormService } from './nguoi-dung-form.service';

@Component({
  selector: 'jhi-nguoi-dung-update',
  templateUrl: './nguoi-dung-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class NguoiDungUpdateComponent implements OnInit {
  isSaving = false;
  nguoiDung: INguoiDung | null = null;

  protected nguoiDungService = inject(NguoiDungService);
  protected nguoiDungFormService = inject(NguoiDungFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: NguoiDungFormGroup = this.nguoiDungFormService.createNguoiDungFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ nguoiDung }) => {
      this.nguoiDung = nguoiDung;
      if (nguoiDung) {
        this.updateForm(nguoiDung);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const nguoiDung = this.nguoiDungFormService.getNguoiDung(this.editForm);
    if (nguoiDung.id !== null) {
      this.subscribeToSaveResponse(this.nguoiDungService.update(nguoiDung));
    } else {
      this.subscribeToSaveResponse(this.nguoiDungService.create(nguoiDung));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<INguoiDung>>): void {
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

  protected updateForm(nguoiDung: INguoiDung): void {
    this.nguoiDung = nguoiDung;
    this.nguoiDungFormService.resetForm(this.editForm, nguoiDung);
  }
}
