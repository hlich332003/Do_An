package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.ChiTietFB} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChiTietFBDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer soLuong;

    private BigDecimal giaBan;

    private DichVuFBDTO dichVuFB;

    private HoaDonDTO hoaDon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public DichVuFBDTO getDichVuFB() {
        return dichVuFB;
    }

    public void setDichVuFB(DichVuFBDTO dichVuFB) {
        this.dichVuFB = dichVuFB;
    }

    public HoaDonDTO getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonDTO hoaDon) {
        this.hoaDon = hoaDon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChiTietFBDTO)) {
            return false;
        }

        ChiTietFBDTO chiTietFBDTO = (ChiTietFBDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, chiTietFBDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChiTietFBDTO{" +
            "id=" + getId() +
            ", soLuong=" + getSoLuong() +
            ", giaBan=" + getGiaBan() +
            ", dichVuFB=" + getDichVuFB() +
            ", hoaDon=" + getHoaDon() +
            "}";
    }
}
