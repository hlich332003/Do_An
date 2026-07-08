package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.service.dto.NguoiDungDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link NguoiDung} and its DTO {@link NguoiDungDTO}.
 */
@Mapper(componentModel = "spring")
public interface NguoiDungMapper extends EntityMapper<NguoiDungDTO, NguoiDung> {}
