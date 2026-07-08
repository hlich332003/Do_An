package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.DanhGia;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DanhGia entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {
    List<DanhGia> findByPhimIdOrderByCreatedAtDesc(Long phimId);

    Optional<DanhGia> findFirstByPhimIdAndNguoiDungId(Long phimId, Long nguoiDungId);

    @Query("select coalesce(avg(d.soSao), 0) from DanhGia d where d.phim.id = :phimId")
    Double getAverageRatingByPhimId(@Param("phimId") Long phimId);

    @Query("select coalesce(avg(d.soSao), 0) from DanhGia d")
    Double getOverallAverageRating();

    @Query("select count(d) from DanhGia d")
    long countAllReviews();

    @Query("select d.phim.id, coalesce(avg(d.soSao), 0), count(d) from DanhGia d group by d.phim.id")
    List<Object[]> findRatingSummaryByPhimId();

    List<DanhGia> findByCreatedAtBetweenOrderByCreatedAtAsc(ZonedDateTime from, ZonedDateTime to);

    @Query("select d from DanhGia d left join fetch d.phim where d.createdAt between :from and :to order by d.createdAt asc")
    List<DanhGia> findByCreatedAtBetweenOrderByCreatedAtAscWithPhim(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
