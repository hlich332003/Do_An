import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { NguoiDungService } from '../service/nguoi-dung.service';
import { INguoiDung } from '../nguoi-dung.model';
import { NguoiDungFormService } from './nguoi-dung-form.service';

import { NguoiDungUpdateComponent } from './nguoi-dung-update.component';

describe('NguoiDung Management Update Component', () => {
  let comp: NguoiDungUpdateComponent;
  let fixture: ComponentFixture<NguoiDungUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let nguoiDungFormService: NguoiDungFormService;
  let nguoiDungService: NguoiDungService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NguoiDungUpdateComponent],
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
      .overrideTemplate(NguoiDungUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(NguoiDungUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    nguoiDungFormService = TestBed.inject(NguoiDungFormService);
    nguoiDungService = TestBed.inject(NguoiDungService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const nguoiDung: INguoiDung = { id: 29205 };

      activatedRoute.data = of({ nguoiDung });
      comp.ngOnInit();

      expect(comp.nguoiDung).toEqual(nguoiDung);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<INguoiDung>>();
      const nguoiDung = { id: 27009 };
      jest.spyOn(nguoiDungFormService, 'getNguoiDung').mockReturnValue(nguoiDung);
      jest.spyOn(nguoiDungService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ nguoiDung });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: nguoiDung }));
      saveSubject.complete();

      // THEN
      expect(nguoiDungFormService.getNguoiDung).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(nguoiDungService.update).toHaveBeenCalledWith(expect.objectContaining(nguoiDung));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<INguoiDung>>();
      const nguoiDung = { id: 27009 };
      jest.spyOn(nguoiDungFormService, 'getNguoiDung').mockReturnValue({ id: null });
      jest.spyOn(nguoiDungService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ nguoiDung: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: nguoiDung }));
      saveSubject.complete();

      // THEN
      expect(nguoiDungFormService.getNguoiDung).toHaveBeenCalled();
      expect(nguoiDungService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<INguoiDung>>();
      const nguoiDung = { id: 27009 };
      jest.spyOn(nguoiDungService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ nguoiDung });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(nguoiDungService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
