import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { PhongChieuService } from '../service/phong-chieu.service';
import { IPhongChieu } from '../phong-chieu.model';
import { PhongChieuFormService } from './phong-chieu-form.service';

import { PhongChieuUpdateComponent } from './phong-chieu-update.component';
import { GheService } from '../../ghe/service/ghe.service';

describe('PhongChieu Management Update Component', () => {
  let comp: PhongChieuUpdateComponent;
  let fixture: ComponentFixture<PhongChieuUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let phongChieuFormService: PhongChieuFormService;
  let phongChieuService: PhongChieuService;
  let gheService: GheService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PhongChieuUpdateComponent],
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
      .overrideTemplate(PhongChieuUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PhongChieuUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    phongChieuFormService = TestBed.inject(PhongChieuFormService);
    phongChieuService = TestBed.inject(PhongChieuService);
    gheService = TestBed.inject(GheService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const phongChieu: IPhongChieu = { id: 14106 };

      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();

      expect(comp.phongChieu).toEqual(phongChieu);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuFormService, 'getPhongChieu').mockReturnValue(phongChieu);
      jest.spyOn(phongChieuService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: phongChieu }));
      saveSubject.complete();

      // THEN
      expect(phongChieuFormService.getPhongChieu).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(phongChieuService.update).toHaveBeenCalledWith(expect.objectContaining(phongChieu));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuFormService, 'getPhongChieu').mockReturnValue({ id: null });
      jest.spyOn(phongChieuService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phongChieu: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: phongChieu }));
      saveSubject.complete();

      // THEN
      expect(phongChieuFormService.getPhongChieu).toHaveBeenCalled();
      expect(phongChieuService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(phongChieuService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });

    it('should not call updateBatchSeats on save if isSeatMapDirty is false', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuFormService, 'getPhongChieu').mockReturnValue(phongChieu);
      jest.spyOn(phongChieuService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(gheService, 'updateBatchSeats');
      jest.spyOn(comp, 'previousState');

      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();
      comp.isSeatMapDirty = false;

      // WHEN
      comp.save();
      saveSubject.next(new HttpResponse({ body: phongChieu }));
      saveSubject.complete();

      // THEN
      expect(gheService.updateBatchSeats).not.toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should call updateBatchSeats on save if isSeatMapDirty is true and ghes.length > 0', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuFormService, 'getPhongChieu').mockReturnValue(phongChieu);
      jest.spyOn(phongChieuService, 'update').mockReturnValue(saveSubject);

      const batchSubject = new Subject<HttpResponse<any>>();
      jest.spyOn(gheService, 'updateBatchSeats').mockReturnValue(batchSubject);
      jest.spyOn(comp, 'previousState');

      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();
      comp.isSeatMapDirty = true;
      comp.ghes = [{ id: 1, maGhe: 'A1', hang: 'A', cot: 1, loaiGhe: 1 }];

      // WHEN
      comp.save();
      saveSubject.next(new HttpResponse({ body: phongChieu }));
      saveSubject.complete();

      // THEN
      expect(gheService.updateBatchSeats).toHaveBeenCalledWith(phongChieu.id, expect.any(Array));

      // WHEN batch completes
      batchSubject.next(new HttpResponse({ body: null }));
      batchSubject.complete();

      // THEN
      expect(comp.isSeatMapDirty).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set saveErrorMessage on batch save error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhongChieu>>();
      const phongChieu = { id: 26571 };
      jest.spyOn(phongChieuFormService, 'getPhongChieu').mockReturnValue(phongChieu);
      jest.spyOn(phongChieuService, 'update').mockReturnValue(saveSubject);

      const batchSubject = new Subject<HttpResponse<any>>();
      jest.spyOn(gheService, 'updateBatchSeats').mockReturnValue(batchSubject);
      jest.spyOn(comp, 'previousState');

      activatedRoute.data = of({ phongChieu });
      comp.ngOnInit();
      comp.isSeatMapDirty = true;
      comp.ghes = [{ id: 1, maGhe: 'A1', hang: 'A', cot: 1, loaiGhe: 1 }];

      // WHEN
      comp.save();
      saveSubject.next(new HttpResponse({ body: phongChieu }));
      saveSubject.complete();

      // trigger batch error
      batchSubject.error({ error: { title: 'Backend error' } });

      // THEN
      expect(comp.saveErrorMessage).toEqual('Backend error');
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('layout configuration', () => {
    it('should update rowConfigs with defaults on updateRowConfigs', () => {
      comp.numRows = 5;
      comp.updateRowConfigs();
      expect(comp.rowConfigs.length).toEqual(5);
      expect(comp.rowConfigs[0].rowLabel).toEqual('A');
      expect(comp.rowConfigs[0].type).toEqual(1); // Standard default
    });

    it('should generate seats locally on generateSeatsLocal', () => {
      comp.phongChieu = { id: 123, tenPhong: 'IMAX' };
      comp.numRows = 2;
      comp.numSeatsPerRow = 5;
      comp.updateRowConfigs();

      comp.generateSeatsLocal();

      expect(comp.ghes.length).toEqual(10);
      expect(comp.ghes[0].maGhe).toEqual('A1');
      expect(comp.ghes[0].phongChieu?.id).toEqual(123);
      expect(comp.isSeatMapDirty).toEqual(true);
      expect(comp.showRegenPanel).toEqual(false);
    });

    it('should confirm and reset seat map on enableRegenLayout', () => {
      comp.ghes = [{ id: 1 }];
      jest.spyOn(window, 'confirm').mockReturnValue(true);

      comp.enableRegenLayout();

      expect(comp.ghes.length).toEqual(0);
      expect(comp.showRegenPanel).toEqual(true);
    });
  });
});
