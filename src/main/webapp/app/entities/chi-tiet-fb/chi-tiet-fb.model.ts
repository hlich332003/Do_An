import { IDichVuFB } from 'app/entities/dich-vu-fb/dich-vu-fb.model';
import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';

export interface IChiTietFB {
  id: number;
  soLuong?: number | null;
  giaBan?: number | null;
  dichVuFB?: Pick<IDichVuFB, 'id' | 'tenCombo'> | null;
  hoaDon?: Pick<IHoaDon, 'id'> | null;
}

export type NewChiTietFB = Omit<IChiTietFB, 'id'> & { id: null };
