import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../ghe.test-samples';

import { GheFormService } from './ghe-form.service';

describe('Ghe Form Service', () => {
  let service: GheFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GheFormService);
  });

  describe('Service methods', () => {
    describe('createGheFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createGheFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maGhe: expect.any(Object),
            hang: expect.any(Object),
            cot: expect.any(Object),
            loaiGhe: expect.any(Object),
            trangThai: expect.any(Object),
            ve: expect.any(Object),
            phongChieu: expect.any(Object),
          }),
        );
      });

      it('passing IGhe should create a new form with FormGroup', () => {
        const formGroup = service.createGheFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maGhe: expect.any(Object),
            hang: expect.any(Object),
            cot: expect.any(Object),
            loaiGhe: expect.any(Object),
            trangThai: expect.any(Object),
            ve: expect.any(Object),
            phongChieu: expect.any(Object),
          }),
        );
      });
    });

    describe('getGhe', () => {
      it('should return NewGhe for default Ghe initial value', () => {
        const formGroup = service.createGheFormGroup(sampleWithNewData);

        const ghe = service.getGhe(formGroup) as any;

        expect(ghe).toMatchObject(sampleWithNewData);
      });

      it('should return NewGhe for empty Ghe initial value', () => {
        const formGroup = service.createGheFormGroup();

        const ghe = service.getGhe(formGroup) as any;

        expect(ghe).toMatchObject({});
      });

      it('should return IGhe', () => {
        const formGroup = service.createGheFormGroup(sampleWithRequiredData);

        const ghe = service.getGhe(formGroup) as any;

        expect(ghe).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IGhe should not enable id FormControl', () => {
        const formGroup = service.createGheFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewGhe should disable id FormControl', () => {
        const formGroup = service.createGheFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
