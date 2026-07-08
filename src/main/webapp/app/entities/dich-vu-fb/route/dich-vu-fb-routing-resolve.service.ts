import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IDichVuFB } from '../dich-vu-fb.model';
import { DichVuFBService } from '../service/dich-vu-fb.service';

const dichVuFBResolve = (route: ActivatedRouteSnapshot): Observable<null | IDichVuFB> => {
  const id = route.params.id;
  if (id) {
    return inject(DichVuFBService)
      .find(id)
      .pipe(
        mergeMap((dichVuFB: HttpResponse<IDichVuFB>) => {
          if (dichVuFB.body) {
            return of(dichVuFB.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default dichVuFBResolve;
