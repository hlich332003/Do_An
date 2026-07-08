import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../nguoi-dung.test-samples';

import { NguoiDungFormService } from './nguoi-dung-form.service';

describe('NguoiDung Form Service', () => {
  let service: NguoiDungFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NguoiDungFormService);
  });

  describe('Service methods', () => {
    describe('createNguoiDungFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createNguoiDungFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            hoTen: expect.any(Object),
            email: expect.any(Object),
            matKhau: expect.any(Object),
            soDienThoai: expect.any(Object),
            diaChi: expect.any(Object),
            diemTichLuy: expect.any(Object),
            vaiTro: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });

      it('passing INguoiDung should create a new form with FormGroup', () => {
        const formGroup = service.createNguoiDungFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            hoTen: expect.any(Object),
            email: expect.any(Object),
            matKhau: expect.any(Object),
            soDienThoai: expect.any(Object),
            diaChi: expect.any(Object),
            diemTichLuy: expect.any(Object),
            vaiTro: expect.any(Object),
            trangThai: expect.any(Object),
          }),
        );
      });
    });

    describe('getNguoiDung', () => {
      it('should return NewNguoiDung for default NguoiDung initial value', () => {
        const formGroup = service.createNguoiDungFormGroup(sampleWithNewData);

        const nguoiDung = service.getNguoiDung(formGroup) as any;

        expect(nguoiDung).toMatchObject(sampleWithNewData);
      });

      it('should return NewNguoiDung for empty NguoiDung initial value', () => {
        const formGroup = service.createNguoiDungFormGroup();

        const nguoiDung = service.getNguoiDung(formGroup) as any;

        expect(nguoiDung).toMatchObject({});
      });

      it('should return INguoiDung', () => {
        const formGroup = service.createNguoiDungFormGroup(sampleWithRequiredData);

        const nguoiDung = service.getNguoiDung(formGroup) as any;

        expect(nguoiDung).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing INguoiDung should not enable id FormControl', () => {
        const formGroup = service.createNguoiDungFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewNguoiDung should disable id FormControl', () => {
        const formGroup = service.createNguoiDungFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
