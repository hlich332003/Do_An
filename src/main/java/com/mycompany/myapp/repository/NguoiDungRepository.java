package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.NguoiDung;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NguoiDung entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findOneByEmailIgnoreCase(String email);
    Optional<NguoiDung> findOneByEmailIgnoreCaseOrSoDienThoai(String email, String soDienThoai);
    Optional<NguoiDung> findOneBySoDienThoai(String soDienThoai);
    long countByCreatedAtLessThanEqual(ZonedDateTime createdAt);
    long countByCreatedAtBetween(ZonedDateTime from, ZonedDateTime to);
}
