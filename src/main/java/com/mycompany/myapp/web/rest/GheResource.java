package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.GheRepository;
import com.mycompany.myapp.repository.PhongChieuRepository;
import com.mycompany.myapp.service.BookingService;
import com.mycompany.myapp.service.DatVeService;
import com.mycompany.myapp.service.DistributedLockService;
import com.mycompany.myapp.service.GheService;
import com.mycompany.myapp.service.dto.GheDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/ghes")
public class GheResource {

    private static final Logger LOG = LoggerFactory.getLogger(GheResource.class);
    private static final String ENTITY_NAME = "ghe";
    private static final int DEFAULT_SEATS_PER_ROW = 12;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GheService gheService;
    private final GheRepository gheRepository;
    private final PhongChieuRepository phongChieuRepository;
    private final DatVeService datVeService;
    private final DistributedLockService lockService;
    private final BookingService bookingService;

    public GheResource(
        GheService gheService,
        GheRepository gheRepository,
        PhongChieuRepository phongChieuRepository,
        DatVeService datVeService,
        DistributedLockService lockService,
        BookingService bookingService
    ) {
        this.gheService = gheService;
        this.gheRepository = gheRepository;
        this.phongChieuRepository = phongChieuRepository;
        this.datVeService = datVeService;
        this.lockService = lockService;
        this.bookingService = bookingService;
    }

    @PostMapping("")
    public ResponseEntity<GheDTO> createGhe(@Valid @RequestBody GheDTO gheDTO) throws URISyntaxException {
        LOG.debug("REST request to save Ghe : {}", gheDTO);
        if (gheDTO.getId() != null) {
            throw new BadRequestAlertException("A new ghe cannot already have an ID", ENTITY_NAME, "idexists");
        }
        gheDTO = gheService.save(gheDTO);
        return ResponseEntity.created(new URI("/api/ghes/" + gheDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, gheDTO.getId().toString()))
            .body(gheDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GheDTO> updateGhe(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody GheDTO gheDTO)
        throws URISyntaxException {
        LOG.debug("REST request to update Ghe : {}, {}", id, gheDTO);
        if (gheDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gheDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!gheRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        gheDTO = gheService.update(gheDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, gheDTO.getId().toString()))
            .body(gheDTO);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<GheDTO> partialUpdateGhe(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody GheDTO gheDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Ghe partially : {}, {}", id, gheDTO);
        if (gheDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gheDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!gheRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GheDTO> result = gheService.partialUpdate(gheDTO);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, gheDTO.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<GheDTO>> getAllGhes(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Ghes");
        Page<GheDTO> page = eagerload ? gheService.findAllWithEagerRelationships(pageable) : gheService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<List<GheDTO>> searchGhes(
        @RequestParam(required = false) String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to search Ghes by query: {}", query);
        Page<GheDTO> page = query != null && !query.isBlank()
            ? gheService.search(query, pageable)
            : eagerload ? gheService.findAllWithEagerRelationships(pageable) : gheService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GheDTO> getGhe(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Ghe : {}", id);
        Optional<GheDTO> gheDTO = gheService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gheDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGhe(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Ghe : {}", id);
        gheService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/so-do/{suatChieuId}/{phongChieuId}")
    public ResponseEntity<List<GheDTO>> getSeatMap(
        @PathVariable("suatChieuId") Long suatChieuId,
        @PathVariable("phongChieuId") Long phongChieuId
    ) {
        LOG.debug("REST request to get seat map for suatChieu : {}", suatChieuId);

        bookingService.cleanupExpiredBookings();

        List<GheDTO> ghes = gheService
            .findAll(Pageable.unpaged())
            .getContent()
            .stream()
            .filter(g -> g.getPhongChieu() != null && phongChieuId.equals(g.getPhongChieu().getId()))
            .toList();

        ghes.forEach(ghe -> {
            if (datVeService.isSeatSoldForShowtime(suatChieuId, ghe.getId())) {
                ghe.setTrangThai("2");
            } else if (ghe.getMaGhe() != null && lockService.isSeatLocked(suatChieuId, ghe.getMaGhe())) {
                ghe.setTrangThai("1");
            } else {
                ghe.setTrangThai("0");
            }
        });

        return ResponseEntity.ok().body(ghes);
    }

    @GetMapping("/phong-chieu/{phongChieuId}")
    public ResponseEntity<List<GheDTO>> getGhesByPhongChieu(@PathVariable("phongChieuId") Long phongChieuId) {
        LOG.debug("REST request to get ghes by phongChieuId : {}", phongChieuId);
        List<GheDTO> ghes = gheService
            .findAll(Pageable.unpaged())
            .getContent()
            .stream()
            .filter(g -> g.getPhongChieu() != null && phongChieuId.equals(g.getPhongChieu().getId()))
            .toList();
        return ResponseEntity.ok().body(ghes);
    }

    @PostMapping("/phong-chieu/{phongChieuId}/generate")
    public ResponseEntity<List<GheDTO>> generateGhes(@PathVariable("phongChieuId") Long phongChieuId) {
        LOG.debug("REST request to generate ghes by phongChieuId : {}", phongChieuId);
        List<GheDTO> existing = gheService
            .findAll(Pageable.unpaged())
            .getContent()
            .stream()
            .filter(g -> g.getPhongChieu() != null && phongChieuId.equals(g.getPhongChieu().getId()))
            .toList();
        if (!existing.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PhongChieuDTO phongChieuDTO = new PhongChieuDTO();
        phongChieuDTO.setId(phongChieuId);

        int totalSeats = phongChieuRepository
            .findById(phongChieuId)
            .map(phong -> phong.getSoLuongGhe() != null ? phong.getSoLuongGhe() : 0)
            .orElse(0);
        if (totalSeats <= 0) {
            totalSeats = 120;
        }

        int totalRows = Math.max(1, (int) Math.ceil((double) totalSeats / DEFAULT_SEATS_PER_ROW));
        int vipStart = Math.max(1, (int) Math.floor(totalRows * 0.55));
        int coupleStart = Math.max(vipStart + 1, totalRows - 2);

        String roomName = phongChieuRepository
            .findById(phongChieuId)
            .map(phong -> phong.getTenPhong() != null ? phong.getTenPhong().toLowerCase() : "")
            .orElse("");
        boolean coupleOnly =
            roomName.contains("sweetbox") || roomName.contains("couple") || roomName.contains("đôi") || roomName.contains("doi");
        boolean vipOnly = roomName.contains("vip") || roomName.contains("gold class") || roomName.contains("premium");

        for (int index = 0; index < totalSeats; index++) {
            String row = toRowLabel(index / DEFAULT_SEATS_PER_ROW);
            int col = (index % DEFAULT_SEATS_PER_ROW) + 1;

            int seatType;
            if (coupleOnly) {
                seatType = 3;
            } else if (vipOnly) {
                seatType = 2;
            } else {
                int rowIndex = index / DEFAULT_SEATS_PER_ROW;
                if (rowIndex >= coupleStart) {
                    seatType = 3;
                } else if (rowIndex >= vipStart) {
                    seatType = 2;
                } else {
                    seatType = 1;
                }
            }

            GheDTO ghe = new GheDTO();
            ghe.setMaGhe(row + col);
            ghe.setHang(row);
            ghe.setCot(col);
            ghe.setTrangThai("1");
            ghe.setLoaiGhe(seatType);
            ghe.setPhongChieu(phongChieuDTO);
            gheService.save(ghe);
        }

        List<GheDTO> ghes = gheService
            .findAll(Pageable.unpaged())
            .getContent()
            .stream()
            .filter(g -> g.getPhongChieu() != null && phongChieuId.equals(g.getPhongChieu().getId()))
            .toList();
        return ResponseEntity.ok().body(ghes);
    }

    @PutMapping("/phong-chieu/{phongChieuId}/batch")
    public ResponseEntity<Void> updateBatchGhes(@PathVariable("phongChieuId") Long phongChieuId, @RequestBody List<GheDTO> ghes) {
        LOG.debug("REST request to update batch ghes by phongChieuId : {}", phongChieuId);
        try {
            gheService.replaceBatchGhes(phongChieuId, ghes);
        } catch (org.springframework.dao.DataIntegrityViolationException | jakarta.persistence.PersistenceException e) {
            throw new BadRequestAlertException(
                "Không thể thay đổi sơ đồ ghế vì phòng chiếu này đã phát sinh vé đặt.",
                ENTITY_NAME,
                "hasAssociatedTickets"
            );
        }
        return ResponseEntity.ok().build();
    }

    private String toRowLabel(int index) {
        StringBuilder label = new StringBuilder();
        int value = index + 1;
        while (value > 0) {
            value--;
            label.insert(0, (char) ('A' + (value % 26)));
            value /= 26;
        }
        return label.toString();
    }
}
