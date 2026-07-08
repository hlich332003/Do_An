import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';
import { Login } from './login.model';
import { BookingService } from 'app/dat-ve/service/booking.service';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly accountService = inject(AccountService);
  private readonly authServerProvider = inject(AuthServerProvider);
  private readonly bookingService = inject(BookingService);

  login(credentials: Login): Observable<Account | null> {
    return this.authServerProvider.login(credentials).pipe(mergeMap(() => this.accountService.identity(true)));
  }

  logout(): void {
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i);
      if (key && key.startsWith('booking_state_')) {
        try {
          const suatChieuId = Number(key.replace('booking_state_', ''));
          const state = JSON.parse(sessionStorage.getItem(key) || '{}');
          if (state.gheDaChon && state.gheDaChon.length > 0) {
            state.gheDaChon.forEach((g: any) => {
              if (g.maGhe) {
                this.bookingService.releaseSeat(suatChieuId, g.maGhe).subscribe({
                  error: err => console.error('Error releasing seat on logout', err),
                });
              }
            });
          }
        } catch (e) {
          console.error('Error parsing booking state on logout', e);
        }
        sessionStorage.removeItem(key);
        i--;
      }
    }
    this.authServerProvider.logout().subscribe({ complete: () => this.accountService.authenticate(null) });
  }
}
