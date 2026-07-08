package com.mycompany.myapp.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "lichsuchat_ai")
public class LichSuChatAi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoidung_id")
    private NguoiDung nguoiDung;

    @Column(name = "cauhoi_khachhang", columnDefinition = "NVARCHAR(MAX)")
    private String cauHoiKhachHang;

    @Column(name = "cautraloi_bot", columnDefinition = "NVARCHAR(MAX)")
    private String cauTraLoiBot;

    @Column(name = "thoigian_chat")
    private ZonedDateTime thoiGianChat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getCauHoiKhachHang() {
        return cauHoiKhachHang;
    }

    public void setCauHoiKhachHang(String cauHoiKhachHang) {
        this.cauHoiKhachHang = cauHoiKhachHang;
    }

    public String getCauTraLoiBot() {
        return cauTraLoiBot;
    }

    public void setCauTraLoiBot(String cauTraLoiBot) {
        this.cauTraLoiBot = cauTraLoiBot;
    }

    public ZonedDateTime getThoiGianChat() {
        return thoiGianChat;
    }

    public void setThoiGianChat(ZonedDateTime thoiGianChat) {
        this.thoiGianChat = thoiGianChat;
    }
}
