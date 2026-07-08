import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { HoaDonDetailComponent } from './hoa-don-detail.component';

describe('HoaDon Management Detail Component', () => {
  let comp: HoaDonDetailComponent;
  let fixture: ComponentFixture<HoaDonDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HoaDonDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./hoa-don-detail.component').then(m => m.HoaDonDetailComponent),
              resolve: { hoaDon: () => of({ id: 24513 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(HoaDonDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HoaDonDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load hoaDon on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', HoaDonDetailComponent);

      // THEN
      expect(instance.hoaDon()).toEqual(expect.objectContaining({ id: 24513 }));
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
