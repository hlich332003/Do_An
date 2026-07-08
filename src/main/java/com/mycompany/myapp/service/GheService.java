package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ghe;
import com.mycompany.myapp.repository.GheRepository;
import com.mycompany.myapp.service.dto.GheDTO;
import com.mycompany.myapp.service.mapper.GheMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Ghe}.
 */
@Service
@Transactional
public class GheService {

    private static final Logger LOG = LoggerFactory.getLogger(GheService.class);

    private final GheRepository gheRepository;

    private final GheMapper gheMapper;

    public GheService(GheRepository gheRepository, GheMapper gheMapper) {
        this.gheRepository = gheRepository;
        this.gheMapper = gheMapper;
    }

    /**
     * Save a ghe.
     *
     * @param gheDTO the entity to save.
     * @return the persisted entity.
     */
    public GheDTO save(GheDTO gheDTO) {
        LOG.debug("Request to save Ghe : {}", gheDTO);
        Ghe ghe = gheMapper.toEntity(gheDTO);
        ghe = gheRepository.save(ghe);
        return gheMapper.toDto(ghe);
    }

    /**
     * Update a ghe.
     *
     * @param gheDTO the entity to save.
     * @return the persisted entity.
     */
    public GheDTO update(GheDTO gheDTO) {
        LOG.debug("Request to update Ghe : {}", gheDTO);
        Ghe ghe = gheMapper.toEntity(gheDTO);
        ghe = gheRepository.save(ghe);
        return gheMapper.toDto(ghe);
    }

    /**
     * Partially update a ghe.
     *
     * @param gheDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GheDTO> partialUpdate(GheDTO gheDTO) {
        LOG.debug("Request to partially update Ghe : {}", gheDTO);

        return gheRepository
            .findById(gheDTO.getId())
            .map(existingGhe -> {
                gheMapper.partialUpdate(existingGhe, gheDTO);

                return existingGhe;
            })
            .map(gheRepository::save)
            .map(gheMapper::toDto);
    }

    /**
     * Get all the ghes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<GheDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Ghes");
        return gheRepository.findAll(pageable).map(gheMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<GheDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search Ghes by query: {}", query);
        return gheRepository.search(query, pageable).map(gheMapper::toDto);
    }

    /**
     * Get all the ghes with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<GheDTO> findAllWithEagerRelationships(Pageable pageable) {
        return gheRepository.findAllWithEagerRelationships(pageable).map(gheMapper::toDto);
    }

    /**
     * Get one ghe by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GheDTO> findOne(Long id) {
        LOG.debug("Request to get Ghe : {}", id);
        return gheRepository.findOneWithEagerRelationships(id).map(gheMapper::toDto);
    }

    /**
     * Delete the ghe by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Ghe : {}", id);
        gheRepository.deleteById(id);
    }

    /**
     * Replace batch ghes for a room in a single transaction.
     */
    public void replaceBatchGhes(Long phongChieuId, List<GheDTO> ghes) {
        LOG.debug("Request to replace batch ghes by phongChieuId : {}", phongChieuId);
        List<Ghe> existing = gheRepository.findByPhongChieuId(phongChieuId);
        if (existing != null && !existing.isEmpty()) {
            gheRepository.deleteAllInBatch(existing);
        }

        for (GheDTO gheDTO : ghes) {
            gheDTO.setId(null);
            Ghe ghe = gheMapper.toEntity(gheDTO);

            com.mycompany.myapp.domain.PhongChieu pc = new com.mycompany.myapp.domain.PhongChieu();
            pc.setId(phongChieuId);
            ghe.setPhongChieu(pc);

            gheRepository.save(ghe);
        }
    }
}
