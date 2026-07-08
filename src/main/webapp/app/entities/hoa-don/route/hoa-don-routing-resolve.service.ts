import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IHoaDon } from '../hoa-don.model';
import { HoaDonService } from '../service/hoa-don.service';

const hoaDonResolve = (route: ActivatedRouteSnapshot): Observable<null | IHoaDon> => {
  const id = route.params.id;
  if (id) {
    return inject(HoaDonService)
      .find(id)
      .pipe(
        mergeMap((hoaDon: HttpResponse<IHoaDon>) => {
          if (hoaDon.body) {
            return of(hoaDon.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default hoaDonResolve;
