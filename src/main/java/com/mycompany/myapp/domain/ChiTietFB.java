package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A ChiTietFB.
 */
@Entity
@Table(name = "chitietfb")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChiTietFB implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "soluong", nullable = false)
    private Integer soLuong;

    @Column(name = "giaban", precision = 21, scale = 2)
    private BigDecimal giaBan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fb_id")
    private DichVuFB dichVuFB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoadon_id")
    @JsonIgnoreProperties(value = { "ves", "chiTietFBS", "nguoiDung", "chiTietHoaDons" }, allowSetters = true)
    private HoaDon hoaDon;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChiTietFB id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSoLuong() {
        return this.soLuong;
    }

    public ChiTietFB soLuong(Integer soLuong) {
        this.setSoLuong(soLuong);
        return this;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getGiaBan() {
        return this.giaBan;
    }

    public ChiTietFB giaBan(BigDecimal giaBan) {
        this.setGiaBan(giaBan);
        return this;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public DichVuFB getDichVuFB() {
        return this.dichVuFB;
    }

    public void setDichVuFB(DichVuFB dichVuFB) {
        this.dichVuFB = dichVuFB;
    }

    public ChiTietFB dichVuFB(DichVuFB dichVuFB) {
        this.setDichVuFB(dichVuFB);
        return this;
    }

    public HoaDon getHoaDon() {
        return this.hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public ChiTietFB hoaDon(HoaDon hoaDon) {
        this.setHoaDon(hoaDon);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChiTietFB)) {
            return false;
        }
        return getId() != null && getId().equals(((ChiTietFB) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChiTietFB{" +
            "id=" + getId() +
            ", soLuong=" + getSoLuong() +
            ", giaBan=" + getGiaBan() +
            "}";
    }
}
