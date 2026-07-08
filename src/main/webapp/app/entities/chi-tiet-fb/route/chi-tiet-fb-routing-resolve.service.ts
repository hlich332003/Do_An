import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IChiTietFB } from '../chi-tiet-fb.model';
import { ChiTietFBService } from '../service/chi-tiet-fb.service';

const chiTietFBResolve = (route: ActivatedRouteSnapshot): Observable<null | IChiTietFB> => {
  const id = route.params.id;
  if (id) {
    return inject(ChiTietFBService)
      .find(id)
      .pipe(
        mergeMap((chiTietFB: HttpResponse<IChiTietFB>) => {
          if (chiTietFB.body) {
            return of(chiTietFB.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default chiTietFBResolve;
