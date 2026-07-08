import { IChiTietFB, NewChiTietFB } from './chi-tiet-fb.model';

export const sampleWithRequiredData: IChiTietFB = {
  id: 13777,
  soLuong: 15628,
};

export const sampleWithPartialData: IChiTietFB = {
  id: 15518,
  soLuong: 28763,
  giaBan: 21439.03,
};

export const sampleWithFullData: IChiTietFB = {
  id: 21701,
  soLuong: 23029,
  giaBan: 31832.4,
};

export const sampleWithNewData: NewChiTietFB = {
  soLuong: 24391,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
