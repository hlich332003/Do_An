package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DatVeService;
import com.mycompany.myapp.web.rest.dto.SeatHoldRequestDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dat-ve")
public class DatVeResource {

    private static final Logger LOG = LoggerFactory.getLogger(DatVeResource.class);
    private static final String ENTITY_NAME = "datVe";

    private final DatVeService datVeService;

    public DatVeResource(DatVeService datVeService) {
        this.datVeService = datVeService;
    }

    /**
     * {@code POST  /giu-ghe} : Giữ một ghế cho một suất chiếu cụ thể.
     *
     * @param seatHoldRequestDTO the request containing suatChieuId and gheId.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} if the seat is held, or with status {@code 409 (Conflict)} if the seat is already held.
     */
    @PostMapping({ "/giu-ghe", "/seat/hold" })
    public ResponseEntity<Void> holdSeat(@RequestBody SeatHoldRequestDTO seatHoldRequestDTO) {
        LOG.debug("REST request to hold seat : {}", seatHoldRequestDTO);
        boolean success = datVeService.holdSeat(seatHoldRequestDTO.getSuatChieuId(), seatHoldRequestDTO.getGheId());
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            throw new BadRequestAlertException("Ghế đã có người khác giữ", ENTITY_NAME, "seatheld");
        }
    }

    @PostMapping({ "/giai-phong-ghe", "/seat/release" })
    public ResponseEntity<Void> releaseSeat(@RequestBody SeatHoldRequestDTO seatHoldRequestDTO) {
        LOG.debug("REST request to release seat : {}", seatHoldRequestDTO);
        datVeService.releaseSeat(seatHoldRequestDTO.getSuatChieuId(), seatHoldRequestDTO.getGheId());
        return ResponseEntity.ok().build();
    }
}
