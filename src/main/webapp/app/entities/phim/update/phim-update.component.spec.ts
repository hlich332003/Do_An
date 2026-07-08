import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { PhimService } from '../service/phim.service';
import { IPhim } from '../phim.model';
import { PhimFormService } from './phim-form.service';

import { PhimUpdateComponent } from './phim-update.component';

describe('Phim Management Update Component', () => {
  let comp: PhimUpdateComponent;
  let fixture: ComponentFixture<PhimUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let phimFormService: PhimFormService;
  let phimService: PhimService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PhimUpdateComponent],
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
      .overrideTemplate(PhimUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PhimUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    phimFormService = TestBed.inject(PhimFormService);
    phimService = TestBed.inject(PhimService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const phim: IPhim = { id: 10211 };

      activatedRoute.data = of({ phim });
      comp.ngOnInit();

      expect(comp.phim).toEqual(phim);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhim>>();
      const phim = { id: 13810 };
      jest.spyOn(phimFormService, 'getPhim').mockReturnValue(phim);
      jest.spyOn(phimService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phim });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: phim }));
      saveSubject.complete();

      // THEN
      expect(phimFormService.getPhim).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(phimService.update).toHaveBeenCalledWith(expect.objectContaining(phim));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhim>>();
      const phim = { id: 13810 };
      jest.spyOn(phimFormService, 'getPhim').mockReturnValue({ id: null });
      jest.spyOn(phimService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phim: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: phim }));
      saveSubject.complete();

      // THEN
      expect(phimFormService.getPhim).toHaveBeenCalled();
      expect(phimService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPhim>>();
      const phim = { id: 13810 };
      jest.spyOn(phimService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ phim });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(phimService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
