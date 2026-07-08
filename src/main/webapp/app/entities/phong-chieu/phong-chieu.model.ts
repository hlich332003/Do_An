export interface IPhongChieu {
  id: number;
  tenPhong?: string | null;
  soLuongGhe?: number | null;
  trangThai?: string | null;
}

export type NewPhongChieu = Omit<IPhongChieu, 'id'> & { id: null };
