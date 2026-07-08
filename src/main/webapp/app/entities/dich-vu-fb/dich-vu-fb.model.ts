export interface IDichVuFB {
  id: number;
  tenCombo?: string | null;
  moTa?: string | null;
  gia?: number | null;
  hinhAnh?: string | null;
  trangThai?: string | null;
}

export type NewDichVuFB = Omit<IDichVuFB, 'id'> & { id: null };
