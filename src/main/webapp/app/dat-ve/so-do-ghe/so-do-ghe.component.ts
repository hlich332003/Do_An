import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Subject, Subscription, firstValueFrom, interval } from 'rxjs';
import { startWith, switchMap, takeUntil } from 'rxjs/operators';
import { IGhe } from 'app/entities/ghe/ghe.model';
import { GheService } from 'app/entities/ghe/service/ghe.service';
import { BookingService } from '../service/booking.service';

export type TrangThaiGheTrongSuatChieu = 'TRONG' | 'DANG_GIU' | 'DA_BAN' | 'DANG_CHON';

export interface IGheVM extends IGhe {
  trangThaiTrongSuat?: TrangThaiGheTrongSuatChieu;
}

interface ISeatDisplayGroup {
  id: string;
  isCouple: boolean;
  seats: IGheVM[];
  state: TrangThaiGheTrongSuatChieu;
  primarySeat: IGheVM;
}

@Component({
  selector: 'jhi-so-do-ghe',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './so-do-ghe.component.html',
  styleUrls: ['./so-do-ghe.component.scss'],
})
export class SoDoGheComponent implements OnInit, OnDestroy {
  @Input() suatChieuId!: number;
  @Input() phongChieuId?: number;
  @Input() initialGheDaChon: IGheVM[] = [];
  @Output() gheDaChonChange = new EventEmitter<IGheVM[]>();

  seatRows: ISeatDisplayGroup[][] = [];
  gheDaChon: IGheVM[] = [];
  isLoading = false;
  errorMessage = '';

  protected gheService = inject(GheService);
  protected bookingService = inject(BookingService);

  private readonly destroy$ = new Subject<void>();
  private pollingSubscription?: Subscription;

  ngOnInit(): void {
    this.gheDaChon = [...this.initialGheDaChon];

    this.pollingSubscription = interval(3000)
      .pipe(
        startWith(0),
        switchMap(() => this.gheService.getSeatsForShowtime(this.suatChieuId, this.phongChieuId!)),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (res: HttpResponse<IGhe[]>) => this.updateSeats(res.body ?? []),
        error: () => {
          this.errorMessage = 'Không tải được sơ đồ ghế.';
        },
      });
  }

  updateSeats(ghes: IGhe[]): void {
    const selectedSeatIds = new Set(this.gheDaChon.map(g => g.id));
    const ghesVM: IGheVM[] = ghes.map(ghe => ({
      ...ghe,
      loaiGhe: ghe.loaiGhe != null ? Number(ghe.loaiGhe) : null,
      trangThaiTrongSuat: this.mapSeatState(ghe, selectedSeatIds),
    }));

    const soldSeatIds = new Set(ghesVM.filter(ghe => ghe.trangThaiTrongSuat === 'DA_BAN').map(ghe => ghe.id));
    const nextSelected = this.gheDaChon.filter(seat => !soldSeatIds.has(seat.id));
    if (nextSelected.length !== this.gheDaChon.length) {
      this.gheDaChon = nextSelected;
      this.gheDaChonChange.emit(this.gheDaChon);
    }

    this.organizeSeatsIntoRows(ghesVM);
    this.isLoading = false;
  }

  private mapSeatState(ghe: IGheVM, selectedSeatIds: Set<number>): TrangThaiGheTrongSuatChieu {
    const baseState = this.mapTrangThai(ghe.trangThai);
    if (baseState === 'DA_BAN') {
      return 'DA_BAN';
    }
    if (selectedSeatIds.has(ghe.id)) {
      return 'DANG_CHON';
    }
    return baseState;
  }

  mapTrangThai(trangThai?: string | number | null): TrangThaiGheTrongSuatChieu {
    const normalized = `${trangThai ?? ''}`.trim().toUpperCase();
    if (normalized === '2' || normalized === 'DA_BAN' || normalized === 'DA DAT' || normalized === 'SOLD') return 'DA_BAN';
    if (normalized === '1' || normalized === 'DANG_GIU' || normalized === 'DANG GIU') return 'DANG_GIU';
    return 'TRONG';
  }

  organizeSeatsIntoRows(ghes: IGheVM[]): void {
    const seatMap = new Map<string, IGheVM[]>();
    ghes.forEach(ghe => {
      const rowLabel = ghe.hang ?? ghe.maGhe?.charAt(0);
      if (!rowLabel) return;
      if (!seatMap.has(rowLabel)) seatMap.set(rowLabel, []);
      seatMap.get(rowLabel)?.push(ghe);
    });

    this.seatRows = Array.from(seatMap.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([, rowSeats]) => {
        const ordered = [...rowSeats].sort((a, b) => (a.cot ?? 0) - (b.cot ?? 0));
        const groups: ISeatDisplayGroup[] = [];

        for (let i = 0; i < ordered.length; i++) {
          const seat = ordered[i];
          const nextSeat = ordered[i + 1];
          const isCouplePair =
            this.isCoupleSeat(seat) &&
            !!nextSeat &&
            this.isCoupleSeat(nextSeat) &&
            seat.hang === nextSeat.hang &&
            (seat.cot ?? 0) + 1 === (nextSeat.cot ?? 0);

          if (isCouplePair) {
            const group: ISeatDisplayGroup = {
              id: `${seat.hang}-${seat.cot}-${nextSeat.cot}`,
              isCouple: true,
              seats: [seat, nextSeat],
              state: seat.trangThaiTrongSuat ?? nextSeat.trangThaiTrongSuat ?? 'TRONG',
              primarySeat: seat,
            };
            this.refreshGroupState(group);
            groups.push(group);
            i++;
            continue;
          }

          const group: ISeatDisplayGroup = {
            id: `${seat.hang}-${seat.cot}`,
            isCouple: false,
            seats: [seat],
            state: seat.trangThaiTrongSuat ?? 'TRONG',
            primarySeat: seat,
          };
          this.refreshGroupState(group);
          groups.push(group);
        }

        return groups;
      });
  }

  async toggleSeat(group: ISeatDisplayGroup): Promise<void> {
    if (group.state === 'DA_BAN' || group.state === 'DANG_GIU') return;
    if (group.isCouple && group.seats.length === 2) {
      await this.toggleCoupleSeat(group);
      return;
    }

    const seat = group.primarySeat;
    if (seat.trangThaiTrongSuat === 'TRONG') {
      if (this.gheDaChon.length >= 8) {
        alert('Bạn chỉ được chọn tối đa 8 ghế trong một giao dịch!');
        return;
      }
      try {
        await firstValueFrom(this.bookingService.holdSeat(this.suatChieuId, seat.maGhe!));
        seat.trangThaiTrongSuat = 'DANG_CHON';
        this.pushSelectedSeats([seat]);
      } catch (err: any) {
        this.handleHoldSeatError(seat, err);
      } finally {
        this.refreshGroupState(group);
      }
      return;
    }

    if (seat.trangThaiTrongSuat === 'DANG_CHON') {
      await firstValueFrom(this.bookingService.releaseSeat(this.suatChieuId, seat.maGhe!));
      seat.trangThaiTrongSuat = 'TRONG';
      this.removeSelectedSeats([seat]);
      this.refreshGroupState(group);
    }
  }

  getDisplayLabel(group: ISeatDisplayGroup): string {
    if (group.isCouple && group.seats.length === 2) {
      return `${group.seats[0].maGhe} • ${group.seats[1].maGhe}`;
    }
    return group.primarySeat.maGhe ?? '';
  }

  getDisplayNumber(group: ISeatDisplayGroup): string {
    if (group.isCouple && group.seats.length === 2) {
      return `${group.seats[0].cot}-${group.seats[1].cot}`;
    }
    return `${group.primarySeat.cot ?? ''}`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.pollingSubscription?.unsubscribe();
  }

  private async toggleCoupleSeat(group: ISeatDisplayGroup): Promise<void> {
    const seats = group.seats;
    if (seats.length !== 2) {
      return;
    }
    const seatCodes = seats.map(seat => seat.maGhe).join(' - ');

    if (group.state === 'TRONG') {
      if (this.gheDaChon.length + 2 > 8) {
        alert('Bạn chỉ được chọn tối đa 8 ghế trong một giao dịch!');
        return;
      }
      const lockedSeats: IGheVM[] = [];
      try {
        for (const seat of seats) {
          await firstValueFrom(this.bookingService.holdSeat(this.suatChieuId, seat.maGhe!));
          seat.trangThaiTrongSuat = 'DANG_CHON';
          lockedSeats.push(seat);
        }
        this.pushSelectedSeats(seats);
      } catch {
        await Promise.all(
          lockedSeats.map(async lockedSeat => {
            try {
              await firstValueFrom(this.bookingService.releaseSeat(this.suatChieuId, lockedSeat.maGhe!));
            } catch {
              return;
            }
          }),
        );
        seats.forEach(seat => {
          seat.trangThaiTrongSuat = seat.trangThaiTrongSuat === 'DA_BAN' ? 'DA_BAN' : 'TRONG';
        });
        alert(`Ghế đôi ${seatCodes} hiện không thể giữ trọn cặp. Bạn vui lòng chọn ghế khác nhé!`);
      } finally {
        this.refreshGroupState(group);
      }
      return;
    }

    if (group.state === 'DANG_CHON') {
      await Promise.all(seats.map(seat => firstValueFrom(this.bookingService.releaseSeat(this.suatChieuId, seat.maGhe!))));
      seats.forEach(seat => {
        seat.trangThaiTrongSuat = 'TRONG';
      });
      this.removeSelectedSeats(seats);
      this.refreshGroupState(group);
    }
  }

  private pushSelectedSeats(seats: IGheVM[]): void {
    const selectedMap = new Map(this.gheDaChon.map(seat => [seat.id, seat]));
    seats.forEach(seat => selectedMap.set(seat.id, seat));
    this.gheDaChon = Array.from(selectedMap.values());
    this.gheDaChonChange.emit(this.gheDaChon);
  }

  private removeSelectedSeats(seats: IGheVM[]): void {
    const idsToRemove = new Set(seats.map(seat => seat.id));
    this.gheDaChon = this.gheDaChon.filter(seat => !idsToRemove.has(seat.id));
    this.gheDaChonChange.emit(this.gheDaChon);
  }

  private refreshGroupState(group: ISeatDisplayGroup): void {
    if (group.seats.every(seat => seat.trangThaiTrongSuat === 'DANG_CHON')) {
      group.state = 'DANG_CHON';
      return;
    }
    if (group.seats.some(seat => seat.trangThaiTrongSuat === 'DA_BAN')) {
      group.state = 'DA_BAN';
      return;
    }
    if (group.seats.some(seat => seat.trangThaiTrongSuat === 'DANG_GIU')) {
      group.state = 'DANG_GIU';
      return;
    }
    if (group.seats.some(seat => seat.trangThaiTrongSuat === 'DANG_CHON')) {
      group.state = 'DANG_CHON';
      return;
    }
    group.state = 'TRONG';
  }

  private handleHoldSeatError(seat: IGheVM, err: any): void {
    seat.trangThaiTrongSuat = 'DANG_GIU';
    if (err.status === 409) {
      alert(`Rất tiếc! Ghế ${seat.maGhe} vừa có người nhanh tay hơn chọn mất rồi. Bạn vui lòng chọn lại ghế khác nhé!`);
      seat.trangThaiTrongSuat = 'DA_BAN';
      return;
    }
    alert('Ghế đang được giữ bởi người khác hoặc có lỗi xảy ra.');
  }

  private isCoupleSeat(seat: IGheVM): boolean {
    return Number(seat.loaiGhe) === 3;
  }
}
