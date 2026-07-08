import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPhongChieu, NewPhongChieu } from '../phong-chieu.model';

export type PartialUpdatePhongChieu = Partial<IPhongChieu> & Pick<IPhongChieu, 'id'>;

export type EntityResponseType = HttpResponse<IPhongChieu>;
export type EntityArrayResponseType = HttpResponse<IPhongChieu[]>;

@Injectable({ providedIn: 'root' })
export class PhongChieuService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/phong-chieus');

  create(phongChieu: NewPhongChieu): Observable<EntityResponseType> {
    return this.http.post<IPhongChieu>(this.resourceUrl, phongChieu, { observe: 'response' });
  }

  update(phongChieu: IPhongChieu): Observable<EntityResponseType> {
    return this.http.put<IPhongChieu>(`${this.resourceUrl}/${this.getPhongChieuIdentifier(phongChieu)}`, phongChieu, {
      observe: 'response',
    });
  }

  partialUpdate(phongChieu: PartialUpdatePhongChieu): Observable<EntityResponseType> {
    return this.http.patch<IPhongChieu>(`${this.resourceUrl}/${this.getPhongChieuIdentifier(phongChieu)}`, phongChieu, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPhongChieu>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPhongChieu[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPhongChieu[]>(`${this.resourceUrl}/search`, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPhongChieuIdentifier(phongChieu: Pick<IPhongChieu, 'id'>): number {
    return phongChieu.id;
  }

  comparePhongChieu(o1: Pick<IPhongChieu, 'id'> | null, o2: Pick<IPhongChieu, 'id'> | null): boolean {
    return o1 && o2 ? this.getPhongChieuIdentifier(o1) === this.getPhongChieuIdentifier(o2) : o1 === o2;
  }

  addPhongChieuToCollectionIfMissing<Type extends Pick<IPhongChieu, 'id'>>(
    phongChieuCollection: Type[],
    ...phongChieusToCheck: (Type | null | undefined)[]
  ): Type[] {
    const phongChieus: Type[] = phongChieusToCheck.filter(isPresent);
    if (phongChieus.length > 0) {
      const phongChieuCollectionIdentifiers = phongChieuCollection.map(phongChieuItem => this.getPhongChieuIdentifier(phongChieuItem));
      const phongChieusToAdd = phongChieus.filter(phongChieuItem => {
        const phongChieuIdentifier = this.getPhongChieuIdentifier(phongChieuItem);
        if (phongChieuCollectionIdentifiers.includes(phongChieuIdentifier)) {
          return false;
        }
        phongChieuCollectionIdentifiers.push(phongChieuIdentifier);
        return true;
      });
      return [...phongChieusToAdd, ...phongChieuCollection];
    }
    return phongChieuCollection;
  }
}
