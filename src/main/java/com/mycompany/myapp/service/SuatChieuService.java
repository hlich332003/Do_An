package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ghe;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.GheRepository;
import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.dto.SuatChieuDTO;
import com.mycompany.myapp.service.mapper.SuatChieuMapper;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.SuatChieu}.
 */
@Service
@Transactional
public class SuatChieuService {

    private static final Logger LOG = LoggerFactory.getLogger(SuatChieuService.class);
    private final SuatChieuRepository suatChieuRepository;
    private final SuatChieuMapper suatChieuMapper;
    private final GheRepository gheRepository;
    private final VeRepository veRepository;

    public SuatChieuService(
        SuatChieuRepository suatChieuRepository,
        SuatChieuMapper suatChieuMapper,
        GheRepository gheRepository,
        VeRepository veRepository
    ) {
        this.suatChieuRepository = suatChieuRepository;
        this.suatChieuMapper = suatChieuMapper;
        this.gheRepository = gheRepository;
        this.veRepository = veRepository;
    }

    /**
     * Save a suatChieu.
     *
     * @param suatChieuDTO the entity to save.
     * @return the persisted entity.
     */
    public SuatChieuDTO save(SuatChieuDTO suatChieuDTO) {
        LOG.debug("Request to save SuatChieu : {}", suatChieuDTO);
        validateOverlap(suatChieuDTO);
        SuatChieu suatChieu = suatChieuMapper.toEntity(suatChieuDTO);
        suatChieu = suatChieuRepository.save(suatChieu);

        // Tự động sinh "Phôi vé" cho tất cả các ghế trong phòng chiếu này
        if (suatChieu.getPhongChieu() != null) {
            List<Ghe> gheList = gheRepository.findByPhongChieuId(suatChieu.getPhongChieu().getId());
            for (Ghe ghe : gheList) {
                Ve ve = new Ve();
                ve.setMaVe(UUID.randomUUID().toString()); // Sinh mã ngẫu nhiên, hoặc có thể dùng cấu trúc "CIN-"+suatChieu.getId()+"-"+ghe.getMaGhe()
                ve.setSuatChieu(suatChieu);
                ve.setGhe(ghe);
                ve.setTrangThai("CHUA_DAT");
                ve.setGiaVe(BigDecimal.ZERO);
                ve.setHoaDon(null);
                veRepository.save(ve);
            }
        }

        return suatChieuMapper.toDto(suatChieu);
    }

    /**
     * Update a suatChieu.
     *
     * @param suatChieuDTO the entity to save.
     * @return the persisted entity.
     */
    public SuatChieuDTO update(SuatChieuDTO suatChieuDTO) {
        LOG.debug("Request to update SuatChieu : {}", suatChieuDTO);
        validateOverlap(suatChieuDTO);

        // Khóa việc đổi Phòng Chiếu để bảo toàn phôi vé.
        // Lấy SuatChieu cũ từ DB để giữ nguyên phong_id
        SuatChieu oldSuatChieu = suatChieuRepository.findById(suatChieuDTO.getId()).orElse(null);
        SuatChieu suatChieu = suatChieuMapper.toEntity(suatChieuDTO);
        if (oldSuatChieu != null) {
            suatChieu.setPhongChieu(oldSuatChieu.getPhongChieu());
        }

        suatChieu = suatChieuRepository.save(suatChieu);
        return suatChieuMapper.toDto(suatChieu);
    }

    /**
     * Partially update a suatChieu.
     *
     * @param suatChieuDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SuatChieuDTO> partialUpdate(SuatChieuDTO suatChieuDTO) {
        LOG.debug("Request to partially update SuatChieu : {}", suatChieuDTO);

        return suatChieuRepository
            .findById(suatChieuDTO.getId())
            .map(existingSuatChieu -> {
                suatChieuMapper.partialUpdate(existingSuatChieu, suatChieuDTO);
                validateOverlap(suatChieuMapper.toDto(existingSuatChieu));

                return existingSuatChieu;
            })
            .map(suatChieuRepository::save)
            .map(suatChieuMapper::toDto);
    }

    /**
     * Get all the suatChieus.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SuatChieuDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all SuatChieus");
        return suatChieuRepository.findAll(pageable).map(suatChieuMapper::toDto);
    }

    /**
     * Get all the suatChieus with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<SuatChieuDTO> findAllWithEagerRelationships(Pageable pageable) {
        return suatChieuRepository.findAllWithEagerRelationships(pageable).map(suatChieuMapper::toDto);
    }

    /**
     * Get one suatChieu by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SuatChieuDTO> findOne(Long id) {
        LOG.debug("Request to get SuatChieu : {}", id);
        return suatChieuRepository.findOneWithEagerRelationships(id).map(suatChieuMapper::toDto);
    }

    /**
     * Delete the suatChieu by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete SuatChieu : {}", id);
        SuatChieu suatChieu = suatChieuRepository
            .findById(id)
            .orElseThrow(() ->
                new com.mycompany.myapp.web.rest.errors.BadRequestAlertException("Suất chiếu không tồn tại", "suatChieu", "notfound")
            );

        if (suatChieu.getThoiGianBatDau() != null && suatChieu.getThoiGianBatDau().isBefore(java.time.ZonedDateTime.now())) {
            throw new com.mycompany.myapp.web.rest.errors.BadRequestAlertException(
                "Không thể xóa suất chiếu đã chiếu rồi.",
                "suatChieu",
                "alreadyPlayed"
            );
        }

        if (veRepository.existsLockedOrSoldBySuatChieuId(id)) {
            throw new com.mycompany.myapp.web.rest.errors.BadRequestAlertException(
                "Không thể xóa suất chiếu đã có người đặt hoặc đang giữ vé.",
                "suatChieu",
                "has-booked-tickets"
            );
        }
        veRepository.deleteBySuatChieuId(id);
        suatChieuRepository.deleteById(id);
    }

    private void validateOverlap(SuatChieuDTO suatChieuDTO) {
        if (suatChieuDTO.getPhongChieu() != null && suatChieuDTO.getThoiGianBatDau() != null && suatChieuDTO.getThoiGianKetThuc() != null) {
            List<SuatChieu> siblings = suatChieuRepository.findByPhongChieuIdOrderByThoiGianBatDauAsc(suatChieuDTO.getPhongChieu().getId());
            java.time.ZonedDateTime newStart = suatChieuDTO.getThoiGianBatDau();
            java.time.ZonedDateTime newEnd = suatChieuDTO.getThoiGianKetThuc();

            boolean hasOverlap = siblings
                .stream()
                .anyMatch(existing -> {
                    if (existing.getId() != null && existing.getId().equals(suatChieuDTO.getId())) {
                        return false;
                    }
                    if (existing.getThoiGianBatDau() == null || existing.getThoiGianKetThuc() == null) {
                        return false;
                    }
                    java.time.ZonedDateTime existingEnd = existing.getThoiGianKetThuc();
                    return existing.getThoiGianBatDau().isBefore(newEnd) && existingEnd.isAfter(newStart);
                });

            if (hasOverlap) {
                throw new com.mycompany.myapp.web.rest.errors.BadRequestAlertException(
                    "Phòng chiếu đã có suất chiếu khác trong khoảng thời gian này!",
                    "suatChieu",
                    "overlapping"
                );
            }
        }
    }

    /**
     * Xóa các suất chiếu chồng lấn hoặc quá sát nhau trong cùng một phòng.
     *
     * @return số bản ghi đã bị xóa
     */
    public int cleanupOverlappingShowtimes() {
        List<SuatChieu> allShowtimes = suatChieuRepository.findAllWithToOneRelationships();
        java.util.Map<Long, List<SuatChieu>> byRoom = allShowtimes
            .stream()
            .filter(item -> item.getPhongChieu() != null && item.getPhongChieu().getId() != null)
            .collect(java.util.stream.Collectors.groupingBy(item -> item.getPhongChieu().getId()));

        List<Long> idsToDelete = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, List<SuatChieu>> entry : byRoom.entrySet()) {
            java.util.Set<String> seenExactSlots = new java.util.HashSet<>();
            List<SuatChieu> ordered = entry
                .getValue()
                .stream()
                .filter(item -> item.getThoiGianBatDau() != null && item.getThoiGianKetThuc() != null)
                .sorted(java.util.Comparator.comparing(SuatChieu::getThoiGianBatDau))
                .toList();

            java.time.ZonedDateTime keepUntil = null;
            for (SuatChieu item : ordered) {
                if (veRepository.existsLockedOrSoldBySuatChieuId(item.getId())) {
                    keepUntil = item.getThoiGianKetThuc();
                    continue;
                }

                String exactSlotKey =
                    item.getPhongChieu().getId() +
                    "|" +
                    item.getThoiGianBatDau().toInstant() +
                    "|" +
                    item.getThoiGianKetThuc().toInstant() +
                    "|" +
                    (item.getPhim() != null && item.getPhim().getId() != null ? item.getPhim().getId() : -1);

                if (!seenExactSlots.add(exactSlotKey)) {
                    if (veRepository.existsLockedOrSoldBySuatChieuId(item.getId())) {
                        keepUntil = item.getThoiGianKetThuc();
                        continue;
                    }
                    idsToDelete.add(item.getId());
                    continue;
                }

                java.time.ZonedDateTime blockedUntil = item.getThoiGianKetThuc();
                if (keepUntil == null || !item.getThoiGianBatDau().isBefore(keepUntil)) {
                    keepUntil = blockedUntil;
                    continue;
                }
                if (veRepository.existsLockedOrSoldBySuatChieuId(item.getId())) {
                    keepUntil = blockedUntil;
                    continue;
                }
                idsToDelete.add(item.getId());
            }
        }

        if (!idsToDelete.isEmpty()) {
            idsToDelete.forEach(veRepository::deleteBySuatChieuId);
            suatChieuRepository.deleteAllByIdInBatch(idsToDelete);
        }

        return idsToDelete.size();
    }

    /**
     * Search for suatChieus by filters.
     */
    @Transactional(readOnly = true)
    public java.util.List<SuatChieuDTO> search(
        List<Long> phimIds,
        Long phongChieuId,
        String ngayChieu,
        String gioBatDau,
        String gioKetThuc,
        String gioChieu,
        String query
    ) {
        LOG.debug(
            "Request to search SuatChieus by phimIds: {}, phongChieuId: {}, ngayChieu: {}, gioBatDau: {}, gioKetThuc: {}, gioChieu: {}, query: {}",
            phimIds,
            phongChieuId,
            ngayChieu,
            gioBatDau,
            gioKetThuc,
            gioChieu,
            query
        );
        java.time.ZonedDateTime startOfDay = null;
        java.time.ZonedDateTime endOfDay = null;

        if (ngayChieu != null && !ngayChieu.trim().isEmpty()) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(ngayChieu);
                startOfDay = date.atStartOfDay(java.time.ZoneId.systemDefault());
                endOfDay = date.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault());
            } catch (Exception e) {
                LOG.error("Invalid date format for ngayChieu: {}", ngayChieu);
            }
        }

        final LocalTime startTime = parseExpectedTime(gioBatDau != null ? gioBatDau : gioChieu);
        final LocalTime endTime = parseExpectedTime(gioKetThuc);

        String normalizedQuery = query == null ? null : query.trim().toLowerCase();

        return suatChieuRepository
            .searchByFilters(phimIds == null || phimIds.isEmpty() ? null : phimIds, phongChieuId, startOfDay, endOfDay)
            .stream()
            .filter(suatChieu -> matchesQuery(suatChieu, normalizedQuery))
            .filter(suatChieu -> matchesTimeRange(suatChieu, startTime, endTime))
            .sorted(Comparator.comparing(SuatChieu::getThoiGianBatDau, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(suatChieuMapper::toDto)
            .collect(java.util.stream.Collectors.toList());
    }

    private boolean matchesQuery(SuatChieu suatChieu, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String movieName = suatChieu.getPhim() != null && suatChieu.getPhim().getTenPhim() != null ? suatChieu.getPhim().getTenPhim() : "";
        String roomName = suatChieu.getPhongChieu() != null && suatChieu.getPhongChieu().getTenPhong() != null
            ? suatChieu.getPhongChieu().getTenPhong()
            : "";
        String startText = suatChieu.getThoiGianBatDau() != null ? suatChieu.getThoiGianBatDau().toString() : "";
        String endText = suatChieu.getThoiGianKetThuc() != null ? suatChieu.getThoiGianKetThuc().toString() : "";

        return (
            movieName.toLowerCase().contains(query) ||
            roomName.toLowerCase().contains(query) ||
            startText.toLowerCase().contains(query) ||
            endText.toLowerCase().contains(query)
        );
    }

    private boolean matchesTimeRange(SuatChieu suatChieu, LocalTime startTime, LocalTime endTime) {
        if (suatChieu.getThoiGianBatDau() == null) {
            return true;
        }

        if (startTime == null && endTime == null) {
            return true;
        }

        LocalTime showStart = suatChieu.getThoiGianBatDau().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        LocalTime normalizedStart = startTime != null ? startTime.truncatedTo(ChronoUnit.MINUTES) : null;
        LocalTime normalizedEnd = endTime != null ? endTime.truncatedTo(ChronoUnit.MINUTES) : null;

        if (normalizedStart != null && normalizedEnd != null && normalizedStart.isAfter(normalizedEnd)) {
            LocalTime swap = normalizedStart;
            normalizedStart = normalizedEnd;
            normalizedEnd = swap;
        }

        if (normalizedStart != null && showStart.isBefore(normalizedStart)) {
            return false;
        }
        return normalizedEnd == null || !showStart.isAfter(normalizedEnd);
    }

    private LocalTime parseExpectedTime(String gioChieu) {
        if (gioChieu == null || gioChieu.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(gioChieu.trim());
        } catch (Exception e) {
            LOG.error("Invalid time format for gioChieu: {}", gioChieu);
            return null;
        }
    }
}
