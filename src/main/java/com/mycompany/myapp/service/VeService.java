package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.dto.VeDTO;
import com.mycompany.myapp.service.mapper.VeMapper;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Ve}.
 */
@Service
@Transactional
public class VeService {

    private static final Logger LOG = LoggerFactory.getLogger(VeService.class);

    private final VeRepository veRepository;

    private final VeMapper veMapper;

    public VeService(VeRepository veRepository, VeMapper veMapper) {
        this.veRepository = veRepository;
        this.veMapper = veMapper;
    }

    /**
     * Save a ve.
     *
     * @param veDTO the entity to save.
     * @return the persisted entity.
     */
    public VeDTO save(VeDTO veDTO) {
        LOG.debug("Request to save Ve : {}", veDTO);
        Ve ve = veMapper.toEntity(veDTO);
        ve = veRepository.save(ve);
        return veMapper.toDto(ve);
    }

    /**
     * Update a ve.
     *
     * @param veDTO the entity to save.
     * @return the persisted entity.
     */
    public VeDTO update(VeDTO veDTO) {
        LOG.debug("Request to update Ve : {}", veDTO);
        Ve ve = veMapper.toEntity(veDTO);
        ve = veRepository.save(ve);
        return veMapper.toDto(ve);
    }

    /**
     * Partially update a ve.
     *
     * @param veDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VeDTO> partialUpdate(VeDTO veDTO) {
        LOG.debug("Request to partially update Ve : {}", veDTO);

        return veRepository
            .findById(veDTO.getId())
            .map(existingVe -> {
                veMapper.partialUpdate(existingVe, veDTO);

                return existingVe;
            })
            .map(veRepository::save)
            .map(veMapper::toDto);
    }

    /**
     * Get all the ves.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<VeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Ves");
        return veRepository.findAll(pageable).map(veMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<VeDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search Ves by query: {}", query);
        String normalizedQuery = normalize(query);
        List<VeDTO> filtered = veRepository
            .findAll()
            .stream()
            .filter(ve -> matchesQuery(ve, normalizedQuery))
            .map(veMapper::toDto)
            .collect(Collectors.toList());

        if (pageable.getSort().isSorted()) {
            filtered.sort((a, b) -> {
                for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                    int comp = compareVeDTO(a, b, order.getProperty());
                    if (comp != 0) {
                        return order.isAscending() ? comp : -comp;
                    }
                }
                return 0;
            });
        }

        return toPage(filtered, pageable);
    }

    private int compareVeDTO(VeDTO a, VeDTO b, String property) {
        if ("id".equals(property)) {
            return a.getId().compareTo(b.getId());
        } else if ("maVe".equals(property)) {
            String valA = a.getMaVe() != null ? a.getMaVe() : "";
            String valB = b.getMaVe() != null ? b.getMaVe() : "";
            return valA.compareTo(valB);
        } else if ("giaVe".equals(property)) {
            BigDecimal valA = a.getGiaVe() != null ? a.getGiaVe() : BigDecimal.ZERO;
            BigDecimal valB = b.getGiaVe() != null ? b.getGiaVe() : BigDecimal.ZERO;
            return valA.compareTo(valB);
        } else if ("trangThai".equals(property)) {
            String valA = a.getTrangThai() != null ? a.getTrangThai() : "";
            String valB = b.getTrangThai() != null ? b.getTrangThai() : "";
            return valA.compareTo(valB);
        } else if ("hoaDon.id".equals(property)) {
            Long valA = a.getHoaDon() != null ? a.getHoaDon().getId() : 0L;
            Long valB = b.getHoaDon() != null ? b.getHoaDon().getId() : 0L;
            return valA.compareTo(valB);
        } else if ("suatChieu.id".equals(property)) {
            Long valA = a.getSuatChieu() != null ? a.getSuatChieu().getId() : 0L;
            Long valB = b.getSuatChieu() != null ? b.getSuatChieu().getId() : 0L;
            return valA.compareTo(valB);
        }
        return 0;
    }

    /**
     *  Get all the ves where Ghe is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VeDTO> findAllWhereGheIsNull() {
        LOG.debug("Request to get all ves where Ghe is null");
        return StreamSupport.stream(veRepository.findAll().spliterator(), false)
            .filter(ve -> ve.getGhe() == null)
            .map(veMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one ve by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VeDTO> findOne(Long id) {
        LOG.debug("Request to get Ve : {}", id);
        return veRepository.findById(id).map(veMapper::toDto);
    }

    /**
     * Delete the ve by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Ve : {}", id);
        veRepository.deleteById(id);
    }

    private boolean matchesQuery(Ve ve, String query) {
        if (query == null) {
            return true;
        }

        String idText = ve.getId() != null ? String.valueOf(ve.getId()) : "";
        String hoaDonId = ve.getHoaDon() != null && ve.getHoaDon().getId() != null ? String.valueOf(ve.getHoaDon().getId()) : "";
        String suatChieuId = ve.getSuatChieu() != null && ve.getSuatChieu().getId() != null
            ? String.valueOf(ve.getSuatChieu().getId())
            : "";

        return (
            contains(idText, query) ||
            contains(ve.getMaVe(), query) ||
            contains(ve.getTrangThai(), query) ||
            contains(hoaDonId, query) ||
            contains(suatChieuId, query)
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<VeDTO> toPage(List<VeDTO> data, Pageable pageable) {
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
