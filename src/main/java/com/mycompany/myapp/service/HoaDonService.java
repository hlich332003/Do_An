package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.mapper.HoaDonMapper;
import java.time.ZonedDateTime;
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
 * Service Implementation for managing {@link com.mycompany.myapp.domain.HoaDon}.
 */
@Service
@Transactional
public class HoaDonService {

    private static final Logger LOG = LoggerFactory.getLogger(HoaDonService.class);

    private final HoaDonRepository hoaDonRepository;

    private final HoaDonMapper hoaDonMapper;

    public HoaDonService(HoaDonRepository hoaDonRepository, HoaDonMapper hoaDonMapper) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonMapper = hoaDonMapper;
    }

    /**
     * Save a hoaDon.
     *
     * @param hoaDonDTO the entity to save.
     * @return the persisted entity.
     */
    public HoaDonDTO save(HoaDonDTO hoaDonDTO) {
        LOG.debug("Request to save HoaDon : {}", hoaDonDTO);
        HoaDon hoaDon = hoaDonMapper.toEntity(hoaDonDTO);
        hoaDon = hoaDonRepository.save(hoaDon);
        return hoaDonMapper.toDto(hoaDon);
    }

    /**
     * Update a hoaDon.
     *
     * @param hoaDonDTO the entity to save.
     * @return the persisted entity.
     */
    public HoaDonDTO update(HoaDonDTO hoaDonDTO) {
        LOG.debug("Request to update HoaDon : {}", hoaDonDTO);
        HoaDon hoaDon = hoaDonMapper.toEntity(hoaDonDTO);
        hoaDon = hoaDonRepository.save(hoaDon);
        return hoaDonMapper.toDto(hoaDon);
    }

    /**
     * Partially update a hoaDon.
     *
     * @param hoaDonDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<HoaDonDTO> partialUpdate(HoaDonDTO hoaDonDTO) {
        LOG.debug("Request to partially update HoaDon : {}", hoaDonDTO);

        return hoaDonRepository
            .findById(hoaDonDTO.getId())
            .map(existingHoaDon -> {
                hoaDonMapper.partialUpdate(existingHoaDon, hoaDonDTO);

                return existingHoaDon;
            })
            .map(hoaDonRepository::save)
            .map(hoaDonMapper::toDto);
    }

    /**
     * Get all the hoaDons.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<HoaDonDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all HoaDons");
        return hoaDonRepository.findAll(pageable).map(hoaDonMapper::toDto);
    }

    /**
     * Get all the hoaDons with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<HoaDonDTO> findAllWithEagerRelationships(Pageable pageable) {
        return hoaDonRepository.findAllWithEagerRelationships(pageable).map(hoaDonMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<HoaDonDTO> search(String query, ZonedDateTime from, ZonedDateTime to, Pageable pageable) {
        LOG.debug("Request to search HoaDons by query: {}, from: {}, to: {}", query, from, to);

        ZonedDateTime effectiveFrom = from != null ? from : ZonedDateTime.now().minusYears(100);
        ZonedDateTime effectiveTo = to != null ? to : ZonedDateTime.now().plusYears(100);

        List<HoaDon> results;
        if (query == null || query.trim().isEmpty()) {
            results = hoaDonRepository.findAllByCreatedAtBetweenWithUser(effectiveFrom, effectiveTo);
        } else {
            String dbQuery = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            results = hoaDonRepository.searchHoaDons(dbQuery, effectiveFrom, effectiveTo);
        }

        List<HoaDonDTO> mapped = results.stream().map(hoaDonMapper::toDto).toList();

        return toPage(mapped, pageable);
    }

    /**
     * Get one hoaDon by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<HoaDonDTO> findOne(Long id) {
        LOG.debug("Request to get HoaDon : {}", id);
        return hoaDonRepository.findOneWithEagerRelationships(id).map(hoaDonMapper::toDto);
    }

    /**
     * Delete the hoaDon by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete HoaDon : {}", id);
        hoaDonRepository.deleteById(id);
    }

    private boolean matchesDateRange(HoaDon hoaDon, ZonedDateTime from, ZonedDateTime to) {
        if (hoaDon.getCreatedAt() == null) {
            return false;
        }
        if (from != null && hoaDon.getCreatedAt().isBefore(from)) {
            return false;
        }
        return to == null || !hoaDon.getCreatedAt().isAfter(to);
    }

    private boolean matchesQuery(HoaDon hoaDon, String query) {
        if (query == null) {
            return true;
        }

        String invoiceId = hoaDon.getId() != null ? String.valueOf(hoaDon.getId()) : "";
        String userEmail = hoaDon.getNguoiDung() != null && hoaDon.getNguoiDung().getEmail() != null
            ? hoaDon.getNguoiDung().getEmail()
            : "";
        String userName = hoaDon.getNguoiDung() != null && hoaDon.getNguoiDung().getHoTen() != null ? hoaDon.getNguoiDung().getHoTen() : "";
        String userPhone = hoaDon.getNguoiDung() != null && hoaDon.getNguoiDung().getSoDienThoai() != null
            ? hoaDon.getNguoiDung().getSoDienThoai()
            : "";

        boolean matchesTicket = false;
        if (hoaDon.getVes() != null) {
            matchesTicket = hoaDon.getVes().stream().anyMatch(v -> contains(v.getMaVe(), query));
        }

        return (
            contains(invoiceId, query) ||
            contains(hoaDon.getMaGiaoDich(), query) ||
            contains(hoaDon.getMaGiamGia(), query) ||
            contains(hoaDon.getPhuongThucThanhToan(), query) ||
            contains(hoaDon.getTrangThai(), query) ||
            contains(userEmail, query) ||
            contains(userName, query) ||
            contains(userPhone, query) ||
            matchesTicket
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<HoaDonDTO> toPage(List<HoaDonDTO> data, Pageable pageable) {
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
