import { Component, OnDestroy, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { Alert, AlertService } from 'app/core/util/alert.service';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { AlertError } from './alert-error.model';

@Component({
  selector: 'jhi-alert-error',
  templateUrl: './alert-error.component.html',
  imports: [CommonModule, NgbModule],
})
export class AlertErrorComponent implements OnDestroy {
  alerts = signal<Alert[]>([]);
  errorListener: Subscription;
  httpErrorListener: Subscription;

  private readonly alertService = inject(AlertService);
  private readonly eventManager = inject(EventManager);

  constructor() {
    this.errorListener = this.eventManager.subscribe('cinemaTickApp.error', (response: EventWithContent<unknown> | string) => {
      const errorResponse = (response as EventWithContent<AlertError>).content;
      this.addErrorAlert(errorResponse.message);
    });

    this.httpErrorListener = this.eventManager.subscribe('cinemaTickApp.httpError', (response: EventWithContent<unknown> | string) => {
      this.handleHttpError(response);
    });
  }

  setClasses(alert: Alert): Record<string, boolean> {
    const classes = { 'jhi-toast': Boolean(alert.toast) };
    if (alert.position) {
      return { ...classes, [alert.position]: true };
    }
    return classes;
  }

  ngOnDestroy(): void {
    this.eventManager.destroy(this.errorListener);
    this.eventManager.destroy(this.httpErrorListener);
  }

  close(alert: Alert): void {
    alert.close?.(this.alerts());
  }

  private addErrorAlert(message?: string): void {
    let messageVi = message || 'Có lỗi xảy ra!';
    const lowerMessage = messageVi.toLowerCase();
    if (lowerMessage.includes('server not reachable')) {
      messageVi = 'Không thể kết nối đến máy chủ!';
    } else if (lowerMessage.includes('not found')) {
      messageVi = 'Không tìm thấy dữ liệu!';
    } else if (lowerMessage.includes('error on field')) {
      const fieldMatch = messageVi.match(/field "([^"]+)"/i);
      const fieldName = fieldMatch ? fieldMatch[1] : '';
      messageVi = fieldName ? `Lỗi dữ liệu tại trường: ${fieldName}` : 'Lỗi dữ liệu nhập vào!';
    } else if (
      lowerMessage.includes('email already in use') ||
      lowerMessage.includes('email is already in use') ||
      lowerMessage.includes('emailexists')
    ) {
      messageVi = 'Email này đã được sử dụng!';
    } else if (lowerMessage.includes('login already in use') || lowerMessage.includes('loginexists')) {
      messageVi = 'Tên đăng nhập đã được sử dụng!';
    } else if (lowerMessage.includes('bad request') || lowerMessage.includes('invalid') || lowerMessage.includes('validation')) {
      messageVi = 'Yêu cầu không hợp lệ hoặc lỗi xác thực!';
    } else {
      messageVi = 'Thao tác thất bại hoặc có lỗi xảy ra!';
    }
    this.alertService.addAlert({ type: 'danger', message: messageVi }, this.alerts());
  }

  private handleHttpError(response: EventWithContent<unknown> | string): void {
    const httpErrorResponse = (response as EventWithContent<HttpErrorResponse>).content;
    switch (httpErrorResponse.status) {
      // connection refused, server not reachable
      case 0:
        this.addErrorAlert('Server not reachable');
        break;

      case 400: {
        this.handleBadRequest(httpErrorResponse);
        break;
      }

      case 404:
        this.addErrorAlert('Not found');
        break;

      default:
        this.handleDefaultError(httpErrorResponse);
    }
  }

  private handleBadRequest(httpErrorResponse: HttpErrorResponse): void {
    const arr = httpErrorResponse.headers.keys();
    let errorHeader: string | null = null;
    for (const entry of arr) {
      if (entry.toLowerCase().endsWith('app-error')) {
        errorHeader = httpErrorResponse.headers.get(entry);
      }
    }
    if (errorHeader) {
      this.addErrorAlert(errorHeader);
    } else if (httpErrorResponse.error && typeof httpErrorResponse.error === 'object' && httpErrorResponse.error.fieldErrors) {
      this.handleFieldsError(httpErrorResponse);
    } else if (httpErrorResponse.error && typeof httpErrorResponse.error === 'object') {
      const errorBody = httpErrorResponse.error;
      const msg = errorBody.title || errorBody.detail || errorBody.properties?.message || errorBody.message;
      if (msg) {
        this.addErrorAlert(msg);
      } else {
        this.addErrorAlert(httpErrorResponse.message);
      }
    } else {
      this.addErrorAlert(
        typeof httpErrorResponse.error === 'string' && httpErrorResponse.error ? httpErrorResponse.error : httpErrorResponse.message,
      );
    }
  }

  private handleDefaultError(httpErrorResponse: HttpErrorResponse): void {
    if (httpErrorResponse.error && typeof httpErrorResponse.error === 'object') {
      const errorBody = httpErrorResponse.error;
      const msg = errorBody.title || errorBody.detail || errorBody.properties?.message || errorBody.message;
      this.addErrorAlert(msg || httpErrorResponse.message);
    } else {
      this.addErrorAlert(
        typeof httpErrorResponse.error === 'string' && httpErrorResponse.error ? httpErrorResponse.error : httpErrorResponse.message,
      );
    }
  }

  private handleFieldsError(httpErrorResponse: HttpErrorResponse): void {
    const { fieldErrors } = httpErrorResponse.error;
    for (const fieldError of fieldErrors) {
      if (['Min', 'Max', 'DecimalMin', 'DecimalMax'].includes(fieldError.message)) {
        fieldError.message = 'Size';
      }
      // convert 'something[14].other[4].id' to 'something[].other[].id' so translations can be written to it
      const convertedField: string = fieldError.field.replace(/\[\d*\]/g, '[]');
      const fieldName: string = convertedField.charAt(0).toUpperCase() + convertedField.slice(1);
      this.addErrorAlert(`Error on field "${fieldName}"`);
    }
  }
}
