package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonLichSuDTO implements Serializable {

    private Long id;
    private String tenPhim;
    private String maVe;
    private ZonedDateTime gioChieu;
    private String tenRap;
    private String tenPhongChieu;
    private String danhSachGhe;
    private String danhSachCombo;
    private BigDecimal tongTien;
    private String phuongThucThanhToan;
    private String trangThai;
    private String maGiaoDich;
    private List<Long> veIds = new ArrayList<>();

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

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public ZonedDateTime getGioChieu() {
        return gioChieu;
    }

    public void setGioChieu(ZonedDateTime gioChieu) {
        this.gioChieu = gioChieu;
    }

    public String getTenRap() {
        return tenRap;
    }

    public void setTenRap(String tenRap) {
        this.tenRap = tenRap;
    }

    public String getTenPhongChieu() {
        return tenPhongChieu;
    }

    public void setTenPhongChieu(String tenPhongChieu) {
        this.tenPhongChieu = tenPhongChieu;
    }

    public String getDanhSachGhe() {
        return danhSachGhe;
    }

    public void setDanhSachGhe(String danhSachGhe) {
        this.danhSachGhe = danhSachGhe;
    }

    public String getDanhSachCombo() {
        return danhSachCombo;
    }

    public void setDanhSachCombo(String danhSachCombo) {
        this.danhSachCombo = danhSachCombo;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public List<Long> getVeIds() {
        return veIds;
    }

    public void setVeIds(List<Long> veIds) {
        this.veIds = veIds;
    }
}
