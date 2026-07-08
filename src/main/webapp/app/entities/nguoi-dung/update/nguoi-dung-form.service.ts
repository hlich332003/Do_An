import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { INguoiDung, NewNguoiDung } from '../nguoi-dung.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts INguoiDung for edit and NewNguoiDungFormGroupInput for create.
 */
type NguoiDungFormGroupInput = INguoiDung | PartialWithRequiredKeyOf<NewNguoiDung>;

type NguoiDungFormDefaults = Pick<NewNguoiDung, 'id'>;

type NguoiDungFormGroupContent = {
  id: FormControl<INguoiDung['id'] | NewNguoiDung['id']>;
  hoTen: FormControl<INguoiDung['hoTen']>;
  email: FormControl<INguoiDung['email']>;
  matKhau: FormControl<INguoiDung['matKhau']>;
  soDienThoai: FormControl<INguoiDung['soDienThoai']>;
  diaChi: FormControl<INguoiDung['diaChi']>;
  diemTichLuy: FormControl<INguoiDung['diemTichLuy']>;
  vaiTro: FormControl<INguoiDung['vaiTro']>;
  trangThai: FormControl<INguoiDung['trangThai']>;
};

export type NguoiDungFormGroup = FormGroup<NguoiDungFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class NguoiDungFormService {
  createNguoiDungFormGroup(nguoiDung: NguoiDungFormGroupInput = { id: null }): NguoiDungFormGroup {
    const nguoiDungRawValue = {
      ...this.getFormDefaults(),
      ...nguoiDung,
    };
    return new FormGroup<NguoiDungFormGroupContent>({
      id: new FormControl(
        { value: nguoiDungRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      hoTen: new FormControl(nguoiDungRawValue.hoTen),
      email: new FormControl(nguoiDungRawValue.email, {
        validators: [Validators.required],
      }),
      matKhau: new FormControl(nguoiDungRawValue.matKhau, {
        validators: [Validators.required],
      }),
      soDienThoai: new FormControl(nguoiDungRawValue.soDienThoai),
      diaChi: new FormControl(nguoiDungRawValue.diaChi),
      diemTichLuy: new FormControl(nguoiDungRawValue.diemTichLuy),
      vaiTro: new FormControl(nguoiDungRawValue.vaiTro),
      trangThai: new FormControl(nguoiDungRawValue.trangThai),
    });
  }

  getNguoiDung(form: NguoiDungFormGroup): INguoiDung | NewNguoiDung {
    return form.getRawValue() as INguoiDung | NewNguoiDung;
  }

  resetForm(form: NguoiDungFormGroup, nguoiDung: NguoiDungFormGroupInput): void {
    const nguoiDungRawValue = { ...this.getFormDefaults(), ...nguoiDung };
    form.reset(
      {
        ...nguoiDungRawValue,
        id: { value: nguoiDungRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): NguoiDungFormDefaults {
    return {
      id: null,
    };
  }
}
