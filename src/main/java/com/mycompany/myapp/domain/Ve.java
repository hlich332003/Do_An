package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A Ve.
 */
@Entity
@Table(name = "ve")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Ve implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "mave", nullable = false)
    private String maVe;

    @Column(name = "giave", precision = 21, scale = 2)
    private BigDecimal giaVe;

    @Column(name = "trangthai")
    private String trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoadon_id")
    @JsonIgnoreProperties(value = { "ves", "chiTietFBS", "nguoiDung", "chiTietHoaDons" }, allowSetters = true)
    private HoaDon hoaDon;

    @JsonIgnoreProperties(value = { "ves", "phongChieu" }, allowSetters = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ghe_id")
    private Ghe ghe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suatchieu_id")
    @JsonIgnoreProperties(value = { "ves", "phim", "phongChieu" }, allowSetters = true)
    private SuatChieu suatChieu;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Ve id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaVe() {
        return this.maVe;
    }

    public Ve maVe(String maVe) {
        this.setMaVe(maVe);
        return this;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public BigDecimal getGiaVe() {
        return this.giaVe;
    }

    public Ve giaVe(BigDecimal giaVe) {
        this.setGiaVe(giaVe);
        return this;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public Ve trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public HoaDon getHoaDon() {
        return this.hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public Ve hoaDon(HoaDon hoaDon) {
        this.setHoaDon(hoaDon);
        return this;
    }

    public Ghe getGhe() {
        return this.ghe;
    }

    public void setGhe(Ghe ghe) {
        this.ghe = ghe;
    }

    public Ve ghe(Ghe ghe) {
        this.setGhe(ghe);
        return this;
    }

    public SuatChieu getSuatChieu() {
        return this.suatChieu;
    }

    public void setSuatChieu(SuatChieu suatChieu) {
        this.suatChieu = suatChieu;
    }

    public Ve suatChieu(SuatChieu suatChieu) {
        this.setSuatChieu(suatChieu);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ve)) {
            return false;
        }
        return getId() != null && getId().equals(((Ve) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Ve{" +
            "id=" + getId() +
            ", maVe='" + getMaVe() + "'" +
            ", giaVe=" + getGiaVe() +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
