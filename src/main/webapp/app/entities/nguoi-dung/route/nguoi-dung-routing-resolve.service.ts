import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { INguoiDung } from '../nguoi-dung.model';
import { NguoiDungService } from '../service/nguoi-dung.service';

const nguoiDungResolve = (route: ActivatedRouteSnapshot): Observable<null | INguoiDung> => {
  const id = route.params.id;
  if (id) {
    return inject(NguoiDungService)
      .find(id)
      .pipe(
        mergeMap((nguoiDung: HttpResponse<INguoiDung>) => {
          if (nguoiDung.body) {
            return of(nguoiDung.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default nguoiDungResolve;
