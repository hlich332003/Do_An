export interface INguoiDung {
  id: number;
  hoTen?: string | null;
  email?: string | null;
  matKhau?: string | null;
  soDienThoai?: string | null;
  diaChi?: string | null;
  diemTichLuy?: number | null;
  vaiTro?: string | null;
  trangThai?: string | null;
}

export type NewNguoiDung = Omit<INguoiDung, 'id'> & { id: null };
