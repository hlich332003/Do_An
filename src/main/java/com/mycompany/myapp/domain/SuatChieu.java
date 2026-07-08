package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A SuatChieu.
 */
@Entity
@Table(name = "suatchieu")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SuatChieu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "thoigianbatdau", nullable = false)
    private ZonedDateTime thoiGianBatDau;

    @NotNull
    @Column(name = "thoigianketthuc", nullable = false)
    private ZonedDateTime thoiGianKetThuc;

    @Column(name = "giathuong", precision = 21, scale = 2)
    private BigDecimal giaThuong;

    @Column(name = "giavip", precision = 21, scale = 2)
    private BigDecimal giaVip;

    @Column(name = "trangthai")
    private String trangThai;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "suatChieu")
    @JsonIgnoreProperties(value = { "hoaDon", "ghe", "suatChieu", "chiTietHoaDon" }, allowSetters = true)
    private Set<Ve> ves = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phim_id")
    @JsonIgnoreProperties(value = { "suatChieus" }, allowSetters = true)
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id")
    @JsonIgnoreProperties(value = { "suatChieus", "ghes" }, allowSetters = true)
    private PhongChieu phongChieu;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SuatChieu id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getThoiGianBatDau() {
        return this.thoiGianBatDau;
    }

    public SuatChieu thoiGianBatDau(ZonedDateTime thoiGianBatDau) {
        this.setThoiGianBatDau(thoiGianBatDau);
        return this;
    }

    public void setThoiGianBatDau(ZonedDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public ZonedDateTime getThoiGianKetThuc() {
        return this.thoiGianKetThuc;
    }

    public SuatChieu thoiGianKetThuc(ZonedDateTime thoiGianKetThuc) {
        this.setThoiGianKetThuc(thoiGianKetThuc);
        return this;
    }

    public void setThoiGianKetThuc(ZonedDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public BigDecimal getGiaThuong() {
        return this.giaThuong;
    }

    public SuatChieu giaThuong(BigDecimal giaThuong) {
        this.setGiaThuong(giaThuong);
        return this;
    }

    public void setGiaThuong(BigDecimal giaThuong) {
        this.giaThuong = giaThuong;
    }

    public BigDecimal getGiaVip() {
        return this.giaVip;
    }

    public SuatChieu giaVip(BigDecimal giaVip) {
        this.setGiaVip(giaVip);
        return this;
    }

    public void setGiaVip(BigDecimal giaVip) {
        this.giaVip = giaVip;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public SuatChieu trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public SuatChieu createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Ve> getVes() {
        return this.ves;
    }

    public void setVes(Set<Ve> ves) {
        if (this.ves != null) {
            this.ves.forEach(i -> i.setSuatChieu(null));
        }
        if (ves != null) {
            ves.forEach(i -> i.setSuatChieu(this));
        }
        this.ves = ves;
    }

    public SuatChieu ves(Set<Ve> ves) {
        this.setVes(ves);
        return this;
    }

    public SuatChieu addVe(Ve ve) {
        this.ves.add(ve);
        ve.setSuatChieu(this);
        return this;
    }

    public SuatChieu removeVe(Ve ve) {
        this.ves.remove(ve);
        ve.setSuatChieu(null);
        return this;
    }

    public Phim getPhim() {
        return this.phim;
    }

    public void setPhim(Phim phim) {
        this.phim = phim;
    }

    public SuatChieu phim(Phim phim) {
        this.setPhim(phim);
        return this;
    }

    public PhongChieu getPhongChieu() {
        return this.phongChieu;
    }

    public void setPhongChieu(PhongChieu phongChieu) {
        this.phongChieu = phongChieu;
    }

    public SuatChieu phongChieu(PhongChieu phongChieu) {
        this.setPhongChieu(phongChieu);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SuatChieu)) {
            return false;
        }
        return getId() != null && getId().equals(((SuatChieu) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SuatChieu{" +
            "id=" + getId() +
            ", thoiGianBatDau='" + getThoiGianBatDau() + "'" +
            ", thoiGianKetThuc='" + getThoiGianKetThuc() + "'" +
            "}";
    }
}
