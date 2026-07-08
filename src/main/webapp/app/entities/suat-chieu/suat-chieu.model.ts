import dayjs from 'dayjs/esm';
import { IPhim } from 'app/entities/phim/phim.model';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';

export interface ISuatChieu {
  id: number;
  thoiGianBatDau?: dayjs.Dayjs | null;
  thoiGianKetThuc?: dayjs.Dayjs | null;
  giaThuong?: number | null;
  giaVip?: number | null;
  phim?: Pick<IPhim, 'id' | 'tenPhim' | 'poster'> | null;
  phongChieu?: Pick<IPhongChieu, 'id' | 'tenPhong'> | null;
}

export type NewSuatChieu = Omit<ISuatChieu, 'id'> & { id: null };
