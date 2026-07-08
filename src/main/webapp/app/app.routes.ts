import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import PhimResolve from './entities/phim/route/phim-routing-resolve.service';
import SuatChieuResolve from './entities/suat-chieu/route/suat-chieu-routing-resolve.service';
import { errorRoute } from './layouts/error/error.route';

export const routes: Routes = [
  // --- ADMIN ROUTES (Uses AdminLayoutComponent) ---
  {
    path: 'admin',
    loadComponent: () => import('./layouts/admin-layout/admin-layout.component'),
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    children: [
      {
        path: '',
        loadChildren: () => import('./admin/admin.routes'),
      },
      {
        path: '',
        loadChildren: () => import(`./entities/entity.routes`),
      },
      {
        path: 'ban-ve',
        loadComponent: () => import('./entities/suat-chieu/lich-chieu/lich-chieu.component').then(m => m.LichChieuComponent),
        title: 'Bán vé tại quầy',
      },
      {
        path: 'dat-ve/ket-qua',
        loadComponent: () => import('./dat-ve/ket-qua/ket-qua.component').then(m => m.KetQuaComponent),
        title: 'Kết quả bán vé',
      },
      {
        path: 'dat-ve/:id',
        loadComponent: () => import('./dat-ve/dat-ve.component').then(m => m.DatVeComponent),
        resolve: {
          suatChieu: SuatChieuResolve,
        },
        title: 'Bán vé tại quầy',
      },
    ],
  },

  // --- PUBLIC/CUSTOMER ROUTES (Uses MainComponent) ---
  {
    path: '',
    loadComponent: () => import('./layouts/main/main.component'),
    children: [
      {
        path: '',
        loadComponent: () => import('./home/home.component'),
        title: 'home.title',
      },
      {
        path: '',
        loadComponent: () => import('./layouts/navbar/navbar.component'),
        outlet: 'navbar',
      },
      {
        path: 'account',
        loadChildren: () => import('./account/account.route'),
      },
      {
        path: 'login',
        loadComponent: () => import('./login/login.component'),
        title: 'login.title',
      },
      {
        path: 'dat-ve',
        loadChildren: () => import(`./dat-ve/dat-ve.routes`),
      },
      {
        path: 'lich-chieu',
        loadComponent: () => import('./entities/suat-chieu/lich-chieu/lich-chieu.component').then(m => m.LichChieuComponent),
        title: 'Lịch chiếu',
      },
      {
        path: 'phim-danh-sach',
        loadComponent: () => import('./entities/phim/phim-danh-sach/phim-danh-sach.component').then(m => m.PhimDanhSachComponent),
        title: 'Danh sách Phim',
      },
      {
        path: 'phim-chi-tiet/:id',
        loadComponent: () => import('./entities/phim/phim-chi-tiet/phim-chi-tiet.component').then(m => m.PhimChiTietComponent),
        resolve: {
          phim: PhimResolve,
        },
        title: 'Chi tiết Phim',
      },
      ...errorRoute,
    ],
  },
];

export default routes;
