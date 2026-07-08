import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IDichVuFB, NewDichVuFB } from '../dich-vu-fb.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IDichVuFB for edit and NewDichVuFBFormGroupInput for create.
 */
type DichVuFBFormGroupInput = IDichVuFB | PartialWithRequiredKeyOf<NewDichVuFB>;

type DichVuFBFormDefaults = Pick<NewDichVuFB, 'id'>;

type DichVuFBFormGroupContent = {
  id: FormControl<IDichVuFB['id'] | NewDichVuFB['id']>;
  tenCombo: FormControl<IDichVuFB['tenCombo']>;
  moTa: FormControl<IDichVuFB['moTa']>;
  gia: FormControl<IDichVuFB['gia']>;
  hinhAnh: FormControl<IDichVuFB['hinhAnh']>;
  trangThai: FormControl<IDichVuFB['trangThai']>;
};

export type DichVuFBFormGroup = FormGroup<DichVuFBFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class DichVuFBFormService {
  createDichVuFBFormGroup(dichVuFB: DichVuFBFormGroupInput = { id: null }): DichVuFBFormGroup {
    const dichVuFBRawValue = {
      ...this.getFormDefaults(),
      ...dichVuFB,
    };
    return new FormGroup<DichVuFBFormGroupContent>({
      id: new FormControl(
        { value: dichVuFBRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      tenCombo: new FormControl(dichVuFBRawValue.tenCombo, {
        validators: [Validators.required],
      }),
      moTa: new FormControl(dichVuFBRawValue.moTa),
      gia: new FormControl(dichVuFBRawValue.gia, {
        validators: [Validators.required],
      }),
      hinhAnh: new FormControl(dichVuFBRawValue.hinhAnh),
      trangThai: new FormControl(dichVuFBRawValue.trangThai),
    });
  }

  getDichVuFB(form: DichVuFBFormGroup): IDichVuFB | NewDichVuFB {
    return form.getRawValue() as IDichVuFB | NewDichVuFB;
  }

  resetForm(form: DichVuFBFormGroup, dichVuFB: DichVuFBFormGroupInput): void {
    const dichVuFBRawValue = { ...this.getFormDefaults(), ...dichVuFB };
    form.reset(
      {
        ...dichVuFBRawValue,
        id: { value: dichVuFBRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): DichVuFBFormDefaults {
    return {
      id: null,
    };
  }
}
