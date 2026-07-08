import { Component, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService, IDashboardData, IDashboardItem, IMetricComparison } from 'app/dat-ve/service/booking.service';
import Chart from 'chart.js/auto';

type QuickRangeKey = 'today' | 'yesterday' | 'last7Days' | 'thisMonth';

@Component({
  selector: 'jhi-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  dashboard: IDashboardData | null = null;
  isLoading = true;

  paddedTopMovies: IDashboardItem[] = [];
  paddedTopRooms: IDashboardItem[] = [];
  paddedTopCombos: IDashboardItem[] = [];

  fromDate = '';
  toDate = '';
  selectedQuickRange: QuickRangeKey = 'last7Days';

  readonly quickRanges: { key: QuickRangeKey; label: string }[] = [
    { key: 'today', label: 'Hôm nay' },
    { key: 'yesterday', label: 'Hôm qua' },
    { key: 'last7Days', label: '7 ngày qua' },
    { key: 'thisMonth', label: 'Tháng này' },
  ];

  @ViewChild('revenuePieChart') revenuePieChartRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('movieBarChart') movieBarChartRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusPieChart') statusPieChartRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('peakHourChart') peakHourChartRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('ticketTypeChart') ticketTypeChartRef?: ElementRef<HTMLCanvasElement>;

  revenuePieChart?: Chart;
  movieBarChart?: Chart;
  statusPieChart?: Chart;
  peakHourChart?: Chart;
  ticketTypeChart?: Chart;

  private readonly bookingService = inject(BookingService);

  ngOnInit(): void {
    this.applyQuickRange('last7Days', false);
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.bookingService.getDashboard(this.fromDate, this.toDate).subscribe({
      next: data => {
        this.dashboard = data;
        this.isLoading = false;
        if (data) {
          this.paddedTopMovies = this.getPaddedItems(data.topMovies);
          this.paddedTopRooms = this.getPaddedItems(data.topRooms);
          this.paddedTopCombos = this.getPaddedItems(data.topCombos);
        } else {
          this.paddedTopMovies = [];
          this.paddedTopRooms = [];
          this.paddedTopCombos = [];
        }
        setTimeout(() => this.renderCharts(), 100);
      },
      error: () => {
        this.dashboard = null;
        this.isLoading = false;
        this.paddedTopMovies = [];
        this.paddedTopRooms = [];
        this.paddedTopCombos = [];
        this.destroyCharts();
      },
    });
  }

  exportExcel(): void {
    this.bookingService.exportDashboard(this.fromDate, this.toDate).subscribe({
      next: blob => {
        const fileName = 'ThongKe_Dashboard.xlsx';
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: err => {
        console.error('Export Excel failed', err);
      },
    });
  }

  onDateChange(): void {
    this.selectedQuickRange = this.detectQuickRange();
    this.loadData();
  }

  applyQuickRange(range: QuickRangeKey, shouldReload = true): void {
    const today = new Date();
    let from: Date;
    let to: Date;

    switch (range) {
      case 'today':
        from = new Date(today);
        to = new Date(today);
        break;
      case 'yesterday':
        from = this.addDays(today, -1);
        to = this.addDays(today, -1);
        break;
      case 'thisMonth':
        from = new Date(today.getFullYear(), today.getMonth(), 1);
        to = new Date(today);
        break;
      case 'last7Days':
      default:
        from = this.addDays(today, -6);
        to = new Date(today);
        break;
    }

    this.selectedQuickRange = range;
    this.fromDate = this.formatDateInput(from);
    this.toDate = this.formatDateInput(to);

    if (shouldReload) {
      this.loadData();
    }
  }

  hasRankingData(items: Array<unknown> | null | undefined): boolean {
    return !!items?.length;
  }

  getPaddedItems(items: IDashboardItem[] | null | undefined, limit = 5): IDashboardItem[] {
    if (!items) return [];
    const padded = [...items];
    while (padded.length < limit) {
      padded.push({
        name: '',
        count: 0,
        capacity: 0,
        revenue: 0,
        percentage: 0,
        averageRating: 0,
      });
    }
    return padded;
  }

  hasDataPoints(items: IDashboardItem[] | null | undefined): boolean {
    return !!items?.some(item => (item.count ?? 0) > 0 || (item.revenue ?? 0) > 0 || (item.percentage ?? 0) > 0);
  }

  hasRevenueData(): boolean {
    return !!this.dashboard && (this.dashboard.totalRevenue > 0 || this.dashboard.totalFbRevenue > 0);
  }

  getComparisonClass(comparison: IMetricComparison | null | undefined): string {
    const delta = comparison?.deltaValue ?? 0;
    if (delta > 0) {
      return 'metric-trend trend-up';
    }
    if (delta < 0) {
      return 'metric-trend trend-down';
    }
    return 'metric-trend trend-flat';
  }

  getComparisonText(comparison: IMetricComparison | null | undefined, unit: 'currency' | 'count'): string {
    if (!comparison) {
      return 'So với kỳ trước: 0';
    }

    const previousValue = comparison.previousValue ?? 0;
    const deltaValue = comparison.deltaValue ?? 0;
    const deltaPercent = comparison.deltaPercent ?? 0;
    const action = deltaValue > 0 ? 'Tăng' : deltaValue < 0 ? 'Giảm' : 'Không đổi';
    const formattedPrevious = unit === 'currency' ? this.formatCurrency(previousValue) : this.formatNumber(previousValue);
    const formattedDelta = unit === 'currency' ? this.formatCurrency(Math.abs(deltaValue)) : this.formatNumber(Math.abs(deltaValue));
    const formattedPercent = Math.abs(deltaPercent).toFixed(1);

    if (deltaValue === 0) {
      return `Kỳ trước: ${formattedPrevious}`;
    }

    return `${action} ${formattedDelta} (${formattedPercent}%) so với kỳ trước: ${formattedPrevious}`;
  }

  getTrendBadgeClass(comparison: IMetricComparison | null | undefined): string {
    if (!comparison) return 'trend-badge badge-flat';
    const delta = comparison.deltaValue ?? 0;
    return delta > 0 ? 'trend-badge badge-up' : delta < 0 ? 'trend-badge badge-down' : 'trend-badge badge-flat';
  }

  getTrendValueClass(comparison: IMetricComparison | null | undefined): string {
    if (!comparison) return 'trend-value value-flat';
    const delta = comparison.deltaValue ?? 0;
    return delta > 0 ? 'trend-value value-up' : delta < 0 ? 'trend-value value-down' : 'trend-value value-flat';
  }

  getTrendIcon(comparison: IMetricComparison | null | undefined): string {
    if (!comparison) return '';
    const delta = comparison.deltaValue ?? 0;
    return delta > 0 ? '▲' : delta < 0 ? '▼' : '';
  }

  getComparisonActionSymbol(comparison: IMetricComparison | null | undefined): string {
    if (!comparison) return '';
    const delta = comparison.deltaValue ?? 0;
    return delta > 0 ? '+' : delta < 0 ? '-' : '';
  }

  getComparisonDelta(comparison: IMetricComparison | null | undefined, unit: 'currency' | 'count'): string {
    if (!comparison) return '0';
    const delta = Math.abs(comparison.deltaValue ?? 0);
    return unit === 'currency' ? this.formatCurrency(delta) : this.formatNumber(delta);
  }

  getComparisonPercent(comparison: IMetricComparison | null | undefined): string {
    if (!comparison) return '0.0';
    return Math.abs(comparison.deltaPercent ?? 0).toFixed(1);
  }

  getComparisonPrevious(comparison: IMetricComparison | null | undefined, unit: 'currency' | 'count'): string {
    if (!comparison) return '0';
    const prev = comparison.previousValue ?? 0;
    return unit === 'currency' ? this.formatCurrency(prev) : this.formatNumber(prev);
  }

  getPercentage(value?: number | null): string {
    return `${(value ?? 0).toFixed(1)}%`;
  }

  getComboAttachText(): string {
    if (!this.dashboard) {
      return 'Chưa có dữ liệu...';
    }
    return `${this.dashboard.invoicesWithCombo}/${this.dashboard.paidInvoicesCount} đơn thanh toán có mua kèm combo`;
  }

  renderCharts(): void {
    this.destroyCharts();

    if (!this.dashboard) {
      return;
    }

    this.renderRevenueChart();
    this.renderMovieChart();
    this.renderStatusChart();
    this.renderPeakHourChart();
    this.renderTicketTypeChart();
  }

  private renderRevenueChart(): void {
    if (!this.revenuePieChartRef || !this.hasRevenueData() || !this.dashboard) {
      return;
    }

    const ticketRevenue = Math.max(0, this.dashboard.totalRevenue - this.dashboard.totalFbRevenue);
    const fbRevenue = this.dashboard.totalFbRevenue;

    this.revenuePieChart = new Chart(this.revenuePieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: ['Doanh thu vé', 'Doanh thu F&B'],
        datasets: [
          {
            data: [ticketRevenue, fbRevenue],
            backgroundColor: ['#2563eb', '#f59e0b'],
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  private renderMovieChart(): void {
    if (!this.movieBarChartRef || !this.dashboard || !this.hasDataPoints(this.dashboard.topMovies)) {
      return;
    }

    this.movieBarChart = new Chart(this.movieBarChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: this.dashboard.topMovies.map(item => item.name),
        datasets: [
          {
            label: 'Doanh thu (VNĐ)',
            data: this.dashboard.topMovies.map(item => item.revenue),
            backgroundColor: '#0ea5e9',
            borderRadius: 10,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  private renderStatusChart(): void {
    if (!this.statusPieChartRef || !this.dashboard || !this.hasDataPoints(this.dashboard.statusBreakdown)) {
      return;
    }

    this.statusPieChart = new Chart(this.statusPieChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: this.dashboard.statusBreakdown.map(item => item.name),
        datasets: [
          {
            data: this.dashboard.statusBreakdown.map(item => item.count),
            backgroundColor: ['#16a34a', '#f59e0b', '#ef4444'],
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  private renderPeakHourChart(): void {
    if (!this.peakHourChartRef || !this.dashboard || !this.hasDataPoints(this.dashboard.peakHours)) {
      return;
    }

    this.peakHourChart = new Chart(this.peakHourChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: this.dashboard.peakHours.map(item => item.name),
        datasets: [
          {
            label: 'Lượng khách',
            data: this.dashboard.peakHours.map(item => item.count),
            backgroundColor: ['#cbd5f5', '#93c5fd', '#2563eb', '#1e293b'],
            borderRadius: 10,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  private renderTicketTypeChart(): void {
    if (!this.ticketTypeChartRef || !this.dashboard || !this.hasDataPoints(this.dashboard.ticketTypeBreakdown)) {
      return;
    }

    this.ticketTypeChart = new Chart(this.ticketTypeChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: this.dashboard.ticketTypeBreakdown.map(item => item.name),
        datasets: [
          {
            data: this.dashboard.ticketTypeBreakdown.map(item => item.count),
            backgroundColor: ['#38bdf8', '#fbbf24', '#f472b6'],
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  private destroyCharts(): void {
    this.revenuePieChart?.destroy();
    this.movieBarChart?.destroy();
    this.statusPieChart?.destroy();
    this.peakHourChart?.destroy();
    this.ticketTypeChart?.destroy();

    this.revenuePieChart = undefined;
    this.movieBarChart = undefined;
    this.statusPieChart = undefined;
    this.peakHourChart = undefined;
    this.ticketTypeChart = undefined;
  }

  private detectQuickRange(): QuickRangeKey {
    const now = new Date();
    const today = this.formatDateInput(now);
    const yesterday = this.formatDateInput(this.addDays(now, -1));
    const last7From = this.formatDateInput(this.addDays(now, -6));
    const monthStart = this.formatDateInput(new Date(now.getFullYear(), now.getMonth(), 1));

    if (this.fromDate === today && this.toDate === today) {
      return 'today';
    }
    if (this.fromDate === yesterday && this.toDate === yesterday) {
      return 'yesterday';
    }
    if (this.fromDate === last7From && this.toDate === today) {
      return 'last7Days';
    }
    if (this.fromDate === monthStart && this.toDate === today) {
      return 'thisMonth';
    }
    return this.selectedQuickRange;
  }

  private addDays(date: Date, days: number): Date {
    const nextDate = new Date(date);
    nextDate.setDate(nextDate.getDate() + days);
    return nextDate;
  }

  private formatDateInput(date: Date): string {
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  }

  private formatCurrency(value: number): string {
    return `${this.formatNumber(value)}đ`;
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(value);
  }
}
