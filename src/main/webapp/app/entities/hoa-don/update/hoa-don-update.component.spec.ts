import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { INguoiDung } from 'app/entities/nguoi-dung/nguoi-dung.model';
import { NguoiDungService } from 'app/entities/nguoi-dung/service/nguoi-dung.service';
import { HoaDonService } from '../service/hoa-don.service';
import { IHoaDon } from '../hoa-don.model';
import { HoaDonFormService } from './hoa-don-form.service';

import { HoaDonUpdateComponent } from './hoa-don-update.component';

describe('HoaDon Management Update Component', () => {
  let comp: HoaDonUpdateComponent;
  let fixture: ComponentFixture<HoaDonUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let hoaDonFormService: HoaDonFormService;
  let hoaDonService: HoaDonService;
  let nguoiDungService: NguoiDungService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HoaDonUpdateComponent],
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
      .overrideTemplate(HoaDonUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(HoaDonUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    hoaDonFormService = TestBed.inject(HoaDonFormService);
    hoaDonService = TestBed.inject(HoaDonService);
    nguoiDungService = TestBed.inject(NguoiDungService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call NguoiDung query and add missing value', () => {
      const hoaDon: IHoaDon = { id: 31365 };
      const nguoiDung: INguoiDung = { id: 27009 };
      hoaDon.nguoiDung = nguoiDung;

      const nguoiDungCollection: INguoiDung[] = [{ id: 27009 }];
      jest.spyOn(nguoiDungService, 'query').mockReturnValue(of(new HttpResponse({ body: nguoiDungCollection })));
      const additionalNguoiDungs = [nguoiDung];
      const expectedCollection: INguoiDung[] = [...additionalNguoiDungs, ...nguoiDungCollection];
      jest.spyOn(nguoiDungService, 'addNguoiDungToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ hoaDon });
      comp.ngOnInit();

      expect(nguoiDungService.query).toHaveBeenCalled();
      expect(nguoiDungService.addNguoiDungToCollectionIfMissing).toHaveBeenCalledWith(
        nguoiDungCollection,
        ...additionalNguoiDungs.map(expect.objectContaining),
      );
      expect(comp.nguoiDungsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const hoaDon: IHoaDon = { id: 31365 };
      const nguoiDung: INguoiDung = { id: 27009 };
      hoaDon.nguoiDung = nguoiDung;

      activatedRoute.data = of({ hoaDon });
      comp.ngOnInit();

      expect(comp.nguoiDungsSharedCollection).toContainEqual(nguoiDung);
      expect(comp.hoaDon).toEqual(hoaDon);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IHoaDon>>();
      const hoaDon = { id: 24513 };
      jest.spyOn(hoaDonFormService, 'getHoaDon').mockReturnValue(hoaDon);
      jest.spyOn(hoaDonService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ hoaDon });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: hoaDon }));
      saveSubject.complete();

      // THEN
      expect(hoaDonFormService.getHoaDon).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(hoaDonService.update).toHaveBeenCalledWith(expect.objectContaining(hoaDon));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IHoaDon>>();
      const hoaDon = { id: 24513 };
      jest.spyOn(hoaDonFormService, 'getHoaDon').mockReturnValue({ id: null });
      jest.spyOn(hoaDonService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ hoaDon: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: hoaDon }));
      saveSubject.complete();

      // THEN
      expect(hoaDonFormService.getHoaDon).toHaveBeenCalled();
      expect(hoaDonService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IHoaDon>>();
      const hoaDon = { id: 24513 };
      jest.spyOn(hoaDonService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ hoaDon });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(hoaDonService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareNguoiDung', () => {
      it('should forward to nguoiDungService', () => {
        const entity = { id: 27009 };
        const entity2 = { id: 29205 };
        jest.spyOn(nguoiDungService, 'compareNguoiDung');
        comp.compareNguoiDung(entity, entity2);
        expect(nguoiDungService.compareNguoiDung).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
