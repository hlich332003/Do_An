import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../suat-chieu.test-samples';

import { SuatChieuFormService } from './suat-chieu-form.service';

describe('SuatChieu Form Service', () => {
  let service: SuatChieuFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SuatChieuFormService);
  });

  describe('Service methods', () => {
    describe('createSuatChieuFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createSuatChieuFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            thoiGianBatDau: expect.any(Object),
            thoiGianKetThuc: expect.any(Object),
            phim: expect.any(Object),
            phongChieu: expect.any(Object),
          }),
        );
      });

      it('passing ISuatChieu should create a new form with FormGroup', () => {
        const formGroup = service.createSuatChieuFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            thoiGianBatDau: expect.any(Object),
            thoiGianKetThuc: expect.any(Object),
            phim: expect.any(Object),
            phongChieu: expect.any(Object),
          }),
        );
      });
    });

    describe('getSuatChieu', () => {
      it('should return NewSuatChieu for default SuatChieu initial value', () => {
        const formGroup = service.createSuatChieuFormGroup(sampleWithNewData);

        const suatChieu = service.getSuatChieu(formGroup) as any;

        expect(suatChieu).toMatchObject(sampleWithNewData);
      });

      it('should return NewSuatChieu for empty SuatChieu initial value', () => {
        const formGroup = service.createSuatChieuFormGroup();

        const suatChieu = service.getSuatChieu(formGroup) as any;

        expect(suatChieu).toMatchObject({});
      });

      it('should return ISuatChieu', () => {
        const formGroup = service.createSuatChieuFormGroup(sampleWithRequiredData);

        const suatChieu = service.getSuatChieu(formGroup) as any;

        expect(suatChieu).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ISuatChieu should not enable id FormControl', () => {
        const formGroup = service.createSuatChieuFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewSuatChieu should disable id FormControl', () => {
        const formGroup = service.createSuatChieuFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
