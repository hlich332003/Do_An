import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { GheDetailComponent } from './ghe-detail.component';

describe('Ghe Management Detail Component', () => {
  let comp: GheDetailComponent;
  let fixture: ComponentFixture<GheDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GheDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./ghe-detail.component').then(m => m.GheDetailComponent),
              resolve: { ghe: () => of({ id: 14590 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(GheDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GheDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load ghe on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', GheDetailComponent);

      // THEN
      expect(instance.ghe()).toEqual(expect.objectContaining({ id: 14590 }));
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
