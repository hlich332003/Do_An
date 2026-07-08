package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A DanhGia.
 */
@Entity
@Table(name = "danhgia")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DanhGia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(1)
    @Max(10)
    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @Column(name = "noi_dung", columnDefinition = "nvarchar(max)")
    private String noiDung;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phim_id", nullable = false)
    @JsonIgnoreProperties(value = { "suatChieus" }, allowSetters = true)
    private Phim phim;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id", nullable = false)
    @JsonIgnoreProperties(value = { "hoaDons" }, allowSetters = true)
    private NguoiDung nguoiDung;

    public Long getId() {
        return this.id;
    }

    public DanhGia id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSoSao() {
        return this.soSao;
    }

    public DanhGia soSao(Integer soSao) {
        this.setSoSao(soSao);
        return this;
    }

    public void setSoSao(Integer soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return this.noiDung;
    }

    public DanhGia noiDung(String noiDung) {
        this.setNoiDung(noiDung);
        return this;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public DanhGia createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Phim getPhim() {
        return this.phim;
    }

    public void setPhim(Phim phim) {
        this.phim = phim;
    }

    public DanhGia phim(Phim phim) {
        this.setPhim(phim);
        return this;
    }

    public NguoiDung getNguoiDung() {
        return this.nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public DanhGia nguoiDung(NguoiDung nguoiDung) {
        this.setNguoiDung(nguoiDung);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DanhGia)) {
            return false;
        }
        return getId() != null && getId().equals(((DanhGia) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "DanhGia{" +
            "id=" +
            getId() +
            ", soSao=" +
            getSoSao() +
            ", noiDung='" +
            getNoiDung() +
            "'" +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            "}"
        );
    }
}
