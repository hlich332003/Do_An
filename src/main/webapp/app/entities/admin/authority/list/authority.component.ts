import { Component, NgZone, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormsModule } from '@angular/forms';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IAuthority } from '../authority.model';
import { AuthorityService, EntityArrayResponseType } from '../service/authority.service';
import { AuthorityDeleteDialogComponent } from '../delete/authority-delete-dialog.component';

@Component({
  selector: 'jhi-authority',
  templateUrl: './authority.component.html',
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective],
})
export class AuthorityComponent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  allAuthorities = signal<IAuthority[]>([]);
  authorities = signal<IAuthority[]>([]);
  isLoading = false;
  searchTerm = '';

  sortState = sortStateSignal({});

  public readonly router = inject(Router);
  protected readonly authorityService = inject(AuthorityService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackName = (item: IAuthority): string => this.authorityService.getAuthorityIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => {
          if (this.authorities().length === 0) {
            this.load();
          } else {
            this.authorities.set(this.refineData(this.authorities()));
          }
        }),
      )
      .subscribe();
  }

  delete(authority: IAuthority): void {
    const modalRef = this.modalService.open(AuthorityDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.authority = authority;
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
    this.handleNavigation(event);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.searchTerm = params.get('search') ?? '';
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.allAuthorities.set(dataFromBody);
    this.authorities.set(this.refineData(dataFromBody));
  }

  protected refineData(data: IAuthority[]): IAuthority[] {
    const { predicate, order } = this.sortState();
    const filtered = this.searchTerm.trim()
      ? data.filter(authority => authority.name.toLowerCase().includes(this.searchTerm.trim().toLowerCase()))
      : data;
    return predicate && order ? filtered.sort(this.sortService.startSort({ predicate, order })) : filtered;
  }

  protected fillComponentAttributesFromResponseBody(data: IAuthority[] | null): IAuthority[] {
    return data ?? [];
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const queryObject: any = {
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    return this.authorityService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(sortState: SortState): void {
    const queryParamsObj: any = {
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
    this.authorities.set(this.refineData(this.allAuthorities()));
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.authorities.set(this.refineData(this.allAuthorities()));
  }
  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
