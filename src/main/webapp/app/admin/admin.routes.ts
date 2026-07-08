import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    title: 'Bảng điều khiển',
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    title: 'Bảng điều khiển',
  },

  {
    path: 'docs',
    loadComponent: () => import('./docs/docs.component'),
    title: 'global.menu.admin.apidocs',
  },
  {
    path: 'configuration',
    loadComponent: () => import('./configuration/configuration.component'),
    title: 'configuration.title',
  },
  {
    path: 'health',
    loadComponent: () => import('./health/health.component'),
    title: 'health.title',
  },
  {
    path: 'logs',
    loadComponent: () => import('./logs/logs.component'),
    title: 'logs.title',
  },
  {
    path: 'metrics',
    loadComponent: () => import('./metrics/metrics.component'),
    title: 'metrics.title',
  },
];

export default routes;
