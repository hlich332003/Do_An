import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../dich-vu-fb.test-samples';

import { DichVuFBFormService } from './dich-vu-fb-form.service';

describe('DichVuFB Form Service', () => {
  let service: DichVuFBFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DichVuFBFormService);
  });

  describe('Service methods', () => {
    describe('createDichVuFBFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createDichVuFBFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenCombo: expect.any(Object),
            moTa: expect.any(Object),
            gia: expect.any(Object),
            hinhAnh: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });

      it('passing IDichVuFB should create a new form with FormGroup', () => {
        const formGroup = service.createDichVuFBFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenCombo: expect.any(Object),
            moTa: expect.any(Object),
            gia: expect.any(Object),
            hinhAnh: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });
    });

    describe('getDichVuFB', () => {
      it('should return NewDichVuFB for default DichVuFB initial value', () => {
        const formGroup = service.createDichVuFBFormGroup(sampleWithNewData);

        const dichVuFB = service.getDichVuFB(formGroup) as any;

        expect(dichVuFB).toMatchObject(sampleWithNewData);
      });

      it('should return NewDichVuFB for empty DichVuFB initial value', () => {
        const formGroup = service.createDichVuFBFormGroup();

        const dichVuFB = service.getDichVuFB(formGroup) as any;

        expect(dichVuFB).toMatchObject({});
      });

      it('should return IDichVuFB', () => {
        const formGroup = service.createDichVuFBFormGroup(sampleWithRequiredData);

        const dichVuFB = service.getDichVuFB(formGroup) as any;

        expect(dichVuFB).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IDichVuFB should not enable id FormControl', () => {
        const formGroup = service.createDichVuFBFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewDichVuFB should disable id FormControl', () => {
        const formGroup = service.createDichVuFBFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
