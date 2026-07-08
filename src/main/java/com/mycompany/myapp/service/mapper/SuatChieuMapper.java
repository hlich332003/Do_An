package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Phim;
import com.mycompany.myapp.domain.PhongChieu;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.service.dto.PhimDTO;
import com.mycompany.myapp.service.dto.PhongChieuDTO;
import com.mycompany.myapp.service.dto.SuatChieuDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SuatChieu} and its DTO {@link SuatChieuDTO}.
 */
@Mapper(componentModel = "spring")
public interface SuatChieuMapper extends EntityMapper<SuatChieuDTO, SuatChieu> {
    @Mapping(target = "phim", source = "phim", qualifiedByName = "phimTenPhim")
    @Mapping(target = "phongChieu", source = "phongChieu", qualifiedByName = "phongChieuTenPhong")
    SuatChieuDTO toDto(SuatChieu s);

    @Named("phimTenPhim")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenPhim", source = "tenPhim")
    @Mapping(target = "poster", source = "poster")
    PhimDTO toDtoPhimTenPhim(Phim phim);

    @Named("phongChieuTenPhong")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenPhong", source = "tenPhong")
    PhongChieuDTO toDtoPhongChieuTenPhong(PhongChieu phongChieu);
}
