import dayjs from 'dayjs/esm';
import { INguoiDung } from 'app/entities/nguoi-dung/nguoi-dung.model';

export interface IHoaDon {
  id: number;
  tongTien?: number | null;
  maGiamGia?: string | null;
  soTienGiam?: number | null;
  phuongThucThanhToan?: string | null;
  maGiaoDich?: string | null;
  soVe?: number | null;
  trangThai?: string | null;
  ngayTao?: dayjs.Dayjs | null;
  ngayThanhToan?: dayjs.Dayjs | null;
  nguoiDung?: Pick<INguoiDung, 'id' | 'email' | 'hoTen'> | null;
}

export type NewHoaDon = Omit<IHoaDon, 'id'> & { id: null };
