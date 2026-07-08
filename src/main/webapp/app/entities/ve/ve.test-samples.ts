import { IVe, NewVe } from './ve.model';

export const sampleWithRequiredData: IVe = {
  id: 14429,
  maVe: 'throbbing',
};

export const sampleWithPartialData: IVe = {
  id: 16439,
  maVe: 'phew labourer rotten',
  giaVe: 13814.85,
  trangThai: 'er',
};

export const sampleWithFullData: IVe = {
  id: 25154,
  maVe: 'demonstrate mmm',
  giaVe: 2230.31,
  trangThai: 'whoever ham',
};

export const sampleWithNewData: NewVe = {
  maVe: 'provided squid',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
