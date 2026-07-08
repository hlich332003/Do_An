import dayjs from 'dayjs/esm';

import { IPhim, NewPhim } from './phim.model';

export const sampleWithRequiredData: IPhim = {
  id: 13546,
  tenPhim: 'vacation opera',
};

export const sampleWithPartialData: IPhim = {
  id: 30837,
  tenPhim: 'masticate cuckoo at',
  daoDien: 'excluding',
  dienVien: 'commonly unto roger',
  theLoai: 'after defiantly',
  poster: 'shocked when confirm',
};

export const sampleWithFullData: IPhim = {
  id: 17645,
  tenPhim: 'bony um with',
  moTa: 'excepting indeed fax',
  thoiLuong: 16904,
  daoDien: 'pish farmer brr',
  dienVien: 'drat',
  theLoai: 'infinite',
  ngayKhoiChieu: dayjs('2026-06-10'),
  poster: 'sweetly deny woot',
  trailer: 'major mechanically an',
};

export const sampleWithNewData: NewPhim = {
  tenPhim: 'per hmph',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
