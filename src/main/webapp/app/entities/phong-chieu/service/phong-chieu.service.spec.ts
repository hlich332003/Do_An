import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IPhongChieu } from '../phong-chieu.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../phong-chieu.test-samples';

import { PhongChieuService } from './phong-chieu.service';

const requireRestSample: IPhongChieu = {
  ...sampleWithRequiredData,
};

describe('PhongChieu Service', () => {
  let service: PhongChieuService;
  let httpMock: HttpTestingController;
  let expectedResult: IPhongChieu | IPhongChieu[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(PhongChieuService);
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

    it('should create a PhongChieu', () => {
      const phongChieu = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(phongChieu).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a PhongChieu', () => {
      const phongChieu = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(phongChieu).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a PhongChieu', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of PhongChieu', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a PhongChieu', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addPhongChieuToCollectionIfMissing', () => {
      it('should add a PhongChieu to an empty array', () => {
        const phongChieu: IPhongChieu = sampleWithRequiredData;
        expectedResult = service.addPhongChieuToCollectionIfMissing([], phongChieu);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(phongChieu);
      });

      it('should not add a PhongChieu to an array that contains it', () => {
        const phongChieu: IPhongChieu = sampleWithRequiredData;
        const phongChieuCollection: IPhongChieu[] = [
          {
            ...phongChieu,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPhongChieuToCollectionIfMissing(phongChieuCollection, phongChieu);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a PhongChieu to an array that doesn't contain it", () => {
        const phongChieu: IPhongChieu = sampleWithRequiredData;
        const phongChieuCollection: IPhongChieu[] = [sampleWithPartialData];
        expectedResult = service.addPhongChieuToCollectionIfMissing(phongChieuCollection, phongChieu);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(phongChieu);
      });

      it('should add only unique PhongChieu to an array', () => {
        const phongChieuArray: IPhongChieu[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const phongChieuCollection: IPhongChieu[] = [sampleWithRequiredData];
        expectedResult = service.addPhongChieuToCollectionIfMissing(phongChieuCollection, ...phongChieuArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const phongChieu: IPhongChieu = sampleWithRequiredData;
        const phongChieu2: IPhongChieu = sampleWithPartialData;
        expectedResult = service.addPhongChieuToCollectionIfMissing([], phongChieu, phongChieu2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(phongChieu);
        expect(expectedResult).toContain(phongChieu2);
      });

      it('should accept null and undefined values', () => {
        const phongChieu: IPhongChieu = sampleWithRequiredData;
        expectedResult = service.addPhongChieuToCollectionIfMissing([], null, phongChieu, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(phongChieu);
      });

      it('should return initial array if no PhongChieu is added', () => {
        const phongChieuCollection: IPhongChieu[] = [sampleWithRequiredData];
        expectedResult = service.addPhongChieuToCollectionIfMissing(phongChieuCollection, undefined, null);
        expect(expectedResult).toEqual(phongChieuCollection);
      });
    });

    describe('comparePhongChieu', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePhongChieu(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 26571 };
        const entity2 = null;

        const compareResult1 = service.comparePhongChieu(entity1, entity2);
        const compareResult2 = service.comparePhongChieu(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 26571 };
        const entity2 = { id: 14106 };

        const compareResult1 = service.comparePhongChieu(entity1, entity2);
        const compareResult2 = service.comparePhongChieu(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 26571 };
        const entity2 = { id: 26571 };

        const compareResult1 = service.comparePhongChieu(entity1, entity2);
        const compareResult2 = service.comparePhongChieu(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
