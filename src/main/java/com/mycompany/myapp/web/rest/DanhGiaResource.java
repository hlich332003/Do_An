package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DanhGiaService;
import com.mycompany.myapp.service.dto.DanhGiaDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

@RestController
@RequestMapping("/api/danh-gias")
public class DanhGiaResource {

    private static final Logger LOG = LoggerFactory.getLogger(DanhGiaResource.class);

    private static final String ENTITY_NAME = "danhGia";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DanhGiaService danhGiaService;

    public DanhGiaResource(DanhGiaService danhGiaService) {
        this.danhGiaService = danhGiaService;
    }

    @GetMapping("")
    public ResponseEntity<List<DanhGiaDTO>> getDanhGias(@RequestParam Long phimId) {
        LOG.debug("REST request to get reviews for phimId={}", phimId);
        return ResponseEntity.ok(danhGiaService.findByPhimId(phimId));
    }

    @GetMapping("/rating")
    public ResponseEntity<Double> getAverageRating(@RequestParam Long phimId) {
        LOG.debug("REST request to get average rating for phimId={}", phimId);
        return ResponseEntity.ok(danhGiaService.getAverageRating(phimId));
    }

    @PostMapping("")
    public ResponseEntity<DanhGiaDTO> createDanhGia(@Valid @RequestBody DanhGiaDTO danhGiaDTO) throws URISyntaxException {
        LOG.debug("REST request to save DanhGia : {}", danhGiaDTO);
        if (danhGiaDTO.getId() != null) {
            throw new BadRequestAlertException("A new danhGia cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DanhGiaDTO result = danhGiaService.save(danhGiaDTO);
        return ResponseEntity.created(new URI("/api/danh-gias/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DanhGiaDTO> updateDanhGia(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DanhGiaDTO danhGiaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DanhGia : {}, {}", id, danhGiaDTO);
        if (danhGiaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, danhGiaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        DanhGiaDTO result = danhGiaService.update(danhGiaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
}
