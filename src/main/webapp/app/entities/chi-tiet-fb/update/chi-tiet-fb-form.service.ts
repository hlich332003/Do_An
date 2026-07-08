import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IChiTietFB, NewChiTietFB } from '../chi-tiet-fb.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IChiTietFB for edit and NewChiTietFBFormGroupInput for create.
 */
type ChiTietFBFormGroupInput = IChiTietFB | PartialWithRequiredKeyOf<NewChiTietFB>;

type ChiTietFBFormDefaults = Pick<NewChiTietFB, 'id'>;

type ChiTietFBFormGroupContent = {
  id: FormControl<IChiTietFB['id'] | NewChiTietFB['id']>;
  soLuong: FormControl<IChiTietFB['soLuong']>;
  giaBan: FormControl<IChiTietFB['giaBan']>;
  dichVuFB: FormControl<IChiTietFB['dichVuFB']>;
  hoaDon: FormControl<IChiTietFB['hoaDon']>;
};

export type ChiTietFBFormGroup = FormGroup<ChiTietFBFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ChiTietFBFormService {
  createChiTietFBFormGroup(chiTietFB: ChiTietFBFormGroupInput = { id: null }): ChiTietFBFormGroup {
    const chiTietFBRawValue = {
      ...this.getFormDefaults(),
      ...chiTietFB,
    };
    return new FormGroup<ChiTietFBFormGroupContent>({
      id: new FormControl(
        { value: chiTietFBRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      soLuong: new FormControl(chiTietFBRawValue.soLuong, {
        validators: [Validators.required],
      }),
      giaBan: new FormControl(chiTietFBRawValue.giaBan),
      dichVuFB: new FormControl(chiTietFBRawValue.dichVuFB),
      hoaDon: new FormControl(chiTietFBRawValue.hoaDon),
    });
  }

  getChiTietFB(form: ChiTietFBFormGroup): IChiTietFB | NewChiTietFB {
    return form.getRawValue() as IChiTietFB | NewChiTietFB;
  }

  resetForm(form: ChiTietFBFormGroup, chiTietFB: ChiTietFBFormGroupInput): void {
    const chiTietFBRawValue = { ...this.getFormDefaults(), ...chiTietFB };
    form.reset(
      {
        ...chiTietFBRawValue,
        id: { value: chiTietFBRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ChiTietFBFormDefaults {
    return {
      id: null,
    };
  }
}
