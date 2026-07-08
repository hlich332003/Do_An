import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ChiTietFBResolve from './route/chi-tiet-fb-routing-resolve.service';

const chiTietFBRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/chi-tiet-fb.component').then(m => m.ChiTietFBComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/chi-tiet-fb-detail.component').then(m => m.ChiTietFBDetailComponent),
    resolve: {
      chiTietFB: ChiTietFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/chi-tiet-fb-update.component').then(m => m.ChiTietFBUpdateComponent),
    resolve: {
      chiTietFB: ChiTietFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/chi-tiet-fb-update.component').then(m => m.ChiTietFBUpdateComponent),
    resolve: {
      chiTietFB: ChiTietFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default chiTietFBRoute;
