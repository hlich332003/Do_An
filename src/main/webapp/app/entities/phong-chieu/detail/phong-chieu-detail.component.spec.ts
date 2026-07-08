import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { PhongChieuDetailComponent } from './phong-chieu-detail.component';

describe('PhongChieu Management Detail Component', () => {
  let comp: PhongChieuDetailComponent;
  let fixture: ComponentFixture<PhongChieuDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhongChieuDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./phong-chieu-detail.component').then(m => m.PhongChieuDetailComponent),
              resolve: { phongChieu: () => of({ id: 26571 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(PhongChieuDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PhongChieuDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load phongChieu on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', PhongChieuDetailComponent);

      // THEN
      expect(instance.phongChieu()).toEqual(expect.objectContaining({ id: 26571 }));
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
