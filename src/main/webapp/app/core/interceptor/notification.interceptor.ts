import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { AlertService } from 'app/core/util/alert.service';

@Injectable()
export class NotificationInterceptor implements HttpInterceptor {
  private readonly alertService = inject(AlertService);

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          let alert: string | null = null;

          for (const headerKey of event.headers.keys()) {
            if (headerKey.toLowerCase().endsWith('app-alert')) {
              alert = event.headers.get(headerKey);
            }
          }

          if (alert) {
            let messageVi = 'Thành công!';
            const lowerAlert = alert.toLowerCase();
            if (lowerAlert.includes('created') || lowerAlert.includes('tao moi') || lowerAlert.includes('them moi')) {
              messageVi = 'Thêm mới thành công!';
            } else if (lowerAlert.includes('updated') || lowerAlert.includes('cap nhat') || lowerAlert.includes('update')) {
              messageVi = 'Cập nhật thành công!';
            } else if (lowerAlert.includes('deleted') || lowerAlert.includes('xoa') || lowerAlert.includes('delete')) {
              messageVi = 'Xóa thành công!';
            }
            this.alertService.addAlert({
              type: 'success',
              message: messageVi,
            });
          }
        }
      }),
    );
  }
}
