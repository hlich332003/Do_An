package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Phim;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Phim entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PhimRepository extends JpaRepository<Phim, Long> {
    @Query(
        "SELECT p FROM Phim p " +
        "WHERE (p.trangThai IS NULL OR UPPER(p.trangThai) <> 'INACTIVE') " +
        "AND p.ngayKhoiChieu IS NOT NULL " +
        "AND p.ngayKhoiChieu <= CURRENT_DATE"
    )
    Page<Phim> findShowing(Pageable pageable);

    @Query(
        "SELECT p FROM Phim p " +
        "WHERE (p.trangThai IS NULL OR UPPER(p.trangThai) <> 'INACTIVE') " +
        "AND p.ngayKhoiChieu IS NOT NULL " +
        "AND p.ngayKhoiChieu > CURRENT_DATE"
    )
    Page<Phim> findComingSoon(Pageable pageable);

    Page<Phim> findByTenPhimContainingIgnoreCase(String tenPhim, Pageable pageable);

    @Query(
        "SELECT p FROM Phim p " +
        "WHERE (p.trangThai IS NULL OR UPPER(p.trangThai) <> 'INACTIVE') " +
        "AND p.ngayKhoiChieu IS NOT NULL " +
        "AND p.ngayKhoiChieu <= CURRENT_DATE"
    )
    List<Phim> findShowing();

    @Query(
        "SELECT p FROM Phim p " +
        "WHERE (p.trangThai IS NULL OR UPPER(p.trangThai) <> 'INACTIVE') " +
        "AND p.ngayKhoiChieu IS NOT NULL " +
        "AND p.ngayKhoiChieu > CURRENT_DATE"
    )
    List<Phim> findComingSoon();
}
