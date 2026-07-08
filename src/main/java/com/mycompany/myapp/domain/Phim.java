package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A Phim.
 */
@Entity
@Table(name = "phim")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Phim implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tenphim", nullable = false, columnDefinition = "nvarchar(max)")
    private String tenPhim;

    @Column(name = "mota", columnDefinition = "nvarchar(max)")
    private String moTa;

    @Column(name = "thoiluong")
    private Integer thoiLuong;

    @Column(name = "daodien", columnDefinition = "nvarchar(max)")
    private String daoDien;

    @Column(name = "dienvien", columnDefinition = "nvarchar(max)")
    private String dienVien;

    @Column(name = "theloai", columnDefinition = "nvarchar(max)")
    private String theLoai;

    @Column(name = "ngaykhoichieu")
    private LocalDate ngayKhoiChieu;

    @Column(name = "poster", columnDefinition = "nvarchar(max)")
    private String poster;

    @Column(name = "trailer", columnDefinition = "nvarchar(max)")
    private String trailer;

    @Column(name = "trangthai")
    private String trangThai;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "phim")
    @JsonIgnoreProperties(value = { "ves", "phim", "phongChieu" }, allowSetters = true)
    private Set<SuatChieu> suatChieus = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Phim id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenPhim() {
        return this.tenPhim;
    }

    public Phim tenPhim(String tenPhim) {
        this.setTenPhim(tenPhim);
        return this;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public String getMoTa() {
        return this.moTa;
    }

    public Phim moTa(String moTa) {
        this.setMoTa(moTa);
        return this;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getThoiLuong() {
        return this.thoiLuong;
    }

    public Phim thoiLuong(Integer thoiLuong) {
        this.setThoiLuong(thoiLuong);
        return this;
    }

    public void setThoiLuong(Integer thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public String getDaoDien() {
        return this.daoDien;
    }

    public Phim daoDien(String daoDien) {
        this.setDaoDien(daoDien);
        return this;
    }

    public void setDaoDien(String daoDien) {
        this.daoDien = daoDien;
    }

    public String getDienVien() {
        return this.dienVien;
    }

    public Phim dienVien(String dienVien) {
        this.setDienVien(dienVien);
        return this;
    }

    public void setDienVien(String dienVien) {
        this.dienVien = dienVien;
    }

    public String getTheLoai() {
        return this.theLoai;
    }

    public Phim theLoai(String theLoai) {
        this.setTheLoai(theLoai);
        return this;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public LocalDate getNgayKhoiChieu() {
        return this.ngayKhoiChieu;
    }

    public Phim ngayKhoiChieu(LocalDate ngayKhoiChieu) {
        this.setNgayKhoiChieu(ngayKhoiChieu);
        return this;
    }

    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) {
        this.ngayKhoiChieu = ngayKhoiChieu;
    }

    public String getPoster() {
        return this.poster;
    }

    public Phim poster(String poster) {
        this.setPoster(poster);
        return this;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTrailer() {
        return this.trailer;
    }

    public Phim trailer(String trailer) {
        this.setTrailer(trailer);
        return this;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public Phim trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Phim createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<SuatChieu> getSuatChieus() {
        return this.suatChieus;
    }

    public void setSuatChieus(Set<SuatChieu> suatChieus) {
        if (this.suatChieus != null) {
            this.suatChieus.forEach(i -> i.setPhim(null));
        }
        if (suatChieus != null) {
            suatChieus.forEach(i -> i.setPhim(this));
        }
        this.suatChieus = suatChieus;
    }

    public Phim suatChieus(Set<SuatChieu> suatChieus) {
        this.setSuatChieus(suatChieus);
        return this;
    }

    public Phim addSuatChieu(SuatChieu suatChieu) {
        this.suatChieus.add(suatChieu);
        suatChieu.setPhim(this);
        return this;
    }

    public Phim removeSuatChieu(SuatChieu suatChieu) {
        this.suatChieus.remove(suatChieu);
        suatChieu.setPhim(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Phim)) {
            return false;
        }
        return getId() != null && getId().equals(((Phim) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Phim{" +
            "id=" + getId() +
            ", tenPhim='" + getTenPhim() + "'" +
            ", moTa='" + getMoTa() + "'" +
            ", thoiLuong=" + getThoiLuong() +
            ", daoDien='" + getDaoDien() + "'" +
            ", dienVien='" + getDienVien() + "'" +
            ", theLoai='" + getTheLoai() + "'" +
            ", ngayKhoiChieu='" + getNgayKhoiChieu() + "'" +
            ", poster='" + getPoster() + "'" +
            ", trailer='" + getTrailer() + "'" +
            "}";
    }
}
