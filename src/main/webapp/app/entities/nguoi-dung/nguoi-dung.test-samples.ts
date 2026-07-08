import { INguoiDung, NewNguoiDung } from './nguoi-dung.model';

export const sampleWithRequiredData: INguoiDung = {
  id: 2511,
  email: 'Narciso78@yahoo.com',
  matKhau: 'enormously fluffy',
};

export const sampleWithPartialData: INguoiDung = {
  id: 36,
  hoTen: 'meaningfully',
  email: 'Danika_Muller@hotmail.com',
  matKhau: 'unlucky',
  trangThai: 'shadowy nougat deflect',
};

export const sampleWithFullData: INguoiDung = {
  id: 8281,
  hoTen: 'and terribly pepper',
  email: 'Shea84@hotmail.com',
  matKhau: 'dutiful',
  soDienThoai: 'barring distorted',
  diaChi: 'gadzooks below',
  diemTichLuy: 9670,
  vaiTro: 'surprisingly perfection',
  trangThai: 'overproduce',
};

export const sampleWithNewData: NewNguoiDung = {
  email: 'Ines48@hotmail.com',
  matKhau: 'nor enthusiastically',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
