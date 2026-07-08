import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPhongChieu } from '../phong-chieu.model';
import { PhongChieuService } from '../service/phong-chieu.service';
import { PhongChieuFormGroup, PhongChieuFormService } from './phong-chieu-form.service';
import { IGhe } from '../../ghe/ghe.model';
import { GheService } from '../../ghe/service/ghe.service';

type SeatType = 1 | 2 | 3;

interface ISeatGroup {
  id: string;
  isCouple: boolean;
  seats: IGhe[];
  primarySeat: IGhe;
}

@Component({
  selector: 'jhi-phong-chieu-update',
  templateUrl: './phong-chieu-update.component.html',
  styleUrls: ['./phong-chieu-update.component.scss'],
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule],
})
export class PhongChieuUpdateComponent implements OnInit {
  isSaving = false;
  phongChieu: IPhongChieu | null = null;
  ghes: IGhe[] = [];
  seatRows: ISeatGroup[][] = [];
  isSeatMapLoading = false;
  readonly seatColumnsPerRow = 12;
  selectedSeatType: SeatType = 1;

  isSeatMapDirty = false;
  saveErrorMessage = '';
  numRows = 10;
  numSeatsPerRow = 12;
  rowConfigs: Array<{ rowLabel: string; type: SeatType }> = [];
  showRegenPanel = false;

  protected phongChieuService = inject(PhongChieuService);
  protected phongChieuFormService = inject(PhongChieuFormService);
  protected activatedRoute = inject(ActivatedRoute);
  protected gheService = inject(GheService);

  editForm: PhongChieuFormGroup = this.phongChieuFormService.createPhongChieuFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ phongChieu }) => {
      this.phongChieu = phongChieu;
      if (phongChieu) {
        this.updateForm(phongChieu);
      }

      if (phongChieu?.id) {
        this.loadSeats();
      } else {
        this.initializeSeatLayoutDefaults();
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    this.saveErrorMessage = '';
    const phongChieu = {
      ...this.phongChieuFormService.getPhongChieu(this.editForm),
      trangThai: this.normalizeRoomStatus(this.editForm.controls.trangThai.value),
    };
    const save$ = phongChieu.id !== null ? this.phongChieuService.update(phongChieu) : this.phongChieuService.create(phongChieu);
    this.subscribeToSaveResponse(save$);
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPhongChieu>>): void {
    result.subscribe({
      next: res => {
        this.phongChieu = res.body ?? this.phongChieu;
        const savedRoomId = res.body?.id ?? this.editForm.controls.id.value ?? null;

        if (this.editForm.controls.id.value === null && res.body?.id) {
          this.editForm.patchValue({ id: res.body.id });
        }

        const shouldSaveSeats = !this.isEditMode || this.isSeatMapDirty || !this.hasSeatMap;
        if (shouldSaveSeats && savedRoomId) {
          const seatPayload = this.buildSeatPayloadFromLayout(savedRoomId);

          this.gheService.updateBatchSeats(savedRoomId, seatPayload).subscribe({
            next: () => {
              this.isSeatMapDirty = false;
              this.onSaveSuccess();
            },
            error: err => this.onSaveError(err),
          });
          return;
        }

        this.onSaveSuccess();
      },
      error: err => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.onSaveFinalize();
    this.previousState();
  }

  protected onSaveError(err?: any): void {
    this.saveErrorMessage =
      err?.error?.message || err?.error?.detail || err?.error?.title || 'Không thể lưu thông tin phòng chiếu. Vui lòng kiểm tra lại.';
    this.onSaveFinalize();
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(phongChieu: IPhongChieu): void {
    this.phongChieu = phongChieu;
    this.phongChieuFormService.resetForm(this.editForm, {
      ...phongChieu,
      trangThai: this.normalizeRoomStatus(phongChieu.trangThai),
    });
  }

  updateRowConfigs(): void {
    const existingTypes = new Map(this.rowConfigs.map(row => [row.rowLabel, row.type]));
    const tempConfigs: Array<{ rowLabel: string; type: SeatType }> = [];
    for (let i = 0; i < this.numRows; i++) {
      const label = this.toAlphabetLabel(i);
      tempConfigs.push({ rowLabel: label, type: existingTypes.get(label) ?? 1 });
    }
    this.rowConfigs = tempConfigs;
    this.syncSeatCountFromLayout();
  }

  onLayoutChange(): void {
    this.updateRowConfigs();
  }

  enableRegenLayout(): void {
    if (
      confirm('Lưu ý: Việc cấu hình lại sơ đồ sẽ xóa toàn bộ sơ đồ ghế hiện tại sau khi bạn lưu phòng. Bạn có chắc chắn muốn cấu hình lại?')
    ) {
      this.isSeatMapDirty = true;
      this.showRegenPanel = true;
    }
  }

  loadSeats(): void {
    if (!this.phongChieu?.id) {
      return;
    }

    this.isSeatMapLoading = true;
    this.gheService.getSeatsByPhongChieu(this.phongChieu.id).subscribe({
      next: (res: HttpResponse<IGhe[]>) => {
        this.ghes = (res.body ?? []).map(seat => ({ ...seat, trangThai: '1' }));

        if (this.ghes.length > 0) {
          const rowLabels = [...new Set(this.ghes.map(seat => seat.hang).filter((row): row is string => !!row))].sort();
          this.numRows = rowLabels.length;
          this.numSeatsPerRow = this.ghes.reduce((max, seat) => Math.max(max, seat.cot ?? 0), 0);
          this.rowConfigs = rowLabels.map(rowLabel => {
            const rowSeat = this.ghes.find(seat => seat.hang === rowLabel);
            const type = Number(rowSeat?.loaiGhe ?? 1) as SeatType;
            return { rowLabel, type };
          });
          this.syncSeatCountFromLayout();
        } else {
          this.initializeSeatLayoutDefaults();
        }

        this.rebuildSeatRows();
        this.isSeatMapLoading = false;
      },
      error: () => {
        this.isSeatMapLoading = false;
      },
    });
  }

  generateSeats(): void {
    if (!this.phongChieu?.id) {
      return;
    }

    this.isSeatMapLoading = true;
    this.gheService.generateSeatsByPhongChieu(this.phongChieu.id).subscribe({
      next: (res: HttpResponse<IGhe[]>) => {
        this.ghes = (res.body ?? []).map(seat => ({ ...seat, trangThai: '1' }));
        this.rebuildSeatRows();
        this.isSeatMapLoading = false;
      },
      error: () => {
        this.isSeatMapLoading = false;
      },
    });
  }

  generateSeatsLocal(): void {
    const savedRoomId = this.phongChieu?.id ?? this.editForm.controls.id.value;
    if (!savedRoomId) {
      return;
    }

    this.ghes = this.buildSeatPayloadFromLayout(savedRoomId);
    this.rebuildSeatRows();
    this.isSeatMapDirty = true;
    this.showRegenPanel = false;
  }

  selectSeatType(type: SeatType): void {
    this.selectedSeatType = type;
  }

  applySeatType(group: ISeatGroup): void {
    this.isSeatMapDirty = true;
    if (Number(this.selectedSeatType) === 3) {
      this.applyCoupleType(group.primarySeat);
      return;
    }

    group.seats.forEach(seat => {
      seat.loaiGhe = this.selectedSeatType;
    });
    this.rebuildSeatRows();
  }

  getSeatClass(seat: IGhe): string {
    if (Number(seat.loaiGhe) === 3) return 'seat-ghe-doi';
    if (Number(seat.loaiGhe) === 2) return 'seat-ghe-vip';
    return 'seat-ghe-thuong';
  }

  getSeatTitle(seat: IGhe): string {
    const seatCode = seat.maGhe ?? `${seat.hang ?? ''}${seat.cot ?? ''}`;
    if (Number(seat.loaiGhe) === 3) return `Ghế đôi - ${seatCode}`;
    if (Number(seat.loaiGhe) === 2) return `Ghế VIP - ${seatCode}`;
    return `Ghế thường - ${seatCode}`;
  }

  getGroupLabel(group: ISeatGroup): string {
    if (group.isCouple && group.seats.length === 2) {
      return `${group.seats[0].cot}-${group.seats[1].cot}`;
    }
    return `${group.primarySeat.cot ?? ''}`;
  }

  getGroupWidth(group: ISeatGroup): number {
    return group.isCouple ? 54 : 26;
  }

  getRowLabel(row: ISeatGroup[]): string {
    return row[0]?.primarySeat.hang ?? '';
  }

  getSeatLabel(seat: IGhe): string {
    return seat.maGhe ?? `${seat.hang ?? ''}${seat.cot ?? ''}`;
  }

  getSeat(row: string, col: number): IGhe | undefined {
    return this.ghes.find(g => g.hang === row && g.cot === col);
  }

  get isEditMode(): boolean {
    return this.editForm.controls.id.value !== null;
  }

  get hasSeatMap(): boolean {
    return this.ghes.length > 0;
  }

  get plannedSeatCount(): number {
    if (this.rowConfigs.length > 0) {
      return this.rowConfigs.reduce((total, rowConf) => total + this.getEffectiveSeatsPerRow(Number(rowConf.type) as SeatType), 0);
    }

    const value = Number(this.editForm.get('soLuongGhe')?.value ?? 0);
    return Number.isFinite(value) && value > 0 ? value : 0;
  }

  get isSeatCountLocked(): boolean {
    return true;
  }

  get seatRowsPreview(): string[] {
    const actualRows = [...new Set(this.ghes.map(seat => seat.hang).filter((row): row is string => !!row))];
    if (actualRows.length > 0) {
      return actualRows.sort();
    }

    if (this.rowConfigs.length > 0) {
      return this.rowConfigs.map(row => row.rowLabel);
    }

    return this.buildRowLabels(this.numRows);
  }

  get seatCols(): number[] {
    const maxCol = this.ghes.reduce((max, seat) => Math.max(max, seat.cot ?? 0), 0);
    const previewCols = Math.max(this.numSeatsPerRow, 0);
    const totalCols = maxCol || previewCols || this.seatColumnsPerRow;
    return Array.from({ length: totalCols }, (_, index) => index + 1);
  }

  getPreviewSeatNumbers(rowConf: { rowLabel: string; type: SeatType }): number[] {
    const count = this.getEffectiveSeatsPerRow(rowConf.type);
    return Array.from({ length: count }, (_, index) => index + 1);
  }

  getPreviewCouplePairs(rowConf: { rowLabel: string; type: SeatType }): Array<{ label: string }> {
    const count = this.getEffectiveSeatsPerRow(rowConf.type);
    const pairCount = Math.floor(count / 2);
    return Array.from({ length: pairCount }, (_, index) => {
      const left = index * 2 + 1;
      return { label: `${left}-${left + 1}` };
    });
  }

  getPreviewCoupleSeatNumbers(rowConf: { rowLabel: string; type: SeatType }): Array<{ left: number; right: number }> {
    const count = this.getEffectiveSeatsPerRow(rowConf.type);
    const pairCount = Math.floor(count / 2);
    return Array.from({ length: pairCount }, (_, index) => {
      const left = index * 2 + 1;
      return { left, right: left + 1 };
    });
  }

  getPreviewSeatVariant(rowType: SeatType): string {
    if (rowType === 3) {
      return 'preview-seat-double';
    }
    if (rowType === 2) {
      return 'preview-seat-vip';
    }
    return 'preview-seat-single';
  }

  get totalSeatCount(): number {
    return this.ghes.length;
  }

  get canCreateShowtime(): boolean {
    return this.isEditMode && this.hasSeatMap;
  }

  getRoomStatusLabel(status?: string | null): string {
    const normalized = this.normalizeRoomStatus(status);
    if (normalized === 'Bảo Trì') {
      return 'Bảo trì';
    }
    if (normalized === 'Không Hoạt Động') {
      return 'Ngừng khai thác';
    }
    return 'Hoạt động';
  }

  getRoomStatusClass(status?: string | null): string {
    const normalized = this.normalizeRoomStatus(status);
    if (normalized === 'Bảo Trì') {
      return 'text-bg-warning';
    }
    if (normalized === 'Không Hoạt Động') {
      return 'text-bg-secondary';
    }
    return 'text-bg-success';
  }

  private initializeSeatLayoutDefaults(): void {
    this.updateRowConfigs();
    this.syncSeatCountFromLayout();
  }

  private syncSeatCountFromLayout(): void {
    const totalSeats =
      this.rowConfigs.length > 0
        ? this.rowConfigs.reduce((total, rowConf) => total + this.getEffectiveSeatsPerRow(Number(rowConf.type) as SeatType), 0)
        : Math.max(1, this.numRows) * Math.max(1, this.numSeatsPerRow);
    this.editForm.patchValue({ soLuongGhe: totalSeats });
  }

  private buildSeatPayloadFromLayout(savedRoomId: number): IGhe[] {
    const seats: IGhe[] = [];
    for (const rowConf of this.rowConfigs) {
      const rowType = Number(rowConf.type) as SeatType;
      const seatsInRow = this.getEffectiveSeatsPerRow(rowType);
      for (let col = 1; col <= seatsInRow; col++) {
        seats.push({
          id: null as any,
          maGhe: `${rowConf.rowLabel}${col}`,
          hang: rowConf.rowLabel,
          cot: col,
          loaiGhe: rowType,
          trangThai: '1',
          phongChieu: { id: savedRoomId, tenPhong: this.editForm.controls.tenPhong.value ?? null },
        });
      }
    }
    return seats;
  }

  private getEffectiveSeatsPerRow(rowType: SeatType): number {
    const baseSeats = Math.max(1, this.numSeatsPerRow);
    if (rowType !== 3) {
      return baseSeats;
    }

    const evenSeats = baseSeats % 2 === 0 ? baseSeats : baseSeats - 1;
    return evenSeats >= 2 ? evenSeats : 0;
  }

  private rebuildSeatRows(): void {
    const seatMap = new Map<string, IGhe[]>();
    this.ghes.forEach(seat => {
      const rowLabel = seat.hang?.charAt(0);
      if (!rowLabel) {
        return;
      }
      if (!seatMap.has(rowLabel)) {
        seatMap.set(rowLabel, []);
      }
      seatMap.get(rowLabel)?.push(seat);
    });

    this.seatRows = Array.from(seatMap.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([, rowSeats]) =>
        rowSeats
          .sort((a, b) => (a.cot ?? 0) - (b.cot ?? 0))
          .reduce<ISeatGroup[]>((groups, seat, index, seats) => {
            if (this.isCoupleSeat(seat)) {
              const previousSeat = seats[index - 1];
              if (
                previousSeat &&
                this.isCoupleSeat(previousSeat) &&
                previousSeat.hang === seat.hang &&
                (previousSeat.cot ?? 0) + 1 === (seat.cot ?? 0)
              ) {
                const lastGroup = groups[groups.length - 1];
                if (lastGroup?.isCouple && lastGroup.seats.length === 1 && lastGroup.primarySeat.id === previousSeat.id) {
                  lastGroup.seats.push(seat);
                  return groups;
                }
              }
            }

            groups.push({
              id: `${seat.hang}-${seat.cot}`,
              isCouple: this.isCoupleSeat(seat),
              seats: [seat],
              primarySeat: seat,
            });
            return groups;
          }, []),
      );
  }

  trackGroup = (_index: number, group: ISeatGroup): string => group.id;

  private applyCoupleType(anchorSeat: IGhe): void {
    const rowSeats = this.ghes.filter(seat => seat.hang === anchorSeat.hang).sort((a, b) => (a.cot ?? 0) - (b.cot ?? 0));
    const currentIndex = rowSeats.findIndex(seat => seat.id === anchorSeat.id);
    if (currentIndex < 0) {
      return;
    }

    const leftSeat = rowSeats[currentIndex - 1];
    const rightSeat = rowSeats[currentIndex + 1];
    const pairSeat = leftSeat && currentIndex % 2 === 1 ? leftSeat : rightSeat;
    const primarySeat = pairSeat ?? anchorSeat;
    const secondarySeat = pairSeat ? anchorSeat : rightSeat;

    if (!secondarySeat) {
      alert('Ghế đôi cần 2 ghế liền nhau trong cùng một hàng.');
      return;
    }

    primarySeat.loaiGhe = 3;
    secondarySeat.loaiGhe = 3;
    this.isSeatMapDirty = true;
    this.rebuildSeatRows();
  }

  private buildRowLabels(totalRows: number): string[] {
    return Array.from({ length: Math.max(totalRows, 0) }, (_, index) => this.toAlphabetLabel(index));
  }

  private toAlphabetLabel(index: number): string {
    let value = index + 1;
    let label = '';
    while (value > 0) {
      value -= 1;
      label = String.fromCharCode(65 + (value % 26)) + label;
      value = Math.floor(value / 26);
    }
    return label;
  }

  private isCoupleSeat(seat: IGhe): boolean {
    return Number(seat.loaiGhe ?? 1) === 3;
  }

  private normalizeRoomStatus(status?: string | null): string {
    const normalized = String(status ?? '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');

    if (normalized.includes('bao') || normalized.includes('tri')) {
      return 'Bảo Trì';
    }
    if (normalized.includes('khong') || normalized.includes('ngung') || normalized.includes('inactive')) {
      return 'Không Hoạt Động';
    }
    return 'Hoạt Động';
  }
}
