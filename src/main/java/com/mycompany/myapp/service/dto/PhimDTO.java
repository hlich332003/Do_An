package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Phim} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PhimDTO implements Serializable {

    private Long id;

    @NotNull
    private String tenPhim;

    private String moTa;

    private Integer thoiLuong;

    private String daoDien;

    private String dienVien;

    private String theLoai;

    private LocalDate ngayKhoiChieu;

    private String poster;

    private String trailer;

    private String trangThai;

    private java.time.ZonedDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(Integer thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public String getDaoDien() {
        return daoDien;
    }

    public void setDaoDien(String daoDien) {
        this.daoDien = daoDien;
    }

    public String getDienVien() {
        return dienVien;
    }

    public void setDienVien(String dienVien) {
        this.dienVien = dienVien;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public LocalDate getNgayKhoiChieu() {
        return ngayKhoiChieu;
    }

    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) {
        this.ngayKhoiChieu = ngayKhoiChieu;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public java.time.ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhimDTO)) {
            return false;
        }

        PhimDTO phimDTO = (PhimDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, phimDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PhimDTO{" +
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
            ", trangThai='" + getTrangThai() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
