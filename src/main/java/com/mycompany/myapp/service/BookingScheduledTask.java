package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.service.BookingService;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingScheduledTask {

    private final Logger log = LoggerFactory.getLogger(BookingScheduledTask.class);

    private final HoaDonRepository hoaDonRepository;
    private final DatVeService datVeService;
    private final BookingService bookingService;

    public BookingScheduledTask(HoaDonRepository hoaDonRepository, DatVeService datVeService, BookingService bookingService) {
        this.hoaDonRepository = hoaDonRepository;
        this.datVeService = datVeService;
        this.bookingService = bookingService;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void cleanupExpiredBookings() {
        log.debug("Running cleanup task for expired bookings");

        try {
            ZonedDateTime fiveMinutesAgo = ZonedDateTime.now().minusMinutes(5);

            List<HoaDon> expiredBookings = hoaDonRepository
                .findAll()
                .stream()
                .filter(
                    h ->
                        ("1".equals(h.getTrangThai()) || "PENDING".equalsIgnoreCase(h.getTrangThai())) &&
                        h.getCreatedAt() != null &&
                        h.getCreatedAt().isBefore(fiveMinutesAgo)
                )
                .toList();

            for (HoaDon hoaDon : expiredBookings) {
                log.info("Cancelling expired booking: {}", hoaDon.getId());
                bookingService.cancelBooking(hoaDon.getId());
            }

            log.debug("Cleanup task completed. Found {} expired bookings", expiredBookings.size());
        } catch (Exception e) {
            log.error("Error during cleanup task", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void archiveOldBookings() {
        log.debug("Running archive task for old bookings");

        try {
            ZonedDateTime threeMonthsAgo = ZonedDateTime.now().minusMonths(3);

            List<HoaDon> oldBookings = hoaDonRepository
                .findAll()
                .stream()
                .filter(
                    h ->
                        ("3".equals(h.getTrangThai()) || "CANCELLED".equalsIgnoreCase(h.getTrangThai())) &&
                        h.getCreatedAt() != null &&
                        h.getCreatedAt().isBefore(threeMonthsAgo)
                )
                .toList();

            log.debug("Found {} old bookings to archive", oldBookings.size());
        } catch (Exception e) {
            log.error("Error during archive task", e);
        }
    }
}
