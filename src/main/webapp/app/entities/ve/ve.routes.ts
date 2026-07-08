import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC, DESC } from 'app/config/navigation.constants';
import VeResolve from './route/ve-routing-resolve.service';

const veRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/ve.component').then(m => m.VeComponent),
    data: {
      defaultSort: `id,${DESC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/ve-detail.component').then(m => m.VeDetailComponent),
    resolve: {
      ve: VeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/ve-update.component').then(m => m.VeUpdateComponent),
    resolve: {
      ve: VeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/ve-update.component').then(m => m.VeUpdateComponent),
    resolve: {
      ve: VeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default veRoute;
