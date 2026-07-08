import { IPhongChieu, NewPhongChieu } from './phong-chieu.model';

export const sampleWithRequiredData: IPhongChieu = {
  id: 9958,
  tenPhong: 'thoroughly',
};

export const sampleWithPartialData: IPhongChieu = {
  id: 15108,
  tenPhong: 'so reopen',
};

export const sampleWithFullData: IPhongChieu = {
  id: 13790,
  tenPhong: 'whose fooey unfinished',
  soLuongGhe: 1311,
  trangThai: 'blond than',
};

export const sampleWithNewData: NewPhongChieu = {
  tenPhong: 'fundraising',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
