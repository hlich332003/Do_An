package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.PhongChieu} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PhongChieuDTO implements Serializable {

    private Long id;

    @NotNull
    private String tenPhong;

    private Integer soLuongGhe;

    private String trangThai;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public Integer getSoLuongGhe() {
        return soLuongGhe;
    }

    public void setSoLuongGhe(Integer soLuongGhe) {
        this.soLuongGhe = soLuongGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhongChieuDTO)) {
            return false;
        }

        PhongChieuDTO phongChieuDTO = (PhongChieuDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, phongChieuDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PhongChieuDTO{" +
            "id=" + getId() +
            ", tenPhong='" + getTenPhong() + "'" +
            ", soLuongGhe=" + getSoLuongGhe() +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
