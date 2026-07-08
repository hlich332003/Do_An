import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { DichVuFBDetailComponent } from './dich-vu-fb-detail.component';

describe('DichVuFB Management Detail Component', () => {
  let comp: DichVuFBDetailComponent;
  let fixture: ComponentFixture<DichVuFBDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DichVuFBDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./dich-vu-fb-detail.component').then(m => m.DichVuFBDetailComponent),
              resolve: { dichVuFB: () => of({ id: 18926 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(DichVuFBDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DichVuFBDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load dichVuFB on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', DichVuFBDetailComponent);

      // THEN
      expect(instance.dichVuFB()).toEqual(expect.objectContaining({ id: 18926 }));
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
