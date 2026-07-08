package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.ChiTietFB;
import com.mycompany.myapp.domain.DichVuFB;
import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.service.dto.ChiTietFBDTO;
import com.mycompany.myapp.service.dto.DichVuFBDTO;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChiTietFB} and its DTO {@link ChiTietFBDTO}.
 */
@Mapper(componentModel = "spring")
public interface ChiTietFBMapper extends EntityMapper<ChiTietFBDTO, ChiTietFB> {
    @Mapping(target = "dichVuFB", source = "dichVuFB", qualifiedByName = "dichVuFBTenCombo")
    @Mapping(target = "hoaDon", source = "hoaDon", qualifiedByName = "hoaDonId")
    ChiTietFBDTO toDto(ChiTietFB s);

    @Named("dichVuFBTenCombo")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenCombo", source = "tenCombo")
    DichVuFBDTO toDtoDichVuFBTenCombo(DichVuFB dichVuFB);

    @Named("hoaDonId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    HoaDonDTO toDtoHoaDonId(HoaDon hoaDon);
}
