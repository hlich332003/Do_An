package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.HoaDon;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the HoaDon entity.
 */
@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    List<HoaDon> findByNguoiDungEmailOrderByIdDesc(String email);

    default Optional<HoaDon> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<HoaDon> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<HoaDon> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select hoaDon from HoaDon hoaDon left join fetch hoaDon.nguoiDung",
        countQuery = "select count(hoaDon) from HoaDon hoaDon"
    )
    Page<HoaDon> findAllWithToOneRelationships(Pageable pageable);

    @Query("select hoaDon from HoaDon hoaDon left join fetch hoaDon.nguoiDung")
    List<HoaDon> findAllWithToOneRelationships();

    @Query("select hoaDon from HoaDon hoaDon left join fetch hoaDon.nguoiDung where hoaDon.id =:id")
    Optional<HoaDon> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("select hoaDon from HoaDon hoaDon left join fetch hoaDon.nguoiDung " + "where hoaDon.createdAt between :from and :to")
    List<HoaDon> findAllByCreatedAtBetweenWithUser(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select distinct h from HoaDon h left join fetch h.nguoiDung u left join h.ves v " +
        "where h.createdAt between :from and :to " +
        "and (lower(cast(h.id as string)) like :query " +
        "or lower(h.maGiaoDich) like :query " +
        "or lower(h.maGiamGia) like :query " +
        "or lower(h.phuongThucThanhToan) like :query " +
        "or lower(h.trangThai) like :query " +
        "or lower(u.email) like :query " +
        "or lower(u.hoTen) like :query " +
        "or lower(u.soDienThoai) like :query " +
        "or lower(v.maVe) like :query)"
    )
    List<HoaDon> searchHoaDons(@Param("query") String query, @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    long countByCreatedAtLessThanEqual(ZonedDateTime createdAt);

    @Query(
        "select coalesce(sum(h.tongTien), 0) from HoaDon h " +
        "where h.createdAt between :from and :to " +
        "and h.trangThai is not null " +
        "and upper(h.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    java.math.BigDecimal sumRevenueBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select count(h) from HoaDon h " +
        "where h.createdAt between :from and :to " +
        "and h.trangThai is not null " +
        "and upper(h.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    long countPaidInvoicesBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select count(distinct h) from HoaDon h join h.chiTietFBS c " +
        "where h.createdAt between :from and :to " +
        "and h.trangThai is not null " +
        "and upper(h.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    long countPaidInvoicesWithComboBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query("select h.trangThai, count(h) from HoaDon h " + "where h.createdAt between :from and :to " + "group by h.trangThai")
    List<Object[]> getStatusBreakdownBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
