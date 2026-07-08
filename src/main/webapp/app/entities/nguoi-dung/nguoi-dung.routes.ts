import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import NguoiDungResolve from './route/nguoi-dung-routing-resolve.service';

const nguoiDungRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/nguoi-dung.component').then(m => m.NguoiDungComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/nguoi-dung-detail.component').then(m => m.NguoiDungDetailComponent),
    resolve: {
      nguoiDung: NguoiDungResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/nguoi-dung-update.component').then(m => m.NguoiDungUpdateComponent),
    resolve: {
      nguoiDung: NguoiDungResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/nguoi-dung-update.component').then(m => m.NguoiDungUpdateComponent),
    resolve: {
      nguoiDung: NguoiDungResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default nguoiDungRoute;
