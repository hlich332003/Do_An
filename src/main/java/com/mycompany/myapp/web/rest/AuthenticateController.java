package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.mycompany.myapp.security.SecurityUtils.JWT_ALGORITHM;
import static com.mycompany.myapp.security.SecurityUtils.USER_ID_CLAIM;

import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.DomainUserDetailsService.UserWithId;
import com.mycompany.myapp.web.rest.vm.LoginVM;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateController.class);

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final NguoiDungRepository nguoiDungRepository;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthenticateController(
        JwtEncoder jwtEncoder,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        UserRepository userRepository,
        NguoiDungRepository nguoiDungRepository
    ) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userRepository = userRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @PostMapping({ "/authenticate", "/auth/login" })
    public ResponseEntity<Map<String, String>> authorize(@Valid @RequestBody LoginVM loginVM) {
        String login = resolveLogin(loginVM.getUsername());
        Optional<NguoiDung> existingUser = findNguoiDung(loginVM.getUsername());
        existingUser.ifPresent(this::ensureNotLocked);

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login, loginVM.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            existingUser.ifPresent(this::resetLoginAttempts);
            String jwt = this.createToken(authentication, loginVM.isRememberMe());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBearerAuth(jwt);
            Map<String, String> body = new HashMap<>();
            body.put("id_token", jwt);
            return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
        } catch (RuntimeException ex) {
            existingUser.ifPresent(this::registerFailedLogin);
            throw ex;
        }
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)},
     * or with status {@code 401 (Unauthorized)} if not authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        Instant now = Instant.now();
        Instant validity;
        if (rememberMe) {
            validity = now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS);
        } else {
            validity = now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);
        }

        // @formatter:off
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities);
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    private String resolveLogin(String usernameOrEmail) {
        return findNguoiDung(usernameOrEmail).map(NguoiDung::getEmail).orElse(usernameOrEmail);
    }

    private Optional<NguoiDung> findNguoiDung(String usernameOrEmail) {
        return nguoiDungRepository.findOneByEmailIgnoreCaseOrSoDienThoai(usernameOrEmail, usernameOrEmail);
    }

    private void ensureNotLocked(NguoiDung user) {
        Instant lockedUntil = user.getLockedUntil();
        if (lockedUntil != null && lockedUntil.isAfter(Instant.now())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.LOCKED, "Tài khoản đang bị khóa tạm thời");
        }
        if (lockedUntil != null && !lockedUntil.isAfter(Instant.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            nguoiDungRepository.save(user);
        }
    }

    private void registerFailedLogin(NguoiDung user) {
        int failedAttempts = Optional.ofNullable(user.getFailedLoginAttempts()).orElse(0) + 1;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= 5) {
            user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15)));
        }
        nguoiDungRepository.save(user);
    }

    private void resetLoginAttempts(NguoiDung user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        nguoiDungRepository.save(user);
    }
}
