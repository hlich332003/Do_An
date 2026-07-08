import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IVe, NewVe } from '../ve.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVe for edit and NewVeFormGroupInput for create.
 */
type VeFormGroupInput = IVe | PartialWithRequiredKeyOf<NewVe>;

type VeFormDefaults = Pick<NewVe, 'id'>;

type VeFormGroupContent = {
  id: FormControl<IVe['id'] | NewVe['id']>;
  maVe: FormControl<IVe['maVe']>;
  giaVe: FormControl<IVe['giaVe']>;
  trangThai: FormControl<IVe['trangThai']>;
  hoaDon: FormControl<IVe['hoaDon']>;
  suatChieu: FormControl<IVe['suatChieu']>;
};

export type VeFormGroup = FormGroup<VeFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VeFormService {
  createVeFormGroup(ve: VeFormGroupInput = { id: null }): VeFormGroup {
    const veRawValue = {
      ...this.getFormDefaults(),
      ...ve,
    };
    return new FormGroup<VeFormGroupContent>({
      id: new FormControl(
        { value: veRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      maVe: new FormControl(veRawValue.maVe, {
        validators: [Validators.required],
      }),
      giaVe: new FormControl(veRawValue.giaVe),
      trangThai: new FormControl(veRawValue.trangThai),
      hoaDon: new FormControl(veRawValue.hoaDon),
      suatChieu: new FormControl(veRawValue.suatChieu),
    });
  }

  getVe(form: VeFormGroup): IVe | NewVe {
    return form.getRawValue() as IVe | NewVe;
  }

  resetForm(form: VeFormGroup, ve: VeFormGroupInput): void {
    const veRawValue = { ...this.getFormDefaults(), ...ve };
    form.reset(
      {
        ...veRawValue,
        id: { value: veRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): VeFormDefaults {
    return {
      id: null,
    };
  }
}
