import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IGhe, NewGhe } from '../ghe.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IGhe for edit and NewGheFormGroupInput for create.
 */
type GheFormGroupInput = IGhe | PartialWithRequiredKeyOf<NewGhe>;

type GheFormDefaults = Pick<NewGhe, 'id'>;

type GheFormGroupContent = {
  id: FormControl<IGhe['id'] | NewGhe['id']>;
  maGhe: FormControl<IGhe['maGhe']>;
  hang: FormControl<IGhe['hang']>;
  cot: FormControl<IGhe['cot']>;
  loaiGhe: FormControl<IGhe['loaiGhe']>;
  trangThai: FormControl<IGhe['trangThai']>;
  phongChieu: FormControl<IGhe['phongChieu']>;
};

export type GheFormGroup = FormGroup<GheFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class GheFormService {
  createGheFormGroup(ghe: GheFormGroupInput = { id: null }): GheFormGroup {
    const gheRawValue = {
      ...this.getFormDefaults(),
      ...ghe,
    };
    return new FormGroup<GheFormGroupContent>({
      id: new FormControl(
        { value: gheRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      maGhe: new FormControl(gheRawValue.maGhe, {
        validators: [Validators.required],
      }),
      hang: new FormControl(gheRawValue.hang),
      cot: new FormControl(gheRawValue.cot),
      loaiGhe: new FormControl(gheRawValue.loaiGhe),
      trangThai: new FormControl(gheRawValue.trangThai),
      phongChieu: new FormControl(gheRawValue.phongChieu),
    });
  }

  getGhe(form: GheFormGroup): IGhe | NewGhe {
    return form.getRawValue() as IGhe | NewGhe;
  }

  resetForm(form: GheFormGroup, ghe: GheFormGroupInput): void {
    const gheRawValue = { ...this.getFormDefaults(), ...ghe };
    form.reset(
      {
        ...gheRawValue,
        id: { value: gheRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): GheFormDefaults {
    return {
      id: null,
    };
  }
}
