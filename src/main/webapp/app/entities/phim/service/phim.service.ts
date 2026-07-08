import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPhim, NewPhim } from '../phim.model';

export type PartialUpdatePhim = Partial<IPhim> & Pick<IPhim, 'id'>;

type RestOf<T extends IPhim | NewPhim> = Omit<T, 'ngayKhoiChieu'> & {
  ngayKhoiChieu?: string | null;
};

export type RestPhim = RestOf<IPhim>;

export type NewRestPhim = RestOf<NewPhim>;

export type PartialUpdateRestPhim = RestOf<PartialUpdatePhim>;

export type EntityResponseType = HttpResponse<IPhim>;
export type EntityArrayResponseType = HttpResponse<IPhim[]>;

@Injectable({ providedIn: 'root' })
export class PhimService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/phims');

  create(phim: NewPhim): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(phim);
    return this.http.post<RestPhim>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(phim: IPhim): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(phim);
    return this.http
      .put<RestPhim>(`${this.resourceUrl}/${this.getPhimIdentifier(phim)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(phim: PartialUpdatePhim): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(phim);
    return this.http
      .patch<RestPhim>(`${this.resourceUrl}/${this.getPhimIdentifier(phim)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestPhim>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPhim[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  search(query: string, req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPhim[]>(`${this.resourceUrl}/search?query=${encodeURIComponent(query)}`, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getShowing(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPhim[]>(`${this.resourceUrl}/showing`, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getComingSoon(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPhim[]>(`${this.resourceUrl}/sap-chieu`, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  clearCache(): void {
    // No longer needed
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPhimIdentifier(phim: Pick<IPhim, 'id'>): number {
    return phim.id;
  }

  comparePhim(o1: Pick<IPhim, 'id'> | null, o2: Pick<IPhim, 'id'> | null): boolean {
    return o1 && o2 ? this.getPhimIdentifier(o1) === this.getPhimIdentifier(o2) : o1 === o2;
  }

  addPhimToCollectionIfMissing<Type extends Pick<IPhim, 'id'>>(
    phimCollection: Type[],
    ...phimsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const phims: Type[] = phimsToCheck.filter(isPresent);
    if (phims.length > 0) {
      const phimCollectionIdentifiers = phimCollection.map(phimItem => this.getPhimIdentifier(phimItem));
      const phimsToAdd = phims.filter(phimItem => {
        const phimIdentifier = this.getPhimIdentifier(phimItem);
        if (phimCollectionIdentifiers.includes(phimIdentifier)) {
          return false;
        }
        phimCollectionIdentifiers.push(phimIdentifier);
        return true;
      });
      return [...phimsToAdd, ...phimCollection];
    }
    return phimCollection;
  }

  protected convertDateFromClient<T extends IPhim | NewPhim | PartialUpdatePhim>(phim: T): RestOf<T> {
    return {
      ...phim,
      ngayKhoiChieu: phim.ngayKhoiChieu
        ? (dayjs.isDayjs(phim.ngayKhoiChieu) ? phim.ngayKhoiChieu : dayjs(phim.ngayKhoiChieu)).format(DATE_FORMAT)
        : null,
    };
  }

  protected convertDateFromServer(restPhim: RestPhim): IPhim {
    return {
      ...restPhim,
      ngayKhoiChieu: restPhim.ngayKhoiChieu ? dayjs(restPhim.ngayKhoiChieu) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestPhim>): HttpResponse<IPhim> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestPhim[]>): HttpResponse<IPhim[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
