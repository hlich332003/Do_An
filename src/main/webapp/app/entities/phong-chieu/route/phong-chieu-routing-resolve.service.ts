import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPhongChieu } from '../phong-chieu.model';
import { PhongChieuService } from '../service/phong-chieu.service';

const phongChieuResolve = (route: ActivatedRouteSnapshot): Observable<null | IPhongChieu> => {
  const id = route.params.id;
  if (id) {
    return inject(PhongChieuService)
      .find(id)
      .pipe(
        mergeMap((phongChieu: HttpResponse<IPhongChieu>) => {
          if (phongChieu.body) {
            return of(phongChieu.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default phongChieuResolve;
