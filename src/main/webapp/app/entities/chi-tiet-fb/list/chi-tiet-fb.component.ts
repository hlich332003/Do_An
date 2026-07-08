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
import { IChiTietFB } from '../chi-tiet-fb.model';
import { ChiTietFBService, EntityArrayResponseType } from '../service/chi-tiet-fb.service';
import { ChiTietFBDeleteDialogComponent } from '../delete/chi-tiet-fb-delete-dialog.component';

@Component({
  selector: 'jhi-chi-tiet-fb',
  templateUrl: './chi-tiet-fb.component.html',
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, ItemCountComponent],
})
export class ChiTietFBComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  chiTietFBS = signal<IChiTietFB[]>([]);
  isLoading = false;
  searchTerm = '';
  searchSuggestions: string[] = [];

  sortState = sortStateSignal({});

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  public readonly router = inject(Router);
  protected readonly chiTietFBService = inject(ChiTietFBService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IChiTietFB): number => this.chiTietFBService.getChiTietFBIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(chiTietFB: IChiTietFB): void {
    const modalRef = this.modalService.open(ChiTietFBDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.chiTietFB = chiTietFB;
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
    this.chiTietFBS.set(dataFromBody);
    this.searchSuggestions = this.buildSearchSuggestions(dataFromBody);
  }

  protected fillComponentAttributesFromResponseBody(data: IChiTietFB[] | null): IChiTietFB[] {
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
    if (this.searchTerm.trim()) {
      queryObject.query = this.searchTerm.trim();
      return this.chiTietFBService.search(queryObject).pipe(tap(() => (this.isLoading = false)));
    }
    return this.chiTietFBService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
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

  private buildSearchSuggestions(data: IChiTietFB[]): string[] {
    return Array.from(
      new Set(
        data
          .flatMap(item => [
            item.id?.toString(),
            item.soLuong?.toString(),
            item.giaBan?.toString(),
            item.dichVuFB?.tenCombo,
            item.hoaDon?.id?.toString(),
          ])
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
