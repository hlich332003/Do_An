import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { PhimDetailComponent } from './phim-detail.component';

describe('Phim Management Detail Component', () => {
  let comp: PhimDetailComponent;
  let fixture: ComponentFixture<PhimDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhimDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./phim-detail.component').then(m => m.PhimDetailComponent),
              resolve: { phim: () => of({ id: 13810 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(PhimDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PhimDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load phim on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', PhimDetailComponent);

      // THEN
      expect(instance.phim()).toEqual(expect.objectContaining({ id: 13810 }));
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
