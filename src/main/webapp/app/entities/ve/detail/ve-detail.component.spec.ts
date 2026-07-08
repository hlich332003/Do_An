import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { VeDetailComponent } from './ve-detail.component';

describe('Ve Management Detail Component', () => {
  let comp: VeDetailComponent;
  let fixture: ComponentFixture<VeDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VeDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./ve-detail.component').then(m => m.VeDetailComponent),
              resolve: { ve: () => of({ id: 30679 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(VeDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VeDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load ve on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', VeDetailComponent);

      // THEN
      expect(instance.ve()).toEqual(expect.objectContaining({ id: 30679 }));
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
