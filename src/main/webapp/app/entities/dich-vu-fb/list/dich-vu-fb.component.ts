import { Component, NgZone, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IDichVuFB } from '../dich-vu-fb.model';
import { DichVuFBService, EntityArrayResponseType } from '../service/dich-vu-fb.service';
import { DichVuFBDeleteDialogComponent } from '../delete/dich-vu-fb-delete-dialog.component';

@Component({
  selector: 'jhi-dich-vu-fb',
  templateUrl: './dich-vu-fb.component.html',
  styleUrls: ['./dich-vu-fb.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, ItemCountComponent],
})
export class DichVuFBComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  dichVuFBS = signal<IDichVuFB[]>([]);
  isLoading = false;
  searchTerm = '';
  searchSuggestions: string[] = [];

  sortState = sortStateSignal({});

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly dichVuFBService = inject(DichVuFBService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IDichVuFB): number => this.dichVuFBService.getDichVuFBIdentifier(item);

  getImageSrc(dichVuFB: IDichVuFB): string {
    return dichVuFB.hinhAnh?.trim() || 'content/images/no-product-image.png';
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (!target.src.includes('content/images/no-product-image.png')) {
      target.src = 'content/images/no-product-image.png';
    }
  }

  formatPrice(value?: number | null): string {
    return new Intl.NumberFormat('vi-VN').format(value ?? 0);
  }

  formatStatusLabel(value?: string | null): string {
    if (value === '1') {
      return 'Đang bán';
    }
    if (value === '0') {
      return 'Ngừng bán';
    }
    return value?.trim() || 'Chưa cập nhật trạng thái';
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(dichVuFB: IDichVuFB): void {
    const modalRef = this.modalService.open(DichVuFBDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.dichVuFB = dichVuFB;
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
    this.dichVuFBS.set(dataFromBody);
    this.searchSuggestions = this.buildSearchSuggestions(dataFromBody);
  }

  protected fillComponentAttributesFromResponseBody(data: IDichVuFB[] | null): IDichVuFB[] {
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
      return this.dichVuFBService.search(queryObject).pipe(tap(() => (this.isLoading = false)));
    }
    return this.dichVuFBService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
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

  private buildSearchSuggestions(data: IDichVuFB[]): string[] {
    return Array.from(
      new Set(
        data
          .flatMap(item => [item.id?.toString(), item.tenCombo, item.moTa, item.trangThai])
          .map(value => value?.trim())
          .filter((value): value is string => !!value),
      ),
    ).slice(0, 20);
  }
  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
