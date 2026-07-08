import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IPhim } from 'app/entities/phim/phim.model';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';
import { ISuatChieu } from '../suat-chieu.model';
import { SuatChieuService } from '../service/suat-chieu.service';
import { SuatChieuFormService } from './suat-chieu-form.service';

import { SuatChieuUpdateComponent } from './suat-chieu-update.component';

describe('SuatChieu Management Update Component', () => {
  let comp: SuatChieuUpdateComponent;
  let fixture: ComponentFixture<SuatChieuUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let suatChieuFormService: SuatChieuFormService;
  let suatChieuService: SuatChieuService;
  let phimService: PhimService;
  let phongChieuService: PhongChieuService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SuatChieuUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(SuatChieuUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SuatChieuUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    suatChieuFormService = TestBed.inject(SuatChieuFormService);
    suatChieuService = TestBed.inject(SuatChieuService);
    phimService = TestBed.inject(PhimService);
    phongChieuService = TestBed.inject(PhongChieuService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Phim query and add missing value', () => {
      const suatChieu: ISuatChieu = { id: 3753 };
      const phim: IPhim = { id: 13810 };
      suatChieu.phim = phim;

      const phimCollection: IPhim[] = [{ id: 13810 }];
      jest.spyOn(phimService, 'query').mockReturnValue(of(new HttpResponse({ body: phimCollection })));
      const additionalPhims = [phim];
      const expectedCollection: IPhim[] = [...additionalPhims, ...phimCollection];
      jest.spyOn(phimService, 'addPhimToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ suatChieu });
      comp.ngOnInit();

      expect(phimService.query).toHaveBeenCalledWith(expect.objectContaining({ page: 0, size: 1000, sort: ['id,desc'] }));
      expect(phimService.addPhimToCollectionIfMissing).toHaveBeenCalledWith(
        phimCollection,
        ...additionalPhims.map(expect.objectContaining),
      );
      expect(comp.phimsSharedCollection).toEqual(expectedCollection);
    });

    it('should call PhongChieu query and add missing value', () => {
      const suatChieu: ISuatChieu = { id: 3753 };
      const phongChieu: IPhongChieu = { id: 26571 };
      suatChieu.phongChieu = phongChieu;

      const phongChieuCollection: IPhongChieu[] = [{ id: 26571 }];
      jest.spyOn(phongChieuService, 'query').mockReturnValue(of(new HttpResponse({ body: phongChieuCollection })));
      const additionalPhongChieus = [phongChieu];
      const expectedCollection: IPhongChieu[] = [...additionalPhongChieus, ...phongChieuCollection];
      jest.spyOn(phongChieuService, 'addPhongChieuToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ suatChieu });
      comp.ngOnInit();

      expect(phongChieuService.query).toHaveBeenCalledWith(expect.objectContaining({ page: 0, size: 1000, sort: ['id,desc'] }));
      expect(phongChieuService.addPhongChieuToCollectionIfMissing).toHaveBeenCalledWith(
        phongChieuCollection,
        ...additionalPhongChieus.map(expect.objectContaining),
      );
      expect(comp.phongChieusSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const suatChieu: ISuatChieu = { id: 3753 };
      const phim: IPhim = { id: 13810 };
      suatChieu.phim = phim;
      const phongChieu: IPhongChieu = { id: 26571 };
      suatChieu.phongChieu = phongChieu;

      activatedRoute.data = of({ suatChieu });
      comp.ngOnInit();

      expect(comp.phimsSharedCollection).toContainEqual(phim);
      expect(comp.phongChieusSharedCollection).toContainEqual(phongChieu);
      expect(comp.suatChieu).toEqual(suatChieu);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISuatChieu>>();
      const suatChieu = { id: 17976 };
      jest.spyOn(suatChieuFormService, 'getSuatChieu').mockReturnValue(suatChieu);
      jest.spyOn(suatChieuService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ suatChieu });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: suatChieu }));
      saveSubject.complete();

      // THEN
      expect(suatChieuFormService.getSuatChieu).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(suatChieuService.update).toHaveBeenCalledWith(expect.objectContaining(suatChieu));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISuatChieu>>();
      const suatChieu = { id: 17976 };
      jest.spyOn(suatChieuFormService, 'getSuatChieu').mockReturnValue({ id: null });
      jest.spyOn(suatChieuService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ suatChieu: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: suatChieu }));
      saveSubject.complete();

      // THEN
      expect(suatChieuFormService.getSuatChieu).toHaveBeenCalled();
      expect(suatChieuService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISuatChieu>>();
      const suatChieu = { id: 17976 };
      jest.spyOn(suatChieuService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ suatChieu });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(suatChieuService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('comparePhim', () => {
      it('should forward to phimService', () => {
        const entity = { id: 13810 };
        const entity2 = { id: 10211 };
        jest.spyOn(phimService, 'comparePhim');
        comp.comparePhim(entity, entity2);
        expect(phimService.comparePhim).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('comparePhongChieu', () => {
      it('should forward to phongChieuService', () => {
        const entity = { id: 26571 };
        const entity2 = { id: 14106 };
        jest.spyOn(phongChieuService, 'comparePhongChieu');
        comp.comparePhongChieu(entity, entity2);
        expect(phongChieuService.comparePhongChieu).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
