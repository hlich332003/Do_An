package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Phim;
import com.mycompany.myapp.service.dto.PhimDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Phim} and its DTO {@link PhimDTO}.
 */
@Mapper(componentModel = "spring")
public interface PhimMapper extends EntityMapper<PhimDTO, Phim> {}
