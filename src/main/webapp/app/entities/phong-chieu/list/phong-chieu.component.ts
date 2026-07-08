import { Component, NgZone, OnInit, OnDestroy, inject, signal } from '@angular/core';
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
import { IPhongChieu } from '../phong-chieu.model';
import { EntityArrayResponseType, PhongChieuService } from '../service/phong-chieu.service';
import { PhongChieuDeleteDialogComponent } from '../delete/phong-chieu-delete-dialog.component';

@Component({
  selector: 'jhi-phong-chieu',
  templateUrl: './phong-chieu.component.html',
  styleUrls: ['./phong-chieu.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, ItemCountComponent],
})
export class PhongChieuComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  phongChieus = signal<IPhongChieu[]>([]);
  isLoading = false;
  searchTerm = '';
  searchSuggestions: string[] = [];

  sortState = sortStateSignal({});

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly phongChieuService = inject(PhongChieuService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IPhongChieu): number => this.phongChieuService.getPhongChieuIdentifier(item);

  getRoomStatusLabel(status?: string | number | null): string {
    const normalized = String(status ?? '').toLowerCase();
    if (normalized.includes('bao') || normalized.includes('tri')) {
      return 'Bảo trì';
    }
    if (normalized.includes('khong')) {
      return 'Ngừng khai thác';
    }
    return 'Hoạt động';
  }

  getRoomStatusClass(status?: string | number | null): string {
    const normalized = String(status ?? '').toLowerCase();
    if (normalized.includes('bao') || normalized.includes('tri')) {
      return 'text-bg-warning';
    }
    if (normalized.includes('khong')) {
      return 'text-bg-secondary';
    }
    return 'text-bg-success';
  }

  getSeatReadinessLabel(room: IPhongChieu): string {
    const seatCount = Number(room.soLuongGhe ?? 0);
    if (seatCount <= 0) {
      return 'Chưa khai báo số ghế';
    }
    if (seatCount < 50) {
      return `${seatCount} ghế - phòng nhỏ`;
    }
    if (seatCount < 100) {
      return `${seatCount} ghế - phòng tiêu chuẩn`;
    }
    return `${seatCount} ghế - phòng lớn`;
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(phongChieu: IPhongChieu): void {
    const modalRef = this.modalService.open(PhongChieuDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.phongChieu = phongChieu;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page, event);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.searchTerm = params.get('search') ?? '';
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.phongChieus.set(dataFromBody);
    this.searchSuggestions = this.buildSearchSuggestions(dataFromBody);
  }

  protected fillComponentAttributesFromResponseBody(data: IPhongChieu[] | null): IPhongChieu[] {
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
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    if (this.searchTerm.trim()) {
      queryObject.query = this.searchTerm.trim();
      return this.phongChieuService.search(queryObject).pipe(tap(() => (this.isLoading = false)));
    }
    return this.phongChieuService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };
    if (this.searchTerm.trim()) {
      queryParamsObj.search = this.searchTerm.trim();
    }

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }

  onSearch(): void {
    this.handleNavigation(1, this.sortState());
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.handleNavigation(1, this.sortState());
  }
  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private buildSearchSuggestions(phongChieus: IPhongChieu[]): string[] {
    return Array.from(
      new Set(
        phongChieus
          .flatMap(phong => [phong.tenPhong, `Phòng #${phong.id}`, phong.trangThai])
          .map(value => value?.trim())
          .filter((value): value is string => !!value),
      ),
    ).slice(0, 12);
  }
}
