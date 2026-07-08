package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Ghe;
import com.mycompany.myapp.domain.PhongChieu;
import com.mycompany.myapp.service.dto.GheDTO;
import com.mycompany.myapp.service.dto.PhongChieuDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ghe} and its DTO {@link GheDTO}.
 */
@Mapper(componentModel = "spring")
public interface GheMapper extends EntityMapper<GheDTO, Ghe> {
    @Mapping(target = "phongChieu", source = "phongChieu", qualifiedByName = "phongChieuTenPhong")
    GheDTO toDto(Ghe s);

    @Named("phongChieuTenPhong")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenPhong", source = "tenPhong")
    PhongChieuDTO toDtoPhongChieuTenPhong(PhongChieu phongChieu);
}
