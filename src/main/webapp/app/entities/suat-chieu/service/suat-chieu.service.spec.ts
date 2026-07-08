import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ISuatChieu } from '../suat-chieu.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../suat-chieu.test-samples';

import { RestSuatChieu, SuatChieuService } from './suat-chieu.service';

const requireRestSample: RestSuatChieu = {
  ...sampleWithRequiredData,
  thoiGianBatDau: sampleWithRequiredData.thoiGianBatDau?.toJSON(),
  thoiGianKetThuc: sampleWithRequiredData.thoiGianKetThuc?.toJSON(),
};

describe('SuatChieu Service', () => {
  let service: SuatChieuService;
  let httpMock: HttpTestingController;
  let expectedResult: ISuatChieu | ISuatChieu[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(SuatChieuService);
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

    it('should create a SuatChieu', () => {
      const suatChieu = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(suatChieu).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a SuatChieu', () => {
      const suatChieu = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(suatChieu).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a SuatChieu', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of SuatChieu', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a SuatChieu', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addSuatChieuToCollectionIfMissing', () => {
      it('should add a SuatChieu to an empty array', () => {
        const suatChieu: ISuatChieu = sampleWithRequiredData;
        expectedResult = service.addSuatChieuToCollectionIfMissing([], suatChieu);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(suatChieu);
      });

      it('should not add a SuatChieu to an array that contains it', () => {
        const suatChieu: ISuatChieu = sampleWithRequiredData;
        const suatChieuCollection: ISuatChieu[] = [
          {
            ...suatChieu,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addSuatChieuToCollectionIfMissing(suatChieuCollection, suatChieu);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a SuatChieu to an array that doesn't contain it", () => {
        const suatChieu: ISuatChieu = sampleWithRequiredData;
        const suatChieuCollection: ISuatChieu[] = [sampleWithPartialData];
        expectedResult = service.addSuatChieuToCollectionIfMissing(suatChieuCollection, suatChieu);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(suatChieu);
      });

      it('should add only unique SuatChieu to an array', () => {
        const suatChieuArray: ISuatChieu[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const suatChieuCollection: ISuatChieu[] = [sampleWithRequiredData];
        expectedResult = service.addSuatChieuToCollectionIfMissing(suatChieuCollection, ...suatChieuArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const suatChieu: ISuatChieu = sampleWithRequiredData;
        const suatChieu2: ISuatChieu = sampleWithPartialData;
        expectedResult = service.addSuatChieuToCollectionIfMissing([], suatChieu, suatChieu2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(suatChieu);
        expect(expectedResult).toContain(suatChieu2);
      });

      it('should accept null and undefined values', () => {
        const suatChieu: ISuatChieu = sampleWithRequiredData;
        expectedResult = service.addSuatChieuToCollectionIfMissing([], null, suatChieu, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(suatChieu);
      });

      it('should return initial array if no SuatChieu is added', () => {
        const suatChieuCollection: ISuatChieu[] = [sampleWithRequiredData];
        expectedResult = service.addSuatChieuToCollectionIfMissing(suatChieuCollection, undefined, null);
        expect(expectedResult).toEqual(suatChieuCollection);
      });
    });

    describe('compareSuatChieu', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareSuatChieu(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 17976 };
        const entity2 = null;

        const compareResult1 = service.compareSuatChieu(entity1, entity2);
        const compareResult2 = service.compareSuatChieu(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 17976 };
        const entity2 = { id: 3753 };

        const compareResult1 = service.compareSuatChieu(entity1, entity2);
        const compareResult2 = service.compareSuatChieu(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 17976 };
        const entity2 = { id: 17976 };

        const compareResult1 = service.compareSuatChieu(entity1, entity2);
        const compareResult2 = service.compareSuatChieu(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
