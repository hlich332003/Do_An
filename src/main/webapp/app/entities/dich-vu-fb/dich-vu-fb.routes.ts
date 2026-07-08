import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import DichVuFBResolve from './route/dich-vu-fb-routing-resolve.service';

const dichVuFBRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/dich-vu-fb.component').then(m => m.DichVuFBComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/dich-vu-fb-detail.component').then(m => m.DichVuFBDetailComponent),
    resolve: {
      dichVuFB: DichVuFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/dich-vu-fb-update.component').then(m => m.DichVuFBUpdateComponent),
    resolve: {
      dichVuFB: DichVuFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/dich-vu-fb-update.component').then(m => m.DichVuFBUpdateComponent),
    resolve: {
      dichVuFB: DichVuFBResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default dichVuFBRoute;
