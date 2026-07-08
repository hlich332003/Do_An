import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuService } from '../service/suat-chieu.service';

const suatChieuResolve = (route: ActivatedRouteSnapshot): Observable<null | ISuatChieu> => {
  const id = route.params.id;
  if (id) {
    return inject(SuatChieuService)
      .find(id)
      .pipe(
        mergeMap((suatChieu: HttpResponse<ISuatChieu>) => {
          if (suatChieu.body) {
            return of(suatChieu.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default suatChieuResolve;
