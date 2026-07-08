import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISuatChieu, NewSuatChieu } from '../suat-chieu.model';

export type PartialUpdateSuatChieu = Partial<ISuatChieu> & Pick<ISuatChieu, 'id'>;

type RestOf<T extends ISuatChieu | NewSuatChieu> = Omit<T, 'thoiGianBatDau' | 'thoiGianKetThuc'> & {
  thoiGianBatDau?: string | null;
  thoiGianKetThuc?: string | null;
};

export type RestSuatChieu = RestOf<ISuatChieu>;

export type NewRestSuatChieu = RestOf<NewSuatChieu>;

export type PartialUpdateRestSuatChieu = RestOf<PartialUpdateSuatChieu>;

export type EntityResponseType = HttpResponse<ISuatChieu>;
export type EntityArrayResponseType = HttpResponse<ISuatChieu[]>;

@Injectable({ providedIn: 'root' })
export class SuatChieuService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/suat-chieus');

  create(suatChieu: NewSuatChieu): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(suatChieu);
    return this.http
      .post<RestSuatChieu>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(suatChieu: ISuatChieu): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(suatChieu);
    return this.http
      .put<RestSuatChieu>(`${this.resourceUrl}/${this.getSuatChieuIdentifier(suatChieu)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(suatChieu: PartialUpdateSuatChieu): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(suatChieu);
    return this.http
      .patch<RestSuatChieu>(`${this.resourceUrl}/${this.getSuatChieuIdentifier(suatChieu)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestSuatChieu>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestSuatChieu[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestSuatChieu[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  cleanupOverlaps(): Observable<HttpResponse<{}>> {
    return this.http.post(`${this.resourceUrl}/cleanup-overlaps`, {}, { observe: 'response' });
  }

  getSuatChieuIdentifier(suatChieu: Pick<ISuatChieu, 'id'>): number {
    return suatChieu.id;
  }

  compareSuatChieu(o1: Pick<ISuatChieu, 'id'> | null, o2: Pick<ISuatChieu, 'id'> | null): boolean {
    return o1 && o2 ? this.getSuatChieuIdentifier(o1) === this.getSuatChieuIdentifier(o2) : o1 === o2;
  }

  addSuatChieuToCollectionIfMissing<Type extends Pick<ISuatChieu, 'id'>>(
    suatChieuCollection: Type[],
    ...suatChieusToCheck: (Type | null | undefined)[]
  ): Type[] {
    const suatChieus: Type[] = suatChieusToCheck.filter(isPresent);
    if (suatChieus.length > 0) {
      const suatChieuCollectionIdentifiers = suatChieuCollection.map(suatChieuItem => this.getSuatChieuIdentifier(suatChieuItem));
      const suatChieusToAdd = suatChieus.filter(suatChieuItem => {
        const suatChieuIdentifier = this.getSuatChieuIdentifier(suatChieuItem);
        if (suatChieuCollectionIdentifiers.includes(suatChieuIdentifier)) {
          return false;
        }
        suatChieuCollectionIdentifiers.push(suatChieuIdentifier);
        return true;
      });
      return [...suatChieusToAdd, ...suatChieuCollection];
    }
    return suatChieuCollection;
  }

  protected convertDateFromClient<T extends ISuatChieu | NewSuatChieu | PartialUpdateSuatChieu>(suatChieu: T): RestOf<T> {
    return {
      ...suatChieu,
      thoiGianBatDau: suatChieu.thoiGianBatDau?.toJSON() ?? null,
      thoiGianKetThuc: suatChieu.thoiGianKetThuc?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restSuatChieu: RestSuatChieu): ISuatChieu {
    return {
      ...restSuatChieu,
      thoiGianBatDau: restSuatChieu.thoiGianBatDau ? dayjs(restSuatChieu.thoiGianBatDau) : undefined,
      thoiGianKetThuc: restSuatChieu.thoiGianKetThuc ? dayjs(restSuatChieu.thoiGianKetThuc) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestSuatChieu>): HttpResponse<ISuatChieu> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestSuatChieu[]>): HttpResponse<ISuatChieu[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
