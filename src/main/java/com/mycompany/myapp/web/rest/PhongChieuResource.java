package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.PhongChieuRepository;
import com.mycompany.myapp.service.PhongChieuService;
import com.mycompany.myapp.service.dto.PhongChieuDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.PhongChieu}.
 */
@RestController
@RequestMapping("/api/phong-chieus")
public class PhongChieuResource {

    private static final Logger LOG = LoggerFactory.getLogger(PhongChieuResource.class);

    private static final String ENTITY_NAME = "phongChieu";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PhongChieuService phongChieuService;

    private final PhongChieuRepository phongChieuRepository;

    public PhongChieuResource(PhongChieuService phongChieuService, PhongChieuRepository phongChieuRepository) {
        this.phongChieuService = phongChieuService;
        this.phongChieuRepository = phongChieuRepository;
    }

    /**
     * {@code POST  /phong-chieus} : Create a new phongChieu.
     *
     * @param phongChieuDTO the phongChieuDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new phongChieuDTO, or with status {@code 400 (Bad Request)} if the phongChieu has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PhongChieuDTO> createPhongChieu(@Valid @RequestBody PhongChieuDTO phongChieuDTO) throws URISyntaxException {
        LOG.debug("REST request to save PhongChieu : {}", phongChieuDTO);
        if (phongChieuDTO.getId() != null) {
            throw new BadRequestAlertException("A new phongChieu cannot already have an ID", ENTITY_NAME, "idexists");
        }
        phongChieuDTO = phongChieuService.save(phongChieuDTO);
        return ResponseEntity.created(new URI("/api/phong-chieus/" + phongChieuDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, phongChieuDTO.getId().toString()))
            .body(phongChieuDTO);
    }

    /**
     * {@code PUT  /phong-chieus/:id} : Updates an existing phongChieu.
     *
     * @param id the id of the phongChieuDTO to save.
     * @param phongChieuDTO the phongChieuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated phongChieuDTO,
     * or with status {@code 400 (Bad Request)} if the phongChieuDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the phongChieuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PhongChieuDTO> updatePhongChieu(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PhongChieuDTO phongChieuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update PhongChieu : {}, {}", id, phongChieuDTO);
        if (phongChieuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, phongChieuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!phongChieuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        phongChieuDTO = phongChieuService.update(phongChieuDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, phongChieuDTO.getId().toString()))
            .body(phongChieuDTO);
    }

    /**
     * {@code PATCH  /phong-chieus/:id} : Partial updates given fields of an existing phongChieu, field will ignore if it is null
     *
     * @param id the id of the phongChieuDTO to save.
     * @param phongChieuDTO the phongChieuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated phongChieuDTO,
     * or with status {@code 400 (Bad Request)} if the phongChieuDTO is not valid,
     * or with status {@code 404 (Not Found)} if the phongChieuDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the phongChieuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PhongChieuDTO> partialUpdatePhongChieu(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PhongChieuDTO phongChieuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update PhongChieu partially : {}, {}", id, phongChieuDTO);
        if (phongChieuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, phongChieuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!phongChieuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PhongChieuDTO> result = phongChieuService.partialUpdate(phongChieuDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, phongChieuDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /phong-chieus} : get all the phongChieus.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of phongChieus in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PhongChieuDTO>> getAllPhongChieus(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of PhongChieus");
        Page<PhongChieuDTO> page = phongChieuService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PhongChieuDTO>> searchPhongChieus(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search PhongChieus by query: {}", query);
        Page<PhongChieuDTO> page = phongChieuService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /phong-chieus/:id} : get the "id" phongChieu.
     *
     * @param id the id of the phongChieuDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the phongChieuDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhongChieuDTO> getPhongChieu(@PathVariable("id") Long id) {
        LOG.debug("REST request to get PhongChieu : {}", id);
        Optional<PhongChieuDTO> phongChieuDTO = phongChieuService.findOne(id);
        return ResponseUtil.wrapOrNotFound(phongChieuDTO);
    }

    /**
     * {@code DELETE  /phong-chieus/:id} : delete the "id" phongChieu.
     *
     * @param id the id of the phongChieuDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhongChieu(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete PhongChieu : {}", id);
        phongChieuService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
