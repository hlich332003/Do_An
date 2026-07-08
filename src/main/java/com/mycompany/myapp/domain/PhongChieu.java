package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A PhongChieu.
 */
@Entity
@Table(name = "phongchieu")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PhongChieu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tenphong", nullable = false)
    private String tenPhong;

    @Column(name = "soluongghe")
    private Integer soLuongGhe;

    @Column(name = "trangthai")
    private String trangThai;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "phongChieu")
    @JsonIgnoreProperties(value = { "ves", "phim", "phongChieu" }, allowSetters = true)
    private Set<SuatChieu> suatChieus = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "phongChieu")
    @JsonIgnoreProperties(value = { "ve", "phongChieu" }, allowSetters = true)
    private Set<Ghe> ghes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PhongChieu id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenPhong() {
        return this.tenPhong;
    }

    public PhongChieu tenPhong(String tenPhong) {
        this.setTenPhong(tenPhong);
        return this;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public Integer getSoLuongGhe() {
        return this.soLuongGhe;
    }

    public PhongChieu soLuongGhe(Integer soLuongGhe) {
        this.setSoLuongGhe(soLuongGhe);
        return this;
    }

    public void setSoLuongGhe(Integer soLuongGhe) {
        this.soLuongGhe = soLuongGhe;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public PhongChieu trangThai(String trangThai) {
        this.setTrangThai(trangThai);
        return this;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Set<SuatChieu> getSuatChieus() {
        return this.suatChieus;
    }

    public void setSuatChieus(Set<SuatChieu> suatChieus) {
        if (this.suatChieus != null) {
            this.suatChieus.forEach(i -> i.setPhongChieu(null));
        }
        if (suatChieus != null) {
            suatChieus.forEach(i -> i.setPhongChieu(this));
        }
        this.suatChieus = suatChieus;
    }

    public PhongChieu suatChieus(Set<SuatChieu> suatChieus) {
        this.setSuatChieus(suatChieus);
        return this;
    }

    public PhongChieu addSuatChieu(SuatChieu suatChieu) {
        this.suatChieus.add(suatChieu);
        suatChieu.setPhongChieu(this);
        return this;
    }

    public PhongChieu removeSuatChieu(SuatChieu suatChieu) {
        this.suatChieus.remove(suatChieu);
        suatChieu.setPhongChieu(null);
        return this;
    }

    public Set<Ghe> getGhes() {
        return this.ghes;
    }

    public void setGhes(Set<Ghe> ghes) {
        if (this.ghes != null) {
            this.ghes.forEach(i -> i.setPhongChieu(null));
        }
        if (ghes != null) {
            ghes.forEach(i -> i.setPhongChieu(this));
        }
        this.ghes = ghes;
    }

    public PhongChieu ghes(Set<Ghe> ghes) {
        this.setGhes(ghes);
        return this;
    }

    public PhongChieu addGhe(Ghe ghe) {
        this.ghes.add(ghe);
        ghe.setPhongChieu(this);
        return this;
    }

    public PhongChieu removeGhe(Ghe ghe) {
        this.ghes.remove(ghe);
        ghe.setPhongChieu(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhongChieu)) {
            return false;
        }
        return getId() != null && getId().equals(((PhongChieu) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PhongChieu{" +
            "id=" + getId() +
            ", tenPhong='" + getTenPhong() + "'" +
            ", soLuongGhe=" + getSoLuongGhe() +
            ", trangThai='" + getTrangThai() + "'" +
            "}";
    }
}
