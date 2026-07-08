package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.DichVuFBRepository;
import com.mycompany.myapp.service.DichVuFBService;
import com.mycompany.myapp.service.dto.DichVuFBDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.DichVuFB}.
 */
@RestController
@RequestMapping("/api/dich-vu-fbs")
public class DichVuFBResource {

    private static final Logger LOG = LoggerFactory.getLogger(DichVuFBResource.class);

    private static final String ENTITY_NAME = "dichVuFB";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DichVuFBService dichVuFBService;

    private final DichVuFBRepository dichVuFBRepository;

    public DichVuFBResource(DichVuFBService dichVuFBService, DichVuFBRepository dichVuFBRepository) {
        this.dichVuFBService = dichVuFBService;
        this.dichVuFBRepository = dichVuFBRepository;
    }

    /**
     * {@code POST  /dich-vu-fbs} : Create a new dichVuFB.
     *
     * @param dichVuFBDTO the dichVuFBDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dichVuFBDTO, or with status {@code 400 (Bad Request)} if the dichVuFB has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DichVuFBDTO> createDichVuFB(@Valid @RequestBody DichVuFBDTO dichVuFBDTO) throws URISyntaxException {
        LOG.debug("REST request to save DichVuFB : {}", dichVuFBDTO);
        if (dichVuFBDTO.getId() != null) {
            throw new BadRequestAlertException("A new dichVuFB cannot already have an ID", ENTITY_NAME, "idexists");
        }
        dichVuFBDTO = dichVuFBService.save(dichVuFBDTO);
        return ResponseEntity.created(new URI("/api/dich-vu-fbs/" + dichVuFBDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, dichVuFBDTO.getId().toString()))
            .body(dichVuFBDTO);
    }

    /**
     * {@code PUT  /dich-vu-fbs/:id} : Updates an existing dichVuFB.
     *
     * @param id the id of the dichVuFBDTO to save.
     * @param dichVuFBDTO the dichVuFBDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dichVuFBDTO,
     * or with status {@code 400 (Bad Request)} if the dichVuFBDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dichVuFBDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DichVuFBDTO> updateDichVuFB(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DichVuFBDTO dichVuFBDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DichVuFB : {}, {}", id, dichVuFBDTO);
        if (dichVuFBDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dichVuFBDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dichVuFBRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        dichVuFBDTO = dichVuFBService.update(dichVuFBDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dichVuFBDTO.getId().toString()))
            .body(dichVuFBDTO);
    }

    /**
     * {@code PATCH  /dich-vu-fbs/:id} : Partial updates given fields of an existing dichVuFB, field will ignore if it is null
     *
     * @param id the id of the dichVuFBDTO to save.
     * @param dichVuFBDTO the dichVuFBDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dichVuFBDTO,
     * or with status {@code 400 (Bad Request)} if the dichVuFBDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dichVuFBDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dichVuFBDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DichVuFBDTO> partialUpdateDichVuFB(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DichVuFBDTO dichVuFBDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DichVuFB partially : {}, {}", id, dichVuFBDTO);
        if (dichVuFBDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dichVuFBDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dichVuFBRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DichVuFBDTO> result = dichVuFBService.partialUpdate(dichVuFBDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dichVuFBDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /dich-vu-fbs} : get all the dichVuFBS.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dichVuFBS in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DichVuFBDTO>> getAllDichVuFBS(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of DichVuFBS");
        Page<DichVuFBDTO> page = dichVuFBService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DichVuFBDTO>> searchDichVuFBS(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search DichVuFBS by query: {}", query);
        Page<DichVuFBDTO> page = dichVuFBService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DichVuFBDTO>> getActiveDichVuFBS() {
        LOG.debug("REST request to get active DichVuFBS");
        return ResponseEntity.ok(dichVuFBService.findActive());
    }

    /**
     * {@code GET  /dich-vu-fbs/:id} : get the "id" dichVuFB.
     *
     * @param id the id of the dichVuFBDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dichVuFBDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DichVuFBDTO> getDichVuFB(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DichVuFB : {}", id);
        Optional<DichVuFBDTO> dichVuFBDTO = dichVuFBService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dichVuFBDTO);
    }

    /**
     * {@code DELETE  /dich-vu-fbs/:id} : delete the "id" dichVuFB.
     *
     * @param id the id of the dichVuFBDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDichVuFB(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DichVuFB : {}", id);
        dichVuFBService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
