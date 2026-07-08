import { Component, EventEmitter, OnInit, Output, Input, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { IDichVuFB } from 'app/entities/dich-vu-fb/dich-vu-fb.model';
import { DichVuFBService } from 'app/entities/dich-vu-fb/service/dich-vu-fb.service';

export interface IDichVuFBVM extends IDichVuFB {
  soLuong?: number;
}

@Component({
  selector: 'jhi-chon-dich-vu',
  standalone: true,
  imports: [CommonModule, FaIconComponent],
  templateUrl: './chon-dich-vu.component.html',
  styleUrls: ['./chon-dich-vu.component.scss'],
})
export class ChonDichVuComponent implements OnInit {
  @Input() initialCombosDaChon: IDichVuFBVM[] = [];
  @Output() combosDaChonChange = new EventEmitter<IDichVuFBVM[]>();

  dichVuFBs: IDichVuFBVM[] = [];

  protected dichVuFBService = inject(DichVuFBService);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.dichVuFBService.getActive({ size: 1000 }).subscribe({
      next: (res: HttpResponse<IDichVuFB[]>) => {
        this.dichVuFBs = (res.body ?? []).map(fb => {
          const initialCombo = this.initialCombosDaChon.find(c => c.id === fb.id);
          return { ...fb, soLuong: initialCombo ? initialCombo.soLuong : 0 };
        });
      },
    });
  }

  increaseQuantity(combo: IDichVuFBVM): void {
    if (combo.soLuong === undefined) {
      combo.soLuong = 0;
    }
    combo.soLuong++;
    this.emitSelection();
  }

  decreaseQuantity(combo: IDichVuFBVM): void {
    if (combo.soLuong && combo.soLuong > 0) {
      combo.soLuong--;
      this.emitSelection();
    }
  }

  emitSelection(): void {
    const selectedCombos = this.dichVuFBs.filter(c => c.soLuong && c.soLuong > 0);
    this.combosDaChonChange.emit(selectedCombos);
  }
}
