import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import HoaDonResolve from './route/hoa-don-routing-resolve.service';

const hoaDonRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/hoa-don.component').then(m => m.HoaDonComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/hoa-don-detail.component').then(m => m.HoaDonDetailComponent),
    resolve: {
      hoaDon: HoaDonResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/hoa-don-update.component').then(m => m.HoaDonUpdateComponent),
    resolve: {
      hoaDon: HoaDonResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/hoa-don-update.component').then(m => m.HoaDonUpdateComponent),
    resolve: {
      hoaDon: HoaDonResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default hoaDonRoute;
