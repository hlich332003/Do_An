import { IDichVuFB, NewDichVuFB } from './dich-vu-fb.model';

export const sampleWithRequiredData: IDichVuFB = {
  id: 10205,
  tenCombo: 'neatly properly',
  gia: 21435.23,
};

export const sampleWithPartialData: IDichVuFB = {
  id: 12588,
  tenCombo: 'however publication creator',
  gia: 29792.52,
  hinhAnh: 'wallaby misappropriate instead',
  trangThai: '1',
};

export const sampleWithFullData: IDichVuFB = {
  id: 31460,
  tenCombo: 'glow whereas punctuation',
  moTa: 'broadly',
  gia: 21917.95,
  hinhAnh: 'wherever',
  trangThai: '0',
};

export const sampleWithNewData: NewDichVuFB = {
  tenCombo: 'obnoxiously',
  gia: 28445.8,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
