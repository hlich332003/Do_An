import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../hoa-don.test-samples';

import { HoaDonFormService } from './hoa-don-form.service';

describe('HoaDon Form Service', () => {
  let service: HoaDonFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HoaDonFormService);
  });

  describe('Service methods', () => {
    describe('createHoaDonFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createHoaDonFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tongTien: expect.any(Object),
            maGiamGia: expect.any(Object),
            soTienGiam: expect.any(Object),
            phuongThucThanhToan: expect.any(Object),
            maGiaoDich: expect.any(Object),
            trangThai: expect.any(Object),
            ngayTao: expect.any(Object),
            ngayThanhToan: expect.any(Object),
            nguoiDung: expect.any(Object),
          }),
        );
      });

      it('passing IHoaDon should create a new form with FormGroup', () => {
        const formGroup = service.createHoaDonFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tongTien: expect.any(Object),
            maGiamGia: expect.any(Object),
            soTienGiam: expect.any(Object),
            phuongThucThanhToan: expect.any(Object),
            maGiaoDich: expect.any(Object),
            trangThai: expect.any(Object),
            ngayTao: expect.any(Object),
            ngayThanhToan: expect.any(Object),
            nguoiDung: expect.any(Object),
          }),
        );
      });
    });

    describe('getHoaDon', () => {
      it('should return NewHoaDon for default HoaDon initial value', () => {
        const formGroup = service.createHoaDonFormGroup(sampleWithNewData);

        const hoaDon = service.getHoaDon(formGroup) as any;

        expect(hoaDon).toMatchObject(sampleWithNewData);
      });

      it('should return NewHoaDon for empty HoaDon initial value', () => {
        const formGroup = service.createHoaDonFormGroup();

        const hoaDon = service.getHoaDon(formGroup) as any;

        expect(hoaDon).toMatchObject({});
      });

      it('should return IHoaDon', () => {
        const formGroup = service.createHoaDonFormGroup(sampleWithRequiredData);

        const hoaDon = service.getHoaDon(formGroup) as any;

        expect(hoaDon).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IHoaDon should not enable id FormControl', () => {
        const formGroup = service.createHoaDonFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewHoaDon should disable id FormControl', () => {
        const formGroup = service.createHoaDonFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
