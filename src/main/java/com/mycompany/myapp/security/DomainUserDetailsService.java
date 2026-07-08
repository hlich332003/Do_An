package com.mycompany.myapp.security;

import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.repository.NguoiDungRepository;
import java.util.*;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final NguoiDungRepository nguoiDungRepository;

    public DomainUserDetailsService(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String login) {
        LOG.debug("Authenticating {}", login);

        return nguoiDungRepository
            .findOneByEmailIgnoreCaseOrSoDienThoai(login, login)
            .map(user -> createSpringSecurityUser(login, user))
            .orElseThrow(() -> new UsernameNotFoundException("User " + login + " was not found in the database"));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, NguoiDung user) {
        String status = user.getTrangThai();
        if (status != null) {
            String upper = status.trim().toUpperCase();
            if (upper.equals("0") || upper.equals("BLOCKED") || upper.equals("LOCKED") || upper.contains("KHOA")) {
                throw new UserNotActivatedException("Tài khoản của bạn đã bị khóa.");
            }
        }
        return UserWithId.fromNguoiDung(user);
    }

    public static class UserWithId extends org.springframework.security.core.userdetails.User {

        private final Long id;

        public UserWithId(String login, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(login, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public static UserWithId fromNguoiDung(NguoiDung user) {
            List<SimpleGrantedAuthority> auths = new ArrayList<>();
            if (user.getVaiTro() != null && !user.getVaiTro().trim().isEmpty()) {
                auths.add(new SimpleGrantedAuthority(user.getVaiTro().trim()));
            } else {
                auths.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return new UserWithId(user.getEmail(), user.getMatKhau(), auths, user.getId());
        }
    }
}
