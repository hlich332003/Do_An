package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Authority;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Mock AuthorityRepository
 */
@Repository
public class AuthorityRepository {

    public Optional<Authority> findById(String id) {
        return Optional.empty();
    }

    public List<Authority> findAll() {
        return List.of();
    }
}
