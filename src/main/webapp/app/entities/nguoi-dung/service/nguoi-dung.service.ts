import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { INguoiDung, NewNguoiDung } from '../nguoi-dung.model';

export type PartialUpdateNguoiDung = Partial<INguoiDung> & Pick<INguoiDung, 'id'>;

export type EntityResponseType = HttpResponse<INguoiDung>;
export type EntityArrayResponseType = HttpResponse<INguoiDung[]>;

@Injectable({ providedIn: 'root' })
export class NguoiDungService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/nguoi-dungs');

  create(nguoiDung: NewNguoiDung): Observable<EntityResponseType> {
    return this.http.post<INguoiDung>(this.resourceUrl, nguoiDung, { observe: 'response' });
  }

  update(nguoiDung: INguoiDung): Observable<EntityResponseType> {
    return this.http.put<INguoiDung>(`${this.resourceUrl}/${this.getNguoiDungIdentifier(nguoiDung)}`, nguoiDung, { observe: 'response' });
  }

  partialUpdate(nguoiDung: PartialUpdateNguoiDung): Observable<EntityResponseType> {
    return this.http.patch<INguoiDung>(`${this.resourceUrl}/${this.getNguoiDungIdentifier(nguoiDung)}`, nguoiDung, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<INguoiDung>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<INguoiDung[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<INguoiDung[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getNguoiDungIdentifier(nguoiDung: Pick<INguoiDung, 'id'>): number {
    return nguoiDung.id;
  }

  compareNguoiDung(o1: Pick<INguoiDung, 'id'> | null, o2: Pick<INguoiDung, 'id'> | null): boolean {
    return o1 && o2 ? this.getNguoiDungIdentifier(o1) === this.getNguoiDungIdentifier(o2) : o1 === o2;
  }

  addNguoiDungToCollectionIfMissing<Type extends Pick<INguoiDung, 'id'>>(
    nguoiDungCollection: Type[],
    ...nguoiDungsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const nguoiDungs: Type[] = nguoiDungsToCheck.filter(isPresent);
    if (nguoiDungs.length > 0) {
      const nguoiDungCollectionIdentifiers = nguoiDungCollection.map(nguoiDungItem => this.getNguoiDungIdentifier(nguoiDungItem));
      const nguoiDungsToAdd = nguoiDungs.filter(nguoiDungItem => {
        const nguoiDungIdentifier = this.getNguoiDungIdentifier(nguoiDungItem);
        if (nguoiDungCollectionIdentifiers.includes(nguoiDungIdentifier)) {
          return false;
        }
        nguoiDungCollectionIdentifiers.push(nguoiDungIdentifier);
        return true;
      });
      return [...nguoiDungsToAdd, ...nguoiDungCollection];
    }
    return nguoiDungCollection;
  }
}
