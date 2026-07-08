import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IVe } from '../ve.model';
import { VeService } from '../service/ve.service';

const veResolve = (route: ActivatedRouteSnapshot): Observable<null | IVe> => {
  const id = route.params.id;
  if (id) {
    return inject(VeService)
      .find(id)
      .pipe(
        mergeMap((ve: HttpResponse<IVe>) => {
          if (ve.body) {
            return of(ve.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default veResolve;
