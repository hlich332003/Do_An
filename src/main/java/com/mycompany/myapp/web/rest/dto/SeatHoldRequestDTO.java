package com.mycompany.myapp.web.rest.dto;

public class SeatHoldRequestDTO {

    private Long suatChieuId;
    private Long gheId;

    public Long getSuatChieuId() {
        return suatChieuId;
    }

    public void setSuatChieuId(Long suatChieuId) {
        this.suatChieuId = suatChieuId;
    }

    public Long getGheId() {
        return gheId;
    }

    public void setGheId(Long gheId) {
        this.gheId = gheId;
    }
}
