import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IHoaDon, NewHoaDon } from '../hoa-don.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IHoaDon for edit and NewHoaDonFormGroupInput for create.
 */
type HoaDonFormGroupInput = IHoaDon | PartialWithRequiredKeyOf<NewHoaDon>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IHoaDon | NewHoaDon> = Omit<T, 'ngayTao' | 'ngayThanhToan'> & {
  ngayTao?: string | null;
  ngayThanhToan?: string | null;
};

type HoaDonFormRawValue = FormValueOf<IHoaDon>;

type NewHoaDonFormRawValue = FormValueOf<NewHoaDon>;

type HoaDonFormDefaults = Pick<NewHoaDon, 'id' | 'ngayTao' | 'ngayThanhToan'>;

type HoaDonFormGroupContent = {
  id: FormControl<HoaDonFormRawValue['id'] | NewHoaDon['id']>;
  tongTien: FormControl<HoaDonFormRawValue['tongTien']>;
  maGiamGia: FormControl<HoaDonFormRawValue['maGiamGia']>;
  soTienGiam: FormControl<HoaDonFormRawValue['soTienGiam']>;
  phuongThucThanhToan: FormControl<HoaDonFormRawValue['phuongThucThanhToan']>;
  maGiaoDich: FormControl<HoaDonFormRawValue['maGiaoDich']>;
  trangThai: FormControl<HoaDonFormRawValue['trangThai']>;
  ngayTao: FormControl<HoaDonFormRawValue['ngayTao']>;
  ngayThanhToan: FormControl<HoaDonFormRawValue['ngayThanhToan']>;
  nguoiDung: FormControl<HoaDonFormRawValue['nguoiDung']>;
};

export type HoaDonFormGroup = FormGroup<HoaDonFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class HoaDonFormService {
  createHoaDonFormGroup(hoaDon: HoaDonFormGroupInput = { id: null }): HoaDonFormGroup {
    const hoaDonRawValue = this.convertHoaDonToHoaDonRawValue({
      ...this.getFormDefaults(),
      ...hoaDon,
    });
    return new FormGroup<HoaDonFormGroupContent>({
      id: new FormControl(
        { value: hoaDonRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      tongTien: new FormControl(hoaDonRawValue.tongTien, {
        validators: [Validators.required],
      }),
      maGiamGia: new FormControl(hoaDonRawValue.maGiamGia),
      soTienGiam: new FormControl(hoaDonRawValue.soTienGiam),
      phuongThucThanhToan: new FormControl(hoaDonRawValue.phuongThucThanhToan),
      maGiaoDich: new FormControl(hoaDonRawValue.maGiaoDich),
      trangThai: new FormControl(hoaDonRawValue.trangThai),
      ngayTao: new FormControl(hoaDonRawValue.ngayTao),
      ngayThanhToan: new FormControl(hoaDonRawValue.ngayThanhToan),
      nguoiDung: new FormControl(hoaDonRawValue.nguoiDung),
    });
  }

  getHoaDon(form: HoaDonFormGroup): IHoaDon | NewHoaDon {
    return this.convertHoaDonRawValueToHoaDon(form.getRawValue() as HoaDonFormRawValue | NewHoaDonFormRawValue);
  }

  resetForm(form: HoaDonFormGroup, hoaDon: HoaDonFormGroupInput): void {
    const hoaDonRawValue = this.convertHoaDonToHoaDonRawValue({ ...this.getFormDefaults(), ...hoaDon });
    form.reset(
      {
        ...hoaDonRawValue,
        id: { value: hoaDonRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): HoaDonFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      ngayTao: currentTime,
      ngayThanhToan: currentTime,
    };
  }

  private convertHoaDonRawValueToHoaDon(rawHoaDon: HoaDonFormRawValue | NewHoaDonFormRawValue): IHoaDon | NewHoaDon {
    return {
      ...rawHoaDon,
      ngayTao: dayjs(rawHoaDon.ngayTao, DATE_TIME_FORMAT),
      ngayThanhToan: dayjs(rawHoaDon.ngayThanhToan, DATE_TIME_FORMAT),
    };
  }

  private convertHoaDonToHoaDonRawValue(
    hoaDon: IHoaDon | (Partial<NewHoaDon> & HoaDonFormDefaults),
  ): HoaDonFormRawValue | PartialWithRequiredKeyOf<NewHoaDonFormRawValue> {
    return {
      ...hoaDon,
      ngayTao: hoaDon.ngayTao ? hoaDon.ngayTao.format(DATE_TIME_FORMAT) : undefined,
      ngayThanhToan: hoaDon.ngayThanhToan ? hoaDon.ngayThanhToan.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
