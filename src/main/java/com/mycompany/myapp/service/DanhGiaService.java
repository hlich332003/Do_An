package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DanhGia;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.Phim;
import com.mycompany.myapp.repository.DanhGiaRepository;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.repository.PhimRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.DanhGiaDTO;
import com.mycompany.myapp.service.mapper.DanhGiaMapper;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DanhGiaService {

    private static final Logger LOG = LoggerFactory.getLogger(DanhGiaService.class);
    private static final String ENTITY_NAME = "danhGia";

    private final DanhGiaRepository danhGiaRepository;
    private final PhimRepository phimRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DanhGiaMapper danhGiaMapper;
    private final DataSource dataSource;

    public DanhGiaService(
        DanhGiaRepository danhGiaRepository,
        PhimRepository phimRepository,
        NguoiDungRepository nguoiDungRepository,
        DanhGiaMapper danhGiaMapper,
        DataSource dataSource
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.phimRepository = phimRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.danhGiaMapper = danhGiaMapper;
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public List<DanhGiaDTO> findByPhimId(Long phimId) {
        if (!reviewTableExists()) {
            return List.of();
        }
        return danhGiaRepository.findByPhimIdOrderByCreatedAtDesc(phimId).stream().map(danhGiaMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long phimId) {
        if (!reviewTableExists()) {
            return 0.0;
        }
        Double avg = danhGiaRepository.getAverageRatingByPhimId(phimId);
        return avg != null ? avg : 0.0;
    }

    public DanhGiaDTO save(DanhGiaDTO danhGiaDTO) {
        LOG.debug("Request to save DanhGia : {}", danhGiaDTO);
        if (!reviewTableExists()) {
            throw new BadRequestAlertException("Hệ thống đánh giá chưa sẵn sàng", ENTITY_NAME, "reviewTableMissing");
        }
        if (danhGiaDTO.getPhimId() == null) {
            throw new BadRequestAlertException("Thiếu phim cần đánh giá", ENTITY_NAME, "missingPhim");
        }
        if (danhGiaDTO.getSoSao() == null || danhGiaDTO.getSoSao() < 1 || danhGiaDTO.getSoSao() > 10) {
            throw new BadRequestAlertException("Số sao không hợp lệ", ENTITY_NAME, "invalidRating");
        }

        Phim phim = phimRepository
            .findById(danhGiaDTO.getPhimId())
            .orElseThrow(() -> new BadRequestAlertException("Phim không tồn tại", ENTITY_NAME, "phimNotFound"));

        NguoiDung nguoiDung = resolveCurrentNguoiDung()
            .orElseThrow(() -> new BadRequestAlertException("Bạn cần đăng nhập để đánh giá phim", ENTITY_NAME, "unauthorized"));

        if (danhGiaRepository.findFirstByPhimIdAndNguoiDungId(phim.getId(), nguoiDung.getId()).isPresent()) {
            throw new BadRequestAlertException("Bạn chỉ được đánh giá mỗi phim một lần", ENTITY_NAME, "reviewAlreadyExists");
        }

        String noiDung = danhGiaDTO.getNoiDung();
        if (noiDung != null) {
            noiDung = noiDung.trim();
            if (noiDung.isBlank()) {
                noiDung = null;
            }
        }

        DanhGia danhGia = new DanhGia();
        danhGia.setPhim(phim);
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSoSao(danhGiaDTO.getSoSao());
        danhGia.setNoiDung(noiDung);
        danhGia.setCreatedAt(ZonedDateTime.now());

        return danhGiaMapper.toDto(danhGiaRepository.save(danhGia));
    }

    public DanhGiaDTO update(DanhGiaDTO danhGiaDTO) {
        LOG.debug("Request to update DanhGia : {}", danhGiaDTO);
        if (!reviewTableExists()) {
            throw new BadRequestAlertException("Hệ thống đánh giá chưa sẵn sàng", ENTITY_NAME, "reviewTableMissing");
        }
        if (danhGiaDTO.getId() == null) {
            throw new BadRequestAlertException("Thiếu id đánh giá", ENTITY_NAME, "missingId");
        }
        if (danhGiaDTO.getSoSao() == null || danhGiaDTO.getSoSao() < 1 || danhGiaDTO.getSoSao() > 10) {
            throw new BadRequestAlertException("Số sao không hợp lệ", ENTITY_NAME, "invalidRating");
        }

        DanhGia existingDanhGia = danhGiaRepository
            .findById(danhGiaDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Đánh giá không tồn tại", ENTITY_NAME, "reviewNotFound"));

        NguoiDung nguoiDung = resolveCurrentNguoiDung()
            .orElseThrow(() -> new BadRequestAlertException("Bạn cần đăng nhập để chỉnh sửa đánh giá", ENTITY_NAME, "unauthorized"));

        // Verify ownership
        if (!existingDanhGia.getNguoiDung().getId().equals(nguoiDung.getId())) {
            throw new BadRequestAlertException("Bạn không có quyền chỉnh sửa đánh giá này", ENTITY_NAME, "forbidden");
        }

        // Verify 7-day limit
        ZonedDateTime limitTime = existingDanhGia.getCreatedAt().plusDays(7);
        if (ZonedDateTime.now().isAfter(limitTime)) {
            throw new BadRequestAlertException("Đã hết thời hạn 7 ngày để chỉnh sửa đánh giá này", ENTITY_NAME, "editTimeExpired");
        }

        String noiDung = danhGiaDTO.getNoiDung();
        if (noiDung != null) {
            noiDung = noiDung.trim();
            if (noiDung.isBlank()) {
                noiDung = null;
            }
        }

        existingDanhGia.setSoSao(danhGiaDTO.getSoSao());
        existingDanhGia.setNoiDung(noiDung);
        // Do NOT change createdAt, keep it as original

        return danhGiaMapper.toDto(danhGiaRepository.save(existingDanhGia));
    }

    private Optional<NguoiDung> resolveCurrentNguoiDung() {
        return SecurityUtils.getCurrentUserLogin().flatMap(nguoiDungRepository::findOneByEmailIgnoreCase);
    }

    private boolean reviewTableExists() {
        try (var connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            try (var resultSet = metaData.getTables(null, null, "danhgia", new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    return true;
                }
            }
            try (var resultSet = metaData.getTables(null, null, "danh_gia", new String[] { "TABLE" })) {
                return resultSet.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
