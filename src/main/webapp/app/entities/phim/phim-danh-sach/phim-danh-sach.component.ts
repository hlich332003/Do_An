import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { IPhim } from '../phim.model';
import { PhimService } from '../service/phim.service';
import { formatGenreLabel, normalizeForMatch } from 'app/shared/util/display-text.util';

@Component({
  selector: 'jhi-phim-danh-sach',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SharedModule, FormatMediumDatePipe],
  templateUrl: './phim-danh-sach.component.html',
  styleUrls: ['./phim-danh-sach.component.scss'],
})
export class PhimDanhSachComponent implements OnInit, OnDestroy {
  allPhims: IPhim[] = [];
  phims: IPhim[] = [];
  isLoading = false;
  searchTerm = '';
  selectedGenre = '';
  genres = [
    'Hành động',
    'Tình cảm',
    'Hoạt hình',
    'Phiêu lưu',
    'Khoa học viễn tưởng',
    'Kinh dị',
    'Hài',
    'Chính kịch',
    'Gia đình',
    'Thần thoại',
    'Trinh thám',
    'Tâm lý',
    'Âm nhạc',
  ];

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  private readonly phimService = inject(PhimService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  trackId = (_index: number, item: IPhim): number => item.id;

  formatGenre(value?: string | null): string {
    return formatGenreLabel(value);
  }

  getAgeRating(phim: IPhim): string {
    const theLoai = normalizeForMatch(phim.theLoai);
    if (theLoai.includes('kinh di') || theLoai.includes('18')) {
      return 'T18';
    }
    if (theLoai.includes('hanh dong') || theLoai.includes('16')) {
      return 'T16';
    }
    if (theLoai.includes('13')) {
      return 'T13';
    }
    return 'P';
  }

  getPosterSrc(phim: IPhim): string {
    return phim.poster?.trim() || 'content/images/no-product-image.png';
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (!target.src.includes('content/images/no-product-image.png')) {
      target.src = 'content/images/no-product-image.png';
    }
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(term => {
      this.searchTerm = term;
      this.onSearch();
    });

    this.route.queryParams.subscribe(params => {
      this.searchTerm = params['search'] || '';
      if (this.allPhims.length > 0) {
        this.applySearchFilter(this.searchTerm);
      } else {
        void this.loadPhims();
      }
    });
  }

  async loadPhims(): Promise<void> {
    this.isLoading = true;
    try {
      const response = await firstValueFrom(this.phimService.query({ page: 0, size: 1000, sort: ['id,desc'] }));
      this.allPhims = (response.body ?? [])
        .filter(phim => phim && phim.id != null && phim.trangThai === 'ACTIVE')
        .map(phim => ({
          ...phim,
          tenPhim: phim.tenPhim?.trim() || `Phim #${phim.id}`,
          theLoai: phim.theLoai?.trim() || '',
          daoDien: phim.daoDien?.trim() || '',
          dienVien: phim.dienVien?.trim() || '',
          poster: phim.poster?.trim() || null,
        }));
      this.applySearchFilter(this.searchTerm);
    } catch {
      this.allPhims = [];
      this.phims = [];
    } finally {
      this.isLoading = false;
    }
  }

  onSearch(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { search: this.searchTerm.trim() || null },
      queryParamsHandling: 'merge',
    });
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.selectedGenre = '';
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { search: null },
      queryParamsHandling: 'merge',
    });
  }

  private applySearchFilter(term: string): void {
    const query = normalizeForMatch(term.trim());
    let filtered = [...this.allPhims];

    if (this.selectedGenre) {
      filtered = filtered.filter(phim => this.formatGenre(phim.theLoai) === this.selectedGenre);
    }

    if (!query) {
      this.phims = filtered.sort((a, b) => (a.tenPhim ?? '').localeCompare(b.tenPhim ?? '', 'vi'));
      return;
    }

    this.phims = filtered
      .filter(phim => {
        const name = normalizeForMatch(phim.tenPhim ?? '');
        return name.includes(query);
      })
      .sort((a, b) => (a.tenPhim ?? '').localeCompare(b.tenPhim ?? '', 'vi'));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
