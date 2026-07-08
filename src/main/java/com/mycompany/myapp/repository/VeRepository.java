package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Ve;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Ve entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VeRepository extends JpaRepository<Ve, Long> {
    Set<Ve> findByHoaDonId(Long id);

    @Query(
        "select v from Ve v left join fetch v.ghe left join fetch v.suatChieu s left join fetch s.phim left join fetch s.phongChieu where v.hoaDon.id in :ids"
    )
    List<Ve> findByHoaDonIdIn(@Param("ids") java.util.Collection<Long> ids);

    java.util.List<Ve> findBySuatChieuIdAndGheId(Long suatChieuId, Long gheId);
    void deleteBySuatChieuId(Long suatChieuId);

    @Query(
        "select (count(v) > 0) from Ve v " +
        "where v.suatChieu.id = :suatChieuId " +
        "and (v.hoaDon is not null or (v.trangThai is not null and upper(v.trangThai) <> 'CHUA_DAT'))"
    )
    boolean existsLockedOrSoldBySuatChieuId(@Param("suatChieuId") Long suatChieuId);

    @Query(
        "select v.suatChieu.phim.id, count(v) " +
        "from Ve v " +
        "where v.hoaDon is not null and v.hoaDon.trangThai is not null and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS') " +
        "group by v.suatChieu.phim.id"
    )
    List<Object[]> countPaidTicketsByPhimId();

    @Query(
        "select v from Ve v " +
        "left join fetch v.ghe " +
        "left join fetch v.suatChieu s " +
        "left join fetch s.phim " +
        "left join fetch s.phongChieu " +
        "left join fetch v.hoaDon " +
        "where v.hoaDon is not null " +
        "and v.hoaDon.createdAt between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    List<Ve> findAllPaidWithRelationshipsByInvoiceCreatedAtBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select count(v) from Ve v " +
        "where v.hoaDon.createdAt between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    long countPaidTicketsBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select count(v) from Ve v " +
        "where v.suatChieu.id in :suatChieuIds " +
        "and v.hoaDon is not null " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    long countPaidTicketsBySuatChieuIds(@Param("suatChieuIds") List<Long> suatChieuIds);

    @Query(
        "select v.suatChieu.phim.tenPhim, count(v), coalesce(sum(v.giaVe), 0) from Ve v " +
        "where v.hoaDon.createdAt between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN') " +
        "group by v.suatChieu.phim.tenPhim " +
        "order by sum(v.giaVe) desc"
    )
    List<Object[]> getTopMoviesBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select v.suatChieu.phongChieu.tenPhong, count(v), coalesce(sum(v.giaVe), 0) from Ve v " +
        "where v.suatChieu.thoiGianBatDau between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN') " +
        "group by v.suatChieu.phongChieu.tenPhong"
    )
    List<Object[]> getRoomRevenueBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select hour(v.suatChieu.thoiGianBatDau), count(v), coalesce(sum(v.giaVe), 0) from Ve v " +
        "where v.hoaDon.createdAt between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN') " +
        "group by hour(v.suatChieu.thoiGianBatDau)"
    )
    List<Object[]> getPeakHoursBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select v.ghe.loaiGhe, count(v) from Ve v " +
        "where v.hoaDon.createdAt between :from and :to " +
        "and v.hoaDon.trangThai is not null " +
        "and upper(v.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN') " +
        "group by v.ghe.loaiGhe"
    )
    List<Object[]> getTicketTypeBreakdownBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
