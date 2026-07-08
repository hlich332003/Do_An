import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../ve.test-samples';

import { VeFormService } from './ve-form.service';

describe('Ve Form Service', () => {
  let service: VeFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VeFormService);
  });

  describe('Service methods', () => {
    describe('createVeFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVeFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maVe: expect.any(Object),
            giaVe: expect.any(Object),
            trangThai: expect.any(Object),
            hoaDon: expect.any(Object),
            suatChieu: expect.any(Object),
            chiTietHoaDon: expect.any(Object),
          }),
        );
      });

      it('passing IVe should create a new form with FormGroup', () => {
        const formGroup = service.createVeFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            maVe: expect.any(Object),
            giaVe: expect.any(Object),
            trangThai: expect.any(Object),
            hoaDon: expect.any(Object),
            suatChieu: expect.any(Object),
            chiTietHoaDon: expect.any(Object),
          }),
        );
      });
    });

    describe('getVe', () => {
      it('should return NewVe for default Ve initial value', () => {
        const formGroup = service.createVeFormGroup(sampleWithNewData);

        const ve = service.getVe(formGroup) as any;

        expect(ve).toMatchObject(sampleWithNewData);
      });

      it('should return NewVe for empty Ve initial value', () => {
        const formGroup = service.createVeFormGroup();

        const ve = service.getVe(formGroup) as any;

        expect(ve).toMatchObject({});
      });

      it('should return IVe', () => {
        const formGroup = service.createVeFormGroup(sampleWithRequiredData);

        const ve = service.getVe(formGroup) as any;

        expect(ve).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVe should not enable id FormControl', () => {
        const formGroup = service.createVeFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVe should disable id FormControl', () => {
        const formGroup = service.createVeFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
