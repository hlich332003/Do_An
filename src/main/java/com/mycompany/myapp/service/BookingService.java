package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.ChiTietFB;
import com.mycompany.myapp.domain.DichVuFB;
import com.mycompany.myapp.domain.Ghe;
import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.NguoiDung;
import com.mycompany.myapp.domain.SuatChieu;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.ChiTietFBRepository;
import com.mycompany.myapp.repository.DichVuFBRepository;
import com.mycompany.myapp.repository.GheRepository;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.repository.SuatChieuRepository;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.dto.HoaDonLichSuDTO;
import com.mycompany.myapp.service.mapper.HoaDonMapper;
import com.mycompany.myapp.service.messaging.MessageProducer;
import com.mycompany.myapp.web.rest.dto.BookingCreateRequestDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingService.class);
    private static final String ENTITY_NAME = "booking";

    private final DatVeService datVeService;
    private final DistributedLockService distributedLockService;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonMapper hoaDonMapper;
    private final SuatChieuRepository suatChieuRepository;
    private final GheRepository gheRepository;
    private final ChiTietFBRepository chiTietFBRepository;
    private final VeRepository veRepository;
    private final DichVuFBRepository dichVuFBRepository;
    private final NguoiDungRepository nguoiDungRepository;

    private final MessageProducer messageProducer;

    public BookingService(
        DatVeService datVeService,
        DistributedLockService distributedLockService,
        HoaDonRepository hoaDonRepository,
        HoaDonMapper hoaDonMapper,
        SuatChieuRepository suatChieuRepository,
        GheRepository gheRepository,
        ChiTietFBRepository chiTietFBRepository,
        VeRepository veRepository,
        DichVuFBRepository dichVuFBRepository,
        NguoiDungRepository nguoiDungRepository,
        MessageProducer messageProducer
    ) {
        this.datVeService = datVeService;
        this.distributedLockService = distributedLockService;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonMapper = hoaDonMapper;
        this.suatChieuRepository = suatChieuRepository;
        this.gheRepository = gheRepository;
        this.chiTietFBRepository = chiTietFBRepository;
        this.veRepository = veRepository;
        this.dichVuFBRepository = dichVuFBRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.messageProducer = messageProducer;
    }

    public HoaDonDTO createBooking(BookingCreateRequestDTO request) {
        LOG.debug("Tao hoa don dat ve: {}", request);

        cleanupExpiredBookings();

        if (request.getSuatChieuId() == null) {
            throw new BadRequestAlertException("Thieu suat chieu", ENTITY_NAME, "missingShowtime");
        }
        if (request.getGheIds() == null || request.getGheIds().isEmpty()) {
            throw new BadRequestAlertException("Vui long chon it nhat 1 ghe", ENTITY_NAME, "noSeats");
        }

        SuatChieu suatChieu = suatChieuRepository
            .findOneWithEagerRelationships(request.getSuatChieuId())
            .orElseThrow(() -> new BadRequestAlertException("Suat chieu khong ton tai", ENTITY_NAME, "showtimeNotFound"));

        validateShowtimeIsBookable(suatChieu);

        List<Long> uniqueSeatIds = request.getGheIds().stream().distinct().collect(Collectors.toList());
        if (uniqueSeatIds.size() > 8) {
            throw new BadRequestAlertException("Chỉ được đặt tối đa 8 ghế trong một giao dịch", ENTITY_NAME, "tooManySeats");
        }
        List<Ghe> selectedGhes = new ArrayList<>();
        List<String> lockedSeatKeys = new ArrayList<>();

        try {
            for (Long gheId : uniqueSeatIds) {
                Ghe ghe = gheRepository
                    .findById(gheId)
                    .orElseThrow(() -> new BadRequestAlertException("Ghe khong ton tai", ENTITY_NAME, "seatNotFound"));
                if (ghe.getMaGhe() == null) {
                    throw new BadRequestAlertException("Ghe khong hop le", ENTITY_NAME, "seatNotFound");
                }
                if (datVeService.isSeatSoldForShowtime(request.getSuatChieuId(), gheId)) {
                    throw new BadRequestAlertException("Ghe " + ghe.getMaGhe() + " da duoc ban", ENTITY_NAME, "seatSold");
                }
                selectedGhes.add(ghe);
            }

            for (Ghe ghe : selectedGhes) {
                boolean locked =
                    distributedLockService.isSeatLocked(request.getSuatChieuId(), ghe.getMaGhe()) ||
                    distributedLockService.lockSeat(request.getSuatChieuId(), ghe.getMaGhe(), 300L);
                if (!locked) {
                    throw new BadRequestAlertException("Ghe da het thoi gian giu hoac chua duoc chon", ENTITY_NAME, "seatLockExpired");
                }
                lockedSeatKeys.add(ghe.getMaGhe());
            }

            BigDecimal tongTienGhe = BigDecimal.ZERO;
            for (Ghe ghe : selectedGhes) {
                tongTienGhe = tongTienGhe.add(resolveSeatPrice(suatChieu, ghe));
            }

            BigDecimal tongTienFb = BigDecimal.ZERO;
            List<ChiTietFB> chiTietFBS = new ArrayList<>();
            if (request.getCombos() != null) {
                for (BookingCreateRequestDTO.ComboItemDTO combo : request.getCombos()) {
                    if (combo.getDichVuFBId() == null || combo.getSoLuong() == null || combo.getSoLuong() <= 0) {
                        continue;
                    }
                    DichVuFB dichVuFB = dichVuFBRepository
                        .findById(combo.getDichVuFBId())
                        .orElseThrow(() -> new BadRequestAlertException("Combo F&B khong ton tai", ENTITY_NAME, "comboNotFound"));
                    if (!isComboActive(dichVuFB.getTrangThai())) {
                        throw new BadRequestAlertException(
                            "Combo " + dichVuFB.getTenCombo() + " da ngung ban",
                            ENTITY_NAME,
                            "comboInactive"
                        );
                    }
                    BigDecimal lineTotal = dichVuFB.getGia().multiply(BigDecimal.valueOf(combo.getSoLuong()));
                    tongTienFb = tongTienFb.add(lineTotal);

                    ChiTietFB chiTietFB = new ChiTietFB();
                    chiTietFB.setDichVuFB(dichVuFB);
                    chiTietFB.setSoLuong(combo.getSoLuong());
                    chiTietFBS.add(chiTietFB);
                }
            }

            BigDecimal subtotal = tongTienGhe.add(tongTienFb);
            BigDecimal soTienGiam = BigDecimal.ZERO;
            String maGiamGia = request.getMaGiamGia();
            if (maGiamGia != null && !maGiamGia.isBlank()) {
                soTienGiam = BigDecimal.ZERO;
            }

            BigDecimal tongTien = subtotal.subtract(soTienGiam);
            if (tongTien.compareTo(BigDecimal.ZERO) < 0) {
                tongTien = BigDecimal.ZERO;
            }

            HoaDon hoaDon = new HoaDon();
            hoaDon.setTongTien(tongTien);
            hoaDon.setMaGiamGia(maGiamGia);
            hoaDon.setSoTienGiam(soTienGiam);
            String phuongThuc = "VNPAY";
            if ("CASH".equalsIgnoreCase(request.getPhuongThucThanhToan())) {
                phuongThuc = "CASH";
            }
            hoaDon.setPhuongThucThanhToan(phuongThuc);
            hoaDon.setTrangThai("1");
            hoaDon.setCreatedAt(ZonedDateTime.now());

            NguoiDung nguoiDung = resolveNguoiDung(request);
            if (nguoiDung != null) {
                hoaDon.setNguoiDung(nguoiDung);
            }
            hoaDon = hoaDonRepository.save(hoaDon);

            messageProducer.sendPaymentTimeoutMessage(hoaDon.getId());

            for (Ghe ghe : selectedGhes) {
                BigDecimal giaVe = resolveSeatPrice(suatChieu, ghe);
                Ve ve = veRepository
                    .findBySuatChieuIdAndGheId(suatChieu.getId(), ghe.getId())
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        Ve newVe = new Ve();
                        newVe.setMaVe(java.util.UUID.randomUUID().toString());
                        newVe.setSuatChieu(suatChieu);
                        newVe.setGhe(ghe);
                        newVe.setTrangThai("CHUA_DAT");
                        newVe.setGiaVe(BigDecimal.ZERO);
                        newVe.setHoaDon(null);
                        return veRepository.save(newVe);
                    });
                ve.setMaVe("CIN" + hoaDon.getId() + "-" + ghe.getMaGhe());
                ve.setTrangThai("DANG_GIU_CHO");
                ve.setHoaDon(hoaDon);
                ve.setGiaVe(giaVe);
                veRepository.save(ve);
            }

            for (ChiTietFB chiTietFB : chiTietFBS) {
                chiTietFB.setHoaDon(hoaDon);
                if (chiTietFB.getGiaBan() == null && chiTietFB.getDichVuFB() != null) {
                    chiTietFB.setGiaBan(chiTietFB.getDichVuFB().getGia());
                }
                chiTietFBRepository.save(chiTietFB);
            }

            return hoaDonMapper.toDto(hoaDon);
        } catch (RuntimeException ex) {
            releaseSeatLocks(request.getSuatChieuId(), lockedSeatKeys);
            throw ex;
        }
    }

    private void validateShowtimeIsBookable(SuatChieu suatChieu) {
        if (suatChieu.getThoiGianBatDau() == null) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        if (suatChieu.getThoiGianBatDau().isBefore(now)) {
            throw new BadRequestAlertException("Suất chiếu này đã qua giờ bắt đầu", ENTITY_NAME, "showtimeExpired");
        }
    }

    @Transactional(readOnly = true)
    public List<HoaDonLichSuDTO> getLichSuByEmail(String email) {
        List<HoaDon> hoaDons = hoaDonRepository.findByNguoiDungEmailOrderByIdDesc(email);
        return toLichSuDtoList(hoaDons);
    }

    @Transactional(readOnly = true)
    public List<HoaDonLichSuDTO> getAllLichSu() {
        List<HoaDon> hoaDons = hoaDonRepository.findAllWithEagerRelationships();
        return toLichSuDtoList(hoaDons);
    }

    private List<HoaDonLichSuDTO> toLichSuDtoList(List<HoaDon> hoaDons) {
        if (hoaDons.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> hoaDonIds = hoaDons.stream().map(HoaDon::getId).collect(Collectors.toList());

        // Fetch all tickets eagerly in 1 query
        List<Ve> allVes = veRepository.findByHoaDonIdIn(hoaDonIds);
        Map<Long, List<Ve>> vesMap = allVes
            .stream()
            .filter(v -> v.getHoaDon() != null)
            .collect(Collectors.groupingBy(v -> v.getHoaDon().getId()));

        // Fetch all combos eagerly in 1 query
        List<ChiTietFB> allCombos = chiTietFBRepository.findByHoaDonIdIn(hoaDonIds);
        Map<Long, List<ChiTietFB>> combosMap = allCombos
            .stream()
            .filter(c -> c.getHoaDon() != null)
            .collect(Collectors.groupingBy(c -> c.getHoaDon().getId()));

        return hoaDons
            .stream()
            .map(h -> {
                List<Ve> invoiceVes = vesMap.getOrDefault(h.getId(), List.of());
                List<ChiTietFB> invoiceCombos = combosMap.getOrDefault(h.getId(), List.of());
                return toLichSuDtoWithBatch(h, invoiceVes, invoiceCombos);
            })
            .collect(Collectors.toList());
    }

    private HoaDonLichSuDTO toLichSuDtoWithBatch(HoaDon hoaDon, List<Ve> ves, List<ChiTietFB> combosList) {
        HoaDonLichSuDTO dto = new HoaDonLichSuDTO();
        dto.setId(hoaDon.getId());
        dto.setTongTien(hoaDon.getTongTien());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setMaGiaoDich(hoaDon.getMaGiaoDich());
        String rawStatus = hoaDon.getTrangThai();
        String ttString = "PENDING";
        if (rawStatus != null) {
            String upper = rawStatus.trim().toUpperCase();
            if (
                upper.equals("2") ||
                upper.equals("SUCCESS") ||
                upper.equals("PAID") ||
                upper.equals("DA_THANH_TOAN") ||
                upper.equals("DONE")
            ) {
                ttString = "PAID";
            } else if (upper.equals("3") || upper.equals("CANCELLED") || upper.equals("DA_HUY") || upper.equals("REFUNDED")) {
                ttString = "CANCELLED";
            }
        }
        dto.setTrangThai(ttString);

        Set<String> gheNames = new LinkedHashSet<>();
        List<Long> veIds = new ArrayList<>();
        for (Ve ve : ves) {
            veIds.add(ve.getId());
            if (ve.getGhe() != null) {
                gheNames.add(ve.getGhe().getMaGhe());
            }
            if (dto.getMaVe() == null) {
                dto.setMaVe(ve.getMaVe());
            }
            if (ve.getSuatChieu() != null) {
                dto.setGioChieu(ve.getSuatChieu().getThoiGianBatDau());
                if (ve.getSuatChieu().getPhim() != null) {
                    dto.setTenPhim(ve.getSuatChieu().getPhim().getTenPhim());
                }
                if (ve.getSuatChieu().getPhongChieu() != null) {
                    dto.setTenPhongChieu(ve.getSuatChieu().getPhongChieu().getTenPhong());
                    dto.setTenRap("CinemaTick");
                }
            }
        }
        dto.setVeIds(veIds);
        dto.setDanhSachGhe(String.join(", ", gheNames));

        String combos = combosList
            .stream()
            .filter(ct -> ct.getDichVuFB() != null)
            .map(ct -> ct.getSoLuong() + "x " + ct.getDichVuFB().getTenCombo())
            .collect(Collectors.joining(", "));
        dto.setDanhSachCombo(combos);

        return dto;
    }

    private NguoiDung resolveNguoiDung(BookingCreateRequestDTO request) {
        if (request.getNguoiDungId() != null) {
            return nguoiDungRepository.findById(request.getNguoiDungId()).orElse(null);
        }

        Optional<NguoiDung> currentUser = SecurityUtils.getCurrentUserLogin().flatMap(nguoiDungRepository::findOneByEmailIgnoreCase);
        if (currentUser.isPresent()) {
            return currentUser.get();
        }

        return null;
    }

    private BigDecimal resolveSeatPrice(SuatChieu suatChieu, Ghe ghe) {
        BigDecimal giaThuong = suatChieu.getGiaThuong() != null ? suatChieu.getGiaThuong() : BigDecimal.valueOf(50000);
        BigDecimal giaVip = suatChieu.getGiaVip() != null ? suatChieu.getGiaVip() : BigDecimal.valueOf(120000);

        if (ghe.getLoaiGhe() != null && "3".equals(ghe.getLoaiGhe())) {
            return giaVip.multiply(BigDecimal.valueOf(2));
        }
        if (ghe.getLoaiGhe() != null && "2".equals(ghe.getLoaiGhe())) {
            return giaVip;
        }
        return giaThuong;
    }

    private boolean isComboActive(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return false;
        }
        String normalized = java.text.Normalizer.normalize(trangThai, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(java.util.Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
        return "1".equals(normalized);
    }

    private void releaseSeatLocks(Long suatChieuId, List<String> maGhes) {
        if (suatChieuId == null || maGhes == null || maGhes.isEmpty()) {
            return;
        }
        for (String maGhe : new LinkedHashSet<>(maGhes)) {
            if (maGhe != null && !maGhe.isBlank()) {
                distributedLockService.unlockSeat(suatChieuId, maGhe);
            }
        }
    }

    public HoaDon confirmPayment(Long invoiceId) {
        LOG.debug("Xac nhan thanh toan cho hoa don: {}", invoiceId);

        HoaDon hoaDon = hoaDonRepository
            .findById(invoiceId)
            .orElseThrow(() -> new BadRequestAlertException("Hoa don khong ton tai", ENTITY_NAME, "invoiceNotFound"));

        if ("3".equals(hoaDon.getTrangThai())) {
            LOG.error("[CONCURRENCY TRAP] Hoa don {} da bi RabbitMQ huy do qua han 5 phut, nhung callback thanh toan ve muon.", invoiceId);
            return hoaDon;
        }

        if (hoaDon.getTrangThai() != null && !"1".equals(hoaDon.getTrangThai())) {
            return hoaDon;
        }

        hoaDon.setTrangThai("2");
        hoaDon.setNgayThanhToan(java.time.ZonedDateTime.now());
        hoaDon = hoaDonRepository.save(hoaDon);

        veRepository
            .findByHoaDonId(invoiceId)
            .forEach(ve -> {
                ve.setTrangThai("DA_DAT");
                veRepository.save(ve);
                if (ve.getSuatChieu() != null && ve.getGhe() != null && ve.getGhe().getMaGhe() != null) {
                    distributedLockService.unlockSeat(ve.getSuatChieu().getId(), ve.getGhe().getMaGhe());
                }
            });

        try {
            messageProducer.sendEmailTicketMessage(invoiceId);
            messageProducer.sendStatUpdateMessage(invoiceId, hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO);
            LOG.info("📧 [BOOKING] Sent MQ messages for confirmed payment of invoice #{}", invoiceId);
        } catch (Exception e) {
            LOG.error("❌ [BOOKING] Failed to send MQ messages for invoice #{}", invoiceId, e);
        }

        return hoaDon;
    }

    public void cancelBooking(Long invoiceId) {
        LOG.debug("Hủy hóa đơn: {}", invoiceId);
        HoaDon hoaDon = hoaDonRepository
            .findById(invoiceId)
            .orElseThrow(() -> new BadRequestAlertException("Hóa đơn không tồn tại", ENTITY_NAME, "invoiceNotFound"));

        if ("2".equals(hoaDon.getTrangThai())) {
            LOG.info(
                "[CONCURRENCY TRAP] Hóa đơn {} đã được VNPay thanh toán thành công trước khi RabbitMQ kích hoạt hủy đơn. Bỏ qua lệnh hủy.",
                invoiceId
            );
            return;
        }

        if (hoaDon.getTrangThai() != null && !"1".equals(hoaDon.getTrangThai())) {
            return; // Already processed
        }

        hoaDon.setTrangThai("3"); // 3 = CANCELLED
        hoaDonRepository.save(hoaDon);

        veRepository
            .findByHoaDonId(invoiceId)
            .forEach(ve -> {
                ve.setTrangThai("CHUA_DAT");
                // Giữ liên kết với hóa đơn để lịch sử đặt vé vẫn hiển thị đầy đủ
                ve.setHoaDon(hoaDon);
                ve.setGiaVe(BigDecimal.ZERO);
                veRepository.save(ve);
                if (ve.getSuatChieu() != null && ve.getGhe() != null && ve.getGhe().getMaGhe() != null) {
                    distributedLockService.unlockSeat(ve.getSuatChieu().getId(), ve.getGhe().getMaGhe());
                }
            });
    }

    @Transactional(readOnly = true)
    public HoaDonLichSuDTO getLichSuByHoaDonId(Long id) {
        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(id);
        if (hoaDonOpt.isEmpty()) {
            return null;
        }
        HoaDon h = hoaDonOpt.get();
        List<Ve> ves = new ArrayList<>(veRepository.findByHoaDonId(h.getId()));
        List<ChiTietFB> combos = new ArrayList<>(chiTietFBRepository.findByHoaDonId(h.getId()));
        return toLichSuDtoWithBatch(h, ves, combos);
    }

    public void cleanupExpiredBookings() {
        ZonedDateTime fiveMinutesAgo = ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(5);

        List<HoaDon> expiredBookings = hoaDonRepository
            .findAll()
            .stream()
            .filter(
                h ->
                    h.getTrangThai() != null &&
                    ("1".equals(h.getTrangThai()) || "PENDING".equalsIgnoreCase(h.getTrangThai())) &&
                    h.getCreatedAt() != null &&
                    h.getCreatedAt().isBefore(fiveMinutesAgo)
            )
            .toList();

        for (HoaDon hoaDon : expiredBookings) {
            cancelBooking(hoaDon.getId());
        }
    }
}
