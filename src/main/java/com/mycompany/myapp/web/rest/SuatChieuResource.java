package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.service.SuatChieuService;
import com.mycompany.myapp.service.dto.SuatChieuDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.SuatChieu}.
 */
@RestController
@RequestMapping("/api/suat-chieus")
public class SuatChieuResource {

    private static final Logger LOG = LoggerFactory.getLogger(SuatChieuResource.class);

    private static final String ENTITY_NAME = "suatChieu";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SuatChieuService suatChieuService;

    private final SuatChieuRepository suatChieuRepository;

    public SuatChieuResource(SuatChieuService suatChieuService, SuatChieuRepository suatChieuRepository) {
        this.suatChieuService = suatChieuService;
        this.suatChieuRepository = suatChieuRepository;
    }

    /**
     * {@code POST  /suat-chieus} : Create a new suatChieu.
     *
     * @param suatChieuDTO the suatChieuDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new suatChieuDTO, or with status {@code 400 (Bad Request)} if the suatChieu has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SuatChieuDTO> createSuatChieu(@Valid @RequestBody SuatChieuDTO suatChieuDTO) throws URISyntaxException {
        LOG.debug("REST request to save SuatChieu : {}", suatChieuDTO);
        if (suatChieuDTO.getId() != null) {
            throw new BadRequestAlertException("A new suatChieu cannot already have an ID", ENTITY_NAME, "idexists");
        }
        suatChieuDTO = suatChieuService.save(suatChieuDTO);
        return ResponseEntity.created(new URI("/api/suat-chieus/" + suatChieuDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, suatChieuDTO.getId().toString()))
            .body(suatChieuDTO);
    }

    /**
     * {@code PUT  /suat-chieus/:id} : Updates an existing suatChieu.
     *
     * @param id the id of the suatChieuDTO to save.
     * @param suatChieuDTO the suatChieuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated suatChieuDTO,
     * or with status {@code 400 (Bad Request)} if the suatChieuDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the suatChieuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuatChieuDTO> updateSuatChieu(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SuatChieuDTO suatChieuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update SuatChieu : {}, {}", id, suatChieuDTO);
        if (suatChieuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, suatChieuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!suatChieuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        suatChieuDTO = suatChieuService.update(suatChieuDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, suatChieuDTO.getId().toString()))
            .body(suatChieuDTO);
    }

    /**
     * {@code PATCH  /suat-chieus/:id} : Partial updates given fields of an existing suatChieu, field will ignore if it is null
     *
     * @param id the id of the suatChieuDTO to save.
     * @param suatChieuDTO the suatChieuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated suatChieuDTO,
     * or with status {@code 400 (Bad Request)} if the suatChieuDTO is not valid,
     * or with status {@code 404 (Not Found)} if the suatChieuDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the suatChieuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SuatChieuDTO> partialUpdateSuatChieu(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SuatChieuDTO suatChieuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SuatChieu partially : {}, {}", id, suatChieuDTO);
        if (suatChieuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, suatChieuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!suatChieuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SuatChieuDTO> result = suatChieuService.partialUpdate(suatChieuDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, suatChieuDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /suat-chieus} : get all the suatChieus.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of suatChieus in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SuatChieuDTO>> getAllSuatChieus(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of SuatChieus");
        Page<SuatChieuDTO> page;
        if (eagerload) {
            page = suatChieuService.findAllWithEagerRelationships(pageable);
        } else {
            page = suatChieuService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /suat-chieus/search} : search suatChieus by filters.
     */
    @GetMapping("/search")
    public ResponseEntity<List<SuatChieuDTO>> searchSuatChieus(
        @RequestParam(required = false) List<Long> phimIds,
        @RequestParam(required = false) Long phongChieuId,
        @RequestParam(required = false) String ngayChieu,
        @RequestParam(required = false) String gioBatDau,
        @RequestParam(required = false) String gioKetThuc,
        @RequestParam(required = false) String gioChieu,
        @RequestParam(required = false) String query
    ) {
        LOG.debug(
            "REST request to search SuatChieus for phimIds: {}, phongChieuId: {}, ngayChieu: {}, gioBatDau: {}, gioKetThuc: {}, gioChieu: {}, query: {}",
            phimIds,
            phongChieuId,
            ngayChieu,
            gioBatDau,
            gioKetThuc,
            gioChieu,
            query
        );
        List<SuatChieuDTO> result = suatChieuService.search(phimIds, phongChieuId, ngayChieu, gioBatDau, gioKetThuc, gioChieu, query);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /suat-chieus/:id} : get the "id" suatChieu.
     *
     * @param id the id of the suatChieuDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the suatChieuDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuatChieuDTO> getSuatChieu(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SuatChieu : {}", id);
        Optional<SuatChieuDTO> suatChieuDTO = suatChieuService.findOne(id);
        return ResponseUtil.wrapOrNotFound(suatChieuDTO);
    }

    /**
     * {@code DELETE  /suat-chieus/:id} : delete the "id" suatChieu.
     *
     * @param id the id of the suatChieuDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuatChieu(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SuatChieu : {}", id);
        suatChieuService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code POST /suat-chieus/cleanup-overlaps} : remove overlapping showtimes in the same room.
     */
    @PostMapping("/cleanup-overlaps")
    public ResponseEntity<Void> cleanupOverlappingShowtimes() {
        LOG.debug("REST request to cleanup overlapping showtimes");
        int deletedCount = suatChieuService.cleanupOverlappingShowtimes();
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createAlert(applicationName, "Đã xóa " + deletedCount + " suất chiếu trùng hoặc chồng giờ.", ENTITY_NAME))
            .build();
    }
}
