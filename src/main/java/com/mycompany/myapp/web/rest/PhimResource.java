package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.PhimRepository;
import com.mycompany.myapp.service.PhimService;
import com.mycompany.myapp.service.dto.PhimDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Phim}.
 */
@RestController
@RequestMapping("/api/phims")
public class PhimResource {

    private static final Logger LOG = LoggerFactory.getLogger(PhimResource.class);

    private static final String ENTITY_NAME = "phim";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PhimService phimService;

    private final PhimRepository phimRepository;

    public PhimResource(PhimService phimService, PhimRepository phimRepository) {
        this.phimService = phimService;
        this.phimRepository = phimRepository;
    }

    /**
     * {@code POST  /phims} : Create a new phim.
     *
     * @param phimDTO the phimDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new phimDTO, or with status {@code 400 (Bad Request)} if the
     *         phim has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PhimDTO> createPhim(@Valid @RequestBody PhimDTO phimDTO) throws URISyntaxException {
        LOG.debug("REST request to save Phim : {}", phimDTO);
        if (phimDTO.getId() != null) {
            throw new BadRequestAlertException("A new phim cannot already have an ID", ENTITY_NAME, "idexists");
        }
        phimDTO = phimService.save(phimDTO);
        return ResponseEntity.created(new URI("/api/phims/" + phimDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, phimDTO.getId().toString()))
            .body(phimDTO);
    }

    /**
     * {@code PUT  /phims/:id} : Updates an existing phim.
     *
     * @param id      the id of the phimDTO to save.
     * @param phimDTO the phimDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated phimDTO,
     *         or with status {@code 400 (Bad Request)} if the phimDTO is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the phimDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PhimDTO> updatePhim(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PhimDTO phimDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Phim : {}, {}", id, phimDTO);
        if (phimDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, phimDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!phimRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        phimDTO = phimService.update(phimDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, phimDTO.getId().toString()))
            .body(phimDTO);
    }

    /**
     * {@code PATCH  /phims/:id} : Partial updates given fields of an existing phim,
     * field will ignore if it is null
     *
     * @param id      the id of the phimDTO to save.
     * @param phimDTO the phimDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated phimDTO,
     *         or with status {@code 400 (Bad Request)} if the phimDTO is not valid,
     *         or with status {@code 404 (Not Found)} if the phimDTO is not found,
     *         or with status {@code 500 (Internal Server Error)} if the phimDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PhimDTO> partialUpdatePhim(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PhimDTO phimDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Phim partially : {}, {}", id, phimDTO);
        if (phimDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, phimDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!phimRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PhimDTO> result = phimService.partialUpdate(phimDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, phimDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /phims} : get all the phims.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of phims in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PhimDTO>> getAllPhims(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Phims");
        Page<PhimDTO> page = phimService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /phims/search} : search phims by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<PhimDTO>> searchPhims(
        @RequestParam String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search Phims by query: {}", query);
        Page<PhimDTO> page = phimService.searchByTenPhim(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /phims/showing} : get showing phims.
     */
    @GetMapping("/showing")
    public ResponseEntity<List<PhimDTO>> getShowingPhims(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get showing Phims");
        Page<PhimDTO> page = phimService.findShowing(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /phims/sap-chieu} : get coming soon phims.
     */
    @GetMapping("/sap-chieu")
    public ResponseEntity<List<PhimDTO>> getComingSoonPhims(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get coming soon Phims");
        Page<PhimDTO> page = phimService.findComingSoon(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /phims/:id} : get the "id" phim.
     *
     * @param id the id of the phimDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the phimDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhimDTO> getPhim(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Phim : {}", id);
        Optional<PhimDTO> phimDTO = phimService.findOne(id);
        return ResponseUtil.wrapOrNotFound(phimDTO);
    }

    /**
     * {@code DELETE  /phims/:id} : delete the "id" phim.
     *
     * @param id the id of the phimDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhim(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Phim : {}", id);
        try {
            phimService.delete(id);
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                .build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BadRequestAlertException(
                "Không thể xóa phim này vì đã có suất chiếu hoặc hóa đơn bán vé liên kết!",
                ENTITY_NAME,
                "dataintegrity"
            );
        }
    }
}
