package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A NguoiDung.
 */
@Entity
@Table(name = "nguoidung")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NguoiDung implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "hoten")
    private String hoTen;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "matkhau", nullable = false)
    private String matKhau;

    @Column(name = "sodienthoai")
    private String soDienThoai;

    @Column(name = "diachi")
    private String diaChi;

    @Column(name = "diemtichluy")
    private Integer diemTichLuy;

    @Column(name = "vaitro")
    private String vaiTro;

    @Column(name = "trangthai")
    private String trangThai;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "nguoiDung")
    @JsonIgnoreProperties(value = { "ves", "chiTietFBS", "nguoiDung", "chiTietHoaDons" }, allowSetters = true)
    private Set<HoaDon> hoaDons = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public NguoiDung id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return this.hoTen;
    }

    public NguoiDung hoTen(String hoTen) {
        this.setHoTen(hoTen);
        return this;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return this.email;
    }

    public NguoiDung email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return this.matKhau;
    }

    public NguoiDung matKhau(String matKhau) {
        this.setMatKhau(matKhau);
        return this;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getSoDienThoai() {
        return this.soDienThoai;
    }

    public NguoiDung soDienThoai(String soDienThoai) {
        this.setSoDienThoai(soDienThoai);
        return this;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return this.diaChi;
    }

    public NguoiDung diaChi(String diaChi) {
        this.setDiaChi(diaChi);
        return this;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Integer getDiemTichLuy() {
        return this.diemTichLuy;
    }

    public NguoiDung diemTichLuy(Integer diemTichLuy) {
        this.setDiemTichLuy(diemTichLuy);
        return this;
    }

    public void setDiemTichLuy(Integer diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public String getVaiTro() {
        return this.vaiTro;
    }

    public NguoiDung vaiTro(String vaiTro) {
        this.setVaiTro(vaiTro);
        return this;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public NguoiDung trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public NguoiDung createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public NguoiDung updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getFailedLoginAttempts() {
        return this.failedLoginAttempts;
    }

    public NguoiDung failedLoginAttempts(Integer failedLoginAttempts) {
        this.setFailedLoginAttempts(failedLoginAttempts);
        return this;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return this.lockedUntil;
    }

    public NguoiDung lockedUntil(Instant lockedUntil) {
        this.setLockedUntil(lockedUntil);
        return this;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Set<HoaDon> getHoaDons() {
        return this.hoaDons;
    }

    public void setHoaDons(Set<HoaDon> hoaDons) {
        if (this.hoaDons != null) {
            this.hoaDons.forEach(i -> i.setNguoiDung(null));
        }
        if (hoaDons != null) {
            hoaDons.forEach(i -> i.setNguoiDung(this));
        }
        this.hoaDons = hoaDons;
    }

    public NguoiDung hoaDons(Set<HoaDon> hoaDons) {
        this.setHoaDons(hoaDons);
        return this;
    }

    public NguoiDung addHoaDon(HoaDon hoaDon) {
        this.hoaDons.add(hoaDon);
        hoaDon.setNguoiDung(this);
        return this;
    }

    public NguoiDung removeHoaDon(HoaDon hoaDon) {
        this.hoaDons.remove(hoaDon);
        hoaDon.setNguoiDung(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NguoiDung)) {
            return false;
        }
        return getId() != null && getId().equals(((NguoiDung) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NguoiDung{" +
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
