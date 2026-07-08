import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IDanhGia, NewDanhGia } from '../danh-gia.model';

export type PartialUpdateDanhGia = Partial<IDanhGia> & Pick<IDanhGia, 'id'>;

type RestOf<T extends IDanhGia | NewDanhGia> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestDanhGia = RestOf<IDanhGia>;
export type NewRestDanhGia = RestOf<NewDanhGia>;
export type PartialUpdateRestDanhGia = RestOf<PartialUpdateDanhGia>;

export type EntityResponseType = HttpResponse<IDanhGia>;
export type EntityArrayResponseType = HttpResponse<IDanhGia[]>;

@Injectable({ providedIn: 'root' })
export class DanhGiaService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/danh-gias');

  queryByPhim(phimId: number): Observable<EntityArrayResponseType> {
    const options = createRequestOption({ phimId });
    return this.http
      .get<RestDanhGia[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getAverageRating(phimId: number): Observable<number> {
    const options = createRequestOption({ phimId });
    return this.http.get<number>(`${this.resourceUrl}/rating`, { params: options });
  }

  create(danhGia: NewDanhGia): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(danhGia);
    return this.http
      .post<RestDanhGia>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(danhGia: IDanhGia): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(danhGia);
    return this.http
      .put<RestDanhGia>(`${this.resourceUrl}/${danhGia.id}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestDanhGia[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  protected convertDateFromClient<T extends IDanhGia | NewDanhGia | PartialUpdateDanhGia>(danhGia: T): RestOf<T> {
    return {
      ...danhGia,
      createdAt: danhGia.createdAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restDanhGia: RestDanhGia): IDanhGia {
    return {
      ...restDanhGia,
      createdAt: restDanhGia.createdAt ? dayjs(restDanhGia.createdAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestDanhGia>): HttpResponse<IDanhGia> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestDanhGia[]>): HttpResponse<IDanhGia[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
