package com.mycompany.myapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A DichVuFB.
 */
@Entity
@Table(name = "dichvufb")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DichVuFB implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tencombo", nullable = false, columnDefinition = "nvarchar(max)")
    private String tenCombo;

    @Column(name = "mota", columnDefinition = "nvarchar(max)")
    private String moTa;

    @NotNull
    @Column(name = "gia", precision = 21, scale = 2, nullable = false)
    private BigDecimal gia;

    @Column(name = "hinhanh", columnDefinition = "nvarchar(max)")
    private String hinhAnh;

    @Column(name = "trangthai", columnDefinition = "nvarchar(max)")
    private String trangThai;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DichVuFB id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenCombo() {
        return this.tenCombo;
    }

    public DichVuFB tenCombo(String tenCombo) {
        this.setTenCombo(tenCombo);
        return this;
    }

    public void setTenCombo(String tenCombo) {
        this.tenCombo = tenCombo;
    }

    public String getMoTa() {
        return this.moTa;
    }

    public DichVuFB moTa(String moTa) {
        this.setMoTa(moTa);
        return this;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public BigDecimal getGia() {
        return this.gia;
    }

    public DichVuFB gia(BigDecimal gia) {
        this.setGia(gia);
        return this;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getHinhAnh() {
        return this.hinhAnh;
    }

    public DichVuFB hinhAnh(String hinhAnh) {
        this.setHinhAnh(hinhAnh);
        return this;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public DichVuFB trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DichVuFB)) {
            return false;
        }
        return getId() != null && getId().equals(((DichVuFB) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DichVuFB{" +
            "id=" + getId() +
            ", tenCombo='" + getTenCombo() + "'" +
            ", moTa='" + getMoTa() + "'" +
            ", gia=" + getGia() +
            ", hinhAnh='" + getHinhAnh() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
