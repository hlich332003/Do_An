import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../chi-tiet-fb.test-samples';

import { ChiTietFBFormService } from './chi-tiet-fb-form.service';

describe('ChiTietFB Form Service', () => {
  let service: ChiTietFBFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChiTietFBFormService);
  });

  describe('Service methods', () => {
    describe('createChiTietFBFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createChiTietFBFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            soLuong: expect.any(Object),
            giaBan: expect.any(Object),
            dichVuFB: expect.any(Object),
            hoaDon: expect.any(Object),
          }),
        );
      });

      it('passing IChiTietFB should create a new form with FormGroup', () => {
        const formGroup = service.createChiTietFBFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            soLuong: expect.any(Object),
            giaBan: expect.any(Object),
            dichVuFB: expect.any(Object),
            hoaDon: expect.any(Object),
          }),
        );
      });
    });

    describe('getChiTietFB', () => {
      it('should return NewChiTietFB for default ChiTietFB initial value', () => {
        const formGroup = service.createChiTietFBFormGroup(sampleWithNewData);

        const chiTietFB = service.getChiTietFB(formGroup) as any;

        expect(chiTietFB).toMatchObject(sampleWithNewData);
      });

      it('should return NewChiTietFB for empty ChiTietFB initial value', () => {
        const formGroup = service.createChiTietFBFormGroup();

        const chiTietFB = service.getChiTietFB(formGroup) as any;

        expect(chiTietFB).toMatchObject({});
      });

      it('should return IChiTietFB', () => {
        const formGroup = service.createChiTietFBFormGroup(sampleWithRequiredData);

        const chiTietFB = service.getChiTietFB(formGroup) as any;

        expect(chiTietFB).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IChiTietFB should not enable id FormControl', () => {
        const formGroup = service.createChiTietFBFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewChiTietFB should disable id FormControl', () => {
        const formGroup = service.createChiTietFBFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
