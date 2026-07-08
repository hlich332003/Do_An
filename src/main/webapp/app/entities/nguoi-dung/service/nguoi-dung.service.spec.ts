import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { INguoiDung } from '../nguoi-dung.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../nguoi-dung.test-samples';

import { NguoiDungService } from './nguoi-dung.service';

const requireRestSample: INguoiDung = {
  ...sampleWithRequiredData,
};

describe('NguoiDung Service', () => {
  let service: NguoiDungService;
  let httpMock: HttpTestingController;
  let expectedResult: INguoiDung | INguoiDung[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(NguoiDungService);
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

    it('should create a NguoiDung', () => {
      const nguoiDung = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(nguoiDung).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a NguoiDung', () => {
      const nguoiDung = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(nguoiDung).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a NguoiDung', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of NguoiDung', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a NguoiDung', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addNguoiDungToCollectionIfMissing', () => {
      it('should add a NguoiDung to an empty array', () => {
        const nguoiDung: INguoiDung = sampleWithRequiredData;
        expectedResult = service.addNguoiDungToCollectionIfMissing([], nguoiDung);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(nguoiDung);
      });

      it('should not add a NguoiDung to an array that contains it', () => {
        const nguoiDung: INguoiDung = sampleWithRequiredData;
        const nguoiDungCollection: INguoiDung[] = [
          {
            ...nguoiDung,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addNguoiDungToCollectionIfMissing(nguoiDungCollection, nguoiDung);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a NguoiDung to an array that doesn't contain it", () => {
        const nguoiDung: INguoiDung = sampleWithRequiredData;
        const nguoiDungCollection: INguoiDung[] = [sampleWithPartialData];
        expectedResult = service.addNguoiDungToCollectionIfMissing(nguoiDungCollection, nguoiDung);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(nguoiDung);
      });

      it('should add only unique NguoiDung to an array', () => {
        const nguoiDungArray: INguoiDung[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const nguoiDungCollection: INguoiDung[] = [sampleWithRequiredData];
        expectedResult = service.addNguoiDungToCollectionIfMissing(nguoiDungCollection, ...nguoiDungArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const nguoiDung: INguoiDung = sampleWithRequiredData;
        const nguoiDung2: INguoiDung = sampleWithPartialData;
        expectedResult = service.addNguoiDungToCollectionIfMissing([], nguoiDung, nguoiDung2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(nguoiDung);
        expect(expectedResult).toContain(nguoiDung2);
      });

      it('should accept null and undefined values', () => {
        const nguoiDung: INguoiDung = sampleWithRequiredData;
        expectedResult = service.addNguoiDungToCollectionIfMissing([], null, nguoiDung, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(nguoiDung);
      });

      it('should return initial array if no NguoiDung is added', () => {
        const nguoiDungCollection: INguoiDung[] = [sampleWithRequiredData];
        expectedResult = service.addNguoiDungToCollectionIfMissing(nguoiDungCollection, undefined, null);
        expect(expectedResult).toEqual(nguoiDungCollection);
      });
    });

    describe('compareNguoiDung', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareNguoiDung(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 27009 };
        const entity2 = null;

        const compareResult1 = service.compareNguoiDung(entity1, entity2);
        const compareResult2 = service.compareNguoiDung(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 27009 };
        const entity2 = { id: 29205 };

        const compareResult1 = service.compareNguoiDung(entity1, entity2);
        const compareResult2 = service.compareNguoiDung(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 27009 };
        const entity2 = { id: 27009 };

        const compareResult1 = service.compareNguoiDung(entity1, entity2);
        const compareResult2 = service.compareNguoiDung(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
