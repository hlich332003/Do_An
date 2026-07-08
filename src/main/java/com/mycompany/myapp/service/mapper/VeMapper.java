package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.dto.SuatChieuDTO;
import com.mycompany.myapp.service.dto.VeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ve} and its DTO {@link VeDTO}.
 */
@Mapper(componentModel = "spring")
public interface VeMapper extends EntityMapper<VeDTO, Ve> {
    @Mapping(target = "hoaDon", source = "hoaDon", qualifiedByName = "hoaDonId")
    @Mapping(target = "suatChieu", source = "suatChieu", qualifiedByName = "suatChieuId")
    VeDTO toDto(Ve s);

    @Named("hoaDonId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    HoaDonDTO toDtoHoaDonId(HoaDon hoaDon);

    @Named("suatChieuId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    SuatChieuDTO toDtoSuatChieuId(SuatChieu suatChieu);
}
