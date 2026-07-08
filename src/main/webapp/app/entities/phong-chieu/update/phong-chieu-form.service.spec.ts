import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../phong-chieu.test-samples';

import { PhongChieuFormService } from './phong-chieu-form.service';

describe('PhongChieu Form Service', () => {
  let service: PhongChieuFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PhongChieuFormService);
  });

  describe('Service methods', () => {
    describe('createPhongChieuFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPhongChieuFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenPhong: expect.any(Object),
            soLuongGhe: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });

      it('passing IPhongChieu should create a new form with FormGroup', () => {
        const formGroup = service.createPhongChieuFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenPhong: expect.any(Object),
            soLuongGhe: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });
    });

    describe('getPhongChieu', () => {
      it('should return NewPhongChieu for default PhongChieu initial value', () => {
        const formGroup = service.createPhongChieuFormGroup(sampleWithNewData);

        const phongChieu = service.getPhongChieu(formGroup) as any;

        expect(phongChieu).toMatchObject(sampleWithNewData);
      });

      it('should return NewPhongChieu for empty PhongChieu initial value', () => {
        const formGroup = service.createPhongChieuFormGroup();

        const phongChieu = service.getPhongChieu(formGroup) as any;

        expect(phongChieu).toMatchObject({});
      });

      it('should return IPhongChieu', () => {
        const formGroup = service.createPhongChieuFormGroup(sampleWithRequiredData);

        const phongChieu = service.getPhongChieu(formGroup) as any;

        expect(phongChieu).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPhongChieu should not enable id FormControl', () => {
        const formGroup = service.createPhongChieuFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPhongChieu should disable id FormControl', () => {
        const formGroup = service.createPhongChieuFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
