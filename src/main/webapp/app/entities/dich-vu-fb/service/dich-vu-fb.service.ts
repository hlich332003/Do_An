import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IDichVuFB, NewDichVuFB } from '../dich-vu-fb.model';

export type PartialUpdateDichVuFB = Partial<IDichVuFB> & Pick<IDichVuFB, 'id'>;

export type EntityResponseType = HttpResponse<IDichVuFB>;
export type EntityArrayResponseType = HttpResponse<IDichVuFB[]>;

@Injectable({ providedIn: 'root' })
export class DichVuFBService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/dich-vu-fbs');

  create(dichVuFB: NewDichVuFB): Observable<EntityResponseType> {
    return this.http.post<IDichVuFB>(this.resourceUrl, dichVuFB, { observe: 'response' });
  }

  update(dichVuFB: IDichVuFB): Observable<EntityResponseType> {
    return this.http.put<IDichVuFB>(`${this.resourceUrl}/${this.getDichVuFBIdentifier(dichVuFB)}`, dichVuFB, { observe: 'response' });
  }

  partialUpdate(dichVuFB: PartialUpdateDichVuFB): Observable<EntityResponseType> {
    return this.http.patch<IDichVuFB>(`${this.resourceUrl}/${this.getDichVuFBIdentifier(dichVuFB)}`, dichVuFB, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IDichVuFB>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDichVuFB[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDichVuFB[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  getActive(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDichVuFB[]>(`${this.resourceUrl}/active`, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getDichVuFBIdentifier(dichVuFB: Pick<IDichVuFB, 'id'>): number {
    return dichVuFB.id;
  }

  compareDichVuFB(o1: Pick<IDichVuFB, 'id'> | null, o2: Pick<IDichVuFB, 'id'> | null): boolean {
    return o1 && o2 ? this.getDichVuFBIdentifier(o1) === this.getDichVuFBIdentifier(o2) : o1 === o2;
  }

  addDichVuFBToCollectionIfMissing<Type extends Pick<IDichVuFB, 'id'>>(
    dichVuFBCollection: Type[],
    ...dichVuFBSToCheck: (Type | null | undefined)[]
  ): Type[] {
    const dichVuFBS: Type[] = dichVuFBSToCheck.filter(isPresent);
    if (dichVuFBS.length > 0) {
      const dichVuFBCollectionIdentifiers = dichVuFBCollection.map(dichVuFBItem => this.getDichVuFBIdentifier(dichVuFBItem));
      const dichVuFBSToAdd = dichVuFBS.filter(dichVuFBItem => {
        const dichVuFBIdentifier = this.getDichVuFBIdentifier(dichVuFBItem);
        if (dichVuFBCollectionIdentifiers.includes(dichVuFBIdentifier)) {
          return false;
        }
        dichVuFBCollectionIdentifiers.push(dichVuFBIdentifier);
        return true;
      });
      return [...dichVuFBSToAdd, ...dichVuFBCollection];
    }
    return dichVuFBCollection;
  }
}
