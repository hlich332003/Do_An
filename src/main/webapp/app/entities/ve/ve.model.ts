import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';
import { ISuatChieu } from 'app/entities/suat-chieu/suat-chieu.model';

export interface IVe {
  id: number;
  maVe?: string | null;
  giaVe?: number | null;
  trangThai?: string | null;
  hoaDon?: Pick<IHoaDon, 'id'> | null;
  suatChieu?: Pick<ISuatChieu, 'id'> | null;
}

export type NewVe = Omit<IVe, 'id'> & { id: null };
