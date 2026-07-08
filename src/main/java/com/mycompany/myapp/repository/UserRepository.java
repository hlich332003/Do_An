package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

/**
 * Mock UserRepository.
 */
@Repository
public class UserRepository {

    public static final String USERS_BY_LOGIN_CACHE = "usersByLogin";
    public static final String USERS_BY_EMAIL_CACHE = "usersByEmail";

    public Optional<User> findOneByActivationKey(String activationKey) {
        return Optional.empty();
    }

    public List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime) {
        return List.of();
    }

    public Optional<User> findOneByResetKey(String resetKey) {
        return Optional.empty();
    }

    public Optional<User> findOneByEmailIgnoreCase(String email) {
        return Optional.empty();
    }

    public Optional<User> findOneByLogin(String login) {
        return Optional.empty();
    }

    public Optional<User> findOneWithAuthoritiesByLogin(String login) {
        return Optional.empty();
    }

    public Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email) {
        return Optional.empty();
    }

    public Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable) {
        return Page.empty();
    }

    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    public void delete(User user) {}

    public void save(User user) {}

    public void flush() {}

    public Page<User> findAll(Pageable pageable) {
        return Page.empty();
    }
}
