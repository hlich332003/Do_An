import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { RouterModule, Router } from '@angular/router';
import { ISuatChieu } from '../suat-chieu.model';
import dayjs from 'dayjs/esm';

interface GroupedShowtime {
  movieName: string;
  poster?: string | null;
  cinemas: {
    cinemaName: string;
    rooms: {
      roomName: string;
      showtimes: ISuatChieu[];
    }[];
  }[];
}

@Component({
  selector: 'jhi-suat-chieu-phim',
  standalone: true,
  imports: [CommonModule, RouterModule, SharedModule],
  templateUrl: './suat-chieu-phim.component.html',
  styleUrls: ['./suat-chieu-phim.component.scss'],
})
export class SuatChieuPhimComponent implements OnChanges {
  @Input() suatChieus: ISuatChieu[] | null = [];
  @Input() showPastShowtimes = false;
  groupedShowtimes: GroupedShowtime[] = [];
  dates: dayjs.Dayjs[] = [];
  selectedDate: dayjs.Dayjs | null = null;

  private readonly router = inject(Router);

  getBookingLink(suatId: number): any[] {
    if (this.router.url.includes('/admin')) {
      return ['/admin/dat-ve', suatId];
    }
    return ['/dat-ve', suatId];
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['suatChieus'] && this.suatChieus) {
      this.extractDates();
      if (this.dates.length > 0 && !this.selectedDate) {
        this.selectedDate = this.dates[0];
      }
      this.groupShowtimes();
    }
  }

  extractDates(): void {
    if (!this.suatChieus) return;
    const dateMap = new Map<string, dayjs.Dayjs>();
    this.suatChieus.forEach(sc => {
      if (sc.thoiGianBatDau) {
        const dateStr = sc.thoiGianBatDau.format('YYYY-MM-DD');
        if (!dateMap.has(dateStr)) {
          dateMap.set(dateStr, sc.thoiGianBatDau);
        }
      }
    });
    this.dates = Array.from(dateMap.values()).sort((a, b) => a.valueOf() - b.valueOf());
  }

  selectDate(date: dayjs.Dayjs): void {
    this.selectedDate = date;
    this.groupShowtimes();
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (!target.src.includes('content/images/no-product-image.png')) {
      target.src = 'content/images/no-product-image.png';
    }
  }

  groupShowtimes(): void {
    this.groupedShowtimes = [];
    if (!this.suatChieus || !this.selectedDate) return;

    const now = dayjs();
    const selectedDateStr = this.selectedDate.format('YYYY-MM-DD');
    const isToday = selectedDateStr === now.format('YYYY-MM-DD');
    const filtered = this.suatChieus.filter(sc => {
      const start = sc.thoiGianBatDau;
      if (!start) {
        return false;
      }
      if (start.format('YYYY-MM-DD') !== selectedDateStr) {
        return false;
      }
      if (!this.showPastShowtimes && isToday && start.isBefore(now)) {
        return false;
      }
      return true;
    });

    const movieMap = new Map<string, { poster?: string | null; cinemas: Map<string, Map<string, ISuatChieu[]>> }>();

    filtered.forEach(sc => {
      const movieName = sc.phim?.tenPhim || 'Phim Khác';
      const poster = sc.phim?.poster;
      const fullRoomName = sc.phongChieu?.tenPhong || 'Rạp Khác';
      const parts = fullRoomName.split(' - ');
      const cinemaName = parts[0] || 'Rạp Khác';
      const roomName = parts[1] || fullRoomName;

      if (!movieMap.has(movieName)) {
        movieMap.set(movieName, { poster, cinemas: new Map<string, Map<string, ISuatChieu[]>>() });
      }
      const cinemaMap = movieMap.get(movieName)!.cinemas;

      if (!cinemaMap.has(cinemaName)) {
        cinemaMap.set(cinemaName, new Map<string, ISuatChieu[]>());
      }
      const roomMap = cinemaMap.get(cinemaName)!;

      if (!roomMap.has(roomName)) {
        roomMap.set(roomName, []);
      }
      roomMap.get(roomName)!.push(sc);
    });

    movieMap.forEach((movieData, movieName) => {
      const cinemas = Array.from(movieData.cinemas.entries()).map(([cName, roomMap]) => ({
        cinemaName: cName,
        rooms: Array.from(roomMap.entries()).map(([rName, times]) => ({
          roomName: rName,
          showtimes: times.sort((a, b) => (a.thoiGianBatDau?.valueOf() || 0) - (b.thoiGianBatDau?.valueOf() || 0)),
        })),
      }));
      this.groupedShowtimes.push({ movieName, poster: movieData.poster, cinemas });
    });
  }
}
