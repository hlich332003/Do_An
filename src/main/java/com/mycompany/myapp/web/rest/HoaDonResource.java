package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.service.BookingService;
import com.mycompany.myapp.service.HoaDonService;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.dto.HoaDonLichSuDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.HoaDon}.
 */
@RestController
@RequestMapping("/api/hoa-dons")
public class HoaDonResource {

    private static final Logger LOG = LoggerFactory.getLogger(HoaDonResource.class);

    private static final String ENTITY_NAME = "hoaDon";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final HoaDonService hoaDonService;

    private final HoaDonRepository hoaDonRepository;

    private final com.mycompany.myapp.repository.VeRepository veRepository;

    private final com.mycompany.myapp.repository.ChiTietFBRepository chiTietFBRepository;

    private final com.mycompany.myapp.service.UserService userService;

    private final BookingService bookingService;

    public HoaDonResource(
        HoaDonService hoaDonService,
        HoaDonRepository hoaDonRepository,
        com.mycompany.myapp.repository.VeRepository veRepository,
        com.mycompany.myapp.repository.ChiTietFBRepository chiTietFBRepository,
        com.mycompany.myapp.service.UserService userService,
        BookingService bookingService
    ) {
        this.hoaDonService = hoaDonService;
        this.hoaDonRepository = hoaDonRepository;
        this.veRepository = veRepository;
        this.chiTietFBRepository = chiTietFBRepository;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    /**
     * {@code POST  /hoa-dons} : Create a new hoaDon.
     *
     * @param hoaDonDTO the hoaDonDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new hoaDonDTO, or with status {@code 400 (Bad Request)} if the hoaDon has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<HoaDonDTO> createHoaDon(@Valid @RequestBody HoaDonDTO hoaDonDTO) throws URISyntaxException {
        LOG.debug("REST request to save HoaDon : {}", hoaDonDTO);
        if (hoaDonDTO.getId() != null) {
            throw new BadRequestAlertException("A new hoaDon cannot already have an ID", ENTITY_NAME, "idexists");
        }
        hoaDonDTO = hoaDonService.save(hoaDonDTO);
        return ResponseEntity.created(new URI("/api/hoa-dons/" + hoaDonDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, hoaDonDTO.getId().toString()))
            .body(hoaDonDTO);
    }

    /**
     * {@code PUT  /hoa-dons/:id} : Updates an existing hoaDon.
     *
     * @param id the id of the hoaDonDTO to save.
     * @param hoaDonDTO the hoaDonDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hoaDonDTO,
     * or with status {@code 400 (Bad Request)} if the hoaDonDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the hoaDonDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> updateHoaDon(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HoaDonDTO hoaDonDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update HoaDon : {}, {}", id, hoaDonDTO);
        if (hoaDonDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hoaDonDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hoaDonRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        hoaDonDTO = hoaDonService.update(hoaDonDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hoaDonDTO.getId().toString()))
            .body(hoaDonDTO);
    }

    /**
     * {@code PATCH  /hoa-dons/:id} : Partial updates given fields of an existing hoaDon, field will ignore if it is null
     *
     * @param id the id of the hoaDonDTO to save.
     * @param hoaDonDTO the hoaDonDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hoaDonDTO,
     * or with status {@code 400 (Bad Request)} if the hoaDonDTO is not valid,
     * or with status {@code 404 (Not Found)} if the hoaDonDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the hoaDonDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HoaDonDTO> partialUpdateHoaDon(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody HoaDonDTO hoaDonDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update HoaDon partially : {}, {}", id, hoaDonDTO);
        if (hoaDonDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hoaDonDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hoaDonRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HoaDonDTO> result = hoaDonService.partialUpdate(hoaDonDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hoaDonDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /hoa-dons} : get all the hoaDons.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of hoaDons in body.
     */
    @GetMapping("")
    public ResponseEntity<List<HoaDonDTO>> getAllHoaDons(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of HoaDons");
        Page<HoaDonDTO> page;
        if (eagerload) {
            page = hoaDonService.findAllWithEagerRelationships(pageable);
        } else {
            page = hoaDonService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /hoa-dons/search} : search hoaDons by keyword and date range.
     */
    @GetMapping("/search")
    public ResponseEntity<List<HoaDonDTO>> searchHoaDons(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search HoaDons for query: {}, fromDate: {}, toDate: {}", query, fromDate, toDate);
        ZonedDateTime from = parseStartOfDay(fromDate);
        ZonedDateTime to = parseEndOfDay(toDate);
        Page<HoaDonDTO> page = hoaDonService.search(query, from, to, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/lich-su")
    public ResponseEntity<List<HoaDonLichSuDTO>> getLichSu(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.badRequest().build();
        String login = principal.getName();

        java.util.Optional<com.mycompany.myapp.domain.User> jhiUser = userService.getUserWithAuthoritiesByLogin(login);
        if (jhiUser.isEmpty()) return ResponseEntity.ok(new java.util.ArrayList<>());

        String email = jhiUser.get().getEmail();

        List<HoaDonLichSuDTO> result = bookingService.getLichSuByEmail(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/lich-su")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<HoaDonLichSuDTO>> getAdminLichSu() {
        List<HoaDonLichSuDTO> result = bookingService.getAllLichSu();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/lich-su/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<HoaDonLichSuDTO> getAdminLichSuById(@PathVariable("id") Long id) {
        HoaDonLichSuDTO result = bookingService.getLichSuByHoaDonId(id);
        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET  /hoa-dons/:id} : get the "id" hoaDon.
     *
     * @param id the id of the hoaDonDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the hoaDonDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getHoaDon(@PathVariable("id") Long id) {
        LOG.debug("REST request to get HoaDon : {}", id);
        Optional<HoaDonDTO> hoaDonDTO = hoaDonService.findOne(id);
        return ResponseUtil.wrapOrNotFound(hoaDonDTO);
    }

    /**
     * {@code DELETE  /hoa-dons/:id} : delete the "id" hoaDon.
     *
     * @param id the id of the hoaDonDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoaDon(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete HoaDon : {}", id);
        hoaDonService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    private ZonedDateTime parseStartOfDay(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault());
    }

    private ZonedDateTime parseEndOfDay(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value).atTime(23, 59, 59).atZone(ZoneId.systemDefault());
    }
}
