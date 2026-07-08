package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.PhongChieu;
import com.mycompany.myapp.service.dto.PhongChieuDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PhongChieu} and its DTO {@link PhongChieuDTO}.
 */
@Mapper(componentModel = "spring")
public interface PhongChieuMapper extends EntityMapper<PhongChieuDTO, PhongChieu> {}
