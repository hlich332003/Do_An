import dayjs from 'dayjs/esm';

export interface IDanhGia {
  id: number;
  soSao?: number | null;
  noiDung?: string | null;
  createdAt?: dayjs.Dayjs | null;
  phimId?: number | null;
  phimTen?: string | null;
  nguoiDungId?: number | null;
  nguoiDungHoTen?: string | null;
  nguoiDungEmail?: string | null;
}

export type NewDanhGia = Omit<IDanhGia, 'id'> & { id: null };
