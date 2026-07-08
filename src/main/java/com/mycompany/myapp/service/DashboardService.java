package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.ChiTietFB;
import com.mycompany.myapp.domain.DanhGia;
import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.ChiTietFBRepository;
import com.mycompany.myapp.repository.DanhGiaRepository;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.DashboardDTO.DashboardItemDTO;
import com.mycompany.myapp.service.dto.DashboardDTO.MetricComparisonDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    private Boolean reviewTableExistsCached = null;

    private final HoaDonRepository hoaDonRepository;
    private final VeRepository veRepository;
    private final ChiTietFBRepository chiTietFBRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SuatChieuRepository suatChieuRepository;
    private final DataSource dataSource;

    public DashboardService(
        HoaDonRepository hoaDonRepository,
        VeRepository veRepository,
        ChiTietFBRepository chiTietFBRepository,
        DanhGiaRepository danhGiaRepository,
        NguoiDungRepository nguoiDungRepository,
        SuatChieuRepository suatChieuRepository,
        DataSource dataSource
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.veRepository = veRepository;
        this.chiTietFBRepository = chiTietFBRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.suatChieuRepository = suatChieuRepository;
        this.dataSource = dataSource;
    }

    public DashboardDTO getDashboard(String fromDate, String toDate) {
        DashboardDTO dto = new DashboardDTO();
        try {
            DateRange currentRange = parseRange(fromDate, toDate);
            DateRange previousRange = buildPreviousRange(currentRange);

            if (currentRange == null) {
                return dto;
            }

            BigDecimal totalRevenue = hoaDonRepository.sumRevenueBetween(currentRange.from, currentRange.to);
            BigDecimal totalFbRevenue = chiTietFBRepository.sumFbRevenueBetween(currentRange.from, currentRange.to);
            long totalTicketsSold = veRepository.countPaidTicketsBetween(currentRange.from, currentRange.to);
            long totalUsers = nguoiDungRepository.countByCreatedAtLessThanEqual(currentRange.to);
            long paidInvoicesCount = hoaDonRepository.countPaidInvoicesBetween(currentRange.from, currentRange.to);
            long invoicesWithCombo = hoaDonRepository.countPaidInvoicesWithComboBetween(currentRange.from, currentRange.to);

            List<DanhGia> currentDanhGias = loadDanhGiasSafely(currentRange);
            List<SuatChieu> currentSuatChieus = loadSuatChieus(currentRange);

            dto.setTotalRevenue(totalRevenue);
            dto.setTotalFbRevenue(totalFbRevenue);
            dto.setTotalTicketsSold(totalTicketsSold);
            dto.setTotalUsers(totalUsers);
            dto.setPaidInvoicesCount(paidInvoicesCount);
            dto.setInvoicesWithCombo(invoicesWithCombo);

            long totalCapacity = currentSuatChieus
                .stream()
                .filter(sc -> sc.getPhongChieu() != null && sc.getPhongChieu().getSoLuongGhe() != null)
                .mapToLong(sc -> sc.getPhongChieu().getSoLuongGhe())
                .sum();

            BigDecimal averageOccupancyRate = BigDecimal.ZERO;
            if (totalCapacity > 0) {
                List<Long> scIds = currentSuatChieus.stream().map(SuatChieu::getId).collect(Collectors.toList());
                if (!scIds.isEmpty()) {
                    long soldSeatsForShowtimes = veRepository.countPaidTicketsBySuatChieuIds(scIds);
                    averageOccupancyRate = calculateRatio(soldSeatsForShowtimes, totalCapacity);
                }
            }
            dto.setAverageOccupancyRate(averageOccupancyRate);
            dto.setComboAttachRate(calculateRatio(invoicesWithCombo, paidInvoicesCount));
            dto.setReviewCount(currentDanhGias.size());
            dto.setAverageMovieRating(calculateAverageRating(currentDanhGias));

            // Top movies ranking
            List<Object[]> topMoviesRaw = veRepository.getTopMoviesBetween(currentRange.from, currentRange.to);
            List<DashboardItemDTO> topMovies = topMoviesRaw
                .stream()
                .map(row -> new DashboardItemDTO((String) row[0], ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .limit(5)
                .collect(Collectors.toList());
            dto.setTopMovies(topMovies);

            dto.setTopRatedMovies(buildTopRatedMovies(loadDanhGiasSafely(null)));

            // Top rooms ranking
            List<Object[]> roomRevenueRaw = veRepository.getRoomRevenueBetween(currentRange.from, currentRange.to);
            List<Object[]> roomCapacityRaw = suatChieuRepository.getRoomCapacityBetween(currentRange.from, currentRange.to);
            Map<String, Long> capacityMap = roomCapacityRaw
                .stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue(), (a, b) -> a));
            List<DashboardItemDTO> topRooms = roomRevenueRaw
                .stream()
                .map(row -> {
                    String roomName = (String) row[0];
                    long sold = ((Number) row[1]).longValue();
                    BigDecimal revenue = (BigDecimal) row[2];
                    long capacity = capacityMap.getOrDefault(roomName, 0L);
                    BigDecimal percentage = calculateRatio(sold, capacity);
                    return new DashboardItemDTO(roomName, sold, capacity, revenue, percentage);
                })
                .sorted((a, b) -> {
                    int percentCompare = b.getPercentage().compareTo(a.getPercentage());
                    if (percentCompare != 0) return percentCompare;
                    return b.getRevenue().compareTo(a.getRevenue());
                })
                .limit(5)
                .collect(Collectors.toList());
            dto.setTopRooms(topRooms);

            // Top combos ranking
            List<Object[]> topCombosRaw = chiTietFBRepository.getTopCombosBetween(currentRange.from, currentRange.to);
            List<DashboardItemDTO> topCombos = topCombosRaw
                .stream()
                .map(row -> new DashboardItemDTO((String) row[0], ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .limit(5)
                .collect(Collectors.toList());
            dto.setTopCombos(topCombos);

            // Status breakdown
            List<Object[]> statusBreakdownRaw = hoaDonRepository.getStatusBreakdownBetween(currentRange.from, currentRange.to);
            Map<String, Long> statusCounts = new LinkedHashMap<>();
            statusCounts.put("Đã thanh toán", 0L);
            statusCounts.put("Chờ thanh toán", 0L);
            statusCounts.put("Đã hủy / Quá hạn", 0L);
            long totalInvoices = 0;
            for (Object[] row : statusBreakdownRaw) {
                String status = (String) row[0];
                long count = ((Number) row[1]).longValue();
                totalInvoices += count;
                String label = normalizeInvoiceStatus(status);
                statusCounts.put(label, statusCounts.getOrDefault(label, 0L) + count);
            }
            final long finalTotalInvoices = totalInvoices;
            List<DashboardItemDTO> statusBreakdown = statusCounts
                .entrySet()
                .stream()
                .map(entry ->
                    new DashboardItemDTO(
                        entry.getKey(),
                        entry.getValue(),
                        0L,
                        BigDecimal.ZERO,
                        calculateRatio(entry.getValue(), finalTotalInvoices)
                    )
                )
                .collect(Collectors.toList());
            dto.setStatusBreakdown(statusBreakdown);

            // Peak hours
            List<Object[]> peakHoursRaw = veRepository.getPeakHoursBetween(currentRange.from, currentRange.to);
            Map<String, Aggregation> peakBuckets = new LinkedHashMap<>();
            peakBuckets.put("Ca sáng", new Aggregation());
            peakBuckets.put("Ca chiều", new Aggregation());
            peakBuckets.put("Giờ vàng 18h-22h", new Aggregation());
            peakBuckets.put("Ca khuya", new Aggregation());
            for (Object[] row : peakHoursRaw) {
                int hour = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();
                BigDecimal revenue = (BigDecimal) row[2];
                String bucket = mapPeakHourBucket(hour);
                Aggregation agg = peakBuckets.get(bucket);
                if (agg != null) {
                    agg.count += count;
                    agg.revenue = agg.revenue.add(revenue);
                }
            }
            List<DashboardItemDTO> peakHours = peakBuckets
                .entrySet()
                .stream()
                .map(entry -> new DashboardItemDTO(entry.getKey(), entry.getValue().count, entry.getValue().revenue))
                .collect(Collectors.toList());
            dto.setPeakHours(peakHours);

            // Ticket type breakdown
            List<Object[]> ticketTypeRaw = veRepository.getTicketTypeBreakdownBetween(currentRange.from, currentRange.to);
            Map<String, Long> ticketCounts = new LinkedHashMap<>();
            ticketCounts.put("Ghế thường", 0L);
            ticketCounts.put("Ghế VIP", 0L);
            ticketCounts.put("Ghế đôi", 0L);
            long totalTickets = 0;
            for (Object[] row : ticketTypeRaw) {
                String type = (String) row[0];
                long count = ((Number) row[1]).longValue();
                totalTickets += count;
                String label = "Ghế thường";
                if (type != null) {
                    String norm = type.trim().toUpperCase();
                    if (norm.contains("COUPLE") || norm.contains("DOI") || norm.contains("SWEETBOX")) {
                        label = "Ghế đôi";
                    } else if (norm.contains("VIP")) {
                        label = "Ghế VIP";
                    }
                }
                ticketCounts.put(label, ticketCounts.getOrDefault(label, 0L) + count);
            }
            final long finalTotalTickets = totalTickets;
            List<DashboardItemDTO> ticketTypeBreakdown = ticketCounts
                .entrySet()
                .stream()
                .map(entry ->
                    new DashboardItemDTO(
                        entry.getKey(),
                        entry.getValue(),
                        0L,
                        BigDecimal.ZERO,
                        calculateRatio(entry.getValue(), finalTotalTickets)
                    )
                )
                .collect(Collectors.toList());
            dto.setTicketTypeBreakdown(ticketTypeBreakdown);

            if (previousRange != null) {
                BigDecimal prevRevenue = hoaDonRepository.sumRevenueBetween(previousRange.from, previousRange.to);
                BigDecimal prevFbRevenue = chiTietFBRepository.sumFbRevenueBetween(previousRange.from, previousRange.to);
                long prevTickets = veRepository.countPaidTicketsBetween(previousRange.from, previousRange.to);
                long prevUsers = nguoiDungRepository.countByCreatedAtLessThanEqual(previousRange.to);

                dto.setRevenueComparison(buildMetricComparison(totalRevenue, prevRevenue));
                dto.setFbRevenueComparison(buildMetricComparison(totalFbRevenue, prevFbRevenue));
                dto.setTicketsComparison(buildMetricComparison(BigDecimal.valueOf(totalTicketsSold), BigDecimal.valueOf(prevTickets)));
                dto.setUsersComparison(buildMetricComparison(BigDecimal.valueOf(totalUsers), BigDecimal.valueOf(prevUsers)));
            }
        } catch (Exception e) {
            LOG.warn("Không lấy được đầy đủ dữ liệu dashboard, trả về thống kê an toàn", e);
        }

        return dto;
    }

    private List<HoaDon> loadHoaDons(DateRange range) {
        if (range == null) {
            return hoaDonRepository.findAllWithToOneRelationships();
        }
        return hoaDonRepository.findAllByCreatedAtBetweenWithUser(range.from, range.to);
    }

    private List<Ve> loadVes(DateRange range) {
        if (range == null) {
            return veRepository.findAll();
        }
        return veRepository.findAllPaidWithRelationshipsByInvoiceCreatedAtBetween(range.from, range.to);
    }

    private List<ChiTietFB> loadChiTietFbs(DateRange range) {
        if (range == null) {
            return chiTietFBRepository.findAllWithToOneRelationships();
        }
        return chiTietFBRepository.findAllPaidWithRelationshipsByInvoiceCreatedAtBetween(range.from, range.to);
    }

    private List<DanhGia> loadDanhGiasSafely(DateRange range) {
        if (!reviewTableExists()) {
            return List.of();
        }
        try {
            if (range == null) {
                return danhGiaRepository.findAll();
            }
            return danhGiaRepository.findByCreatedAtBetweenOrderByCreatedAtAscWithPhim(range.from, range.to);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<SuatChieu> loadSuatChieus(DateRange range) {
        if (range == null) {
            return suatChieuRepository.findAllWithToOneRelationships();
        }
        return suatChieuRepository.searchByFilters(null, null, range.from, range.to);
    }

    private boolean reviewTableExists() {
        if (reviewTableExistsCached != null) {
            return reviewTableExistsCached;
        }
        try (var connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            try (var resultSet = metaData.getTables(null, null, "danhgia", new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    reviewTableExistsCached = true;
                    return true;
                }
            }
            try (var resultSet = metaData.getTables(null, null, "danh_gia", new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    reviewTableExistsCached = true;
                    return true;
                }
            }
            reviewTableExistsCached = false;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Object getDashboardData(String fromDate, String toDate) {
        return getDashboard(fromDate, toDate);
    }

    private DateRange parseRange(String fromDate, String toDate) {
        try {
            LocalDate from = fromDate != null && !fromDate.isBlank() ? LocalDate.parse(fromDate) : null;
            LocalDate to = toDate != null && !toDate.isBlank() ? LocalDate.parse(toDate) : null;
            if (from == null || to == null) {
                return null;
            }
            if (from.isAfter(to)) {
                LocalDate swap = from;
                from = to;
                to = swap;
            }
            return new DateRange(from.atStartOfDay(ZoneId.systemDefault()), to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
        } catch (Exception e) {
            return null;
        }
    }

    private DateRange buildPreviousRange(DateRange currentRange) {
        if (currentRange == null) {
            return null;
        }

        long days = ChronoUnit.DAYS.between(currentRange.from.toLocalDate(), currentRange.to.toLocalDate()) + 1;
        LocalDate previousTo = currentRange.from.toLocalDate().minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(days - 1);

        return new DateRange(
            previousFrom.atStartOfDay(ZoneId.systemDefault()),
            previousTo.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault())
        );
    }

    private List<HoaDon> filterHoaDons(List<HoaDon> hoaDons, DateRange range) {
        return hoaDons.stream().filter(hd -> isWithinRange(hd.getCreatedAt(), range)).collect(Collectors.toList());
    }

    private List<Ve> filterVes(List<Ve> ves, DateRange range) {
        return ves
            .stream()
            .filter(ve -> ve.getHoaDon() != null && isPaidInvoice(ve.getHoaDon()) && isWithinRange(ve.getHoaDon().getCreatedAt(), range))
            .collect(Collectors.toList());
    }

    private List<ChiTietFB> filterChiTietFbs(List<ChiTietFB> chiTietFBS, DateRange range) {
        return chiTietFBS
            .stream()
            .filter(
                ctfb -> ctfb.getHoaDon() != null && isPaidInvoice(ctfb.getHoaDon()) && isWithinRange(ctfb.getHoaDon().getCreatedAt(), range)
            )
            .collect(Collectors.toList());
    }

    private List<DanhGia> filterDanhGias(List<DanhGia> danhGias, DateRange range) {
        return danhGias.stream().filter(danhGia -> isWithinRange(danhGia.getCreatedAt(), range)).collect(Collectors.toList());
    }

    private List<SuatChieu> filterSuatChieus(List<SuatChieu> suatChieus, DateRange range) {
        return suatChieus.stream().filter(suatChieu -> isWithinRange(suatChieu.getThoiGianBatDau(), range)).collect(Collectors.toList());
    }

    private boolean isWithinRange(ZonedDateTime createdAt, DateRange range) {
        if (range == null) {
            return true;
        }
        if (createdAt == null) {
            return false;
        }
        return !createdAt.isBefore(range.from) && !createdAt.isAfter(range.to);
    }

    private BigDecimal calculateRevenue(List<HoaDon> hoaDons) {
        return hoaDons.stream().map(HoaDon::getTongTien).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateFbRevenue(List<ChiTietFB> chiTietFBS) {
        return chiTietFBS
            .stream()
            .filter(ctfb -> ctfb.getDichVuFB() != null && ctfb.getDichVuFB().getGia() != null && ctfb.getSoLuong() != null)
            .map(ctfb -> ctfb.getDichVuFB().getGia().multiply(BigDecimal.valueOf(ctfb.getSoLuong())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long calculateTotalUsers(DateRange range) {
        if (range == null) {
            return nguoiDungRepository.count();
        }
        return nguoiDungRepository.countByCreatedAtLessThanEqual(range.to);
    }

    private boolean isPaidInvoice(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getTrangThai() == null) {
            return false;
        }
        String normalized = hoaDon.getTrangThai().trim().toUpperCase();
        return (
            "2".equals(normalized) ||
            "PAID".equals(normalized) ||
            "DONE".equals(normalized) ||
            "SUCCESS".equals(normalized) ||
            "DA_THANH_TOAN".equals(normalized)
        );
    }

    private boolean hasCombo(HoaDon hoaDon) {
        return hoaDon != null && hoaDon.getChiTietFBS() != null && !hoaDon.getChiTietFBS().isEmpty();
    }

    private BigDecimal calculateAverageRating(List<DanhGia> danhGias) {
        if (danhGias.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = danhGias
            .stream()
            .map(DanhGia::getSoSao)
            .filter(Objects::nonNull)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(danhGias.size()), 1, RoundingMode.HALF_UP);
    }

    private List<DashboardItemDTO> buildTopRatedMovies(List<DanhGia> danhGias) {
        Map<String, List<DanhGia>> grouped = danhGias
            .stream()
            .filter(d -> d.getPhim() != null && d.getPhim().getTenPhim() != null)
            .collect(Collectors.groupingBy(d -> d.getPhim().getTenPhim()));

        return grouped
            .entrySet()
            .stream()
            .map(entry -> {
                List<DanhGia> reviews = entry.getValue();
                BigDecimal averageRating = calculateAverageRating(reviews);
                return new DashboardItemDTO(entry.getKey(), reviews.size(), 0L, BigDecimal.ZERO, BigDecimal.ZERO, averageRating);
            })
            .sorted((a, b) -> {
                int ratingCompare = b.getAverageRating().compareTo(a.getAverageRating());
                if (ratingCompare != 0) {
                    return ratingCompare;
                }
                return Long.compare(b.getCount(), a.getCount());
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    private MetricComparisonDTO buildMetricComparison(BigDecimal currentValue, BigDecimal previousValue) {
        MetricComparisonDTO dto = new MetricComparisonDTO();
        BigDecimal safeCurrent = currentValue == null ? BigDecimal.ZERO : currentValue;
        BigDecimal safePrevious = previousValue == null ? BigDecimal.ZERO : previousValue;
        BigDecimal deltaValue = safeCurrent.subtract(safePrevious);
        BigDecimal deltaPercent = BigDecimal.ZERO;

        if (safePrevious.compareTo(BigDecimal.ZERO) > 0) {
            deltaPercent = deltaValue.multiply(BigDecimal.valueOf(100)).divide(safePrevious, 2, RoundingMode.HALF_UP);
        } else if (safeCurrent.compareTo(BigDecimal.ZERO) > 0) {
            deltaPercent = BigDecimal.valueOf(100);
        }

        dto.setPreviousValue(safePrevious);
        dto.setDeltaValue(deltaValue);
        dto.setDeltaPercent(deltaPercent);
        return dto;
    }

    private List<DashboardItemDTO> buildRankings(
        java.util.stream.Stream<Ve> stream,
        Function<Ve, String> nameExtractor,
        Function<Ve, BigDecimal> revenueExtractor
    ) {
        Map<String, Aggregation> aggregated = new HashMap<>();
        stream.forEach(ve -> {
            String name = nameExtractor.apply(ve);
            if (name == null) {
                return;
            }
            Aggregation item = aggregated.computeIfAbsent(name, ignored -> new Aggregation());
            item.count++;
            BigDecimal revenue = revenueExtractor.apply(ve);
            if (revenue != null) {
                item.revenue = item.revenue.add(revenue);
            }
        });
        return aggregated
            .entrySet()
            .stream()
            .sorted((a, b) -> {
                int revenueCompare = b.getValue().revenue.compareTo(a.getValue().revenue);
                if (revenueCompare != 0) {
                    return revenueCompare;
                }
                return Long.compare(b.getValue().count, a.getValue().count);
            })
            .limit(5)
            .map(entry -> new DashboardItemDTO(entry.getKey(), entry.getValue().count, entry.getValue().revenue))
            .collect(Collectors.toList());
    }

    private List<DashboardItemDTO> buildRoomRankings(List<Ve> currentVes, List<SuatChieu> currentSuatChieus) {
        Map<String, Aggregation> soldByRoom = new HashMap<>();
        currentVes
            .stream()
            .filter(ve -> ve.getSuatChieu() != null && ve.getSuatChieu().getPhongChieu() != null)
            .forEach(ve -> {
                String roomName = ve.getSuatChieu().getPhongChieu().getTenPhong();
                Aggregation item = soldByRoom.computeIfAbsent(roomName, ignored -> new Aggregation());
                item.count++;
                if (ve.getGiaVe() != null) {
                    item.revenue = item.revenue.add(ve.getGiaVe());
                }
            });

        Map<String, Long> capacityByRoom = new HashMap<>();
        currentSuatChieus
            .stream()
            .filter(suatChieu -> suatChieu.getPhongChieu() != null)
            .forEach(suatChieu -> {
                String roomName = suatChieu.getPhongChieu().getTenPhong();
                long seatCount = suatChieu.getPhongChieu().getSoLuongGhe() != null ? suatChieu.getPhongChieu().getSoLuongGhe() : 0;
                capacityByRoom.merge(roomName, seatCount, Long::sum);
            });

        return soldByRoom
            .entrySet()
            .stream()
            .map(entry -> {
                long capacity = capacityByRoom.getOrDefault(entry.getKey(), 0L);
                BigDecimal occupancy = calculateRatio(entry.getValue().count, capacity);
                return new DashboardItemDTO(entry.getKey(), entry.getValue().count, capacity, entry.getValue().revenue, occupancy);
            })
            .sorted((a, b) -> {
                int occupancyCompare = b.getPercentage().compareTo(a.getPercentage());
                if (occupancyCompare != 0) {
                    return occupancyCompare;
                }
                return b.getRevenue().compareTo(a.getRevenue());
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    private List<DashboardItemDTO> buildComboRankings(List<ChiTietFB> chiTietFBS) {
        Map<String, Aggregation> aggregated = new HashMap<>();
        chiTietFBS.forEach(item -> {
            if (item.getDichVuFB() == null || item.getDichVuFB().getTenCombo() == null) {
                return;
            }
            Aggregation aggregation = aggregated.computeIfAbsent(item.getDichVuFB().getTenCombo(), ignored -> new Aggregation());
            aggregation.count += item.getSoLuong() == null ? 0 : item.getSoLuong();
            if (item.getDichVuFB().getGia() != null && item.getSoLuong() != null) {
                aggregation.revenue = aggregation.revenue.add(item.getDichVuFB().getGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
            }
        });
        return aggregated
            .entrySet()
            .stream()
            .sorted((a, b) -> Long.compare(b.getValue().count, a.getValue().count))
            .limit(5)
            .map(entry -> new DashboardItemDTO(entry.getKey(), entry.getValue().count, entry.getValue().revenue))
            .collect(Collectors.toList());
    }

    private List<DashboardItemDTO> buildStatusBreakdown(List<HoaDon> hoaDons) {
        long total = hoaDons.size();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Đã thanh toán", 0L);
        counts.put("Chờ thanh toán", 0L);
        counts.put("Đã hủy / Quá hạn", 0L);

        for (HoaDon hoaDon : hoaDons) {
            String label = normalizeInvoiceStatus(hoaDon.getTrangThai());
            counts.put(label, counts.getOrDefault(label, 0L) + 1);
        }

        List<DashboardItemDTO> result = new ArrayList<>();
        counts.forEach((label, count) -> result.add(new DashboardItemDTO(label, count, 0L, BigDecimal.ZERO, calculateRatio(count, total))));
        return result;
    }

    private String normalizeInvoiceStatus(String status) {
        if (status == null) {
            return "Chờ thanh toán";
        }
        String normalized = status.trim().toUpperCase();
        if (
            "2".equals(normalized) ||
            "PAID".equals(normalized) ||
            "DONE".equals(normalized) ||
            "SUCCESS".equals(normalized) ||
            "DA_THANH_TOAN".equals(normalized)
        ) {
            return "Đã thanh toán";
        }
        if ("3".equals(normalized) || normalized.contains("CANCEL") || normalized.contains("HUY") || normalized.contains("EXPIRED")) {
            return "Đã hủy / Quá hạn";
        }
        return "Chờ thanh toán";
    }

    private List<DashboardItemDTO> buildPeakHourBreakdown(List<Ve> ves) {
        Map<String, Aggregation> buckets = new LinkedHashMap<>();
        buckets.put("Ca sáng", new Aggregation());
        buckets.put("Ca chiều", new Aggregation());
        buckets.put("Giờ vàng 18h-22h", new Aggregation());
        buckets.put("Ca khuya", new Aggregation());

        ves
            .stream()
            .filter(ve -> ve.getSuatChieu() != null && ve.getSuatChieu().getThoiGianBatDau() != null)
            .forEach(ve -> {
                String bucket = mapPeakHourBucket(ve.getSuatChieu().getThoiGianBatDau().getHour());
                Aggregation aggregation = buckets.get(bucket);
                aggregation.count++;
                if (ve.getGiaVe() != null) {
                    aggregation.revenue = aggregation.revenue.add(ve.getGiaVe());
                }
            });

        return buckets
            .entrySet()
            .stream()
            .map(entry -> new DashboardItemDTO(entry.getKey(), entry.getValue().count, entry.getValue().revenue))
            .collect(Collectors.toList());
    }

    private String mapPeakHourBucket(int hour) {
        if (hour >= 18 && hour <= 22) {
            return "Giờ vàng 18h-22h";
        }
        if (hour >= 12 && hour < 18) {
            return "Ca chiều";
        }
        if (hour >= 6 && hour < 12) {
            return "Ca sáng";
        }
        return "Ca khuya";
    }

    private List<DashboardItemDTO> buildTicketTypeBreakdown(List<Ve> ves) {
        long total = ves.size();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Ghế thường", 0L);
        counts.put("Ghế VIP", 0L);
        counts.put("Ghế đôi", 0L);

        for (Ve ve : ves) {
            String seatType = normalizeSeatType(ve);
            counts.put(seatType, counts.getOrDefault(seatType, 0L) + 1);
        }

        List<DashboardItemDTO> result = new ArrayList<>();
        counts.forEach((label, count) -> result.add(new DashboardItemDTO(label, count, 0L, BigDecimal.ZERO, calculateRatio(count, total))));
        return result;
    }

    private String normalizeSeatType(Ve ve) {
        if (ve.getGhe() == null || ve.getGhe().getLoaiGhe() == null) {
            return "Ghế thường";
        }
        String normalized = ve.getGhe().getLoaiGhe().trim().toUpperCase();
        if (normalized.contains("COUPLE") || normalized.contains("DOI") || normalized.contains("SWEETBOX")) {
            return "Ghế đôi";
        }
        if (normalized.contains("VIP")) {
            return "Ghế VIP";
        }
        return "Ghế thường";
    }

    private BigDecimal calculateAverageOccupancyRate(List<Ve> soldVes, List<SuatChieu> suatChieus) {
        long soldSeats = soldVes.size();
        long totalCapacity = suatChieus
            .stream()
            .filter(suatChieu -> suatChieu.getPhongChieu() != null && suatChieu.getPhongChieu().getSoLuongGhe() != null)
            .mapToLong(suatChieu -> suatChieu.getPhongChieu().getSoLuongGhe())
            .sum();
        return calculateRatio(soldSeats, totalCapacity);
    }

    private BigDecimal calculateRatio(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private static class Aggregation {

        long count;
        BigDecimal revenue = BigDecimal.ZERO;
    }

    private static class DateRange {

        private final ZonedDateTime from;
        private final ZonedDateTime to;

        private DateRange(ZonedDateTime from, ZonedDateTime to) {
            this.from = from;
            this.to = to;
        }
    }
}
