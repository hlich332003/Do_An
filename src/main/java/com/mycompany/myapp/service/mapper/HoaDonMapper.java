package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.dto.NguoiDungDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HoaDon} and its DTO {@link HoaDonDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HoaDonMapper extends EntityMapper<HoaDonDTO, HoaDon> {
    @Mapping(target = "nguoiDung", source = "nguoiDung", qualifiedByName = "nguoiDungEmail")
    @Mapping(target = "ngayTao", source = "createdAt")
    HoaDonDTO toDto(HoaDon s);

    @Mapping(target = "createdAt", source = "ngayTao")
    HoaDon toEntity(HoaDonDTO dto);

    @Named("nguoiDungEmail")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "hoTen", source = "hoTen")
    NguoiDungDTO toDtoNguoiDungEmail(NguoiDung nguoiDung);
}
