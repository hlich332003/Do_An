package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.DichVuFB} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DichVuFBDTO implements Serializable {

    private Long id;

    @NotNull
    private String tenCombo;

    private String moTa;

    @NotNull
    private BigDecimal gia;

    private String hinhAnh;

    private String trangThai;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenCombo() {
        return tenCombo;
    }

    public void setTenCombo(String tenCombo) {
        this.tenCombo = tenCombo;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
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
        if (!(o instanceof DichVuFBDTO)) {
            return false;
        }

        DichVuFBDTO dichVuFBDTO = (DichVuFBDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dichVuFBDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DichVuFBDTO{" +
            "id=" + getId() +
            ", tenCombo='" + getTenCombo() + "'" +
            ", moTa='" + getMoTa() + "'" +
            ", gia=" + getGia() +
            ", hinhAnh='" + getHinhAnh() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
