import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPhim } from '../phim.model';
import { PhimService } from '../service/phim.service';

const phimResolve = (route: ActivatedRouteSnapshot): Observable<null | IPhim> => {
  const id = route.params.id;
  if (id) {
    return inject(PhimService)
      .find(id)
      .pipe(
        mergeMap((phim: HttpResponse<IPhim>) => {
          if (phim.body) {
            return of(phim.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default phimResolve;
