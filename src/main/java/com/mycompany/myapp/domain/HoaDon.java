package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Formula;

/**
 * A HoaDon.
 */
@Entity
@Table(name = "hoadon")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HoaDon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tongtien", precision = 21, scale = 2, nullable = false)
    private BigDecimal tongTien;

    @Column(name = "magiamgia")
    private String maGiamGia;

    @Column(name = "sotiengiam", precision = 21, scale = 2)
    private BigDecimal soTienGiam;

    @Column(name = "phuongthucthanhtoan")
    private String phuongThucThanhToan;

    @Column(name = "trangthai")
    private String trangThai;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "ngaythanhtoan")
    private ZonedDateTime ngayThanhToan;

    @Column(name = "magiaodich")
    private String maGiaoDich;

    @Formula("(select count(*) from ve v where v.hoadon_id = id)")
    private Integer soVe;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "hoaDon")
    @JsonIgnoreProperties(value = { "hoaDon", "ghe", "suatChieu", "chiTietHoaDon" }, allowSetters = true)
    private Set<Ve> ves = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "hoaDon")
    @JsonIgnoreProperties(value = { "dichVuFB", "hoaDon" }, allowSetters = true)
    private Set<ChiTietFB> chiTietFBS = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoidung_id")
    @JsonIgnoreProperties(value = { "hoaDons" }, allowSetters = true)
    private NguoiDung nguoiDung;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public HoaDon id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTongTien() {
        return this.tongTien;
    }

    public HoaDon tongTien(BigDecimal tongTien) {
        this.setTongTien(tongTien);
        return this;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getMaGiamGia() {
        return this.maGiamGia;
    }

    public HoaDon maGiamGia(String maGiamGia) {
        this.setMaGiamGia(maGiamGia);
        return this;
    }

    public void setMaGiamGia(String maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public BigDecimal getSoTienGiam() {
        return this.soTienGiam;
    }

    public HoaDon soTienGiam(BigDecimal soTienGiam) {
        this.setSoTienGiam(soTienGiam);
        return this;
    }

    public void setSoTienGiam(BigDecimal soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public String getPhuongThucThanhToan() {
        return this.phuongThucThanhToan;
    }

    public HoaDon phuongThucThanhToan(String phuongThucThanhToan) {
        this.setPhuongThucThanhToan(phuongThucThanhToan);
        return this;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public HoaDon trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public HoaDon createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getNgayThanhToan() {
        return this.ngayThanhToan;
    }

    public HoaDon ngayThanhToan(ZonedDateTime ngayThanhToan) {
        this.setNgayThanhToan(ngayThanhToan);
        return this;
    }

    public void setNgayThanhToan(ZonedDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getMaGiaoDich() {
        return this.maGiaoDich;
    }

    public HoaDon maGiaoDich(String maGiaoDich) {
        this.setMaGiaoDich(maGiaoDich);
        return this;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public Integer getSoVe() {
        return this.soVe;
    }

    public void setSoVe(Integer soVe) {
        this.soVe = soVe;
    }

    public Set<Ve> getVes() {
        return this.ves;
    }

    public void setVes(Set<Ve> ves) {
        if (this.ves != null) {
            this.ves.forEach(i -> i.setHoaDon(null));
        }
        if (ves != null) {
            ves.forEach(i -> i.setHoaDon(this));
        }
        this.ves = ves;
    }

    public HoaDon ves(Set<Ve> ves) {
        this.setVes(ves);
        return this;
    }

    public HoaDon addVe(Ve ve) {
        this.ves.add(ve);
        ve.setHoaDon(this);
        return this;
    }

    public HoaDon removeVe(Ve ve) {
        this.ves.remove(ve);
        ve.setHoaDon(null);
        return this;
    }

    public Set<ChiTietFB> getChiTietFBS() {
        return this.chiTietFBS;
    }

    public void setChiTietFBS(Set<ChiTietFB> chiTietFBS) {
        if (this.chiTietFBS != null) {
            this.chiTietFBS.forEach(i -> i.setHoaDon(null));
        }
        if (chiTietFBS != null) {
            chiTietFBS.forEach(i -> i.setHoaDon(this));
        }
        this.chiTietFBS = chiTietFBS;
    }

    public HoaDon chiTietFBS(Set<ChiTietFB> chiTietFBS) {
        this.setChiTietFBS(chiTietFBS);
        return this;
    }

    public HoaDon addChiTietFB(ChiTietFB chiTietFB) {
        this.chiTietFBS.add(chiTietFB);
        chiTietFB.setHoaDon(this);
        return this;
    }

    public HoaDon removeChiTietFB(ChiTietFB chiTietFB) {
        this.chiTietFBS.remove(chiTietFB);
        chiTietFB.setHoaDon(null);
        return this;
    }

    public NguoiDung getNguoiDung() {
        return this.nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public HoaDon nguoiDung(NguoiDung nguoiDung) {
        this.setNguoiDung(nguoiDung);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HoaDon)) {
            return false;
        }
        return getId() != null && getId().equals(((HoaDon) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HoaDon{" +
            "id=" + getId() +
            ", tongTien=" + getTongTien() +
            ", maGiamGia='" + getMaGiamGia() + "'" +
            ", soTienGiam=" + getSoTienGiam() +
            ", phuongThucThanhToan='" + getPhuongThucThanhToan() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", ngayThanhToan='" + getNgayThanhToan() + "'" +
            "}";
    }
}
