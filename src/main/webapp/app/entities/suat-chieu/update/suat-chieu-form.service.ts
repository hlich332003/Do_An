import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ISuatChieu, NewSuatChieu } from '../suat-chieu.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISuatChieu for edit and NewSuatChieuFormGroupInput for create.
 */
type SuatChieuFormGroupInput = ISuatChieu | PartialWithRequiredKeyOf<NewSuatChieu>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ISuatChieu | NewSuatChieu> = Omit<T, 'thoiGianBatDau' | 'thoiGianKetThuc'> & {
  thoiGianBatDau?: string | null;
  thoiGianKetThuc?: string | null;
};

type SuatChieuFormRawValue = FormValueOf<ISuatChieu>;

type NewSuatChieuFormRawValue = FormValueOf<NewSuatChieu>;

type SuatChieuFormDefaults = Pick<NewSuatChieu, 'id' | 'thoiGianBatDau' | 'thoiGianKetThuc' | 'giaThuong' | 'giaVip'>;

type SuatChieuFormGroupContent = {
  id: FormControl<SuatChieuFormRawValue['id'] | NewSuatChieu['id']>;
  thoiGianBatDau: FormControl<SuatChieuFormRawValue['thoiGianBatDau']>;
  thoiGianKetThuc: FormControl<SuatChieuFormRawValue['thoiGianKetThuc']>;
  giaThuong: FormControl<SuatChieuFormRawValue['giaThuong']>;
  giaVip: FormControl<SuatChieuFormRawValue['giaVip']>;
  phim: FormControl<SuatChieuFormRawValue['phim']>;
  phongChieu: FormControl<SuatChieuFormRawValue['phongChieu']>;
};

export type SuatChieuFormGroup = FormGroup<SuatChieuFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SuatChieuFormService {
  createSuatChieuFormGroup(suatChieu: SuatChieuFormGroupInput = { id: null }): SuatChieuFormGroup {
    const suatChieuRawValue = this.convertSuatChieuToSuatChieuRawValue({
      ...this.getFormDefaults(),
      ...suatChieu,
    });
    return new FormGroup<SuatChieuFormGroupContent>({
      id: new FormControl(
        { value: suatChieuRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      thoiGianBatDau: new FormControl(suatChieuRawValue.thoiGianBatDau, {
        validators: [Validators.required],
      }),
      thoiGianKetThuc: new FormControl(suatChieuRawValue.thoiGianKetThuc, {
        validators: [Validators.required],
      }),
      giaThuong: new FormControl(suatChieuRawValue.giaThuong, {
        validators: [Validators.required, Validators.min(0)],
      }),
      giaVip: new FormControl(suatChieuRawValue.giaVip, {
        validators: [Validators.required, Validators.min(0)],
      }),
      phim: new FormControl(suatChieuRawValue.phim, {
        validators: [Validators.required],
      }),
      phongChieu: new FormControl(suatChieuRawValue.phongChieu, {
        validators: [Validators.required],
      }),
    });
  }

  getSuatChieu(form: SuatChieuFormGroup): ISuatChieu | NewSuatChieu {
    return this.convertSuatChieuRawValueToSuatChieu(form.getRawValue() as SuatChieuFormRawValue | NewSuatChieuFormRawValue);
  }

  resetForm(form: SuatChieuFormGroup, suatChieu: SuatChieuFormGroupInput): void {
    const suatChieuRawValue = this.convertSuatChieuToSuatChieuRawValue({ ...this.getFormDefaults(), ...suatChieu });
    form.reset(
      {
        ...suatChieuRawValue,
        id: { value: suatChieuRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SuatChieuFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      thoiGianBatDau: currentTime,
      thoiGianKetThuc: currentTime,
      giaThuong: 50000,
      giaVip: 120000,
    };
  }

  private convertSuatChieuRawValueToSuatChieu(rawSuatChieu: SuatChieuFormRawValue | NewSuatChieuFormRawValue): ISuatChieu | NewSuatChieu {
    return {
      ...rawSuatChieu,
      thoiGianBatDau: dayjs(rawSuatChieu.thoiGianBatDau, DATE_TIME_FORMAT),
      thoiGianKetThuc: dayjs(rawSuatChieu.thoiGianKetThuc, DATE_TIME_FORMAT),
    };
  }

  private convertSuatChieuToSuatChieuRawValue(
    suatChieu: ISuatChieu | (Partial<NewSuatChieu> & SuatChieuFormDefaults),
  ): SuatChieuFormRawValue | PartialWithRequiredKeyOf<NewSuatChieuFormRawValue> {
    return {
      ...suatChieu,
      thoiGianBatDau: suatChieu.thoiGianBatDau ? suatChieu.thoiGianBatDau.format(DATE_TIME_FORMAT) : undefined,
      thoiGianKetThuc: suatChieu.thoiGianKetThuc ? suatChieu.thoiGianKetThuc.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
