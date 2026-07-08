import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'nguoi-dung',
    data: { pageTitle: 'NguoiDungs' },
    loadChildren: () => import('./nguoi-dung/nguoi-dung.routes'),
  },
  {
    path: 'dich-vu-fb',
    data: { pageTitle: 'DichVuFBS' },
    loadChildren: () => import('./dich-vu-fb/dich-vu-fb.routes'),
  },
  {
    path: 'phong-chieu',
    data: { pageTitle: 'PhongChieus' },
    loadChildren: () => import('./phong-chieu/phong-chieu.routes'),
  },
  {
    path: 'ghe',
    data: { pageTitle: 'Ghes' },
    loadChildren: () => import('./ghe/ghe.routes'),
  },
  {
    path: 'suat-chieu',
    data: { pageTitle: 'SuatChieus' },
    loadChildren: () => import('./suat-chieu/suat-chieu.routes'),
  },
  {
    path: 'hoa-don',
    data: { pageTitle: 'HoaDons' },
    loadChildren: () => import('./hoa-don/hoa-don.routes'),
  },
  {
    path: 've',
    data: { pageTitle: 'Ves' },
    loadChildren: () => import('./ve/ve.routes'),
  },
  {
    path: 'chi-tiet-fb',
    data: { pageTitle: 'ChiTietFBS' },
    loadChildren: () => import('./chi-tiet-fb/chi-tiet-fb.routes'),
  },
  {
    path: 'phim',
    data: { pageTitle: 'Phims' },
    loadChildren: () => import('./phim/phim.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
