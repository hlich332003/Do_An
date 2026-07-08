package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Ghe.
 */
@Entity
@Table(name = "ghe")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Ghe implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "maghe", nullable = false)
    private String maGhe;

    @Column(name = "hang")
    private String hang;

    @Column(name = "cot")
    private Integer cot;

    @Column(name = "loaighe")
    private String loaiGhe;

    @Column(name = "trangthai")
    private String trangThai;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ghe")
    @JsonIgnoreProperties(value = { "hoaDon", "ghe", "suatChieu", "chiTietHoaDon" }, allowSetters = true)
    private Set<Ve> ves = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id")
    @JsonIgnoreProperties(value = { "suatChieus", "ghes" }, allowSetters = true)
    private PhongChieu phongChieu;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Ghe id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaGhe() {
        return this.maGhe;
    }

    public Ghe maGhe(String maGhe) {
        this.setMaGhe(maGhe);
        return this;
    }

    public void setMaGhe(String maGhe) {
        this.maGhe = maGhe;
    }

    public String getHang() {
        return this.hang;
    }

    public Ghe hang(String hang) {
        this.setHang(hang);
        return this;
    }

    public void setHang(String hang) {
        this.hang = hang;
    }

    public Integer getCot() {
        return this.cot;
    }

    public Ghe cot(Integer cot) {
        this.setCot(cot);
        return this;
    }

    public void setCot(Integer cot) {
        this.cot = cot;
    }

    public String getLoaiGhe() {
        return this.loaiGhe;
    }

    public Ghe loaiGhe(String loaiGhe) {
        this.setLoaiGhe(loaiGhe);
        return this;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public Ghe trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Set<Ve> getVes() {
        return this.ves;
    }

    public void setVes(Set<Ve> ves) {
        if (this.ves != null) {
            this.ves.forEach(i -> i.setGhe(null));
        }
        if (ves != null) {
            ves.forEach(i -> i.setGhe(this));
        }
        this.ves = ves;
    }

    public Ghe ves(Set<Ve> ves) {
        this.setVes(ves);
        return this;
    }

    public Ghe addVe(Ve ve) {
        this.ves.add(ve);
        ve.setGhe(this);
        return this;
    }

    public Ghe removeVe(Ve ve) {
        this.ves.remove(ve);
        ve.setGhe(null);
        return this;
    }

    public PhongChieu getPhongChieu() {
        return this.phongChieu;
    }

    public void setPhongChieu(PhongChieu phongChieu) {
        this.phongChieu = phongChieu;
    }

    public Ghe phongChieu(PhongChieu phongChieu) {
        this.setPhongChieu(phongChieu);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ghe)) {
            return false;
        }
        return getId() != null && getId().equals(((Ghe) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Ghe{" +
            "id=" + getId() +
            ", maGhe='" + getMaGhe() + "'" +
            ", hang='" + getHang() + "'" +
            ", cot=" + getCot() +
            ", loaiGhe='" + getLoaiGhe() + "'" +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
