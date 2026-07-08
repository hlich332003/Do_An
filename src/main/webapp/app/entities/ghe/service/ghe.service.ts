import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IGhe, NewGhe } from '../ghe.model';

export type PartialUpdateGhe = Partial<IGhe> & Pick<IGhe, 'id'>;

export type EntityResponseType = HttpResponse<IGhe>;
export type EntityArrayResponseType = HttpResponse<IGhe[]>;

@Injectable({ providedIn: 'root' })
export class GheService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/ghes');

  create(ghe: NewGhe): Observable<EntityResponseType> {
    return this.http.post<IGhe>(this.resourceUrl, ghe, { observe: 'response' });
  }

  update(ghe: IGhe): Observable<EntityResponseType> {
    return this.http.put<IGhe>(`${this.resourceUrl}/${this.getGheIdentifier(ghe)}`, ghe, { observe: 'response' });
  }

  partialUpdate(ghe: PartialUpdateGhe): Observable<EntityResponseType> {
    return this.http.patch<IGhe>(`${this.resourceUrl}/${this.getGheIdentifier(ghe)}`, ghe, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IGhe>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGhe[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGhe[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  getSeatsForShowtime(suatChieuId: number, phongChieuId: number): Observable<HttpResponse<any[]>> {
    return this.http.get<any[]>(`${this.resourceUrl}/so-do/${suatChieuId}/${phongChieuId}`, { observe: 'response' });
  }

  getSeatsByPhongChieu(phongChieuId: number): Observable<HttpResponse<IGhe[]>> {
    return this.http.get<IGhe[]>(`${this.resourceUrl}/phong-chieu/${phongChieuId}`, { observe: 'response' });
  }

  generateSeatsByPhongChieu(phongChieuId: number): Observable<HttpResponse<IGhe[]>> {
    return this.http.post<IGhe[]>(`${this.resourceUrl}/phong-chieu/${phongChieuId}/generate`, {}, { observe: 'response' });
  }

  updateBatchSeats(phongChieuId: number, ghes: IGhe[]): Observable<HttpResponse<{}>> {
    return this.http.put(`${this.resourceUrl}/phong-chieu/${phongChieuId}/batch`, ghes, { observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getGheIdentifier(ghe: Pick<IGhe, 'id'>): number {
    return ghe.id;
  }

  compareGhe(o1: Pick<IGhe, 'id'> | null, o2: Pick<IGhe, 'id'> | null): boolean {
    return o1 && o2 ? this.getGheIdentifier(o1) === this.getGheIdentifier(o2) : o1 === o2;
  }

  addGheToCollectionIfMissing<Type extends Pick<IGhe, 'id'>>(gheCollection: Type[], ...ghesToCheck: (Type | null | undefined)[]): Type[] {
    const ghes: Type[] = ghesToCheck.filter(isPresent);
    if (ghes.length > 0) {
      const gheCollectionIdentifiers = gheCollection.map(gheItem => this.getGheIdentifier(gheItem));
      const ghesToAdd = ghes.filter(gheItem => {
        const gheIdentifier = this.getGheIdentifier(gheItem);
        if (gheCollectionIdentifiers.includes(gheIdentifier)) {
          return false;
        }
        gheCollectionIdentifiers.push(gheIdentifier);
        return true;
      });
      return [...ghesToAdd, ...gheCollection];
    }
    return gheCollection;
  }
}
