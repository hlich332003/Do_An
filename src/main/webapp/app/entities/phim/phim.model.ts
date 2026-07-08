import dayjs from 'dayjs/esm';

export interface IPhim {
  id: number;
  tenPhim?: string | null;
  moTa?: string | null;
  thoiLuong?: number | null;
  daoDien?: string | null;
  dienVien?: string | null;
  theLoai?: string | null;
  ngayKhoiChieu?: dayjs.Dayjs | null;
  poster?: string | null;
  trailer?: string | null;
  trangThai?: string | null;
  createdAt?: dayjs.Dayjs | null;
}

export type NewPhim = Omit<IPhim, 'id'> & { id: null };
