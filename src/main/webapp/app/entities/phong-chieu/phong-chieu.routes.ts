import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PhongChieuResolve from './route/phong-chieu-routing-resolve.service';

const phongChieuRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/phong-chieu.component').then(m => m.PhongChieuComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/phong-chieu-detail.component').then(m => m.PhongChieuDetailComponent),
    resolve: {
      phongChieu: PhongChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/phong-chieu-update.component').then(m => m.PhongChieuUpdateComponent),
    resolve: {
      phongChieu: PhongChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/phong-chieu-update.component').then(m => m.PhongChieuUpdateComponent),
    resolve: {
      phongChieu: PhongChieuResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default phongChieuRoute;
