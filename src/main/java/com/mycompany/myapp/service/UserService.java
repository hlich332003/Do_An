package com.mycompany.myapp.service;

import com.mycompany.myapp.config.Constants;
import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.AuthorityRepository;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.AdminUserDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    private final NguoiDungRepository nguoiDungRepository;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager,
        NguoiDungRepository nguoiDungRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository
            .findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                this.clearUserCaches(user);
                LOG.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public User registerUser(AdminUserDTO userDTO, String password) {
        userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException();
                }
            });
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new EmailAlreadyUsedException();
                }
            });
        if (userDTO.getSoDienThoai() != null && !userDTO.getSoDienThoai().isBlank()) {
            nguoiDungRepository
                .findOneBySoDienThoai(userDTO.getSoDienThoai().trim())
                .ifPresent(existingNguoiDung -> {
                    throw new PhoneNumberAlreadyUsedException();
                });
        }
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is active immediately (bypass email verification)
        newUser.setActivated(true);
        newUser.setFailedLoginAttempts(0);
        newUser.setLockedUntil(null);
        // no activation key needed
        newUser.setActivationKey(null);
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);

        // Chỉ tạo NguoiDung nếu chưa tồn tại email này trong bảng
        if (!nguoiDungRepository.findOneByEmailIgnoreCase(newUser.getEmail()).isPresent()) {
            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setHoTen(buildDisplayName(userDTO));
            nguoiDung.setEmail(newUser.getEmail());
            nguoiDung.setMatKhau(encryptedPassword);
            nguoiDung.setVaiTro(AuthoritiesConstants.USER);
            nguoiDung.setTrangThai("ACTIVE");
            nguoiDung.setDiemTichLuy(0);
            nguoiDung.setCreatedAt(java.time.ZonedDateTime.now());
            nguoiDung.setUpdatedAt(java.time.ZonedDateTime.now());
            // Lưu SĐT nếu người dùng điền (chỉ set khi có giá trị để tránh vi phạm UNIQUE NULL)
            if (userDTO.getSoDienThoai() != null && !userDTO.getSoDienThoai().isBlank()) {
                nguoiDung.setSoDienThoai(userDTO.getSoDienThoai().trim());
            }
            nguoiDungRepository.save(nguoiDung);
        }

        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        LOG.debug("Created Information for User: {}", user);

        // Đồng bộ tạo NguoiDung để admin-created account có thể đăng nhập
        if (user.getEmail() != null && !nguoiDungRepository.findOneByEmailIgnoreCase(user.getEmail()).isPresent()) {
            String role = (userDTO.getAuthorities() != null && userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN))
                ? AuthoritiesConstants.ADMIN
                : AuthoritiesConstants.USER;
            String displayName = buildDisplayName(userDTO);

            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setHoTen(displayName);
            nguoiDung.setEmail(user.getEmail());
            nguoiDung.setMatKhau(user.getPassword()); // đã được mã hoá
            nguoiDung.setVaiTro(role);
            nguoiDung.setTrangThai("ACTIVE");
            nguoiDung.setDiemTichLuy(0);
            nguoiDung.setCreatedAt(java.time.ZonedDateTime.now());
            nguoiDung.setUpdatedAt(java.time.ZonedDateTime.now());
            nguoiDungRepository.save(nguoiDung);
            LOG.debug("Created NguoiDung entry for admin-created user: {}", user.getEmail());
        }

        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository
            .findOneByLogin(login)
            .ifPresent(user -> {
                userRepository.delete(user);
                this.clearUserCaches(user);
                LOG.debug("Deleted User: {}", user);
            });
    }

    /**
     * Update basic information (first name, last name, email, language) for the
     * current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .ifPresent(currentLogin -> {
                userRepository
                    .findOneByLogin(currentLogin)
                    .ifPresent(user -> {
                        String normalizedEmail = email != null ? email.toLowerCase() : null;
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        if (normalizedEmail != null) {
                            user.setEmail(normalizedEmail);
                        }
                        user.setLangKey(langKey);
                        user.setImageUrl(imageUrl);
                        userRepository.save(user);
                        this.clearUserCaches(user);
                        LOG.debug("Changed Information for User: {}", user);
                    });

                nguoiDungRepository
                    .findOneByEmailIgnoreCase(currentLogin)
                    .ifPresent(nguoiDung -> {
                        String fullName = (firstName != null ? firstName.trim() : "") + " " + (lastName != null ? lastName.trim() : "");
                        nguoiDung.setHoTen(fullName.trim().isEmpty() ? nguoiDung.getHoTen() : fullName.trim());
                        if (email != null && !email.isBlank()) {
                            nguoiDung.setEmail(email.toLowerCase());
                        }
                        nguoiDungRepository.save(nguoiDung);
                    });
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            throw new InvalidPasswordException();
        }

        Optional<User> springUser = userRepository.findOneByLogin(currentLogin);
        if (springUser.isPresent()) {
            User user = springUser.get();
            String currentEncryptedPassword = user.getPassword();
            if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                throw new InvalidPasswordException();
            }
            String encryptedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encryptedPassword);
            this.clearUserCaches(user);
            LOG.debug("Changed password for User: {}", user);
            return;
        }

        nguoiDungRepository
            .findOneByEmailIgnoreCase(currentLogin)
            .ifPresent(nguoiDung -> {
                if (!passwordEncoder.matches(currentClearTextPassword, nguoiDung.getMatKhau())) {
                    throw new InvalidPasswordException();
                }
                nguoiDung.setMatKhau(passwordEncoder.encode(newPassword));
                nguoiDungRepository.save(nguoiDung);
                LOG.debug("Changed password for NguoiDung: {}", nguoiDung.getEmail());
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        Optional<User> fromNguoiDung = nguoiDungRepository
            .findOneByEmailIgnoreCase(login)
            .map(nd -> {
                User user = new User();
                user.setId(nd.getId());
                user.setLogin(nd.getEmail());
                user.setEmail(nd.getEmail());
                String hoTen = nd.getHoTen();
                if (hoTen != null && hoTen.contains(" ")) {
                    user.setFirstName(hoTen.substring(0, hoTen.lastIndexOf(" ")));
                    user.setLastName(hoTen.substring(hoTen.lastIndexOf(" ") + 1));
                } else {
                    user.setFirstName(hoTen);
                    user.setLastName("");
                }
                String status = nd.getTrangThai() != null ? nd.getTrangThai().trim().toUpperCase() : "";
                user.setActivated("1".equals(status) || "ACTIVE".equals(status) || "HOẠT ĐỘNG".equals(status));
                user.setLangKey(Constants.DEFAULT_LANGUAGE);
                Authority userAuth = new Authority();
                if (nd.getVaiTro() != null && !nd.getVaiTro().trim().isEmpty()) {
                    userAuth.setName(nd.getVaiTro().trim());
                } else {
                    userAuth.setName("ROLE_USER");
                }
                Set<Authority> auths = new HashSet<>();
                auths.add(userAuth);
                user.setAuthorities(auths);
                return user;
            });

        if (fromNguoiDung.isPresent()) {
            return fromNguoiDung;
        }

        return Optional.of(buildFallbackUser(login));
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(this::getUserWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evictIfPresent(user.getEmail());
        }
    }

    private String buildDisplayName(AdminUserDTO userDTO) {
        String firstName = userDTO.getFirstName() != null ? userDTO.getFirstName().trim() : "";
        String lastName = userDTO.getLastName() != null ? userDTO.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? userDTO.getLogin() : fullName;
    }

    private User buildFallbackUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login);
        user.setActivated(true);
        user.setLangKey(Constants.DEFAULT_LANGUAGE);

        Set<Authority> auths = new HashSet<>();
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .forEach(authorityName -> {
                    Authority authority = new Authority();
                    authority.setName(authorityName);
                    auths.add(authority);
                });
        }
        if (auths.isEmpty()) {
            Authority authority = new Authority();
            authority.setName(AuthoritiesConstants.USER);
            auths.add(authority);
        }
        user.setAuthorities(auths);
        return user;
    }
}
