import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IChiTietFB, NewChiTietFB } from '../chi-tiet-fb.model';

export type PartialUpdateChiTietFB = Partial<IChiTietFB> & Pick<IChiTietFB, 'id'>;

export type EntityResponseType = HttpResponse<IChiTietFB>;
export type EntityArrayResponseType = HttpResponse<IChiTietFB[]>;

@Injectable({ providedIn: 'root' })
export class ChiTietFBService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/chi-tiet-fbs');

  create(chiTietFB: NewChiTietFB): Observable<EntityResponseType> {
    return this.http.post<IChiTietFB>(this.resourceUrl, chiTietFB, { observe: 'response' });
  }

  update(chiTietFB: IChiTietFB): Observable<EntityResponseType> {
    return this.http.put<IChiTietFB>(`${this.resourceUrl}/${this.getChiTietFBIdentifier(chiTietFB)}`, chiTietFB, { observe: 'response' });
  }

  partialUpdate(chiTietFB: PartialUpdateChiTietFB): Observable<EntityResponseType> {
    return this.http.patch<IChiTietFB>(`${this.resourceUrl}/${this.getChiTietFBIdentifier(chiTietFB)}`, chiTietFB, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IChiTietFB>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IChiTietFB[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IChiTietFB[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getChiTietFBIdentifier(chiTietFB: Pick<IChiTietFB, 'id'>): number {
    return chiTietFB.id;
  }

  compareChiTietFB(o1: Pick<IChiTietFB, 'id'> | null, o2: Pick<IChiTietFB, 'id'> | null): boolean {
    return o1 && o2 ? this.getChiTietFBIdentifier(o1) === this.getChiTietFBIdentifier(o2) : o1 === o2;
  }

  addChiTietFBToCollectionIfMissing<Type extends Pick<IChiTietFB, 'id'>>(
    chiTietFBCollection: Type[],
    ...chiTietFBSToCheck: (Type | null | undefined)[]
  ): Type[] {
    const chiTietFBS: Type[] = chiTietFBSToCheck.filter(isPresent);
    if (chiTietFBS.length > 0) {
      const chiTietFBCollectionIdentifiers = chiTietFBCollection.map(chiTietFBItem => this.getChiTietFBIdentifier(chiTietFBItem));
      const chiTietFBSToAdd = chiTietFBS.filter(chiTietFBItem => {
        const chiTietFBIdentifier = this.getChiTietFBIdentifier(chiTietFBItem);
        if (chiTietFBCollectionIdentifiers.includes(chiTietFBIdentifier)) {
          return false;
        }
        chiTietFBCollectionIdentifiers.push(chiTietFBIdentifier);
        return true;
      });
      return [...chiTietFBSToAdd, ...chiTietFBCollection];
    }
    return chiTietFBCollection;
  }
}
