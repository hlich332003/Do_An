import dayjs from 'dayjs/esm';
import { Component, NgZone, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ISuatChieu } from '../suat-chieu.model';
import { EntityArrayResponseType, SuatChieuService } from '../service/suat-chieu.service';
import { SuatChieuDeleteDialogComponent } from '../delete/suat-chieu-delete-dialog.component';
import { IPhim } from 'app/entities/phim/phim.model';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { IPhongChieu } from 'app/entities/phong-chieu/phong-chieu.model';
import { PhongChieuService } from 'app/entities/phong-chieu/service/phong-chieu.service';

interface TimelineRoom {
  key: string;
  title: string;
  showtimes: TimelineShowtime[];
  maxLane: number;
}

interface TimelineShowtime extends ISuatChieu {
  lane: number;
}

@Component({
  selector: 'jhi-suat-chieu',
  templateUrl: './suat-chieu.component.html',
  styleUrls: ['./suat-chieu.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, ItemCountComponent],
})
export class SuatChieuComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  suatChieus = signal<ISuatChieu[]>([]);
  phims: IPhim[] = [];
  phongChieus: IPhongChieu[] = [];
  timelineRooms: TimelineRoom[] = [];
  conflictIds = new Set<number>();
  isLoading = false;
  isFiltered = false;
  searchTerm = '';
  searchSuggestions: string[] = [];
  phimFilterText = '';

  sortState = sortStateSignal({});

  itemsPerPage = 50;
  totalItems = 0;
  page = 1;
  selectedPhimIds: number[] = [];
  selectedPhongChieuId: number | null = null;
  selectedNgayChieu: string | null = dayjs().format('YYYY-MM-DD');
  selectedGioBatDau: string | null = null;
  selectedGioKetThuc: string | null = null;
  readonly timelineStartHour = 8;
  readonly timelineEndHour = 25;
  readonly timelineTicks = [8, 10, 12, 14, 16, 18, 20, 22, 24, 25];

  public readonly router = inject(Router);
  protected readonly suatChieuService = inject(SuatChieuService);
  protected readonly phimService = inject(PhimService);
  protected readonly phongChieuService = inject(PhongChieuService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: ISuatChieu): number => this.suatChieuService.getSuatChieuIdentifier(item);

  ngOnInit(): void {
    this.loadLookupData();
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(suatChieu: ISuatChieu): void {
    const modalRef = this.modalService.open(SuatChieuDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.suatChieu = suatChieu;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  cleanupOverlaps(): void {
    if (!window.confirm('Bạn có muốn xóa các suất chiếu bị trùng hoặc chồng giờ trong cùng một phòng không?')) {
      return;
    }
    this.isLoading = true;
    this.suatChieuService.cleanupOverlaps().subscribe({
      next: () => this.load(),
      error: () => {
        this.isLoading = false;
      },
    });
  }

  load(): void {
    if (this.isAnyFilterActive()) {
      this.loadFiltered();
      return;
    }

    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  clearFilters(): void {
    this.selectedPhimIds = [];
    this.phimFilterText = '';
    this.selectedPhongChieuId = null;
    this.selectedNgayChieu = null;
    this.selectedGioBatDau = null;
    this.selectedGioKetThuc = null;
    this.searchTerm = '';
    this.page = 1;
    this.updateUrlQueryParams();
  }

  clearPhimFilter(): void {
    this.selectedPhimIds = [];
    this.phimFilterText = '';
    this.page = 1;
    this.updateUrlQueryParams();
  }

  clearPhongFilter(): void {
    this.selectedPhongChieuId = null;
    this.page = 1;
    this.updateUrlQueryParams();
  }

  clearNgayFilter(): void {
    this.selectedNgayChieu = null;
    this.page = 1;
    this.updateUrlQueryParams();
  }

  clearGioFilter(): void {
    this.selectedGioBatDau = null;
    this.selectedGioKetThuc = null;
    this.page = 1;
    this.updateUrlQueryParams();
  }

  clearKeywordFilter(): void {
    this.searchTerm = '';
    this.page = 1;
    this.updateUrlQueryParams();
  }

  useQuickDate(offsetDays: number): void {
    this.selectedNgayChieu = dayjs().add(offsetDays, 'day').format('YYYY-MM-DD');
    this.page = 1;
    this.updateUrlQueryParams();
  }

  onFilterChange(): void {
    this.page = 1;
    this.updateUrlQueryParams();
  }

  onPhimFilterChange(): void {
    const term = this.phimFilterText.trim().toLowerCase();
    if (!term) {
      this.selectedPhimIds = [];
    } else {
      const match = this.phims.find(p => p.tenPhim?.trim().toLowerCase() === term);
      if (match?.id != null) {
        this.selectedPhimIds = [match.id];
      } else {
        this.selectedPhimIds = [];
      }
    }
    this.onFilterChange();
  }

  syncPhimFilterText(): void {
    if (this.selectedPhimIds.length > 0) {
      const phimId = this.selectedPhimIds[0];
      const phim = this.phims.find(p => p.id === phimId);
      if (phim) {
        this.phimFilterText = phim.tenPhim ?? '';
      } else {
        this.phimFilterText = '';
      }
    } else {
      this.phimFilterText = '';
    }
  }

  updateUrlQueryParams(): void {
    const queryParamsObj: Record<string, any> = {
      page: this.page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(this.sortState()),
      phimIds: null,
    };

    if (this.selectedPhimIds.length > 0) {
      queryParamsObj['phimId'] = this.selectedPhimIds[0];
    } else {
      queryParamsObj['phimId'] = null;
    }
    if (this.selectedPhongChieuId !== null) {
      queryParamsObj['phongChieuId'] = this.selectedPhongChieuId;
    } else {
      queryParamsObj['phongChieuId'] = null;
    }
    if (this.selectedNgayChieu) {
      queryParamsObj['ngayChieu'] = this.selectedNgayChieu;
    } else {
      queryParamsObj['ngayChieu'] = null;
    }
    if (this.selectedGioBatDau) {
      queryParamsObj['gioBatDau'] = this.selectedGioBatDau;
    } else {
      queryParamsObj['gioBatDau'] = null;
    }
    if (this.selectedGioKetThuc) {
      queryParamsObj['gioKetThuc'] = this.selectedGioKetThuc;
    } else {
      queryParamsObj['gioKetThuc'] = null;
    }
    if (this.searchTerm.trim()) {
      queryParamsObj['search'] = this.searchTerm.trim();
    } else {
      queryParamsObj['search'] = null;
    }

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.sortState.set(event);
    this.updateUrlQueryParams();
  }

  navigateToPage(page: number): void {
    this.page = page;
    this.updateUrlQueryParams();
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));

    const phimId = params.get('phimId');
    this.selectedPhimIds = phimId ? [Number(phimId)] : [];

    const phongChieuId = params.get('phongChieuId');
    this.selectedPhongChieuId = phongChieuId ? Number(phongChieuId) : null;
    this.selectedNgayChieu = params.get('ngayChieu') ?? this.selectedNgayChieu;
    this.selectedGioBatDau = params.get('gioBatDau') ?? params.get('gioChieu');
    this.selectedGioKetThuc = params.get('gioKetThuc') ?? null;
    this.searchTerm = params.get('search') ?? '';
    this.syncPhimFilterText();
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.suatChieus.set(dataFromBody);
    this.refreshPresentationState(dataFromBody);
    this.isFiltered = false;
  }

  protected fillComponentAttributesFromResponseBody(data: ISuatChieu[] | null): ISuatChieu[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { page } = this;

    this.isLoading = true;
    const pageToLoad: number = page;
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    return this.suatChieuService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  private loadFiltered(): void {
    this.isLoading = true;
    this.isFiltered = true;
    this.suatChieuService
      .search({
        phimIds: this.selectedPhimIds.length > 0 ? this.selectedPhimIds : undefined,
        phongChieuId: this.selectedPhongChieuId ?? undefined,
        ngayChieu: this.selectedNgayChieu ?? undefined,
        gioBatDau: this.selectedGioBatDau ?? undefined,
        gioKetThuc: this.selectedGioKetThuc ?? undefined,
        gioChieu: this.selectedGioBatDau ?? undefined,
        query: this.searchTerm.trim() || undefined,
      })
      .subscribe({
        next: res => {
          const filtered = (res.body ?? []).sort((a, b) => {
            const aTime = a.thoiGianBatDau?.valueOf() ?? 0;
            const bTime = b.thoiGianBatDau?.valueOf() ?? 0;
            return bTime - aTime;
          });
          this.suatChieus.set(filtered);
          this.refreshPresentationState(filtered);
          this.totalItems = filtered.length;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
        },
      });
  }

  private loadLookupData(): void {
    this.phimService.query({ size: 200, sort: ['tenPhim,asc'] }).subscribe(res => {
      this.phims = res.body ?? [];
      this.refreshSearchSuggestions();
      this.syncPhimFilterText();
    });
    this.phongChieuService.query({ size: 100, sort: ['tenPhong,asc'] }).subscribe(res => {
      this.phongChieus = res.body ?? [];
      this.refreshSearchSuggestions();
    });
  }

  private isAnyFilterActive(): boolean {
    return (
      this.selectedPhimIds.length > 0 ||
      this.selectedPhongChieuId !== null ||
      !!this.selectedNgayChieu ||
      !!this.selectedGioBatDau ||
      !!this.selectedGioKetThuc ||
      !!this.searchTerm.trim()
    );
  }

  get selectedNgayChieuLabel(): string {
    return this.selectedNgayChieu ? dayjs(this.selectedNgayChieu).format('DD/MM/YYYY') : 'Tất cả';
  }

  formatClock(value: dayjs.Dayjs | null | undefined): string {
    return value ? value.format('HH:mm') : '--:--';
  }

  formatDateTime(value: dayjs.Dayjs | null | undefined): string {
    return value ? value.format('HH:mm DD/MM') : '--';
  }

  formatPrice(value: number | null | undefined): string {
    return new Intl.NumberFormat('vi-VN').format(value ?? 0);
  }

  getTimelineOffset(item: ISuatChieu): number {
    const start = item.thoiGianBatDau;
    if (!start) {
      return 0;
    }
    const minutesFromTimelineStart = start.hour() * 60 + start.minute() - this.timelineStartHour * 60;
    return Math.max(0, (minutesFromTimelineStart / this.getTimelineDurationMinutes()) * 100);
  }

  getTimelineWidth(item: ISuatChieu): number {
    const start = item.thoiGianBatDau;
    const end = item.thoiGianKetThuc;
    if (!start || !end) {
      return 0;
    }
    const startMinutes = start.hour() * 60 + start.minute();
    let endMinutes = end.hour() * 60 + end.minute();
    if (endMinutes <= startMinutes) {
      endMinutes += 24 * 60;
    }
    const widthMinutes = Math.max(endMinutes - startMinutes, 15);
    return Math.max(4, (widthMinutes / this.getTimelineDurationMinutes()) * 100);
  }

  isConflict(item: ISuatChieu): boolean {
    return !!item.id && this.conflictIds.has(item.id);
  }

  getRoomCount(): number {
    return this.timelineRooms.length;
  }

  getConflictCount(): number {
    return this.conflictIds.size;
  }

  private refreshPresentationState(data: ISuatChieu[]): void {
    const normalized = [...data].sort((a, b) => {
      const roomA = a.phongChieu?.tenPhong ?? '';
      const roomB = b.phongChieu?.tenPhong ?? '';
      if (roomA !== roomB) {
        return roomA.localeCompare(roomB, 'vi');
      }
      return (a.thoiGianBatDau?.valueOf() ?? 0) - (b.thoiGianBatDau?.valueOf() ?? 0);
    });

    this.timelineRooms = this.buildTimelineRooms(normalized);
    this.conflictIds = this.buildConflictIds(normalized);
  }

  private buildTimelineRooms(data: ISuatChieu[]): TimelineRoom[] {
    const roomMap = new Map<string, TimelineRoom>();

    for (const item of data) {
      const key = item.phongChieu?.id?.toString() ?? 'unknown';
      const title = item.phongChieu?.tenPhong ?? 'Chưa gắn phòng';
      const room = roomMap.get(key);
      if (room) {
        room.showtimes.push({ ...item, lane: 0 });
      } else {
        roomMap.set(key, { key, title, showtimes: [{ ...item, lane: 0 }], maxLane: 1 });
      }
    }

    return Array.from(roomMap.values()).map(room => {
      const sorted = [...room.showtimes].sort((a, b) => (a.thoiGianBatDau?.valueOf() ?? 0) - (b.thoiGianBatDau?.valueOf() ?? 0));
      const lanes: number[] = [];
      const placed = sorted.map(item => {
        const start = item.thoiGianBatDau?.valueOf() ?? 0;
        const end = item.thoiGianKetThuc?.valueOf() ?? start;
        let laneIndex = lanes.findIndex(lastEnd => start >= lastEnd);
        if (laneIndex < 0) {
          laneIndex = lanes.length;
          lanes.push(end);
        } else {
          lanes[laneIndex] = end;
        }
        return { ...item, lane: laneIndex };
      });

      return {
        ...room,
        showtimes: placed,
        maxLane: Math.max(1, lanes.length),
      };
    });
  }

  private buildConflictIds(data: ISuatChieu[]): Set<number> {
    const conflictIds = new Set<number>();
    const roomBuckets = this.buildTimelineRooms(data);

    for (const room of roomBuckets) {
      let previousEnd: number | null = null;
      let previousItem: ISuatChieu | null = null;

      for (const item of room.showtimes) {
        const start = item.thoiGianBatDau?.valueOf() ?? null;
        const end = item.thoiGianKetThuc?.valueOf() ?? null;
        if (start !== null && previousEnd !== null && start < previousEnd) {
          if (previousItem?.id !== undefined && previousItem.id !== null) {
            conflictIds.add(previousItem.id);
          }
          if (item.id !== undefined && item.id !== null) {
            conflictIds.add(item.id);
          }
        }
        if (end !== null && (previousEnd === null || end > previousEnd)) {
          previousEnd = end;
          previousItem = item;
        }
      }
    }

    return conflictIds;
  }

  private refreshSearchSuggestions(): void {
    this.searchSuggestions = Array.from(
      new Set([
        ...this.phims.map(phim => phim.tenPhim?.trim()).filter((value): value is string => !!value),
        ...this.phongChieus.map(phong => phong.tenPhong?.trim()).filter((value): value is string => !!value),
      ]),
    ).slice(0, 20);
  }

  getSelectedPhimLabels(): string[] {
    return this.selectedPhimIds
      .map(id => this.phims.find(phim => phim.id === id)?.tenPhim?.trim())
      .filter((value): value is string => !!value);
  }

  private getTimelineDurationMinutes(): number {
    return (this.timelineEndHour - this.timelineStartHour) * 60;
  }

  getTimelineRowHeight(room: TimelineRoom): number {
    return Math.max(92, 42 + room.maxLane * 42);
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj: Record<string, any> = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };

    if (this.selectedPhimIds.length > 0) {
      queryParamsObj['phimIds'] = this.selectedPhimIds;
    }
    if (this.selectedPhongChieuId !== null) {
      queryParamsObj['phongChieuId'] = this.selectedPhongChieuId;
    }
    if (this.selectedNgayChieu) {
      queryParamsObj['ngayChieu'] = this.selectedNgayChieu;
    }
    if (this.selectedGioBatDau) {
      queryParamsObj['gioBatDau'] = this.selectedGioBatDau;
    }
    if (this.selectedGioKetThuc) {
      queryParamsObj['gioKetThuc'] = this.selectedGioKetThuc;
    }
    if (this.searchTerm.trim()) {
      queryParamsObj['search'] = this.searchTerm.trim();
    }

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }
  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
