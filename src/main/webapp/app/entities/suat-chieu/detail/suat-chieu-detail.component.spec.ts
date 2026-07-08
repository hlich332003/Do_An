import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { SuatChieuDetailComponent } from './suat-chieu-detail.component';

describe('SuatChieu Management Detail Component', () => {
  let comp: SuatChieuDetailComponent;
  let fixture: ComponentFixture<SuatChieuDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SuatChieuDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./suat-chieu-detail.component').then(m => m.SuatChieuDetailComponent),
              resolve: { suatChieu: () => of({ id: 17976 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SuatChieuDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SuatChieuDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load suatChieu on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SuatChieuDetailComponent);

      // THEN
      expect(instance.suatChieu()).toEqual(expect.objectContaining({ id: 17976 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
