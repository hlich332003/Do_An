import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../phim.test-samples';

import { PhimFormService } from './phim-form.service';

describe('Phim Form Service', () => {
  let service: PhimFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PhimFormService);
  });

  describe('Service methods', () => {
    describe('createPhimFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPhimFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenPhim: expect.any(Object),
            moTa: expect.any(Object),
            thoiLuong: expect.any(Object),
            daoDien: expect.any(Object),
            dienVien: expect.any(Object),
            theLoai: expect.any(Object),
            ngayKhoiChieu: expect.any(Object),
            poster: expect.any(Object),
            trailer: expect.any(Object),
          }),
        );
      });

      it('passing IPhim should create a new form with FormGroup', () => {
        const formGroup = service.createPhimFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tenPhim: expect.any(Object),
            moTa: expect.any(Object),
            thoiLuong: expect.any(Object),
            daoDien: expect.any(Object),
            dienVien: expect.any(Object),
            theLoai: expect.any(Object),
            ngayKhoiChieu: expect.any(Object),
            poster: expect.any(Object),
            trailer: expect.any(Object),
          }),
        );
      });
    });

    describe('getPhim', () => {
      it('should return NewPhim for default Phim initial value', () => {
        const formGroup = service.createPhimFormGroup(sampleWithNewData);

        const phim = service.getPhim(formGroup) as any;

        expect(phim).toMatchObject(sampleWithNewData);
      });

      it('should return NewPhim for empty Phim initial value', () => {
        const formGroup = service.createPhimFormGroup();

        const phim = service.getPhim(formGroup) as any;

        expect(phim).toMatchObject({});
      });

      it('should return IPhim', () => {
        const formGroup = service.createPhimFormGroup(sampleWithRequiredData);

        const phim = service.getPhim(formGroup) as any;

        expect(phim).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPhim should not enable id FormControl', () => {
        const formGroup = service.createPhimFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPhim should disable id FormControl', () => {
        const formGroup = service.createPhimFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
