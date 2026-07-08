import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IPhongChieu, NewPhongChieu } from '../phong-chieu.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPhongChieu for edit and NewPhongChieuFormGroupInput for create.
 */
type PhongChieuFormGroupInput = IPhongChieu | PartialWithRequiredKeyOf<NewPhongChieu>;

type PhongChieuFormDefaults = Pick<NewPhongChieu, 'id'>;

type PhongChieuFormGroupContent = {
  id: FormControl<IPhongChieu['id'] | NewPhongChieu['id']>;
  tenPhong: FormControl<IPhongChieu['tenPhong']>;
  soLuongGhe: FormControl<IPhongChieu['soLuongGhe']>;
  trangThai: FormControl<IPhongChieu['trangThai']>;
};

export type PhongChieuFormGroup = FormGroup<PhongChieuFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PhongChieuFormService {
  createPhongChieuFormGroup(phongChieu: PhongChieuFormGroupInput = { id: null }): PhongChieuFormGroup {
    const phongChieuRawValue = {
      ...this.getFormDefaults(),
      ...phongChieu,
    };
    return new FormGroup<PhongChieuFormGroupContent>({
      id: new FormControl(
        { value: phongChieuRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      tenPhong: new FormControl(phongChieuRawValue.tenPhong, {
        validators: [Validators.required],
      }),
      soLuongGhe: new FormControl(phongChieuRawValue.soLuongGhe),
      trangThai: new FormControl(phongChieuRawValue.trangThai),
    });
  }

  getPhongChieu(form: PhongChieuFormGroup): IPhongChieu | NewPhongChieu {
    return form.getRawValue() as IPhongChieu | NewPhongChieu;
  }

  resetForm(form: PhongChieuFormGroup, phongChieu: PhongChieuFormGroupInput): void {
    const phongChieuRawValue = { ...this.getFormDefaults(), ...phongChieu };
    form.reset(
      {
        ...phongChieuRawValue,
        id: { value: phongChieuRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PhongChieuFormDefaults {
    return {
      id: null,
    };
  }
}
