import { Routes } from '@angular/router';

import activateRoute from './activate/activate.route';
import passwordRoute from './password/password.route';
import passwordResetFinishRoute from './password-reset/finish/password-reset-finish.route';
import passwordResetInitRoute from './password-reset/init/password-reset-init.route';
import registerRoute from './register/register.route';
import settingsRoute from './settings/settings.route';
import { LichSuDatVeComponent } from './lich-su-dat-ve/lich-su-dat-ve.component';

const accountRoutes: Routes = [
  activateRoute,
  passwordRoute,
  passwordResetFinishRoute,
  passwordResetInitRoute,
  registerRoute,
  settingsRoute,
  {
    path: 'lich-su-dat-ve',
    component: LichSuDatVeComponent,
    title: 'Lịch sử đặt vé',
  },
];

export default accountRoutes;
