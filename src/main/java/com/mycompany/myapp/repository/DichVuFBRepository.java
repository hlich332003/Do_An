package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.DichVuFB;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DichVuFB entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DichVuFBRepository extends JpaRepository<DichVuFB, Long> {
    List<DichVuFB> findByTrangThai(String trangThai);
}
