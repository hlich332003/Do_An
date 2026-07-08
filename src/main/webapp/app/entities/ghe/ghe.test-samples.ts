import { IGhe, NewGhe } from './ghe.model';

export const sampleWithRequiredData: IGhe = {
  id: 1843,
  maGhe: 'afore duh charter',
};

export const sampleWithPartialData: IGhe = {
  id: 27657,
  maGhe: 'punctually glow save',
  loaiGhe: 'where',
};

export const sampleWithFullData: IGhe = {
  id: 455,
  maGhe: 'brr',
  hang: 'quicker even deed',
  cot: 15880,
  loaiGhe: 'instantly elderly',
  trangThai: 'wetly',
};

export const sampleWithNewData: NewGhe = {
  maGhe: 'indeed huge',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
