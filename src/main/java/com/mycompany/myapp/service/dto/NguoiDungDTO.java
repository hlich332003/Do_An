package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.NguoiDung} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NguoiDungDTO implements Serializable {

    private Long id;

    private String hoTen;

    @NotNull
    private String email;

    @NotNull
    private String matKhau;

    private String soDienThoai;

    private String diaChi;

    private Integer diemTichLuy;

    private String vaiTro;

    private String trangThai;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Integer getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(Integer diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
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
        if (!(o instanceof NguoiDungDTO)) {
            return false;
        }

        NguoiDungDTO nguoiDungDTO = (NguoiDungDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, nguoiDungDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NguoiDungDTO{" +
            "id=" + getId() +
            ", hoTen='" + getHoTen() + "'" +
            ", email='" + getEmail() + "'" +
            ", matKhau='" + getMatKhau() + "'" +
            ", soDienThoai='" + getSoDienThoai() + "'" +
            ", diaChi='" + getDiaChi() + "'" +
            ", diemTichLuy=" + getDiemTichLuy() +
            ", vaiTro='" + getVaiTro() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
