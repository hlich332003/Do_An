package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.ChiTietFB;
import com.mycompany.myapp.repository.ChiTietFBRepository;
import com.mycompany.myapp.service.dto.ChiTietFBDTO;
import com.mycompany.myapp.service.mapper.ChiTietFBMapper;
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
 * Service Implementation for managing {@link com.mycompany.myapp.domain.ChiTietFB}.
 */
@Service
@Transactional
public class ChiTietFBService {

    private static final Logger LOG = LoggerFactory.getLogger(ChiTietFBService.class);

    private final ChiTietFBRepository chiTietFBRepository;

    private final ChiTietFBMapper chiTietFBMapper;

    public ChiTietFBService(ChiTietFBRepository chiTietFBRepository, ChiTietFBMapper chiTietFBMapper) {
        this.chiTietFBRepository = chiTietFBRepository;
        this.chiTietFBMapper = chiTietFBMapper;
    }

    /**
     * Save a chiTietFB.
     *
     * @param chiTietFBDTO the entity to save.
     * @return the persisted entity.
     */
    public ChiTietFBDTO save(ChiTietFBDTO chiTietFBDTO) {
        LOG.debug("Request to save ChiTietFB : {}", chiTietFBDTO);
        ChiTietFB chiTietFB = chiTietFBMapper.toEntity(chiTietFBDTO);
        chiTietFB = chiTietFBRepository.save(chiTietFB);
        return chiTietFBMapper.toDto(chiTietFB);
    }

    /**
     * Update a chiTietFB.
     *
     * @param chiTietFBDTO the entity to save.
     * @return the persisted entity.
     */
    public ChiTietFBDTO update(ChiTietFBDTO chiTietFBDTO) {
        LOG.debug("Request to update ChiTietFB : {}", chiTietFBDTO);
        ChiTietFB chiTietFB = chiTietFBMapper.toEntity(chiTietFBDTO);
        chiTietFB = chiTietFBRepository.save(chiTietFB);
        return chiTietFBMapper.toDto(chiTietFB);
    }

    /**
     * Partially update a chiTietFB.
     *
     * @param chiTietFBDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ChiTietFBDTO> partialUpdate(ChiTietFBDTO chiTietFBDTO) {
        LOG.debug("Request to partially update ChiTietFB : {}", chiTietFBDTO);

        return chiTietFBRepository
            .findById(chiTietFBDTO.getId())
            .map(existingChiTietFB -> {
                chiTietFBMapper.partialUpdate(existingChiTietFB, chiTietFBDTO);

                return existingChiTietFB;
            })
            .map(chiTietFBRepository::save)
            .map(chiTietFBMapper::toDto);
    }

    /**
     * Get all the chiTietFBS.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ChiTietFBDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ChiTietFBS");
        return chiTietFBRepository.findAll(pageable).map(chiTietFBMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ChiTietFBDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search ChiTietFBS by query: {}", query);
        String normalizedQuery = normalize(query);
        List<ChiTietFBDTO> filtered = chiTietFBRepository
            .findAllWithEagerRelationships()
            .stream()
            .filter(chiTietFB -> matchesQuery(chiTietFB, normalizedQuery))
            .map(chiTietFBMapper::toDto)
            .toList();
        return toPage(filtered, pageable);
    }

    /**
     * Get all the chiTietFBS with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ChiTietFBDTO> findAllWithEagerRelationships(Pageable pageable) {
        return chiTietFBRepository.findAllWithEagerRelationships(pageable).map(chiTietFBMapper::toDto);
    }

    /**
     * Get one chiTietFB by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ChiTietFBDTO> findOne(Long id) {
        LOG.debug("Request to get ChiTietFB : {}", id);
        return chiTietFBRepository.findOneWithEagerRelationships(id).map(chiTietFBMapper::toDto);
    }

    /**
     * Delete the chiTietFB by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ChiTietFB : {}", id);
        chiTietFBRepository.deleteById(id);
    }

    private boolean matchesQuery(ChiTietFB chiTietFB, String query) {
        if (query == null) {
            return true;
        }

        String idText = chiTietFB.getId() != null ? String.valueOf(chiTietFB.getId()) : "";
        String soLuongText = chiTietFB.getSoLuong() != null ? String.valueOf(chiTietFB.getSoLuong()) : "";
        String giaBanText = chiTietFB.getGiaBan() != null ? String.valueOf(chiTietFB.getGiaBan()) : "";
        String tenCombo = chiTietFB.getDichVuFB() != null ? chiTietFB.getDichVuFB().getTenCombo() : null;
        String hoaDonId = chiTietFB.getHoaDon() != null && chiTietFB.getHoaDon().getId() != null
            ? String.valueOf(chiTietFB.getHoaDon().getId())
            : "";

        return (
            contains(idText, query) ||
            contains(soLuongText, query) ||
            contains(giaBanText, query) ||
            contains(tenCombo, query) ||
            contains(hoaDonId, query)
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<ChiTietFBDTO> toPage(List<ChiTietFBDTO> data, Pageable pageable) {
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
