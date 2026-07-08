package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.PhongChieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PhongChieu entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PhongChieuRepository extends JpaRepository<PhongChieu, Long> {}
