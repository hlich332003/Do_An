import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IDichVuFB } from '../dich-vu-fb.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../dich-vu-fb.test-samples';

import { DichVuFBService } from './dich-vu-fb.service';

const requireRestSample: IDichVuFB = {
  ...sampleWithRequiredData,
};

describe('DichVuFB Service', () => {
  let service: DichVuFBService;
  let httpMock: HttpTestingController;
  let expectedResult: IDichVuFB | IDichVuFB[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(DichVuFBService);
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

    it('should create a DichVuFB', () => {
      const dichVuFB = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(dichVuFB).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a DichVuFB', () => {
      const dichVuFB = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(dichVuFB).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a DichVuFB', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of DichVuFB', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a DichVuFB', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addDichVuFBToCollectionIfMissing', () => {
      it('should add a DichVuFB to an empty array', () => {
        const dichVuFB: IDichVuFB = sampleWithRequiredData;
        expectedResult = service.addDichVuFBToCollectionIfMissing([], dichVuFB);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(dichVuFB);
      });

      it('should not add a DichVuFB to an array that contains it', () => {
        const dichVuFB: IDichVuFB = sampleWithRequiredData;
        const dichVuFBCollection: IDichVuFB[] = [
          {
            ...dichVuFB,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addDichVuFBToCollectionIfMissing(dichVuFBCollection, dichVuFB);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a DichVuFB to an array that doesn't contain it", () => {
        const dichVuFB: IDichVuFB = sampleWithRequiredData;
        const dichVuFBCollection: IDichVuFB[] = [sampleWithPartialData];
        expectedResult = service.addDichVuFBToCollectionIfMissing(dichVuFBCollection, dichVuFB);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(dichVuFB);
      });

      it('should add only unique DichVuFB to an array', () => {
        const dichVuFBArray: IDichVuFB[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const dichVuFBCollection: IDichVuFB[] = [sampleWithRequiredData];
        expectedResult = service.addDichVuFBToCollectionIfMissing(dichVuFBCollection, ...dichVuFBArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const dichVuFB: IDichVuFB = sampleWithRequiredData;
        const dichVuFB2: IDichVuFB = sampleWithPartialData;
        expectedResult = service.addDichVuFBToCollectionIfMissing([], dichVuFB, dichVuFB2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(dichVuFB);
        expect(expectedResult).toContain(dichVuFB2);
      });

      it('should accept null and undefined values', () => {
        const dichVuFB: IDichVuFB = sampleWithRequiredData;
        expectedResult = service.addDichVuFBToCollectionIfMissing([], null, dichVuFB, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(dichVuFB);
      });

      it('should return initial array if no DichVuFB is added', () => {
        const dichVuFBCollection: IDichVuFB[] = [sampleWithRequiredData];
        expectedResult = service.addDichVuFBToCollectionIfMissing(dichVuFBCollection, undefined, null);
        expect(expectedResult).toEqual(dichVuFBCollection);
      });
    });

    describe('compareDichVuFB', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareDichVuFB(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 18926 };
        const entity2 = null;

        const compareResult1 = service.compareDichVuFB(entity1, entity2);
        const compareResult2 = service.compareDichVuFB(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 18926 };
        const entity2 = { id: 13386 };

        const compareResult1 = service.compareDichVuFB(entity1, entity2);
        const compareResult2 = service.compareDichVuFB(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 18926 };
        const entity2 = { id: 18926 };

        const compareResult1 = service.compareDichVuFB(entity1, entity2);
        const compareResult2 = service.compareDichVuFB(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
