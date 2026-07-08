package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.SuatChieu} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SuatChieuDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime thoiGianBatDau;

    @NotNull
    private ZonedDateTime thoiGianKetThuc;

    private BigDecimal giaThuong;

    private BigDecimal giaVip;

    private PhimDTO phim;

    private PhongChieuDTO phongChieu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(ZonedDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public ZonedDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(ZonedDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public BigDecimal getGiaThuong() {
        return giaThuong;
    }

    public void setGiaThuong(BigDecimal giaThuong) {
        this.giaThuong = giaThuong;
    }

    public BigDecimal getGiaVip() {
        return giaVip;
    }

    public void setGiaVip(BigDecimal giaVip) {
        this.giaVip = giaVip;
    }

    public PhimDTO getPhim() {
        return phim;
    }

    public void setPhim(PhimDTO phim) {
        this.phim = phim;
    }

    public PhongChieuDTO getPhongChieu() {
        return phongChieu;
    }

    public void setPhongChieu(PhongChieuDTO phongChieu) {
        this.phongChieu = phongChieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SuatChieuDTO)) {
            return false;
        }

        SuatChieuDTO suatChieuDTO = (SuatChieuDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, suatChieuDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SuatChieuDTO{" +
            "id=" + getId() +
            ", thoiGianBatDau='" + getThoiGianBatDau() + "'" +
            ", thoiGianKetThuc='" + getThoiGianKetThuc() + "'" +
            ", giaThuong=" + getGiaThuong() +
            ", giaVip=" + getGiaVip() +
            ", phim=" + getPhim() +
            ", phongChieu=" + getPhongChieu() +
            "}";
    }
}
