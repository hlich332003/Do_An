package com.mycompany.myapp.web.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingCreateRequestDTO {

    private Long suatChieuId;
    private List<Long> gheIds = new ArrayList<>();
    private List<ComboItemDTO> combos = new ArrayList<>();
    private String maGiamGia;
    private String phuongThucThanhToan;
    private Long nguoiDungId;

    public Long getSuatChieuId() {
        return suatChieuId;
    }

    public void setSuatChieuId(Long suatChieuId) {
        this.suatChieuId = suatChieuId;
    }

    public List<Long> getGheIds() {
        return gheIds;
    }

    public void setGheIds(List<Long> gheIds) {
        this.gheIds = gheIds;
    }

    public List<ComboItemDTO> getCombos() {
        return combos;
    }

    public void setCombos(List<ComboItemDTO> combos) {
        this.combos = combos;
    }

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public static class ComboItemDTO {

        private Long dichVuFBId;
        private Integer soLuong;

        public Long getDichVuFBId() {
            return dichVuFBId;
        }

        public void setDichVuFBId(Long dichVuFBId) {
            this.dichVuFBId = dichVuFBId;
        }

        public Integer getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(Integer soLuong) {
            this.soLuong = soLuong;
        }
    }
}
