import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import SuatChieuResolve from './route/suat-chieu-routing-resolve.service';

const suatChieuRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/suat-chieu.component').then(m => m.SuatChieuComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/suat-chieu-detail.component').then(m => m.SuatChieuDetailComponent),
    resolve: {
      suatChieu: SuatChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/suat-chieu-update.component').then(m => m.SuatChieuUpdateComponent),
    resolve: {
      suatChieu: SuatChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/suat-chieu-update.component').then(m => m.SuatChieuUpdateComponent),
    resolve: {
      suatChieu: SuatChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default suatChieuRoute;
