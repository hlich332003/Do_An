package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.PhongChieu;
import com.mycompany.myapp.repository.PhongChieuRepository;
import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.service.dto.PhongChieuDTO;
import com.mycompany.myapp.service.mapper.PhongChieuMapper;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.PhongChieu}.
 */
@Service
@Transactional
public class PhongChieuService {

    private static final Logger LOG = LoggerFactory.getLogger(PhongChieuService.class);

    private final PhongChieuRepository phongChieuRepository;
    private final SuatChieuRepository suatChieuRepository;
    private final PhongChieuMapper phongChieuMapper;

    public PhongChieuService(
        PhongChieuRepository phongChieuRepository,
        SuatChieuRepository suatChieuRepository,
        PhongChieuMapper phongChieuMapper
    ) {
        this.phongChieuRepository = phongChieuRepository;
        this.suatChieuRepository = suatChieuRepository;
        this.phongChieuMapper = phongChieuMapper;
    }

    /**
     * Save a phongChieu.
     *
     * @param phongChieuDTO the entity to save.
     * @return the persisted entity.
     */
    public PhongChieuDTO save(PhongChieuDTO phongChieuDTO) {
        LOG.debug("Request to save PhongChieu : {}", phongChieuDTO);
        PhongChieu phongChieu = phongChieuMapper.toEntity(phongChieuDTO);
        phongChieu = phongChieuRepository.save(phongChieu);
        return phongChieuMapper.toDto(phongChieu);
    }

    /**
     * Update a phongChieu.
     *
     * @param phongChieuDTO the entity to save.
     * @return the persisted entity.
     */
    public PhongChieuDTO update(PhongChieuDTO phongChieuDTO) {
        LOG.debug("Request to update PhongChieu : {}", phongChieuDTO);
        PhongChieu phongChieu = phongChieuMapper.toEntity(phongChieuDTO);
        phongChieu = phongChieuRepository.save(phongChieu);
        return phongChieuMapper.toDto(phongChieu);
    }

    /**
     * Partially update a phongChieu.
     *
     * @param phongChieuDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PhongChieuDTO> partialUpdate(PhongChieuDTO phongChieuDTO) {
        LOG.debug("Request to partially update PhongChieu : {}", phongChieuDTO);

        return phongChieuRepository
            .findById(phongChieuDTO.getId())
            .map(existingPhongChieu -> {
                phongChieuMapper.partialUpdate(existingPhongChieu, phongChieuDTO);

                return existingPhongChieu;
            })
            .map(phongChieuRepository::save)
            .map(phongChieuMapper::toDto);
    }

    /**
     * Get all the phongChieus.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PhongChieuDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all PhongChieus");
        return phongChieuRepository.findAll(pageable).map(phongChieuMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PhongChieuDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search PhongChieus by query: {}", query);
        String normalizedQuery = normalize(query);
        List<PhongChieuDTO> filtered = phongChieuRepository
            .findAll()
            .stream()
            .filter(phongChieu -> matchesQuery(phongChieu, normalizedQuery))
            .map(phongChieuMapper::toDto)
            .toList();
        return toPage(filtered, pageable);
    }

    /**
     * Get one phongChieu by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PhongChieuDTO> findOne(Long id) {
        LOG.debug("Request to get PhongChieu : {}", id);
        return phongChieuRepository.findById(id).map(phongChieuMapper::toDto);
    }

    /**
     * Delete the phongChieu by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete PhongChieu : {}", id);

        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime limit = now.plusDays(7);

        boolean hasShowtimeIn7Days = suatChieuRepository
            .findByPhongChieuIdOrderByThoiGianBatDauAsc(id)
            .stream()
            .anyMatch(s -> s.getThoiGianBatDau() != null && s.getThoiGianBatDau().isAfter(now) && s.getThoiGianBatDau().isBefore(limit));

        if (hasShowtimeIn7Days) {
            throw new com.mycompany.myapp.web.rest.errors.BadRequestAlertException(
                "Không thể xóa phòng chiếu khi có suất chiếu diễn ra trong 7 ngày tới.",
                "phongChieu",
                "hasShowtimesIn7Days"
            );
        }

        phongChieuRepository.deleteById(id);
    }

    private boolean matchesQuery(PhongChieu phongChieu, String query) {
        if (query == null) {
            return true;
        }

        String idText = phongChieu.getId() != null ? String.valueOf(phongChieu.getId()) : "";
        return contains(idText, query) || contains(phongChieu.getTenPhong(), query) || contains(phongChieu.getTrangThai(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<PhongChieuDTO> toPage(List<PhongChieuDTO> data, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(data);
        }
        int start = (int) pageable.getOffset();
        if (start >= data.size()) {
            return new PageImpl<>(List.of(), pageable, data.size());
        }
        int end = Math.min(start + pageable.getPageSize(), data.size());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }
}
