package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.SuatChieu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SuatChieu entity.
 */
@Repository
public interface SuatChieuRepository extends JpaRepository<SuatChieu, Long> {
    default Optional<SuatChieu> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SuatChieu> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SuatChieu> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select suatChieu from SuatChieu suatChieu left join fetch suatChieu.phim left join fetch suatChieu.phongChieu",
        countQuery = "select count(suatChieu) from SuatChieu suatChieu"
    )
    Page<SuatChieu> findAllWithToOneRelationships(Pageable pageable);

    @Query("select suatChieu from SuatChieu suatChieu left join fetch suatChieu.phim left join fetch suatChieu.phongChieu")
    List<SuatChieu> findAllWithToOneRelationships();

    @Query(
        "select suatChieu from SuatChieu suatChieu left join fetch suatChieu.phim left join fetch suatChieu.phongChieu where suatChieu.id =:id"
    )
    Optional<SuatChieu> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        "SELECT COUNT(s) FROM SuatChieu s WHERE s.phongChieu.id = :phongId AND s.thoiGianBatDau < :ketThuc AND s.thoiGianKetThuc > :batDau AND (:excludeId IS NULL OR s.id != :excludeId)"
    )
    long countOverlapping(
        @Param("phongId") Long phongId,
        @Param("batDau") java.time.ZonedDateTime batDau,
        @Param("ketThuc") java.time.ZonedDateTime ketThuc,
        @Param("excludeId") Long excludeId
    );

    boolean existsByPhimId(Long phimId);

    List<SuatChieu> findByPhongChieuIdOrderByThoiGianBatDauAsc(Long phongChieuId);

    @Query(
        "select s from SuatChieu s left join fetch s.phim left join fetch s.phongChieu where " +
        "(:phimIds IS NULL OR s.phim.id IN :phimIds) " +
        "AND (:phongChieuId IS NULL OR s.phongChieu.id = :phongChieuId) " +
        "AND (cast(:startOfDay as java.time.ZonedDateTime) IS NULL OR s.thoiGianBatDau >= :startOfDay) " +
        "AND (cast(:endOfDay as java.time.ZonedDateTime) IS NULL OR s.thoiGianBatDau <= :endOfDay) " +
        "ORDER BY s.thoiGianBatDau ASC"
    )
    List<SuatChieu> searchByFilters(
        @Param("phimIds") java.util.List<Long> phimIds,
        @Param("phongChieuId") Long phongChieuId,
        @Param("startOfDay") java.time.ZonedDateTime startOfDay,
        @Param("endOfDay") java.time.ZonedDateTime endOfDay
    );

    @Query(
        "select s.phongChieu.tenPhong, coalesce(sum(s.phongChieu.soLuongGhe), 0) from SuatChieu s " +
        "where s.thoiGianBatDau between :from and :to " +
        "group by s.phongChieu.tenPhong"
    )
    List<Object[]> getRoomCapacityBetween(@Param("from") java.time.ZonedDateTime from, @Param("to") java.time.ZonedDateTime to);
}
