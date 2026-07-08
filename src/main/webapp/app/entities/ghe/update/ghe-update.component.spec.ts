import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IVe } from 'app/entities/ve/ve.model';
import { VeService } from 'app/entities/ve/service/ve.service';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';
import { IGhe } from '../ghe.model';
import { GheService } from '../service/ghe.service';
import { GheFormService } from './ghe-form.service';

import { GheUpdateComponent } from './ghe-update.component';

describe('Ghe Management Update Component', () => {
  let comp: GheUpdateComponent;
  let fixture: ComponentFixture<GheUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let gheFormService: GheFormService;
  let gheService: GheService;
  let veService: VeService;
  let phongChieuService: PhongChieuService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GheUpdateComponent],
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
      .overrideTemplate(GheUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(GheUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    gheFormService = TestBed.inject(GheFormService);
    gheService = TestBed.inject(GheService);
    veService = TestBed.inject(VeService);
    phongChieuService = TestBed.inject(PhongChieuService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call ve query and add missing value', () => {
      const ghe: IGhe = { id: 7029 };
      const ve: IVe = { id: 30679 };
      ghe.ve = ve;

      const veCollection: IVe[] = [{ id: 30679 }];
      jest.spyOn(veService, 'query').mockReturnValue(of(new HttpResponse({ body: veCollection })));
      const expectedCollection: IVe[] = [ve, ...veCollection];
      jest.spyOn(veService, 'addVeToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ghe });
      comp.ngOnInit();

      expect(veService.query).toHaveBeenCalled();
      expect(veService.addVeToCollectionIfMissing).toHaveBeenCalledWith(veCollection, ve);
      expect(comp.vesCollection).toEqual(expectedCollection);
    });

    it('should call PhongChieu query and add missing value', () => {
      const ghe: IGhe = { id: 7029 };
      const phongChieu: IPhongChieu = { id: 26571 };
      ghe.phongChieu = phongChieu;

      const phongChieuCollection: IPhongChieu[] = [{ id: 26571 }];
      jest.spyOn(phongChieuService, 'query').mockReturnValue(of(new HttpResponse({ body: phongChieuCollection })));
      const additionalPhongChieus = [phongChieu];
      const expectedCollection: IPhongChieu[] = [...additionalPhongChieus, ...phongChieuCollection];
      jest.spyOn(phongChieuService, 'addPhongChieuToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ghe });
      comp.ngOnInit();

      expect(phongChieuService.query).toHaveBeenCalled();
      expect(phongChieuService.addPhongChieuToCollectionIfMissing).toHaveBeenCalledWith(
        phongChieuCollection,
        ...additionalPhongChieus.map(expect.objectContaining),
      );
      expect(comp.phongChieusSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const ghe: IGhe = { id: 7029 };
      const ve: IVe = { id: 30679 };
      ghe.ve = ve;
      const phongChieu: IPhongChieu = { id: 26571 };
      ghe.phongChieu = phongChieu;

      activatedRoute.data = of({ ghe });
      comp.ngOnInit();

      expect(comp.vesCollection).toContainEqual(ve);
      expect(comp.phongChieusSharedCollection).toContainEqual(phongChieu);
      expect(comp.ghe).toEqual(ghe);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGhe>>();
      const ghe = { id: 14590 };
      jest.spyOn(gheFormService, 'getGhe').mockReturnValue(ghe);
      jest.spyOn(gheService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ghe });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: ghe }));
      saveSubject.complete();

      // THEN
      expect(gheFormService.getGhe).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(gheService.update).toHaveBeenCalledWith(expect.objectContaining(ghe));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGhe>>();
      const ghe = { id: 14590 };
      jest.spyOn(gheFormService, 'getGhe').mockReturnValue({ id: null });
      jest.spyOn(gheService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ghe: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: ghe }));
      saveSubject.complete();

      // THEN
      expect(gheFormService.getGhe).toHaveBeenCalled();
      expect(gheService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGhe>>();
      const ghe = { id: 14590 };
      jest.spyOn(gheService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ghe });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(gheService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareVe', () => {
      it('should forward to veService', () => {
        const entity = { id: 30679 };
        const entity2 = { id: 25849 };
        jest.spyOn(veService, 'compareVe');
        comp.compareVe(entity, entity2);
        expect(veService.compareVe).toHaveBeenCalledWith(entity, entity2);
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
