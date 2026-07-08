import { Injectable } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import dayjs from 'dayjs/esm';

import { IPhim, NewPhim } from '../phim.model';

type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

type PhimFormGroupInput = IPhim | PartialWithRequiredKeyOf<NewPhim>;

type PhimFormDefaults = Pick<NewPhim, 'id'>;

type PhimFormGroupContent = {
  id: FormControl<IPhim['id'] | NewPhim['id']>;
  tenPhim: FormControl<IPhim['tenPhim']>;
  moTa: FormControl<IPhim['moTa']>;
  thoiLuong: FormControl<IPhim['thoiLuong']>;
  daoDien: FormControl<IPhim['daoDien']>;
  dienVien: FormControl<IPhim['dienVien']>;
  theLoai: FormControl<IPhim['theLoai']>;
  ngayKhoiChieu: FormControl<IPhim['ngayKhoiChieu']>;
  poster: FormControl<IPhim['poster']>;
  trailer: FormControl<IPhim['trailer']>;
  trangThai: FormControl<IPhim['trangThai']>;
  createdAt: FormControl<IPhim['createdAt']>;
};

export type PhimFormGroup = FormGroup<PhimFormGroupContent>;

const notBeforeTodayValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (!control.value) {
    return null;
  }

  const normalizedDate = normalizeToDayjs(control.value);
  if (!normalizedDate?.isValid()) {
    return null;
  }

  return normalizedDate.startOf('day').isBefore(dayjs().startOf('day')) ? { beforeToday: true } : null;
};

function normalizeToDayjs(value: unknown): dayjs.Dayjs | null {
  if (!value) {
    return null;
  }

  if (dayjs.isDayjs(value)) {
    return value;
  }

  if (typeof value === 'string' || value instanceof Date) {
    const parsed = dayjs(value);
    return parsed.isValid() ? parsed : null;
  }

  if (typeof value === 'object' && value !== null && 'year' in value && 'month' in value && 'day' in value) {
    const dateValue = value as { year: number; month: number; day: number };
    const parsed = dayjs(`${dateValue.year}-${String(dateValue.month).padStart(2, '0')}-${String(dateValue.day).padStart(2, '0')}`);
    return parsed.isValid() ? parsed : null;
  }

  return null;
}

@Injectable({ providedIn: 'root' })
export class PhimFormService {
  createPhimFormGroup(phim: PhimFormGroupInput = { id: null }): PhimFormGroup {
    const phimRawValue = {
      ...this.getFormDefaults(),
      ...phim,
    };
    const isNewPhim = phimRawValue.id === null;
    return new FormGroup<PhimFormGroupContent>({
      id: new FormControl(
        { value: phimRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      tenPhim: new FormControl(phimRawValue.tenPhim, {
        validators: isNewPhim ? [Validators.required] : [],
      }),
      moTa: new FormControl(phimRawValue.moTa),
      thoiLuong: new FormControl(phimRawValue.thoiLuong),
      daoDien: new FormControl(phimRawValue.daoDien),
      dienVien: new FormControl(phimRawValue.dienVien),
      theLoai: new FormControl(phimRawValue.theLoai),
      ngayKhoiChieu: new FormControl(phimRawValue.ngayKhoiChieu, {
        validators: isNewPhim ? [notBeforeTodayValidator] : [],
      }),
      poster: new FormControl(phimRawValue.poster),
      trailer: new FormControl(phimRawValue.trailer, {
        validators: [Validators.maxLength(4000)],
      }),
      trangThai: new FormControl(phimRawValue.trangThai ?? 'ACTIVE'),
      createdAt: new FormControl(phimRawValue.createdAt ? phimRawValue.createdAt : null),
    });
  }

  getPhim(form: PhimFormGroup): IPhim | NewPhim {
    return form.getRawValue() as IPhim | NewPhim;
  }

  resetForm(form: PhimFormGroup, phim: PhimFormGroupInput): void {
    const phimRawValue = { ...this.getFormDefaults(), ...phim };
    form.reset({
      ...phimRawValue,
      id: { value: phimRawValue.id, disabled: true },
    } as any);
  }

  private getFormDefaults(): PhimFormDefaults {
    return {
      id: null,
    };
  }
}
