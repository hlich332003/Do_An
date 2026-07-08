import { inject } from '@angular/core';
import { Routes, CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AccountService } from 'app/core/auth/account.service';
import SuatChieuResolve from 'app/entities/suat-chieu/route/suat-chieu-routing-resolve.service';
import { DatVeComponent } from './dat-ve.component';
import { KetQuaComponent } from './ket-qua/ket-qua.component';

const nonAdminBookingGuard: CanActivateFn = (_route, state) => {
  const accountService = inject(AccountService);
  const router = inject(Router);

  return accountService.identity().pipe(
    map(account => {
      if (!account) {
        router.navigate(['/login'], { queryParams: { redirectUrl: state.url } });
        return false;
      }

      if (account.authorities?.includes('ROLE_ADMIN')) {
        router.navigate(['/admin/dashboard']);
        return false;
      }

      return true;
    }),
  );
};

const datVeRoutes: Routes = [
  {
    path: 'ket-qua',
    component: KetQuaComponent,
    title: 'Thanh toán thành công',
  },
  {
    path: ':id',
    component: DatVeComponent,
    canActivate: [nonAdminBookingGuard],
    resolve: {
      suatChieu: SuatChieuResolve,
    },
    title: 'Đặt vé',
  },
];

export default datVeRoutes;
