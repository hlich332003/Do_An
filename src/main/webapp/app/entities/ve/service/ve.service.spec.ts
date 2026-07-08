import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IVe } from '../ve.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../ve.test-samples';

import { VeService } from './ve.service';

const requireRestSample: IVe = {
  ...sampleWithRequiredData,
};

describe('Ve Service', () => {
  let service: VeService;
  let httpMock: HttpTestingController;
  let expectedResult: IVe | IVe[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(VeService);
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

    it('should create a Ve', () => {
      const ve = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(ve).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Ve', () => {
      const ve = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(ve).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Ve', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Ve', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Ve', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addVeToCollectionIfMissing', () => {
      it('should add a Ve to an empty array', () => {
        const ve: IVe = sampleWithRequiredData;
        expectedResult = service.addVeToCollectionIfMissing([], ve);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(ve);
      });

      it('should not add a Ve to an array that contains it', () => {
        const ve: IVe = sampleWithRequiredData;
        const veCollection: IVe[] = [
          {
            ...ve,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addVeToCollectionIfMissing(veCollection, ve);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Ve to an array that doesn't contain it", () => {
        const ve: IVe = sampleWithRequiredData;
        const veCollection: IVe[] = [sampleWithPartialData];
        expectedResult = service.addVeToCollectionIfMissing(veCollection, ve);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ve);
      });

      it('should add only unique Ve to an array', () => {
        const veArray: IVe[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const veCollection: IVe[] = [sampleWithRequiredData];
        expectedResult = service.addVeToCollectionIfMissing(veCollection, ...veArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const ve: IVe = sampleWithRequiredData;
        const ve2: IVe = sampleWithPartialData;
        expectedResult = service.addVeToCollectionIfMissing([], ve, ve2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ve);
        expect(expectedResult).toContain(ve2);
      });

      it('should accept null and undefined values', () => {
        const ve: IVe = sampleWithRequiredData;
        expectedResult = service.addVeToCollectionIfMissing([], null, ve, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(ve);
      });

      it('should return initial array if no Ve is added', () => {
        const veCollection: IVe[] = [sampleWithRequiredData];
        expectedResult = service.addVeToCollectionIfMissing(veCollection, undefined, null);
        expect(expectedResult).toEqual(veCollection);
      });
    });

    describe('compareVe', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareVe(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 30679 };
        const entity2 = null;

        const compareResult1 = service.compareVe(entity1, entity2);
        const compareResult2 = service.compareVe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 30679 };
        const entity2 = { id: 25849 };

        const compareResult1 = service.compareVe(entity1, entity2);
        const compareResult2 = service.compareVe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 30679 };
        const entity2 = { id: 30679 };

        const compareResult1 = service.compareVe(entity1, entity2);
        const compareResult2 = service.compareVe(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
