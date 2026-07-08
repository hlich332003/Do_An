package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.DanhGia} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DanhGiaDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer soSao;

    private String noiDung;

    private ZonedDateTime createdAt;

    @NotNull
    private Long phimId;

    private String phimTen;

    private Long nguoiDungId;

    private String nguoiDungHoTen;

    private String nguoiDungEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSoSao() {
        return soSao;
    }

    public void setSoSao(Integer soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getPhimId() {
        return phimId;
    }

    public void setPhimId(Long phimId) {
        this.phimId = phimId;
    }

    public String getPhimTen() {
        return phimTen;
    }

    public void setPhimTen(String phimTen) {
        this.phimTen = phimTen;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getNguoiDungHoTen() {
        return nguoiDungHoTen;
    }

    public void setNguoiDungHoTen(String nguoiDungHoTen) {
        this.nguoiDungHoTen = nguoiDungHoTen;
    }

    public String getNguoiDungEmail() {
        return nguoiDungEmail;
    }

    public void setNguoiDungEmail(String nguoiDungEmail) {
        this.nguoiDungEmail = nguoiDungEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DanhGiaDTO)) {
            return false;
        }

        DanhGiaDTO danhGiaDTO = (DanhGiaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, danhGiaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "DanhGiaDTO{" +
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
            ", phimId=" +
            getPhimId() +
            ", nguoiDungId=" +
            getNguoiDungId() +
            "}"
        );
    }
}
