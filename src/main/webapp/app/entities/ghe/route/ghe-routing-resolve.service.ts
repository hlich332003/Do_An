import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IGhe } from '../ghe.model';
import { GheService } from '../service/ghe.service';

const gheResolve = (route: ActivatedRouteSnapshot): Observable<null | IGhe> => {
  const id = route.params.id;
  if (id) {
    return inject(GheService)
      .find(id)
      .pipe(
        mergeMap((ghe: HttpResponse<IGhe>) => {
          if (ghe.body) {
            return of(ghe.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default gheResolve;
