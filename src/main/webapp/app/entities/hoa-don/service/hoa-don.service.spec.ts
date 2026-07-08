import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IHoaDon } from '../hoa-don.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../hoa-don.test-samples';

import { HoaDonService, RestHoaDon } from './hoa-don.service';

const requireRestSample: RestHoaDon = {
  ...sampleWithRequiredData,
  ngayTao: sampleWithRequiredData.ngayTao?.toJSON(),
  ngayThanhToan: sampleWithRequiredData.ngayThanhToan?.toJSON(),
};

describe('HoaDon Service', () => {
  let service: HoaDonService;
  let httpMock: HttpTestingController;
  let expectedResult: IHoaDon | IHoaDon[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(HoaDonService);
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

    it('should create a HoaDon', () => {
      const hoaDon = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(hoaDon).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a HoaDon', () => {
      const hoaDon = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(hoaDon).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a HoaDon', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of HoaDon', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a HoaDon', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addHoaDonToCollectionIfMissing', () => {
      it('should add a HoaDon to an empty array', () => {
        const hoaDon: IHoaDon = sampleWithRequiredData;
        expectedResult = service.addHoaDonToCollectionIfMissing([], hoaDon);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(hoaDon);
      });

      it('should not add a HoaDon to an array that contains it', () => {
        const hoaDon: IHoaDon = sampleWithRequiredData;
        const hoaDonCollection: IHoaDon[] = [
          {
            ...hoaDon,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addHoaDonToCollectionIfMissing(hoaDonCollection, hoaDon);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a HoaDon to an array that doesn't contain it", () => {
        const hoaDon: IHoaDon = sampleWithRequiredData;
        const hoaDonCollection: IHoaDon[] = [sampleWithPartialData];
        expectedResult = service.addHoaDonToCollectionIfMissing(hoaDonCollection, hoaDon);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(hoaDon);
      });

      it('should add only unique HoaDon to an array', () => {
        const hoaDonArray: IHoaDon[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const hoaDonCollection: IHoaDon[] = [sampleWithRequiredData];
        expectedResult = service.addHoaDonToCollectionIfMissing(hoaDonCollection, ...hoaDonArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const hoaDon: IHoaDon = sampleWithRequiredData;
        const hoaDon2: IHoaDon = sampleWithPartialData;
        expectedResult = service.addHoaDonToCollectionIfMissing([], hoaDon, hoaDon2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(hoaDon);
        expect(expectedResult).toContain(hoaDon2);
      });

      it('should accept null and undefined values', () => {
        const hoaDon: IHoaDon = sampleWithRequiredData;
        expectedResult = service.addHoaDonToCollectionIfMissing([], null, hoaDon, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(hoaDon);
      });

      it('should return initial array if no HoaDon is added', () => {
        const hoaDonCollection: IHoaDon[] = [sampleWithRequiredData];
        expectedResult = service.addHoaDonToCollectionIfMissing(hoaDonCollection, undefined, null);
        expect(expectedResult).toEqual(hoaDonCollection);
      });
    });

    describe('compareHoaDon', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareHoaDon(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 24513 };
        const entity2 = null;

        const compareResult1 = service.compareHoaDon(entity1, entity2);
        const compareResult2 = service.compareHoaDon(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 24513 };
        const entity2 = { id: 31365 };

        const compareResult1 = service.compareHoaDon(entity1, entity2);
        const compareResult2 = service.compareHoaDon(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 24513 };
        const entity2 = { id: 24513 };

        const compareResult1 = service.compareHoaDon(entity1, entity2);
        const compareResult2 = service.compareHoaDon(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
