import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Subject, firstValueFrom } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormatMediumDatePipe } from 'app/shared/date';
import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPhim } from '../phim.model';
import { PhimDeleteDialogComponent } from '../delete/phim-delete-dialog.component';
import { PhimService } from '../service/phim.service';
import { formatGenreLabel } from 'app/shared/util/display-text.util';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'jhi-phim',
  templateUrl: './phim.component.html',
  styleUrls: ['./phim.component.scss'],
  imports: [RouterModule, FormsModule, SharedModule, FormatMediumDatePipe],
})
export class PhimComponent implements OnInit, OnDestroy {
  phims: IPhim[] = [];
  allPhims: IPhim[] = [];
  isLoading = false;
  searchTerm = '';
  selectedGenre = '';
  selectedStatus = '';
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
  statuses = [
    { value: 'ACTIVE', label: 'Hoạt động' },
    { value: 'INACTIVE', label: 'Bị khóa' },
  ];

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  private readonly phimService = inject(PhimService);
  private readonly modalService = inject(NgbModal);

  trackId = (_index: number, item: IPhim): number => item.id;

  formatGenre(value?: string | null): string {
    return formatGenreLabel(value);
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(term => {
      this.searchTerm = term;
      this.applySearch();
    });

    void this.load();
  }

  async load(): Promise<void> {
    this.isLoading = true;
    try {
      const response = await firstValueFrom(this.phimService.query({ page: 0, size: 1000, sort: ['id,desc'] }));
      this.allPhims = (response.body ?? [])
        .filter(phim => phim && phim.id != null)
        .map(phim => ({
          ...phim,
          tenPhim: phim.tenPhim?.trim() || `Phim #${phim.id}`,
          theLoai: phim.theLoai?.trim() || '',
          daoDien: phim.daoDien?.trim() || '',
          dienVien: phim.dienVien?.trim() || '',
          moTa: phim.moTa?.trim() || '',
          poster: phim.poster?.trim() || null,
        }));
      this.phims = [...this.allPhims];
      this.applySearch();
    } catch {
      this.allPhims = [];
      this.phims = [];
    } finally {
      this.isLoading = false;
    }
  }

  delete(phim: IPhim): void {
    const modalRef = this.modalService.open(PhimDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.phim = phim;
    modalRef.closed.subscribe(result => {
      if (result === ITEM_DELETED_EVENT) {
        void this.load();
      }
    });
  }

  toggleStatus(phim: IPhim): void {
    if (!phim.id) {
      return;
    }

    const newStatus = phim.trangThai === 'INACTIVE' ? 'ACTIVE' : 'INACTIVE';
    this.phimService.partialUpdate({ id: phim.id, trangThai: newStatus }).subscribe({
      next: () => {
        phim.trangThai = newStatus;
      },
    });
  }

  onSearch(): void {
    this.applySearch();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.selectedGenre = '';
    this.selectedStatus = '';
    this.applySearch();
  }

  private applySearch(): void {
    const query = this.searchTerm.trim().toLowerCase();
    let filtered = [...this.allPhims];

    if (this.selectedGenre) {
      filtered = filtered.filter(phim => this.formatGenre(phim.theLoai) === this.selectedGenre);
    }

    if (this.selectedStatus) {
      filtered = filtered.filter(phim => phim.trangThai === this.selectedStatus);
    }

    if (!query) {
      this.phims = filtered.sort((a, b) => (a.tenPhim ?? '').localeCompare(b.tenPhim ?? '', 'vi'));
      return;
    }

    this.phims = filtered
      .filter(phim => (phim.tenPhim ?? '').toLowerCase().includes(query))
      .sort((a, b) => (a.tenPhim ?? '').localeCompare(b.tenPhim ?? '', 'vi'));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
