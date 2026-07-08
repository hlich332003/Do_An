import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ChiTietFBDetailComponent } from './chi-tiet-fb-detail.component';

describe('ChiTietFB Management Detail Component', () => {
  let comp: ChiTietFBDetailComponent;
  let fixture: ComponentFixture<ChiTietFBDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChiTietFBDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./chi-tiet-fb-detail.component').then(m => m.ChiTietFBDetailComponent),
              resolve: { chiTietFB: () => of({ id: 585 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ChiTietFBDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChiTietFBDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load chiTietFB on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ChiTietFBDetailComponent);

      // THEN
      expect(instance.chiTietFB()).toEqual(expect.objectContaining({ id: 585 }));
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
