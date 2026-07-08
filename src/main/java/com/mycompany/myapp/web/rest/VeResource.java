package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.QRCodeService;
import com.mycompany.myapp.service.VeService;
import com.mycompany.myapp.service.dto.VeDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Ve}.
 */
@RestController
@RequestMapping("/api/ves")
public class VeResource {

    private static final Logger LOG = LoggerFactory.getLogger(VeResource.class);

    private static final String ENTITY_NAME = "ve";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final VeService veService;

    private final VeRepository veRepository;
    private final QRCodeService qrCodeService;

    public VeResource(VeService veService, VeRepository veRepository, QRCodeService qrCodeService) {
        this.veService = veService;
        this.veRepository = veRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * {@code POST  /ves} : Create a new ve.
     *
     * @param veDTO the veDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new veDTO, or with status {@code 400 (Bad Request)} if the
     *         ve has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VeDTO> createVe(@Valid @RequestBody VeDTO veDTO) throws URISyntaxException {
        LOG.debug("REST request to save Ve : {}", veDTO);
        if (veDTO.getId() != null) {
            throw new BadRequestAlertException("A new ve cannot already have an ID", ENTITY_NAME, "idexists");
        }
        veDTO = veService.save(veDTO);
        return ResponseEntity.created(new URI("/api/ves/" + veDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, veDTO.getId().toString()))
            .body(veDTO);
    }

    /**
     * {@code PUT  /ves/:id} : Updates an existing ve.
     *
     * @param id    the id of the veDTO to save.
     * @param veDTO the veDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated veDTO,
     *         or with status {@code 400 (Bad Request)} if the veDTO is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the veDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VeDTO> updateVe(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody VeDTO veDTO)
        throws URISyntaxException {
        LOG.debug("REST request to update Ve : {}, {}", id, veDTO);
        if (veDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, veDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!veRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        veDTO = veService.update(veDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, veDTO.getId().toString()))
            .body(veDTO);
    }

    /**
     * {@code PATCH  /ves/:id} : Partial updates given fields of an existing ve,
     * field will ignore if it is null
     *
     * @param id    the id of the veDTO to save.
     * @param veDTO the veDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated veDTO,
     *         or with status {@code 400 (Bad Request)} if the veDTO is not valid,
     *         or with status {@code 404 (Not Found)} if the veDTO is not found,
     *         or with status {@code 500 (Internal Server Error)} if the veDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VeDTO> partialUpdateVe(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VeDTO veDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Ve partially : {}, {}", id, veDTO);
        if (veDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, veDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!veRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VeDTO> result = veService.partialUpdate(veDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, veDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /ves} : get all the ves.
     *
     * @param pageable the pagination information.
     * @param filter   the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of ves in body.
     */
    @GetMapping("")
    public ResponseEntity<List<VeDTO>> getAllVes(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        if ("ghe-is-null".equals(filter)) {
            LOG.debug("REST request to get all Ves where ghe is null");
            return new ResponseEntity<>(veService.findAllWhereGheIsNull(), HttpStatus.OK);
        }
        LOG.debug("REST request to get a page of Ves");
        Page<VeDTO> page = veService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<VeDTO>> searchVes(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search Ves by query: {}", query);
        Page<VeDTO> page = veService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ves/:id} : get the "id" ve.
     *
     * @param id the id of the veDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the veDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VeDTO> getVe(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Ve : {}", id);
        Optional<VeDTO> veDTO = veService.findOne(id);
        return ResponseUtil.wrapOrNotFound(veDTO);
    }

    /**
     * {@code DELETE  /ves/:id} : delete the "id" ve.
     *
     * @param id the id of the veDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVe(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Ve : {}", id);
        veService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/{id}/qr-code")
    public ResponseEntity<String> getTicketQrCode(@PathVariable("id") Long id) {
        LOG.debug("REST request to get QR code for Ve : {}", id);
        VeDTO veDTO = veService.findOne(id).orElseThrow(() -> new BadRequestAlertException("Ve khong ton tai", ENTITY_NAME, "idnotfound"));

        String qrData =
            "CINEMATICK|VE:" +
            veDTO.getId() +
            "|MAVE:" +
            (veDTO.getMaVe() != null ? veDTO.getMaVe() : "") +
            "|HD:" +
            (veDTO.getHoaDon() != null && veDTO.getHoaDon().getId() != null ? veDTO.getHoaDon().getId() : "");

        return ResponseEntity.ok(qrCodeService.generateQRCode(qrData));
    }
}
