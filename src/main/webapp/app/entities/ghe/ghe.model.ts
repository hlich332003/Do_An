import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';

export interface IGhe {
  id: number;
  maGhe?: string | null;
  hang?: string | null;
  cot?: number | null;
  loaiGhe?: number | null;
  trangThai?: string | null;
  phongChieu?: Pick<IPhongChieu, 'id' | 'tenPhong'> | null;
}

export type NewGhe = Omit<IGhe, 'id'> & { id: null };
