package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Ghe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Ghe entity.
 */
@Repository
public interface GheRepository extends JpaRepository<Ghe, Long> {
    default Optional<Ghe> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Ghe> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Ghe> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select ghe from Ghe ghe left join fetch ghe.phongChieu", countQuery = "select count(ghe) from Ghe ghe")
    Page<Ghe> findAllWithToOneRelationships(Pageable pageable);

    @Query("select ghe from Ghe ghe left join fetch ghe.phongChieu")
    List<Ghe> findAllWithToOneRelationships();

    @Query("select ghe from Ghe ghe left join fetch ghe.phongChieu where ghe.id =:id")
    Optional<Ghe> findOneWithToOneRelationships(@Param("id") Long id);

    List<Ghe> findByPhongChieuId(Long phongChieuId);

    @Query(
        value = "select ghe from Ghe ghe left join fetch ghe.phongChieu " +
        "where (:query is null or :query = '' or " +
        "lower(coalesce(ghe.maGhe, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.hang, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.trangThai, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.phongChieu.tenPhong, '')) like lower(concat('%', :query, '%')))",
        countQuery = "select count(ghe) from Ghe ghe left join ghe.phongChieu " +
        "where (:query is null or :query = '' or " +
        "lower(coalesce(ghe.maGhe, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.hang, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.trangThai, '')) like lower(concat('%', :query, '%')) or " +
        "lower(coalesce(ghe.phongChieu.tenPhong, '')) like lower(concat('%', :query, '%')))"
    )
    Page<Ghe> search(@Param("query") String query, Pageable pageable);
}
