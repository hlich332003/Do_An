import dayjs from 'dayjs/esm';

import { ISuatChieu, NewSuatChieu } from './suat-chieu.model';

export const sampleWithRequiredData: ISuatChieu = {
  id: 22412,
  thoiGianBatDau: dayjs('2026-06-09T17:05'),
  thoiGianKetThuc: dayjs('2026-06-10T00:40'),
};

export const sampleWithPartialData: ISuatChieu = {
  id: 29525,
  thoiGianBatDau: dayjs('2026-06-09T09:13'),
  thoiGianKetThuc: dayjs('2026-06-09T18:10'),
};

export const sampleWithFullData: ISuatChieu = {
  id: 18108,
  thoiGianBatDau: dayjs('2026-06-10T04:35'),
  thoiGianKetThuc: dayjs('2026-06-10T07:53'),
};

export const sampleWithNewData: NewSuatChieu = {
  thoiGianBatDau: dayjs('2026-06-10T00:17'),
  thoiGianKetThuc: dayjs('2026-06-10T07:35'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
