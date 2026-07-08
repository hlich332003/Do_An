import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { NguoiDungDetailComponent } from './nguoi-dung-detail.component';

describe('NguoiDung Management Detail Component', () => {
  let comp: NguoiDungDetailComponent;
  let fixture: ComponentFixture<NguoiDungDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NguoiDungDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./nguoi-dung-detail.component').then(m => m.NguoiDungDetailComponent),
              resolve: { nguoiDung: () => of({ id: 27009 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(NguoiDungDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NguoiDungDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load nguoiDung on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', NguoiDungDetailComponent);

      // THEN
      expect(instance.nguoiDung()).toEqual(expect.objectContaining({ id: 27009 }));
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
