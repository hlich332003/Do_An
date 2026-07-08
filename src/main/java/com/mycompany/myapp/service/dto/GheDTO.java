package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Ghe} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GheDTO implements Serializable {

    private Long id;

    @NotNull
    private String maGhe;

    private String hang;

    private Integer cot;

    private Integer loaiGhe;

    private String trangThai;

    private PhongChieuDTO phongChieu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaGhe() {
        return maGhe;
    }

    public void setMaGhe(String maGhe) {
        this.maGhe = maGhe;
    }

    public String getHang() {
        return hang;
    }

    public void setHang(String hang) {
        this.hang = hang;
    }

    public Integer getCot() {
        return cot;
    }

    public void setCot(Integer cot) {
        this.cot = cot;
    }

    public Integer getLoaiGhe() {
        return loaiGhe;
    }

    public void setLoaiGhe(Integer loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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
        if (!(o instanceof GheDTO)) {
            return false;
        }

        GheDTO gheDTO = (GheDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, gheDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GheDTO{" +
            "id=" + getId() +
            ", maGhe='" + getMaGhe() + "'" +
            ", hang='" + getHang() + "'" +
            ", cot=" + getCot() +
            ", loaiGhe='" + getLoaiGhe() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            ", phongChieu=" + getPhongChieu() +
            "}";
    }
}
