import { Component, NgZone, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IHoaDon } from '../hoa-don.model';
import { EntityArrayResponseType, HoaDonService } from '../service/hoa-don.service';
import { HoaDonDeleteDialogComponent } from '../delete/hoa-don-delete-dialog.component';
import { BookingService, IHoaDonLichSu } from 'app/dat-ve/service/booking.service';

@Component({
  selector: 'jhi-hoa-don',
  templateUrl: './hoa-don.component.html',
  styleUrls: ['./hoa-don.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, FormatMediumDatetimePipe, ItemCountComponent],
})
export class HoaDonComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  hoaDons = signal<IHoaDon[]>([]);
  isLoading = false;
  searchTerm = '';
  fromDate = '';
  toDate = '';
  searchSuggestions: string[] = [];

  sortState = sortStateSignal({});

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly hoaDonService = inject(HoaDonService);
  protected readonly bookingService = inject(BookingService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IHoaDon): number => this.hoaDonService.getHoaDonIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(hoaDon: IHoaDon): void {
    const modalRef = this.modalService.open(HoaDonDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.hoaDon = hoaDon;
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

  onSearch(): void {
    this.handleNavigation(1, this.sortState());
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.fromDate = '';
    this.toDate = '';
    this.handleNavigation(1, this.sortState());
  }

  clearKeyword(): void {
    this.searchTerm = '';
    this.handleNavigation(1, this.sortState());
  }

  clearFromDate(): void {
    this.fromDate = '';
    this.handleNavigation(1, this.sortState());
  }

  clearToDate(): void {
    this.toDate = '';
    this.handleNavigation(1, this.sortState());
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.searchTerm = params.get('search') ?? '';
    this.fromDate = params.get('fromDate') ?? '';
    this.toDate = params.get('toDate') ?? '';
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.hoaDons.set(dataFromBody);
    this.searchSuggestions = this.buildSearchSuggestions(dataFromBody);
  }

  protected fillComponentAttributesFromResponseBody(data: IHoaDon[] | null): IHoaDon[] {
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
    if (this.hasFilters()) {
      queryObject.query = this.searchTerm.trim();
      if (this.fromDate) {
        queryObject.fromDate = this.fromDate;
      }
      if (this.toDate) {
        queryObject.toDate = this.toDate;
      }
      return this.hoaDonService.search(queryObject).pipe(tap(() => (this.isLoading = false)));
    }
    return this.hoaDonService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };

    if (this.hasFilters()) {
      queryParamsObj.search = this.searchTerm.trim();
      if (this.fromDate) {
        queryParamsObj.fromDate = this.fromDate;
      }
      if (this.toDate) {
        queryParamsObj.toDate = this.toDate;
      }
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

  private hasFilters(): boolean {
    return !!this.searchTerm.trim() || !!this.fromDate || !!this.toDate;
  }

  private buildSearchSuggestions(hoaDons: IHoaDon[]): string[] {
    return Array.from(
      new Set(
        hoaDons
          .flatMap(hoaDon => [
            hoaDon.id?.toString(),
            hoaDon.maGiaoDich,
            hoaDon.maGiamGia,
            hoaDon.trangThai,
            hoaDon.nguoiDung?.email,
            hoaDon.nguoiDung?.id?.toString(),
          ])
          .map(value => value?.trim())
          .filter((value): value is string => !!value),
      ),
    ).slice(0, 20);
  }
}
