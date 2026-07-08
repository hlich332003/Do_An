package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.DichVuFB;
import com.mycompany.myapp.service.dto.DichVuFBDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DichVuFB} and its DTO {@link DichVuFBDTO}.
 */
@Mapper(componentModel = "spring")
public interface DichVuFBMapper extends EntityMapper<DichVuFBDTO, DichVuFB> {}
