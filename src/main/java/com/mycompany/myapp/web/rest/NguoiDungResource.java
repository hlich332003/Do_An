package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.service.NguoiDungService;
import com.mycompany.myapp.service.dto.NguoiDungDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.NguoiDung}.
 */
@RestController
@RequestMapping("/api/nguoi-dungs")
public class NguoiDungResource {

    private static final Logger LOG = LoggerFactory.getLogger(NguoiDungResource.class);

    private static final String ENTITY_NAME = "nguoiDung";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NguoiDungService nguoiDungService;

    private final NguoiDungRepository nguoiDungRepository;

    public NguoiDungResource(NguoiDungService nguoiDungService, NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungService = nguoiDungService;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    /**
     * {@code POST  /nguoi-dungs} : Create a new nguoiDung.
     *
     * @param nguoiDungDTO the nguoiDungDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new nguoiDungDTO, or with status {@code 400 (Bad Request)} if the nguoiDung has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NguoiDungDTO> createNguoiDung(@Valid @RequestBody NguoiDungDTO nguoiDungDTO) throws URISyntaxException {
        LOG.debug("REST request to save NguoiDung : {}", nguoiDungDTO);
        if (nguoiDungDTO.getId() != null) {
            throw new BadRequestAlertException("A new nguoiDung cannot already have an ID", ENTITY_NAME, "idexists");
        }
        nguoiDungDTO = nguoiDungService.save(nguoiDungDTO);
        return ResponseEntity.created(new URI("/api/nguoi-dungs/" + nguoiDungDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, nguoiDungDTO.getId().toString()))
            .body(nguoiDungDTO);
    }

    /**
     * {@code PUT  /nguoi-dungs/:id} : Updates an existing nguoiDung.
     *
     * @param id the id of the nguoiDungDTO to save.
     * @param nguoiDungDTO the nguoiDungDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated nguoiDungDTO,
     * or with status {@code 400 (Bad Request)} if the nguoiDungDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the nguoiDungDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NguoiDungDTO> updateNguoiDung(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody NguoiDungDTO nguoiDungDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update NguoiDung : {}, {}", id, nguoiDungDTO);
        if (nguoiDungDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, nguoiDungDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!nguoiDungRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        nguoiDungDTO = nguoiDungService.update(nguoiDungDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, nguoiDungDTO.getId().toString()))
            .body(nguoiDungDTO);
    }

    /**
     * {@code PATCH  /nguoi-dungs/:id} : Partial updates given fields of an existing nguoiDung, field will ignore if it is null
     *
     * @param id the id of the nguoiDungDTO to save.
     * @param nguoiDungDTO the nguoiDungDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated nguoiDungDTO,
     * or with status {@code 400 (Bad Request)} if the nguoiDungDTO is not valid,
     * or with status {@code 404 (Not Found)} if the nguoiDungDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the nguoiDungDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NguoiDungDTO> partialUpdateNguoiDung(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody NguoiDungDTO nguoiDungDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update NguoiDung partially : {}, {}", id, nguoiDungDTO);
        if (nguoiDungDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, nguoiDungDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!nguoiDungRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NguoiDungDTO> result = nguoiDungService.partialUpdate(nguoiDungDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, nguoiDungDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /nguoi-dungs} : get all the nguoiDungs.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of nguoiDungs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NguoiDungDTO>> getAllNguoiDungs(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "email.equals", required = false) String emailEquals
    ) {
        LOG.debug("REST request to get a page of NguoiDungs");
        if (emailEquals != null && !emailEquals.isBlank()) {
            List<NguoiDungDTO> filtered = nguoiDungRepository
                .findOneByEmailIgnoreCase(emailEquals)
                .flatMap(nguoiDung -> nguoiDungService.findOne(nguoiDung.getId()))
                .map(List::of)
                .orElse(List.of());
            return ResponseEntity.ok().body(filtered);
        }
        Page<NguoiDungDTO> page = nguoiDungService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /nguoi-dungs/search} : search nguoiDungs by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<List<NguoiDungDTO>> searchNguoiDungs(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search NguoiDungs by query: {}", query);
        Page<NguoiDungDTO> page = nguoiDungService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /nguoi-dungs/:id} : get the "id" nguoiDung.
     *
     * @param id the id of the nguoiDungDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the nguoiDungDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NguoiDungDTO> getNguoiDung(@PathVariable("id") Long id) {
        LOG.debug("REST request to get NguoiDung : {}", id);
        Optional<NguoiDungDTO> nguoiDungDTO = nguoiDungService.findOne(id);
        return ResponseUtil.wrapOrNotFound(nguoiDungDTO);
    }

    /**
     * {@code DELETE  /nguoi-dungs/:id} : delete the "id" nguoiDung.
     *
     * @param id the id of the nguoiDungDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNguoiDung(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete NguoiDung : {}", id);
        nguoiDungService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
