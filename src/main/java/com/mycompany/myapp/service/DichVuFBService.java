package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DichVuFB;
import com.mycompany.myapp.repository.DichVuFBRepository;
import com.mycompany.myapp.service.dto.DichVuFBDTO;
import com.mycompany.myapp.service.mapper.DichVuFBMapper;
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
 * Service Implementation for managing {@link com.mycompany.myapp.domain.DichVuFB}.
 */
@Service
@Transactional
public class DichVuFBService {

    private static final Logger LOG = LoggerFactory.getLogger(DichVuFBService.class);

    private final DichVuFBRepository dichVuFBRepository;

    private final DichVuFBMapper dichVuFBMapper;

    public DichVuFBService(DichVuFBRepository dichVuFBRepository, DichVuFBMapper dichVuFBMapper) {
        this.dichVuFBRepository = dichVuFBRepository;
        this.dichVuFBMapper = dichVuFBMapper;
    }

    /**
     * Save a dichVuFB.
     *
     * @param dichVuFBDTO the entity to save.
     * @return the persisted entity.
     */
    public DichVuFBDTO save(DichVuFBDTO dichVuFBDTO) {
        LOG.debug("Request to save DichVuFB : {}", dichVuFBDTO);
        DichVuFB dichVuFB = dichVuFBMapper.toEntity(dichVuFBDTO);
        dichVuFB = dichVuFBRepository.save(dichVuFB);
        return dichVuFBMapper.toDto(dichVuFB);
    }

    /**
     * Update a dichVuFB.
     *
     * @param dichVuFBDTO the entity to save.
     * @return the persisted entity.
     */
    public DichVuFBDTO update(DichVuFBDTO dichVuFBDTO) {
        LOG.debug("Request to update DichVuFB : {}", dichVuFBDTO);
        DichVuFB dichVuFB = dichVuFBMapper.toEntity(dichVuFBDTO);
        dichVuFB = dichVuFBRepository.save(dichVuFB);
        return dichVuFBMapper.toDto(dichVuFB);
    }

    /**
     * Partially update a dichVuFB.
     *
     * @param dichVuFBDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DichVuFBDTO> partialUpdate(DichVuFBDTO dichVuFBDTO) {
        LOG.debug("Request to partially update DichVuFB : {}", dichVuFBDTO);

        return dichVuFBRepository
            .findById(dichVuFBDTO.getId())
            .map(existingDichVuFB -> {
                dichVuFBMapper.partialUpdate(existingDichVuFB, dichVuFBDTO);

                return existingDichVuFB;
            })
            .map(dichVuFBRepository::save)
            .map(dichVuFBMapper::toDto);
    }

    /**
     * Get all the dichVuFBS.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<DichVuFBDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DichVuFBS");
        return dichVuFBRepository.findAll(pageable).map(dichVuFBMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DichVuFBDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search DichVuFBS by query: {}", query);
        String normalizedQuery = normalize(query);
        List<DichVuFBDTO> filtered = dichVuFBRepository
            .findAll()
            .stream()
            .filter(dichVuFB -> matchesQuery(dichVuFB, normalizedQuery))
            .map(dichVuFBMapper::toDto)
            .toList();
        return toPage(filtered, pageable);
    }

    @Transactional(readOnly = true)
    public List<DichVuFBDTO> findActive() {
        LOG.debug("Request to get active DichVuFBS");
        return dichVuFBRepository.findByTrangThai("1").stream().map(dichVuFBMapper::toDto).toList();
    }

    /**
     * Get one dichVuFB by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DichVuFBDTO> findOne(Long id) {
        LOG.debug("Request to get DichVuFB : {}", id);
        return dichVuFBRepository.findById(id).map(dichVuFBMapper::toDto);
    }

    /**
     * Delete the dichVuFB by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete DichVuFB : {}", id);
        dichVuFBRepository.deleteById(id);
    }

    private boolean matchesQuery(DichVuFB dichVuFB, String query) {
        if (query == null) {
            return true;
        }

        String idText = dichVuFB.getId() != null ? String.valueOf(dichVuFB.getId()) : "";
        return (
            contains(idText, query) ||
            contains(dichVuFB.getTenCombo(), query) ||
            contains(dichVuFB.getMoTa(), query) ||
            contains(dichVuFB.getTrangThai(), query)
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<DichVuFBDTO> toPage(List<DichVuFBDTO> data, Pageable pageable) {
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
