package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Ve} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VeDTO implements Serializable {

    private Long id;

    @NotNull
    private String maVe;

    private BigDecimal giaVe;

    private String trangThai;

    private HoaDonDTO hoaDon;

    private SuatChieuDTO suatChieu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public HoaDonDTO getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonDTO hoaDon) {
        this.hoaDon = hoaDon;
    }

    public SuatChieuDTO getSuatChieu() {
        return suatChieu;
    }

    public void setSuatChieu(SuatChieuDTO suatChieu) {
        this.suatChieu = suatChieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VeDTO)) {
            return false;
        }

        VeDTO veDTO = (VeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, veDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VeDTO{" +
            "id=" + getId() +
            ", maVe='" + getMaVe() + "'" +
            ", giaVe=" + getGiaVe() +
            ", trangThai='" + getTrangThai() + "'" +
            ", hoaDon=" + getHoaDon() +
            ", suatChieu=" + getSuatChieu() +
            "}";
    }
}
