import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IVe, NewVe } from '../ve.model';

export type PartialUpdateVe = Partial<IVe> & Pick<IVe, 'id'>;

export type EntityResponseType = HttpResponse<IVe>;
export type EntityArrayResponseType = HttpResponse<IVe[]>;

@Injectable({ providedIn: 'root' })
export class VeService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/ves');

  create(ve: NewVe): Observable<EntityResponseType> {
    return this.http.post<IVe>(this.resourceUrl, ve, { observe: 'response' });
  }

  update(ve: IVe): Observable<EntityResponseType> {
    return this.http.put<IVe>(`${this.resourceUrl}/${this.getVeIdentifier(ve)}`, ve, { observe: 'response' });
  }

  partialUpdate(ve: PartialUpdateVe): Observable<EntityResponseType> {
    return this.http.patch<IVe>(`${this.resourceUrl}/${this.getVeIdentifier(ve)}`, ve, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVe>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVe[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVe[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getVeIdentifier(ve: Pick<IVe, 'id'>): number {
    return ve.id;
  }

  compareVe(o1: Pick<IVe, 'id'> | null, o2: Pick<IVe, 'id'> | null): boolean {
    return o1 && o2 ? this.getVeIdentifier(o1) === this.getVeIdentifier(o2) : o1 === o2;
  }

  addVeToCollectionIfMissing<Type extends Pick<IVe, 'id'>>(veCollection: Type[], ...vesToCheck: (Type | null | undefined)[]): Type[] {
    const ves: Type[] = vesToCheck.filter(isPresent);
    if (ves.length > 0) {
      const veCollectionIdentifiers = veCollection.map(veItem => this.getVeIdentifier(veItem));
      const vesToAdd = ves.filter(veItem => {
        const veIdentifier = this.getVeIdentifier(veItem);
        if (veCollectionIdentifiers.includes(veIdentifier)) {
          return false;
        }
        veCollectionIdentifiers.push(veIdentifier);
        return true;
      });
      return [...vesToAdd, ...veCollection];
    }
    return veCollection;
  }
}
