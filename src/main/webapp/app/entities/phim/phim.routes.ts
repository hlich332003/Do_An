import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PhimResolve from './route/phim-routing-resolve.service';

const phimRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/phim.component').then(m => m.PhimComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/phim-detail.component').then(m => m.PhimDetailComponent),
    resolve: {
      phim: PhimResolve,
    },
  },
  {
    path: 'new',
    loadComponent: () => import('./update/phim-update.component').then(m => m.PhimUpdateComponent),
    resolve: {
      phim: PhimResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/phim-update.component').then(m => m.PhimUpdateComponent),
    resolve: {
      phim: PhimResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default phimRoute;
