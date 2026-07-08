import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';

export interface IComboItem {
  dichVuFBId: number;
  soLuong: number;
}

export interface IBookingCreateRequest {
  suatChieuId: number;
  gheIds: number[];
  combos?: IComboItem[];
  maGiamGia?: string;
  phuongThucThanhToan?: string;
  nguoiDungId?: number;
}

export interface IHoaDonLichSu {
  id: number;
  tenPhim?: string;
  maVe?: string;
  gioChieu?: string;
  tenRap?: string;
  tenPhongChieu?: string;
  danhSachGhe?: string;
  danhSachCombo?: string;
  tongTien?: number;
  phuongThucThanhToan?: string;
  trangThai?: string;
  maGiaoDich?: string;
  veIds?: number[];
}

export interface IMetricComparison {
  previousValue: number;
  deltaValue: number;
  deltaPercent: number;
}

export interface IDashboardItem {
  name: string;
  count: number;
  capacity: number;
  revenue: number;
  percentage: number;
  averageRating?: number;
}

export interface IDashboardData {
  totalRevenue: number;
  totalFbRevenue: number;
  averageOccupancyRate: number;
  comboAttachRate: number;
  totalTicketsSold: number;
  totalUsers: number;
  paidInvoicesCount: number;
  invoicesWithCombo: number;
  reviewCount: number;
  averageMovieRating: number;
  revenueComparison: IMetricComparison;
  fbRevenueComparison: IMetricComparison;
  ticketsComparison: IMetricComparison;
  usersComparison: IMetricComparison;
  topMovies: IDashboardItem[];
  topRatedMovies: IDashboardItem[];
  topRooms: IDashboardItem[];
  topCombos: IDashboardItem[];
  statusBreakdown: IDashboardItem[];
  peakHours: IDashboardItem[];
  ticketTypeBreakdown: IDashboardItem[];
}

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  holdSeat(suatChieuId: number, maGhe: string): Observable<any> {
    return this.http.post<any>(this.applicationConfigService.getEndpointFor('api/booking/hold-seats'), { suatChieuId, maGhes: [maGhe] });
  }

  releaseSeat(suatChieuId: number, maGhe: string): Observable<any> {
    return this.http.post<any>(this.applicationConfigService.getEndpointFor('api/booking/release-seats'), { suatChieuId, maGhes: [maGhe] });
  }

  releaseSeats(suatChieuId: number, maGhes: string[]): Observable<any> {
    return this.http.post<any>(this.applicationConfigService.getEndpointFor('api/booking/release-seats'), { suatChieuId, maGhes });
  }

  extendLock(suatChieuId: number, maGhes: string[]): Observable<any> {
    return this.http.post<any>(this.applicationConfigService.getEndpointFor('api/booking/extend-lock'), { suatChieuId, maGhes });
  }

  createBooking(request: IBookingCreateRequest): Observable<IHoaDon> {
    return this.http.post<IHoaDon>(this.applicationConfigService.getEndpointFor('api/booking/create'), request);
  }

  createVnpayPaymentUrl(invoiceId: number, qrOnly: boolean = true): Observable<{ paymentUrl: string }> {
    const params = new HttpParams().set('qrOnly', String(qrOnly));
    return this.http.post<{ paymentUrl: string }>(
      this.applicationConfigService.getEndpointFor(`api/payment/vnpay/create-url/${invoiceId}`),
      {},
      { params },
    );
  }

  confirmPayment(invoiceId: number, status: string = 'SUCCESS'): Observable<{ result: string; message: string }> {
    if (status === 'SUCCESS') {
      return this.http.post<{ result: string; message: string }>(
        this.applicationConfigService.getEndpointFor(`api/booking/confirm-payment/${invoiceId}`),
        {},
      );
    }
    return this.http.post<{ result: string; message: string }>(this.applicationConfigService.getEndpointFor('api/payment/callback'), {
      invoiceId,
      status,
    });
  }

  getLichSu(): Observable<IHoaDonLichSu[]> {
    return this.http.get<IHoaDonLichSu[]>(this.applicationConfigService.getEndpointFor('api/hoa-dons/lich-su'));
  }

  getAdminLichSu(): Observable<IHoaDonLichSu[]> {
    return this.http.get<IHoaDonLichSu[]>(this.applicationConfigService.getEndpointFor('api/hoa-dons/admin/lich-su'));
  }

  getAdminLichSuById(id: number): Observable<IHoaDonLichSu> {
    return this.http.get<IHoaDonLichSu>(this.applicationConfigService.getEndpointFor(`api/hoa-dons/admin/lich-su/${id}`));
  }

  cancelBooking(hoaDonId: number): Observable<{ result: string; message: string }> {
    return this.http.post<{ result: string; message: string }>(
      this.applicationConfigService.getEndpointFor(`api/hoa-dons/${hoaDonId}/cancel`),
      {},
    );
  }

  getDashboard(fromDate?: string, toDate?: string): Observable<IDashboardData> {
    let params = new HttpParams();
    if (fromDate) params = params.set('fromDate', fromDate);
    if (toDate) params = params.set('toDate', toDate);
    return this.http.get<IDashboardData>(this.applicationConfigService.getEndpointFor('api/admin/dashboard'), { params });
  }

  exportDashboard(fromDate?: string, toDate?: string): Observable<Blob> {
    let params = new HttpParams();
    if (fromDate) params = params.set('fromDate', fromDate);
    if (toDate) params = params.set('toDate', toDate);
    return this.http.get(this.applicationConfigService.getEndpointFor('api/admin/dashboard/export'), {
      params,
      responseType: 'blob',
    });
  }

  getQrCode(veId: number): Observable<string> {
    return this.http.get(this.applicationConfigService.getEndpointFor(`api/ves/${veId}/qr-code`), { responseType: 'text' });
  }
}
