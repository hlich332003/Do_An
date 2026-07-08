package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.service.dto.NguoiDungDTO;
import com.mycompany.myapp.service.mapper.NguoiDungMapper;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.NguoiDung}.
 */
@Service
@Transactional
public class NguoiDungService {

    private static final Logger LOG = LoggerFactory.getLogger(NguoiDungService.class);

    private final NguoiDungRepository nguoiDungRepository;

    private final NguoiDungMapper nguoiDungMapper;

    private final PasswordEncoder passwordEncoder;

    public NguoiDungService(NguoiDungRepository nguoiDungRepository, NguoiDungMapper nguoiDungMapper, PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungMapper = nguoiDungMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Save a nguoiDung.
     *
     * @param nguoiDungDTO the entity to save.
     * @return the persisted entity.
     */
    public NguoiDungDTO save(NguoiDungDTO nguoiDungDTO) {
        LOG.debug("Request to save NguoiDung : {}", nguoiDungDTO);
        NguoiDung nguoiDung = nguoiDungMapper.toEntity(nguoiDungDTO);
        normalizeCredentials(nguoiDung);
        nguoiDung = nguoiDungRepository.save(nguoiDung);
        return nguoiDungMapper.toDto(nguoiDung);
    }

    /**
     * Update a nguoiDung.
     *
     * @param nguoiDungDTO the entity to save.
     * @return the persisted entity.
     */
    public NguoiDungDTO update(NguoiDungDTO nguoiDungDTO) {
        LOG.debug("Request to update NguoiDung : {}", nguoiDungDTO);
        NguoiDung nguoiDung = nguoiDungMapper.toEntity(nguoiDungDTO);
        normalizeCredentials(nguoiDung);
        nguoiDung = nguoiDungRepository.save(nguoiDung);
        return nguoiDungMapper.toDto(nguoiDung);
    }

    /**
     * Partially update a nguoiDung.
     *
     * @param nguoiDungDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NguoiDungDTO> partialUpdate(NguoiDungDTO nguoiDungDTO) {
        LOG.debug("Request to partially update NguoiDung : {}", nguoiDungDTO);

        return nguoiDungRepository
            .findById(nguoiDungDTO.getId())
            .map(existingNguoiDung -> {
                nguoiDungMapper.partialUpdate(existingNguoiDung, nguoiDungDTO);
                normalizeCredentials(existingNguoiDung);

                return existingNguoiDung;
            })
            .map(nguoiDungRepository::save)
            .map(nguoiDungMapper::toDto);
    }

    /**
     * Get all the nguoiDungs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NguoiDungDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all NguoiDungs");
        return nguoiDungRepository.findAll(pageable).map(nguoiDungMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NguoiDungDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search NguoiDungs by query: {}", query);
        String normalizedQuery = normalize(query);
        List<NguoiDungDTO> filtered = nguoiDungRepository
            .findAll()
            .stream()
            .filter(nguoiDung -> matchesQuery(nguoiDung, normalizedQuery))
            .map(nguoiDungMapper::toDto)
            .toList();
        return toPage(filtered, pageable);
    }

    /**
     * Get one nguoiDung by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NguoiDungDTO> findOne(Long id) {
        LOG.debug("Request to get NguoiDung : {}", id);
        return nguoiDungRepository.findById(id).map(nguoiDungMapper::toDto);
    }

    /**
     * Delete the nguoiDung by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete NguoiDung : {}", id);
        nguoiDungRepository.deleteById(id);
    }

    private void normalizeCredentials(NguoiDung nguoiDung) {
        if (nguoiDung == null || nguoiDung.getMatKhau() == null || nguoiDung.getMatKhau().isBlank()) {
            return;
        }

        String matKhau = nguoiDung.getMatKhau().trim();
        if (!matKhau.startsWith("$2")) {
            nguoiDung.setMatKhau(passwordEncoder.encode(matKhau));
        } else {
            nguoiDung.setMatKhau(matKhau);
        }

        if (nguoiDung.getTrangThai() == null || nguoiDung.getTrangThai().isBlank()) {
            nguoiDung.setTrangThai("ACTIVE");
        }
        if (nguoiDung.getVaiTro() == null || nguoiDung.getVaiTro().isBlank()) {
            nguoiDung.setVaiTro("ROLE_USER");
        }
    }

    private boolean matchesQuery(NguoiDung nguoiDung, String query) {
        if (query == null) {
            return true;
        }

        String idText = nguoiDung.getId() != null ? String.valueOf(nguoiDung.getId()) : "";
        return (
            contains(idText, query) ||
            contains(nguoiDung.getHoTen(), query) ||
            contains(nguoiDung.getEmail(), query) ||
            contains(nguoiDung.getSoDienThoai(), query) ||
            contains(nguoiDung.getDiaChi(), query) ||
            contains(nguoiDung.getVaiTro(), query) ||
            contains(nguoiDung.getTrangThai(), query)
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String query) {
        return query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private Page<NguoiDungDTO> toPage(List<NguoiDungDTO> data, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(data);
        }
        int start = (int) pageable.getOffset();
        if (start >= data.size()) {
            return new PageImpl<>(List.of(), pageable, data.size());
        }
        int end = Math.min(start + pageable.getPageSize(), data.size());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }
}
