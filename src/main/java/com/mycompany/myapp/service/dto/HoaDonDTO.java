package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.HoaDon} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HoaDonDTO implements Serializable {

    private Long id;

    @NotNull
    private BigDecimal tongTien;

    private String maGiamGia;

    private BigDecimal soTienGiam;

    private String phuongThucThanhToan;

    private String maGiaoDich;

    private Integer soVe;

    private String trangThai;

    private ZonedDateTime ngayTao;

    private ZonedDateTime ngayThanhToan;

    private NguoiDungDTO nguoiDung;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public BigDecimal getSoTienGiam() {
        return soTienGiam;
    }

    public void setSoTienGiam(BigDecimal soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public Integer getSoVe() {
        return soVe;
    }

    public void setSoVe(Integer soVe) {
        this.soVe = soVe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public ZonedDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(ZonedDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public ZonedDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(ZonedDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public NguoiDungDTO getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDungDTO nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HoaDonDTO)) {
            return false;
        }

        HoaDonDTO hoaDonDTO = (HoaDonDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, hoaDonDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HoaDonDTO{" +
            "id=" + getId() +
            ", tongTien=" + getTongTien() +
            ", maGiamGia='" + getMaGiamGia() + "'" +
            ", soTienGiam=" + getSoTienGiam() +
            ", phuongThucThanhToan='" + getPhuongThucThanhToan() + "'" +
            ", maGiaoDich='" + getMaGiaoDich() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            ", ngayTao='" + getNgayTao() + "'" +
            ", ngayThanhToan='" + getNgayThanhToan() + "'" +
            ", nguoiDung=" + getNguoiDung() +
            "}";
    }
}
