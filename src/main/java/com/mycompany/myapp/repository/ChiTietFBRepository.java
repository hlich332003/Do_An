package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.ChiTietFB;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ChiTietFB entity.
 */
@Repository
public interface ChiTietFBRepository extends JpaRepository<ChiTietFB, Long> {
    java.util.Set<ChiTietFB> findByHoaDonId(Long id);

    @Query("select ct from ChiTietFB ct left join fetch ct.dichVuFB where ct.hoaDon.id in :ids")
    List<ChiTietFB> findByHoaDonIdIn(@Param("ids") java.util.Collection<Long> ids);

    default Optional<ChiTietFB> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ChiTietFB> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ChiTietFB> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select chiTietFB from ChiTietFB chiTietFB left join fetch chiTietFB.dichVuFB",
        countQuery = "select count(chiTietFB) from ChiTietFB chiTietFB"
    )
    Page<ChiTietFB> findAllWithToOneRelationships(Pageable pageable);

    @Query("select chiTietFB from ChiTietFB chiTietFB left join fetch chiTietFB.dichVuFB")
    List<ChiTietFB> findAllWithToOneRelationships();

    @Query("select chiTietFB from ChiTietFB chiTietFB left join fetch chiTietFB.dichVuFB where chiTietFB.id =:id")
    Optional<ChiTietFB> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        "select chiTietFB from ChiTietFB chiTietFB " +
        "left join fetch chiTietFB.dichVuFB " +
        "left join fetch chiTietFB.hoaDon " +
        "where chiTietFB.hoaDon is not null " +
        "and chiTietFB.hoaDon.createdAt between :from and :to " +
        "and chiTietFB.hoaDon.trangThai is not null " +
        "and upper(chiTietFB.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    List<ChiTietFB> findAllPaidWithRelationshipsByInvoiceCreatedAtBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select coalesce(sum(c.soLuong * c.dichVuFB.gia), 0) from ChiTietFB c " +
        "where c.hoaDon.createdAt between :from and :to " +
        "and c.hoaDon.trangThai is not null " +
        "and upper(c.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN')"
    )
    java.math.BigDecimal sumFbRevenueBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    @Query(
        "select c.dichVuFB.tenCombo, sum(c.soLuong), coalesce(sum(c.soLuong * c.dichVuFB.gia), 0) from ChiTietFB c " +
        "where c.hoaDon.createdAt between :from and :to " +
        "and c.hoaDon.trangThai is not null " +
        "and upper(c.hoaDon.trangThai) in ('2', 'PAID', 'DONE', 'SUCCESS', 'DA_THANH_TOAN') " +
        "group by c.dichVuFB.tenCombo " +
        "order by sum(c.soLuong) desc"
    )
    List<Object[]> getTopCombosBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
