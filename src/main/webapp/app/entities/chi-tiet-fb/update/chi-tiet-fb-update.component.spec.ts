import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IDichVuFB } from 'app/entities/dich-vu-fb/dich-vu-fb.model';
import { DichVuFBService } from 'app/entities/dich-vu-fb/service/dich-vu-fb.service';
import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';
import { HoaDonService } from 'app/entities/hoa-don/service/hoa-don.service';
import { IChiTietFB } from '../chi-tiet-fb.model';
import { ChiTietFBService } from '../service/chi-tiet-fb.service';
import { ChiTietFBFormService } from './chi-tiet-fb-form.service';

import { ChiTietFBUpdateComponent } from './chi-tiet-fb-update.component';

describe('ChiTietFB Management Update Component', () => {
  let comp: ChiTietFBUpdateComponent;
  let fixture: ComponentFixture<ChiTietFBUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let chiTietFBFormService: ChiTietFBFormService;
  let chiTietFBService: ChiTietFBService;
  let dichVuFBService: DichVuFBService;
  let hoaDonService: HoaDonService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ChiTietFBUpdateComponent],
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
      .overrideTemplate(ChiTietFBUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ChiTietFBUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    chiTietFBFormService = TestBed.inject(ChiTietFBFormService);
    chiTietFBService = TestBed.inject(ChiTietFBService);
    dichVuFBService = TestBed.inject(DichVuFBService);
    hoaDonService = TestBed.inject(HoaDonService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call DichVuFB query and add missing value', () => {
      const chiTietFB: IChiTietFB = { id: 19585 };
      const dichVuFB: IDichVuFB = { id: 18926 };
      chiTietFB.dichVuFB = dichVuFB;

      const dichVuFBCollection: IDichVuFB[] = [{ id: 18926 }];
      jest.spyOn(dichVuFBService, 'query').mockReturnValue(of(new HttpResponse({ body: dichVuFBCollection })));
      const additionalDichVuFBS = [dichVuFB];
      const expectedCollection: IDichVuFB[] = [...additionalDichVuFBS, ...dichVuFBCollection];
      jest.spyOn(dichVuFBService, 'addDichVuFBToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ chiTietFB });
      comp.ngOnInit();

      expect(dichVuFBService.query).toHaveBeenCalled();
      expect(dichVuFBService.addDichVuFBToCollectionIfMissing).toHaveBeenCalledWith(
        dichVuFBCollection,
        ...additionalDichVuFBS.map(expect.objectContaining),
      );
      expect(comp.dichVuFBSSharedCollection).toEqual(expectedCollection);
    });

    it('should call HoaDon query and add missing value', () => {
      const chiTietFB: IChiTietFB = { id: 19585 };
      const hoaDon: IHoaDon = { id: 24513 };
      chiTietFB.hoaDon = hoaDon;

      const hoaDonCollection: IHoaDon[] = [{ id: 24513 }];
      jest.spyOn(hoaDonService, 'query').mockReturnValue(of(new HttpResponse({ body: hoaDonCollection })));
      const additionalHoaDons = [hoaDon];
      const expectedCollection: IHoaDon[] = [...additionalHoaDons, ...hoaDonCollection];
      jest.spyOn(hoaDonService, 'addHoaDonToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ chiTietFB });
      comp.ngOnInit();

      expect(hoaDonService.query).toHaveBeenCalled();
      expect(hoaDonService.addHoaDonToCollectionIfMissing).toHaveBeenCalledWith(
        hoaDonCollection,
        ...additionalHoaDons.map(expect.objectContaining),
      );
      expect(comp.hoaDonsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const chiTietFB: IChiTietFB = { id: 19585 };
      const dichVuFB: IDichVuFB = { id: 18926 };
      chiTietFB.dichVuFB = dichVuFB;
      const hoaDon: IHoaDon = { id: 24513 };
      chiTietFB.hoaDon = hoaDon;

      activatedRoute.data = of({ chiTietFB });
      comp.ngOnInit();

      expect(comp.dichVuFBSSharedCollection).toContainEqual(dichVuFB);
      expect(comp.hoaDonsSharedCollection).toContainEqual(hoaDon);
      expect(comp.chiTietFB).toEqual(chiTietFB);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChiTietFB>>();
      const chiTietFB = { id: 585 };
      jest.spyOn(chiTietFBFormService, 'getChiTietFB').mockReturnValue(chiTietFB);
      jest.spyOn(chiTietFBService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chiTietFB });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: chiTietFB }));
      saveSubject.complete();

      // THEN
      expect(chiTietFBFormService.getChiTietFB).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(chiTietFBService.update).toHaveBeenCalledWith(expect.objectContaining(chiTietFB));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChiTietFB>>();
      const chiTietFB = { id: 585 };
      jest.spyOn(chiTietFBFormService, 'getChiTietFB').mockReturnValue({ id: null });
      jest.spyOn(chiTietFBService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chiTietFB: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: chiTietFB }));
      saveSubject.complete();

      // THEN
      expect(chiTietFBFormService.getChiTietFB).toHaveBeenCalled();
      expect(chiTietFBService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IChiTietFB>>();
      const chiTietFB = { id: 585 };
      jest.spyOn(chiTietFBService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ chiTietFB });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(chiTietFBService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareDichVuFB', () => {
      it('should forward to dichVuFBService', () => {
        const entity = { id: 18926 };
        const entity2 = { id: 13386 };
        jest.spyOn(dichVuFBService, 'compareDichVuFB');
        comp.compareDichVuFB(entity, entity2);
        expect(dichVuFBService.compareDichVuFB).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareHoaDon', () => {
      it('should forward to hoaDonService', () => {
        const entity = { id: 24513 };
        const entity2 = { id: 31365 };
        jest.spyOn(hoaDonService, 'compareHoaDon');
        comp.compareHoaDon(entity, entity2);
        expect(hoaDonService.compareHoaDon).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
