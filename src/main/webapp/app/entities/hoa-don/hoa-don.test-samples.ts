import dayjs from 'dayjs/esm';

import { IHoaDon, NewHoaDon } from './hoa-don.model';

export const sampleWithRequiredData: IHoaDon = {
  id: 8439,
  tongTien: 21132.97,
};

export const sampleWithPartialData: IHoaDon = {
  id: 22269,
  tongTien: 31571.26,
  maGiamGia: 'deeply round babushka',
  maGiaoDich: 'as stylish precious',
  ngayTao: dayjs('2026-06-09T09:32'),
  ngayThanhToan: dayjs('2026-06-09T20:05'),
};

export const sampleWithFullData: IHoaDon = {
  id: 15477,
  tongTien: 17346.27,
  maGiamGia: 'of bug against',
  soTienGiam: 30120.64,
  phuongThucThanhToan: 'rebound or hm',
  maGiaoDich: 'vibrant',
  trangThai: 'irritably',
  ngayTao: dayjs('2026-06-10T00:48'),
  ngayThanhToan: dayjs('2026-06-10T06:53'),
};

export const sampleWithNewData: NewHoaDon = {
  tongTien: 22382.75,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
