import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IGhe } from '../ghe.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../ghe.test-samples';

import { GheService } from './ghe.service';

const requireRestSample: IGhe = {
  ...sampleWithRequiredData,
};

describe('Ghe Service', () => {
  let service: GheService;
  let httpMock: HttpTestingController;
  let expectedResult: IGhe | IGhe[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(GheService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Ghe', () => {
      const ghe = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(ghe).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Ghe', () => {
      const ghe = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(ghe).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Ghe', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Ghe', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Ghe', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addGheToCollectionIfMissing', () => {
      it('should add a Ghe to an empty array', () => {
        const ghe: IGhe = sampleWithRequiredData;
        expectedResult = service.addGheToCollectionIfMissing([], ghe);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(ghe);
      });

      it('should not add a Ghe to an array that contains it', () => {
        const ghe: IGhe = sampleWithRequiredData;
        const gheCollection: IGhe[] = [
          {
            ...ghe,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addGheToCollectionIfMissing(gheCollection, ghe);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Ghe to an array that doesn't contain it", () => {
        const ghe: IGhe = sampleWithRequiredData;
        const gheCollection: IGhe[] = [sampleWithPartialData];
        expectedResult = service.addGheToCollectionIfMissing(gheCollection, ghe);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ghe);
      });

      it('should add only unique Ghe to an array', () => {
        const gheArray: IGhe[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const gheCollection: IGhe[] = [sampleWithRequiredData];
        expectedResult = service.addGheToCollectionIfMissing(gheCollection, ...gheArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const ghe: IGhe = sampleWithRequiredData;
        const ghe2: IGhe = sampleWithPartialData;
        expectedResult = service.addGheToCollectionIfMissing([], ghe, ghe2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ghe);
        expect(expectedResult).toContain(ghe2);
      });

      it('should accept null and undefined values', () => {
        const ghe: IGhe = sampleWithRequiredData;
        expectedResult = service.addGheToCollectionIfMissing([], null, ghe, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(ghe);
      });

      it('should return initial array if no Ghe is added', () => {
        const gheCollection: IGhe[] = [sampleWithRequiredData];
        expectedResult = service.addGheToCollectionIfMissing(gheCollection, undefined, null);
        expect(expectedResult).toEqual(gheCollection);
      });
    });

    describe('compareGhe', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareGhe(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 14590 };
        const entity2 = null;

        const compareResult1 = service.compareGhe(entity1, entity2);
        const compareResult2 = service.compareGhe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 14590 };
        const entity2 = { id: 7029 };

        const compareResult1 = service.compareGhe(entity1, entity2);
        const compareResult2 = service.compareGhe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 14590 };
        const entity2 = { id: 14590 };

        const compareResult1 = service.compareGhe(entity1, entity2);
        const compareResult2 = service.compareGhe(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
