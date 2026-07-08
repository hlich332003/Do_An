package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.DanhGia;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.Phim;
import com.mycompany.myapp.service.dto.DanhGiaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DanhGia} and its DTO {@link DanhGiaDTO}.
 */
@Mapper(componentModel = "spring")
public interface DanhGiaMapper extends EntityMapper<DanhGiaDTO, DanhGia> {
    @Mapping(target = "phim", source = "phimId", qualifiedByName = "phimFromId")
    @Mapping(target = "nguoiDung", source = "nguoiDungId", qualifiedByName = "nguoiDungFromId")
    DanhGia toEntity(DanhGiaDTO dto);

    @Mapping(target = "phimId", source = "phim.id")
    @Mapping(target = "phimTen", source = "phim.tenPhim")
    @Mapping(target = "nguoiDungId", source = "nguoiDung.id")
    @Mapping(target = "nguoiDungHoTen", source = "nguoiDung.hoTen")
    @Mapping(target = "nguoiDungEmail", source = "nguoiDung.email")
    DanhGiaDTO toDto(DanhGia entity);

    @Named("phimFromId")
    default Phim phimFromId(Long id) {
        if (id == null) {
            return null;
        }
        Phim phim = new Phim();
        phim.setId(id);
        return phim;
    }

    @Named("nguoiDungFromId")
    default NguoiDung nguoiDungFromId(Long id) {
        if (id == null) {
            return null;
        }
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(id);
        return nguoiDung;
    }
}
