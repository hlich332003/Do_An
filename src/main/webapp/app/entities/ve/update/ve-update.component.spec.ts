import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IHoaDon } from 'app/entities/hoa-don/hoa-don.model';
import { HoaDonService } from 'app/entities/hoa-don/service/hoa-don.service';
import { ISuatChieu } from 'app/entities/suat-chieu/suat-chieu.model';
import { SuatChieuService } from 'app/entities/suat-chieu/service/suat-chieu.service';
import { IVe } from '../ve.model';
import { VeService } from '../service/ve.service';
import { VeFormService } from './ve-form.service';

import { VeUpdateComponent } from './ve-update.component';

describe('Ve Management Update Component', () => {
  let comp: VeUpdateComponent;
  let fixture: ComponentFixture<VeUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let veFormService: VeFormService;
  let veService: VeService;
  let hoaDonService: HoaDonService;
  let suatChieuService: SuatChieuService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [VeUpdateComponent],
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
      .overrideTemplate(VeUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(VeUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    veFormService = TestBed.inject(VeFormService);
    veService = TestBed.inject(VeService);
    hoaDonService = TestBed.inject(HoaDonService);
    suatChieuService = TestBed.inject(SuatChieuService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call HoaDon query and add missing value', () => {
      const ve: IVe = { id: 25849 };
      const hoaDon: IHoaDon = { id: 24513 };
      ve.hoaDon = hoaDon;

      const hoaDonCollection: IHoaDon[] = [{ id: 24513 }];
      jest.spyOn(hoaDonService, 'query').mockReturnValue(of(new HttpResponse({ body: hoaDonCollection })));
      const additionalHoaDons = [hoaDon];
      const expectedCollection: IHoaDon[] = [...additionalHoaDons, ...hoaDonCollection];
      jest.spyOn(hoaDonService, 'addHoaDonToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ve });
      comp.ngOnInit();

      expect(hoaDonService.query).toHaveBeenCalled();
      expect(hoaDonService.addHoaDonToCollectionIfMissing).toHaveBeenCalledWith(
        hoaDonCollection,
        ...additionalHoaDons.map(expect.objectContaining),
      );
      expect(comp.hoaDonsSharedCollection).toEqual(expectedCollection);
    });

    it('should call SuatChieu query and add missing value', () => {
      const ve: IVe = { id: 25849 };
      const suatChieu: ISuatChieu = { id: 17976 };
      ve.suatChieu = suatChieu;

      const suatChieuCollection: ISuatChieu[] = [{ id: 17976 }];
      jest.spyOn(suatChieuService, 'query').mockReturnValue(of(new HttpResponse({ body: suatChieuCollection })));
      const additionalSuatChieus = [suatChieu];
      const expectedCollection: ISuatChieu[] = [...additionalSuatChieus, ...suatChieuCollection];
      jest.spyOn(suatChieuService, 'addSuatChieuToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ve });
      comp.ngOnInit();

      expect(suatChieuService.query).toHaveBeenCalled();
      expect(suatChieuService.addSuatChieuToCollectionIfMissing).toHaveBeenCalledWith(
        suatChieuCollection,
        ...additionalSuatChieus.map(expect.objectContaining),
      );
      expect(comp.suatChieusSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const ve: IVe = { id: 25849 };
      const hoaDon: IHoaDon = { id: 24513 };
      ve.hoaDon = hoaDon;
      const suatChieu: ISuatChieu = { id: 17976 };
      ve.suatChieu = suatChieu;
      ve.suatChieu = suatChieu;

      activatedRoute.data = of({ ve });
      comp.ngOnInit();

      expect(comp.hoaDonsSharedCollection).toContainEqual(hoaDon);
      expect(comp.suatChieusSharedCollection).toContainEqual(suatChieu);
      expect(comp.suatChieusSharedCollection).toContainEqual(suatChieu);
      expect(comp.ve).toEqual(ve);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVe>>();
      const ve = { id: 30679 };
      jest.spyOn(veFormService, 'getVe').mockReturnValue(ve);
      jest.spyOn(veService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ve });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: ve }));
      saveSubject.complete();

      // THEN
      expect(veFormService.getVe).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(veService.update).toHaveBeenCalledWith(expect.objectContaining(ve));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVe>>();
      const ve = { id: 30679 };
      jest.spyOn(veFormService, 'getVe').mockReturnValue({ id: null });
      jest.spyOn(veService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ve: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: ve }));
      saveSubject.complete();

      // THEN
      expect(veFormService.getVe).toHaveBeenCalled();
      expect(veService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVe>>();
      const ve = { id: 30679 };
      jest.spyOn(veService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ve });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(veService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareHoaDon', () => {
      it('should forward to hoaDonService', () => {
        const entity = { id: 24513 };
        const entity2 = { id: 31365 };
        jest.spyOn(hoaDonService, 'compareHoaDon');
        comp.compareHoaDon(entity, entity2);
        expect(hoaDonService.compareHoaDon).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareSuatChieu', () => {
      it('should forward to suatChieuService', () => {
        const entity = { id: 17976 };
        const entity2 = { id: 3753 };
        jest.spyOn(suatChieuService, 'compareSuatChieu');
        comp.compareSuatChieu(entity, entity2);
        expect(suatChieuService.compareSuatChieu).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
