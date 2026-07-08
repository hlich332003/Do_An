package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.ChiTietFBRepository;
import com.mycompany.myapp.service.ChiTietFBService;
import com.mycompany.myapp.service.dto.ChiTietFBDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.ChiTietFB}.
 */
@RestController
@RequestMapping("/api/chi-tiet-fbs")
public class ChiTietFBResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChiTietFBResource.class);

    private static final String ENTITY_NAME = "chiTietFB";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChiTietFBService chiTietFBService;

    private final ChiTietFBRepository chiTietFBRepository;

    public ChiTietFBResource(ChiTietFBService chiTietFBService, ChiTietFBRepository chiTietFBRepository) {
        this.chiTietFBService = chiTietFBService;
        this.chiTietFBRepository = chiTietFBRepository;
    }

    /**
     * {@code POST  /chi-tiet-fbs} : Create a new chiTietFB.
     *
     * @param chiTietFBDTO the chiTietFBDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new chiTietFBDTO, or with status {@code 400 (Bad Request)} if the chiTietFB has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ChiTietFBDTO> createChiTietFB(@Valid @RequestBody ChiTietFBDTO chiTietFBDTO) throws URISyntaxException {
        LOG.debug("REST request to save ChiTietFB : {}", chiTietFBDTO);
        if (chiTietFBDTO.getId() != null) {
            throw new BadRequestAlertException("A new chiTietFB cannot already have an ID", ENTITY_NAME, "idexists");
        }
        chiTietFBDTO = chiTietFBService.save(chiTietFBDTO);
        return ResponseEntity.created(new URI("/api/chi-tiet-fbs/" + chiTietFBDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, chiTietFBDTO.getId().toString()))
            .body(chiTietFBDTO);
    }

    /**
     * {@code PUT  /chi-tiet-fbs/:id} : Updates an existing chiTietFB.
     *
     * @param id the id of the chiTietFBDTO to save.
     * @param chiTietFBDTO the chiTietFBDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chiTietFBDTO,
     * or with status {@code 400 (Bad Request)} if the chiTietFBDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the chiTietFBDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChiTietFBDTO> updateChiTietFB(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ChiTietFBDTO chiTietFBDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ChiTietFB : {}, {}", id, chiTietFBDTO);
        if (chiTietFBDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chiTietFBDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chiTietFBRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        chiTietFBDTO = chiTietFBService.update(chiTietFBDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, chiTietFBDTO.getId().toString()))
            .body(chiTietFBDTO);
    }

    /**
     * {@code PATCH  /chi-tiet-fbs/:id} : Partial updates given fields of an existing chiTietFB, field will ignore if it is null
     *
     * @param id the id of the chiTietFBDTO to save.
     * @param chiTietFBDTO the chiTietFBDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chiTietFBDTO,
     * or with status {@code 400 (Bad Request)} if the chiTietFBDTO is not valid,
     * or with status {@code 404 (Not Found)} if the chiTietFBDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the chiTietFBDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ChiTietFBDTO> partialUpdateChiTietFB(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ChiTietFBDTO chiTietFBDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ChiTietFB partially : {}, {}", id, chiTietFBDTO);
        if (chiTietFBDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chiTietFBDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chiTietFBRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ChiTietFBDTO> result = chiTietFBService.partialUpdate(chiTietFBDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, chiTietFBDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /chi-tiet-fbs} : get all the chiTietFBS.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of chiTietFBS in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ChiTietFBDTO>> getAllChiTietFBS(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ChiTietFBS");
        Page<ChiTietFBDTO> page;
        if (eagerload) {
            page = chiTietFBService.findAllWithEagerRelationships(pageable);
        } else {
            page = chiTietFBService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChiTietFBDTO>> searchChiTietFBS(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to search ChiTietFBS by query: {}", query);
        Page<ChiTietFBDTO> page = query != null && !query.isBlank()
            ? chiTietFBService.search(query, pageable)
            : eagerload ? chiTietFBService.findAllWithEagerRelationships(pageable) : chiTietFBService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /chi-tiet-fbs/:id} : get the "id" chiTietFB.
     *
     * @param id the id of the chiTietFBDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chiTietFBDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChiTietFBDTO> getChiTietFB(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ChiTietFB : {}", id);
        Optional<ChiTietFBDTO> chiTietFBDTO = chiTietFBService.findOne(id);
        return ResponseUtil.wrapOrNotFound(chiTietFBDTO);
    }

    /**
     * {@code DELETE  /chi-tiet-fbs/:id} : delete the "id" chiTietFB.
     *
     * @param id the id of the chiTietFBDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChiTietFB(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ChiTietFB : {}", id);
        chiTietFBService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
