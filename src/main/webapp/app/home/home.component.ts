import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { IPhim } from 'app/entities/phim/phim.model';
import { PhimService } from 'app/entities/phim/service/phim.service';
import { formatGenreLabel, normalizeForMatch } from 'app/shared/util/display-text.util';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [CommonModule, SharedModule, RouterModule, FormatMediumDatePipe],
})
export default class HomeComponent implements OnInit {
  showingPhims: IPhim[] = [];
  comingSoonPhims: IPhim[] = [];
  isLoading = false;

  private readonly phimService = inject(PhimService);

  ngOnInit(): void {
    void this.loadMovies();
  }

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

  async loadMovies(): Promise<void> {
    this.isLoading = true;
    try {
      const [showingRes, comingRes] = await Promise.all([
        firstValueFrom(this.phimService.getShowing({ page: 0, size: 12, sort: ['id,desc'] })),
        firstValueFrom(this.phimService.getComingSoon({ page: 0, size: 12, sort: ['id,desc'] })),
      ]);

      this.showingPhims = (showingRes.body ?? []).map(phim => this.mapMovie(phim));
      this.comingSoonPhims = (comingRes.body ?? []).map(phim => this.mapMovie(phim));
    } catch {
      this.showingPhims = [];
      this.comingSoonPhims = [];
    } finally {
      this.isLoading = false;
    }
  }

  private mapMovie(phim: IPhim): IPhim {
    return {
      ...phim,
      tenPhim: phim.tenPhim?.trim() || `Phim #${phim.id}`,
      theLoai: phim.theLoai?.trim() || '',
      poster: phim.poster?.trim() || null,
    };
  }
}
