import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IChiTietFB } from '../chi-tiet-fb.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../chi-tiet-fb.test-samples';

import { ChiTietFBService } from './chi-tiet-fb.service';

const requireRestSample: IChiTietFB = {
  ...sampleWithRequiredData,
};

describe('ChiTietFB Service', () => {
  let service: ChiTietFBService;
  let httpMock: HttpTestingController;
  let expectedResult: IChiTietFB | IChiTietFB[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ChiTietFBService);
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

    it('should create a ChiTietFB', () => {
      const chiTietFB = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(chiTietFB).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ChiTietFB', () => {
      const chiTietFB = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(chiTietFB).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ChiTietFB', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ChiTietFB', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ChiTietFB', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addChiTietFBToCollectionIfMissing', () => {
      it('should add a ChiTietFB to an empty array', () => {
        const chiTietFB: IChiTietFB = sampleWithRequiredData;
        expectedResult = service.addChiTietFBToCollectionIfMissing([], chiTietFB);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(chiTietFB);
      });

      it('should not add a ChiTietFB to an array that contains it', () => {
        const chiTietFB: IChiTietFB = sampleWithRequiredData;
        const chiTietFBCollection: IChiTietFB[] = [
          {
            ...chiTietFB,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addChiTietFBToCollectionIfMissing(chiTietFBCollection, chiTietFB);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ChiTietFB to an array that doesn't contain it", () => {
        const chiTietFB: IChiTietFB = sampleWithRequiredData;
        const chiTietFBCollection: IChiTietFB[] = [sampleWithPartialData];
        expectedResult = service.addChiTietFBToCollectionIfMissing(chiTietFBCollection, chiTietFB);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(chiTietFB);
      });

      it('should add only unique ChiTietFB to an array', () => {
        const chiTietFBArray: IChiTietFB[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const chiTietFBCollection: IChiTietFB[] = [sampleWithRequiredData];
        expectedResult = service.addChiTietFBToCollectionIfMissing(chiTietFBCollection, ...chiTietFBArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const chiTietFB: IChiTietFB = sampleWithRequiredData;
        const chiTietFB2: IChiTietFB = sampleWithPartialData;
        expectedResult = service.addChiTietFBToCollectionIfMissing([], chiTietFB, chiTietFB2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(chiTietFB);
        expect(expectedResult).toContain(chiTietFB2);
      });

      it('should accept null and undefined values', () => {
        const chiTietFB: IChiTietFB = sampleWithRequiredData;
        expectedResult = service.addChiTietFBToCollectionIfMissing([], null, chiTietFB, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(chiTietFB);
      });

      it('should return initial array if no ChiTietFB is added', () => {
        const chiTietFBCollection: IChiTietFB[] = [sampleWithRequiredData];
        expectedResult = service.addChiTietFBToCollectionIfMissing(chiTietFBCollection, undefined, null);
        expect(expectedResult).toEqual(chiTietFBCollection);
      });
    });

    describe('compareChiTietFB', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareChiTietFB(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 585 };
        const entity2 = null;

        const compareResult1 = service.compareChiTietFB(entity1, entity2);
        const compareResult2 = service.compareChiTietFB(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 585 };
        const entity2 = { id: 19585 };

        const compareResult1 = service.compareChiTietFB(entity1, entity2);
        const compareResult2 = service.compareChiTietFB(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 585 };
        const entity2 = { id: 585 };

        const compareResult1 = service.compareChiTietFB(entity1, entity2);
        const compareResult2 = service.compareChiTietFB(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
