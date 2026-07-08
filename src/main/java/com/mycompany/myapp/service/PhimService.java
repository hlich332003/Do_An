package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Phim;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.repository.DanhGiaRepository;
import com.mycompany.myapp.repository.PhimRepository;
import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.dto.PhimDTO;
import com.mycompany.myapp.service.mapper.PhimMapper;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Phim}.
 */
@Service
@Transactional
public class PhimService {

    private static final Logger LOG = LoggerFactory.getLogger(PhimService.class);

    private final PhimRepository phimRepository;
    private final SuatChieuRepository suatChieuRepository;
    private final VeRepository veRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final DataSource dataSource;

    private final PhimMapper phimMapper;

    public PhimService(
        PhimRepository phimRepository,
        SuatChieuRepository suatChieuRepository,
        VeRepository veRepository,
        DanhGiaRepository danhGiaRepository,
        DataSource dataSource,
        PhimMapper phimMapper
    ) {
        this.phimRepository = phimRepository;
        this.suatChieuRepository = suatChieuRepository;
        this.veRepository = veRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.dataSource = dataSource;
        this.phimMapper = phimMapper;
    }

    private void trimStringFields(PhimDTO phimDTO) {
        if (phimDTO.getTenPhim() != null) phimDTO.setTenPhim(phimDTO.getTenPhim().trim());
        if (phimDTO.getMoTa() != null) phimDTO.setMoTa(phimDTO.getMoTa().trim());
        if (phimDTO.getDaoDien() != null) phimDTO.setDaoDien(phimDTO.getDaoDien().trim());
        if (phimDTO.getDienVien() != null) phimDTO.setDienVien(phimDTO.getDienVien().trim());
        if (phimDTO.getTheLoai() != null) phimDTO.setTheLoai(phimDTO.getTheLoai().trim());
        if (phimDTO.getPoster() != null) {
            String poster = phimDTO.getPoster().trim();
            phimDTO.setPoster(poster.isEmpty() || "null".equalsIgnoreCase(poster) ? null : poster);
        }
        if (phimDTO.getTrailer() != null) {
            String trailer = phimDTO.getTrailer().trim();
            phimDTO.setTrailer(trailer.isEmpty() || "null".equalsIgnoreCase(trailer) ? null : trailer);
        }
    }

    /**
     * Save a phim.
     *
     * @param phimDTO the entity to save.
     * @return the persisted entity.
     */
    public PhimDTO save(PhimDTO phimDTO) {
        LOG.debug("Request to save Phim : {}", phimDTO);
        trimStringFields(phimDTO);
        validateReleaseDate(phimDTO);
        Phim phim = phimMapper.toEntity(phimDTO);
        if (phim.getCreatedAt() == null) {
            phim.setCreatedAt(java.time.ZonedDateTime.now());
        }
        if (phim.getTrangThai() == null) {
            phim.setTrangThai("ACTIVE");
        }
        phim = phimRepository.save(phim);
        return phimMapper.toDto(phim);
    }

    /**
     * Update a phim.
     *
     * @param phimDTO the entity to save.
     * @return the persisted entity.
     */
    public PhimDTO update(PhimDTO phimDTO) {
        LOG.debug("Request to update Phim : {}", phimDTO);
        trimStringFields(phimDTO);
        validateReleaseDate(phimDTO);
        Phim phim = phimMapper.toEntity(phimDTO);
        phim = phimRepository.save(phim);
        return phimMapper.toDto(phim);
    }

    /**
     * Partially update a phim.
     *
     * @param phimDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PhimDTO> partialUpdate(PhimDTO phimDTO) {
        LOG.debug("Request to partially update Phim : {}", phimDTO);
        trimStringFields(phimDTO);
        validateReleaseDate(phimDTO);

        return phimRepository
            .findById(phimDTO.getId())
            .map(existingPhim -> {
                phimMapper.partialUpdate(existingPhim, phimDTO);

                return existingPhim;
            })
            .map(phimRepository::save)
            .map(phimMapper::toDto);
    }

    /**
     * Get all the phims.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PhimDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Phims");
        return phimRepository.findAll(pageable).map(phimMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PhimDTO> findShowing(Pageable pageable) {
        LOG.debug("Request to get all showing Phims");
        return paginateHotPhims(phimRepository.findShowing(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<PhimDTO> findComingSoon(Pageable pageable) {
        LOG.debug("Request to get all coming soon Phims");
        return paginateHotPhims(phimRepository.findComingSoon(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<PhimDTO> searchByTenPhim(String query, Pageable pageable) {
        LOG.debug("Request to search Phims by name: {}", query);
        return phimRepository.findByTenPhimContainingIgnoreCase(query, pageable).map(phimMapper::toDto);
    }

    /**
     * Get one phim by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PhimDTO> findOne(Long id) {
        LOG.debug("Request to get Phim : {}", id);
        return phimRepository.findById(id).map(phimMapper::toDto);
    }

    /**
     * Delete the phim by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Phim : {}", id);
        if (suatChieuRepository.existsByPhimId(id)) {
            throw new BadRequestAlertException(
                "Không thể xóa phim đã phát sinh suất chiếu. Hãy chuyển trạng thái INACTIVE nếu cần.",
                "phim",
                "hasShowtimes"
            );
        }

        // Delete associated reviews (danhgia) first to prevent FK constraint failures
        List<com.mycompany.myapp.domain.DanhGia> reviews = danhGiaRepository.findByPhimIdOrderByCreatedAtDesc(id);
        if (reviews != null && !reviews.isEmpty()) {
            danhGiaRepository.deleteAllInBatch(reviews);
        }

        phimRepository.deleteById(id);
    }

    private void validateReleaseDate(PhimDTO phimDTO) {
        if (phimDTO.getNgayKhoiChieu() == null) {
            return;
        }

        LocalDate today = LocalDate.now(java.time.ZoneId.systemDefault());

        // If updating an existing film, check if the release date actually changed
        if (phimDTO.getId() != null) {
            Optional<Phim> existingPhimOpt = phimRepository.findById(phimDTO.getId());
            if (existingPhimOpt.isPresent()) {
                LocalDate oldDate = existingPhimOpt.get().getNgayKhoiChieu();
                if (oldDate != null && oldDate.equals(phimDTO.getNgayKhoiChieu())) {
                    // Date did not change, bypass validation
                    return;
                }
            }
        }

        if (phimDTO.getNgayKhoiChieu().isBefore(today)) {
            throw new BadRequestAlertException("Ngày khởi chiếu không được nhỏ hơn ngày hiện tại.", "phim", "invalidReleaseDate");
        }
    }

    private Page<PhimDTO> paginateHotPhims(List<Phim> phims, Pageable pageable) {
        List<Phim> sorted = new ArrayList<>(phims);
        Map<Long, MovieHotness> hotnessMap = buildHotnessMap(sorted);

        sorted.sort(
            Comparator.comparingLong((Phim phim) -> getHotness(hotnessMap, phim).ticketCount)
                .reversed()
                .thenComparing((Phim phim) -> getHotness(hotnessMap, phim).averageRating, Comparator.reverseOrder())
                .thenComparing((Phim phim) -> getHotness(hotnessMap, phim).reviewCount, Comparator.reverseOrder())
                .thenComparing(Phim::getNgayKhoiChieu, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Phim::getId, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        int start = Math.toIntExact(Math.min(pageable.getOffset(), sorted.size()));
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        List<PhimDTO> pageContent = sorted.subList(start, end).stream().map(phimMapper::toDto).collect(Collectors.toList());
        return new PageImpl<>(pageContent, pageable, sorted.size());
    }

    private Map<Long, MovieHotness> buildHotnessMap(List<Phim> phims) {
        Map<Long, MovieHotness> hotnessMap = new HashMap<>();
        for (Phim phim : phims) {
            if (phim.getId() != null) {
                hotnessMap.put(phim.getId(), new MovieHotness());
            }
        }

        try {
            for (Object[] row : veRepository.countPaidTicketsByPhimId()) {
                if (row[0] instanceof Long phimId) {
                    MovieHotness hotness = hotnessMap.computeIfAbsent(phimId, ignored -> new MovieHotness());
                    hotness.ticketCount = ((Number) row[1]).longValue();
                }
            }
        } catch (Exception e) {
            LOG.warn("Không lấy được thống kê vé bán để xếp hạng phim hot", e);
        }

        if (reviewTableExists()) {
            try {
                for (Object[] row : danhGiaRepository.findRatingSummaryByPhimId()) {
                    if (row[0] instanceof Long phimId) {
                        MovieHotness hotness = hotnessMap.computeIfAbsent(phimId, ignored -> new MovieHotness());
                        hotness.averageRating = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
                        hotness.reviewCount = row[2] == null ? 0 : ((Number) row[2]).longValue();
                    }
                }
            } catch (Exception e) {
                LOG.warn("Không lấy được thống kê đánh giá để xếp hạng phim hot", e);
            }
        }

        return hotnessMap;
    }

    private MovieHotness getHotness(Map<Long, MovieHotness> hotnessMap, Phim phim) {
        if (phim.getId() == null) {
            return new MovieHotness();
        }
        return hotnessMap.getOrDefault(phim.getId(), new MovieHotness());
    }

    private boolean reviewTableExists() {
        try (var connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            try (var resultSet = metaData.getTables(null, null, "danhgia", new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    return true;
                }
            }
            try (var resultSet = metaData.getTables(null, null, "danh_gia", new String[] { "TABLE" })) {
                return resultSet.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static class MovieHotness {

        private long ticketCount;
        private double averageRating;
        private long reviewCount;
    }
}
