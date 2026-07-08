import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPhim } from '../phim.model';
import { PhimService } from '../service/phim.service';
import { PhimFormGroup, PhimFormService } from './phim-form.service';

@Component({
  selector: 'jhi-phim-update',
  templateUrl: './phim-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PhimUpdateComponent implements OnInit {
  isSaving = false;
  phim: IPhim | null = null;
  saveErrorMessage = '';
  readonly maxPosterFileSizeBytes = 5 * 1024 * 1024;
  readonly minReleaseDate = (() => {
    const today = new Date();
    return { year: today.getFullYear(), month: today.getMonth() + 1, day: today.getDate() };
  })();

  protected phimService = inject(PhimService);
  protected phimFormService = inject(PhimFormService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: PhimFormGroup = this.phimFormService.createPhimFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ phim }) => {
      this.phim = phim;
      if (phim) {
        this.updateForm(phim);
      } else {
        this.loadPhimFromRoute();
      }
    });
  }

  protected loadPhimFromRoute(): void {
    const id = this.activatedRoute.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.phimService.find(Number(id)).subscribe({
      next: response => {
        const phim = response.body;
        if (phim) {
          this.phim = phim;
          this.updateForm(phim);
        }
      },
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.saveErrorMessage = '';
    const phim = this.phimFormService.getPhim(this.editForm);
    if (phim.id !== null) {
      this.subscribeToSaveResponse(this.phimService.update(phim));
    } else {
      this.subscribeToSaveResponse(this.phimService.create(phim));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPhim>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: err => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(err?: any): void {
    this.saveErrorMessage = err?.error?.detail || err?.error?.title || 'Không thể lưu phim. Vui lòng kiểm tra lại dữ liệu nhập.';
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(phim: IPhim): void {
    this.phim = phim;
    this.editForm = this.phimFormService.createPhimFormGroup(phim);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    if (file.size > this.maxPosterFileSizeBytes) {
      this.saveErrorMessage = 'Ảnh poster quá lớn. Vui lòng chọn ảnh dưới 5MB.';
      input.value = '';
      return;
    }

    this.saveErrorMessage = '';
    this.compressImage(file)
      .then(posterDataUrl => {
        this.editForm.patchValue({ poster: posterDataUrl });
        this.editForm.get('poster')?.markAsDirty();
      })
      .catch(() => {
        this.saveErrorMessage = 'Không thể xử lý ảnh poster. Vui lòng thử ảnh khác hoặc dán URL trực tiếp.';
      });
  }

  private compressImage(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const image = new Image();
        image.onload = () => {
          const maxWidth = 900;
          const maxHeight = 1350;
          let { width, height } = image;

          if (width > maxWidth || height > maxHeight) {
            const scale = Math.min(maxWidth / width, maxHeight / height);
            width = Math.round(width * scale);
            height = Math.round(height * scale);
          }

          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;

          const context = canvas.getContext('2d');
          if (!context) {
            reject(new Error('Canvas context unavailable'));
            return;
          }

          context.drawImage(image, 0, 0, width, height);
          resolve(canvas.toDataURL('image/jpeg', 0.82));
        };
        image.onerror = () => reject(new Error('Image load failed'));
        image.src = typeof reader.result === 'string' ? reader.result : '';
      };
      reader.onerror = () => reject(new Error('File read failed'));
      reader.readAsDataURL(file);
    });
  }
}
