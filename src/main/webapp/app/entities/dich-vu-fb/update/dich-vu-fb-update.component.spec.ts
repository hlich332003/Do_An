import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { DichVuFBService } from '../service/dich-vu-fb.service';
import { IDichVuFB } from '../dich-vu-fb.model';
import { DichVuFBFormService } from './dich-vu-fb-form.service';

import { DichVuFBUpdateComponent } from './dich-vu-fb-update.component';

describe('DichVuFB Management Update Component', () => {
  let comp: DichVuFBUpdateComponent;
  let fixture: ComponentFixture<DichVuFBUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let dichVuFBFormService: DichVuFBFormService;
  let dichVuFBService: DichVuFBService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [DichVuFBUpdateComponent],
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
      .overrideTemplate(DichVuFBUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(DichVuFBUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    dichVuFBFormService = TestBed.inject(DichVuFBFormService);
    dichVuFBService = TestBed.inject(DichVuFBService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const dichVuFB: IDichVuFB = { id: 13386 };

      activatedRoute.data = of({ dichVuFB });
      comp.ngOnInit();

      expect(comp.dichVuFB).toEqual(dichVuFB);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDichVuFB>>();
      const dichVuFB = { id: 18926 };
      jest.spyOn(dichVuFBFormService, 'getDichVuFB').mockReturnValue(dichVuFB);
      jest.spyOn(dichVuFBService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dichVuFB });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: dichVuFB }));
      saveSubject.complete();

      // THEN
      expect(dichVuFBFormService.getDichVuFB).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(dichVuFBService.update).toHaveBeenCalledWith(expect.objectContaining(dichVuFB));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDichVuFB>>();
      const dichVuFB = { id: 18926 };
      jest.spyOn(dichVuFBFormService, 'getDichVuFB').mockReturnValue({ id: null });
      jest.spyOn(dichVuFBService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dichVuFB: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: dichVuFB }));
      saveSubject.complete();

      // THEN
      expect(dichVuFBFormService.getDichVuFB).toHaveBeenCalled();
      expect(dichVuFBService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDichVuFB>>();
      const dichVuFB = { id: 18926 };
      jest.spyOn(dichVuFBService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dichVuFB });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(dichVuFBService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
