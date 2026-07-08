# CẨM NANG 30 CÂU HỎI & TRẢ LỜI PHẢN BIỆN BÁM SÁT BÁO CÁO ĐỒ ÁN CINEMATICK

**Sinh viên thực hiện:** Nguyễn Hoàng Lịch (MSV: 2151061202)  
**Lớp:** 63CNTT1  
**Đề tài:** Website đặt vé xem phim trực tuyến CinemaTick tích hợp Trợ lý ảo AI

---

> [!NOTE] > **ĐỒNG BỘ CƠ SỞ DỮ LIỆU THÀNH CÔNG:**
> Hiện tại, hệ thống đã được cấu hình đồng bộ sử dụng **Microsoft SQL Server** cho cả hai môi trường:
>
> - **Môi trường phát triển local (Dev profile):** Kết nối tới SQL Server cục bộ trên máy (`application-dev.yml`).
> - **Môi trường container hóa (Docker Compose):** Khởi chạy SQL Server Container (`mssql.yml`), tự động tạo database và import trực tiếp file `database.sql` (66MB) khi khởi động.
>   Do đó, câu trả lời của bạn khi Thầy hỏi về cơ sở dữ liệu sẽ hoàn toàn thống nhất với nội dung viết trong báo cáo đồ án.

---

## 📂 PHẦN 1: CÂU HỎI VỀ KIẾN TRÚC & CÔNG NGHỆ NỀN TẢNG (Mục 3.1 & 5.1 & 5.2)

### Câu 1: Tại Mục 3.1, báo cáo nêu Frontend được xây dựng dưới dạng Single Page Application (SPA) bằng Angular. Hãy giải thích ưu điểm của kiến trúc này đối với hệ thống?

- **Trả lời (Khớp báo cáo):**
  - Giao diện SPA của Angular giúp tải và cập nhật trang bất đồng bộ mà không cần tải lại toàn bộ website khi người dùng click chuyển hướng.
  - Điều này mang lại trải nghiệm mượt mà, tốc độ phản hồi nhanh chóng như ứng dụng di động, đồng thời giảm tải băng thông truyền tải giữa Client và Server.

### Câu 2: Trong Mục 5.1.2, em có nêu kiến trúc tích hợp hệ thống của Chatbot là "Client - Server - API Gateway Proxy". Tại sao em không cho Angular gọi trực tiếp API của Google Gemini?

- **Trả lời (Khớp báo cáo):**
  - **Bảo mật:** Gọi trực tiếp từ Angular sẽ làm lộ API Key của Google Gemini trong mã nguồn JavaScript ở trình duyệt, tạo cơ hội cho kẻ xấu đánh cắp.
  - **Làm giàu ngữ cảnh (RAG):** Backend Spring Boot đóng vai trò làm Proxy để xác thực quyền hạn và truy vấn CSDL (phim, lịch chiếu, combo, thống kê doanh thu) nhúng vào làm ngữ cảnh phong phú trước khi gửi lên AI.

### Câu 3: Ở Mục 5.2.1 và 5.2.2, em có liệt kê hai thư viện Backend là `RestTemplate` và `Jackson`. Hãy giải thích vai trò của chúng trong luồng xử lý của Chatbot?

- **Trả lời (Khớp báo cáo):**
  - `RestTemplate` (thuộc Spring Web) được sử dụng để khởi tạo cuộc gọi HTTP POST đồng bộ (synchronous) từ Server Spring Boot gửi lên API của Google Gemini.
  - `Jackson` chịu trách nhiệm ánh xạ và chuyển đổi dữ liệu tự động giữa định dạng JSON nhận từ Google API và các đối tượng Java (`Map`, `List`, `ArrayList`) trong mã nguồn Backend.

### Câu 4: Ở Mục 5.2.3, em nhắc đến thư viện `HttpClient` và thư viện xử lý bất đồng bộ `RxJS` ở Frontend. Em dùng chúng như thế nào trong component Chatbot?

- **Trả lời (Khớp báo cáo):**
  - `HttpClient` (Angular) dùng để gửi request HTTP POST tin nhắn người dùng tới API `/api/chatbot/ask` ở Backend dưới dạng bất đồng bộ (Observable).
  - `RxJS` (toán tử `finalize`) dùng để tự động tắt cờ `dangTai` (loading spinner) khi quá trình nhận phản hồi từ Backend hoàn tất hoặc gặp lỗi.

### Câu 5: Tại Mục 3.1, em có nêu vai trò của RabbitMQ là để "điều phối các tác vụ chạy ngầm". Tại sao gửi email xác nhận vé lại cần chạy ngầm qua RabbitMQ?

- **Trả lời (Khớp báo cáo):**
  - Tiến trình gửi email qua máy chủ SMTP bên ngoài thường mất từ 2-5 giây.
  - Nếu chạy đồng bộ, khách hàng sẽ bị treo màn hình chờ thanh toán.
  - Dùng RabbitMQ giúp xử lý bất đồng bộ: Hệ thống phản hồi đặt vé thành công cho người dùng ngay lập tức, còn việc gửi email sẽ do consumer chạy ngầm thực hiện độc lập, không gây nghẽn luồng chính.

### Câu 6: Ở Mục 3.1, báo cáo nêu thư viện vẽ biểu đồ được sử dụng là Chart.js & Ng2-charts. Hãy giải thích cách hoạt động của chúng trong phân hệ Admin?

- **Trả lời (Khớp báo cáo):**
  - **Ng2-charts** đóng vai trò là một directive wrapper của Angular bao bọc lấy thư viện vẽ biểu đồ **Chart.js**.
  - Angular Component nhận số liệu doanh thu từ Backend gửi lên, tự động cập nhật mảng dữ liệu đầu vào và trigger Chart.js vẽ lại biểu đồ dạng cột, đường hoặc tròn lên thẻ HTML `<canvas>`.

---

## 💾 PHẦN 2: CÂU HỎI VỀ LUỒNG NGHIỆP VỤ & KIỂM THỬ KHÁCH HÀNG (Mục 3.3.2)

### Câu 7: Trong Bảng 3.1 (TC_AUTH_02 và TC_AUTH_04), hệ thống xử lý thế nào khi người dùng đăng ký bằng email đã tồn tại hoặc email sai định dạng?

- **Trả lời (Khớp báo cáo):**
  - **Email đã tồn tại (TC_AUTH_02):** Backend kiểm tra database, nếu email đã được sử dụng sẽ từ chối đăng ký và trả về thông báo lỗi thích hợp.
  - **Sai định dạng email (TC_AUTH_04):** Hệ thống validate ngay tại Form đăng ký ở Frontend (bằng regex validator của Angular) và chặn không cho click nút đăng ký nếu thiếu ký tự `@`.

### Câu 8: Tại Bảng 3.2 (TC_LOGIN_03), hệ thống xử lý kịch bản "Đăng nhập sai mật khẩu quá số lần" như thế nào?

- **Trả lời (Khớp báo cáo):**
  - Khi người dùng cố tình nhập sai mật khẩu liên tiếp 5 lần, hệ thống sẽ ghi nhận số lần thất bại vào cột `failed_login_attempts` trong database.
  - Khi đạt giới hạn 5 lần, tài khoản sẽ tự động bị khóa tạm thời trong vòng 15 phút để phòng chống tấn công brute-force.

### Câu 9: Trong Bảng 3.2 (TC_LOGIN_04), cơ chế khôi phục mật khẩu tài khoản bằng cách gửi OTP/Link xác nhận hoạt động ra sao?

- **Trả lời (Khớp báo cáo):**
  - Người dùng nhập email yêu cầu khôi phục mật khẩu.
  - Backend kiểm tra email hợp lệ, tạo token khôi phục và bắn một tin nhắn vào RabbitMQ.
  - Consumer chạy ngầm của RabbitMQ tiếp nhận tin nhắn, thực hiện gửi email chứa link xác nhận hoặc mã OTP đổi mật khẩu tới email của khách hàng.

### Câu 10: Trong Bảng 3.3 (TC_MOVIE_01 và TC_MOVIE_03), bộ lọc danh sách phim và tra cứu suất chiếu hoạt động như thế nào?

- **Trả lời (Khớp báo cáo):**
  - **Tìm kiếm (TC_MOVIE_01):** Khách hàng nhập từ khóa tên phim, hệ thống lọc và trả về danh sách phim trùng khớp theo thời gian thực.
  - **Lọc suất chiếu (TC_MOVIE_03):** Người dùng chọn một ngày trên lịch chiếu, hệ thống lọc và chỉ hiển thị các suất chiếu hoạt động thuộc ngày đã chọn đó.

### Câu 11: Trong Bảng 3.3, testcase TC_MOVIE_04 có nhắc đến lỗi hiển thị ngày khởi chiếu "Date Pipe Error". Lỗi này cụ thể là gì và em đã xử lý thế nào?

- **Trả lời (Khớp báo cáo):**
  - Lỗi xảy ra do Angular Date Pipe cố gắng định dạng chuỗi ngày tháng (String) lấy từ Cache thay vì đối tượng ngày (Date) chuẩn, dẫn tới crash giao diện.
  - **Cách xử lý:** Em thực hiện phân tích cú pháp (parse) chuỗi ngày thành đối tượng `Date` thực tế (`new Date(dateString)`) trước khi đẩy ra view Angular để định dạng.

### Câu 12: Trong Bảng 3.4 (TC_BOOK_01 và TC_BOOK_02), hệ thống phản hồi thế nào khi người dùng chọn ghế trống và chọn ghế đã bị giữ/đã bán?

- **Trả lời (Khớp báo cáo):**
  - **Chọn ghế trống (TC_BOOK_01):** Ghế lập tức chuyển sang màu "Đang chọn", tổng tiền vé cập nhật tương ứng.
  - **Ghế đã giữ/bán (TC_BOOK_02):** Nút bấm chọn ghế đó trên sơ đồ phòng chiếu bị vô hiệu hóa (disabled), hiển thị cảnh báo chặn không cho thao tác.

### Câu 13: Quy tắc "Đặt vượt giới hạn số ghế quy định" (Bảng 3.4 - TC_BOOK_03) được cài đặt giới hạn tối đa bao nhiêu ghế và xử lý ra sao?

- **Trả lời (Khớp báo cáo):**
  - Hệ thống giới hạn mỗi khách hàng chỉ được chọn đặt tối đa **8 ghế** trong một giao dịch.
  - Nếu chọn đến ghế thứ 9, giao diện chat hoặc sơ đồ phòng chiếu sẽ hiện cảnh báo lỗi, nút chọn bị chặn không cho thao tác tiếp.

### Câu 14: Tại Bảng 3.4 (TC_BOOK_04), cơ chế xử lý kịch bản "Hết thời gian giữ ghế tạm thời" hoạt động như thế nào?

- **Trả lời (Khớp báo cáo):**
  - Khi khách hàng chọn ghế, hệ thống tạo key giữ ghế tạm trên Redis có thời hạn hết hạn tự động (TTL) là 5 phút.
  - Nếu khách hàng treo máy quá 5 phút mà không tiến hành thanh toán, Redis tự động giải phóng key đó, ghế được đưa về trạng thái trống cho người khác chọn.

### Câu 15: Trong Bảng 3.4 (TC_BOOK_05), kịch bản kiểm thử "Xử lý tranh chấp ghế (Race Condition)" hoạt động như thế nào khi hai tài khoản cùng click chọn một ghế tại một thời điểm?

- **Trả lời (Khớp báo cáo):**
  - Chỉ request gửi lên trước (dù chỉ lệch nhau một phần triệu giây) thực hiện lệnh `SETNX` thành công trên Redis được quyền giữ ghế.
  - Request gửi lên sau kiểm tra thấy key khóa ghế đã tồn tại trên Redis nên bị từ chối ngay lập tức và nhận cảnh báo ghế đã có người giữ.

### Câu 16: Trong Bảng 3.5 (TC_PAY_01), trường hợp khách hàng "Hủy giao dịch giữa chừng tại VNPay" thì hệ thống xử lý ra sao?

- **Trả lời (Khớp báo cáo):**
  - Cổng thanh toán VNPay sẽ chuyển hướng người dùng quay lại website kèm mã lỗi `24` (Khách hàng hủy giao dịch).
  - Backend tiếp nhận mã lỗi `24`, thực hiện hủy hóa đơn tương ứng và giải phóng (xóa key) các ghế đã khóa tạm thời trên Redis để người khác có thể đặt.

### Câu 17: Tại Bảng 3.5 (TC_PAY_03), quy trình xử lý khi VNPay thanh toán thành công NCB test Sandbox hoạt động ra sao?

- **Trả lời (Khớp báo cáo):**
  - Khi nhập đúng thông tin thẻ NCB test Sandbox và mã OTP, VNPay xác thực thành công và trả về mã phản hồi `00`.
  - Hệ thống xác minh chữ ký bảo mật, lưu hóa đơn xuống database, cập nhật ghế sang trạng thái "Đã bán" và xuất vé điện tử gửi qua email cho khách.

### Câu 18: Ở Bảng 3.6 (TC_FB_01 và TC_FB_02), hệ thống tính toán và hiển thị dịch vụ F&B (Combo bắp nước) đi kèm như thế nào?

- **Trả lời (Khớp báo cáo):**
  - **Tính tiền (TC_FB_01):** Khách hàng chọn bắp nước đi kèm, tổng tiền hóa đơn sẽ được tính chính xác bằng: `Giá vé phim + Giá combo F&B`.
  - **Hiển thị (TC_FB_02):** Trong lịch sử đặt vé, hóa đơn hiển thị bóc tách rõ ràng chi tiết số tiền vé và tiền combo bắp nước đi kèm.

### Câu 19: Tại Bảng 3.7 (TC_REV_01 và TC_REV_03), em ràng buộc quyền đánh giá phim của khách hàng như thế nào?

- **Trả lời (Khớp báo cáo):**
  - **Chưa đăng nhập (TC_REV_01):** Hệ thống chặn không cho nhận xét, hiển thị popup yêu cầu đăng nhập.
  - **Đánh giá trùng lặp (TC_REV_03):** Mỗi tài khoản đã đăng nhập chỉ được gửi nhận xét và chấm sao tối đa một lần cho mỗi bộ phim, các lần sau sẽ bị chặn gửi.

---

## ⚙️ PHẦN 3: CÂU HỎI VỀ KIỂM THỰ PHÂN HỆ ADMIN & AI CHATBOT (Mục 3.3.3 & Phần 5)

### Câu 20: Trong Bảng 3.8 (TC_ADM_02), làm thế nào hệ thống phát hiện và chặn việc "Thêm suất chiếu bị trùng lặp lịch"?

- **Trả lời (Khớp báo cáo):**
  - Khi Admin thêm suất chiếu mới, Backend thực hiện câu lệnh truy vấn SQL kiểm tra xem có suất chiếu nào khác trong cùng phòng chiếu có khoảng thời gian chiếu giao thoa (overlap) với khung giờ của suất chiếu mới hay không.
  - Nếu phát hiện trùng lịch, hệ thống chặn lưu và báo lịch chiếu bị xung đột.

### Câu 21: Tại Bảng 3.8 (TC_ADM_03), tại sao hệ thống chặn không cho Admin xóa một bộ phim đang có hóa đơn ràng buộc và trả về lỗi 400 BAD_REQUEST?

- **Trả lời (Khớp báo cáo):**
  - Bộ phim đã mở bán suất chiếu và có khách đặt vé sẽ vướng khóa ngoại ràng buộc trong SQL Server.
  - Hệ thống chặn xóa phim này nhằm bảo toàn dữ liệu lịch sử hóa đơn doanh thu rạp, ngăn lỗi mồ côi dữ liệu (Orphaned records).

### Câu 22: Trong Bảng 3.9 (TC_FIL_02), bộ lọc hóa đơn theo khoảng thời gian xử lý thế nào khi người dùng nhập ngày bắt đầu lớn hơn ngày kết thúc?

- **Trả lời (Khớp báo cáo):**
  - Hệ thống thực hiện validate logic khoảng thời gian ngay tại Backend.
  - Nếu ngày bắt đầu lớn hơn ngày kết thúc, hệ thống chặn gọi truy vấn DB và gửi thông báo lỗi khoảng ngày không hợp lệ.

### Câu 23: Tại Bảng 3.10 (TC_DSH_03), cơ chế bảo mật ngăn chặn tài khoản người dùng thường cố tình truy cập vào trang Admin Dashboard hoạt động thế nào?

- **Trả lời (Khớp báo cáo):**
  - Hệ thống kiểm tra quyền hạn (Role) lưu trong JWT Token gửi kèm request.
  - Nếu Token không có quyền `ROLE_ADMIN`, hệ thống từ chối truy cập và chuyển hướng về giao diện báo lỗi 403 Forbidden.

### Câu 24: Tại Mục 5.1.3, em nêu dữ liệu đầu vào của Chatbot gồm 3 lớp. Hãy giải thích vai trò của System Instruction (Câu lệnh hệ thống)?

- **Trả lời (Khớp báo cáo):**
  - System Instruction là câu lệnh cứng được cài đặt từ Backend nhằm định hình danh tính trợ lý ảo "CinemaTick AI", quy định ngôn ngữ phản hồi (tiếng Việt ngắn gọn, tự nhiên) và thiết lập hàng rào nghiệp vụ để từ chối các câu hỏi ngoài lề rạp phim.

### Câu 25: Cũng tại Mục 5.1.3, thành phần "Ngữ cảnh động (Dynamic Context)" cung cấp những thông tin gì cho mô hình AI đối với tài khoản của Khách hàng?

- **Trả lời (Khớp báo cáo):**
  - Đối với khách hàng, ngữ cảnh động trích xuất trực tiếp từ CSDL cung cấp cho AI: Danh sách phim đang chiếu/sắp chiếu, chi tiết lịch chiếu suất chiếu, danh sách combo bắp nước, lịch sử đặt vé cá nhân và điểm tích lũy thành viên hiện tại của khách hàng đó.

### Câu 26: Tại Mục 5.3.1, em thiết lập tham số nhiệt độ `temperature = 0.3` cho Chatbot. Hãy giải thích ý nghĩa của cấu hình này?

- **Trả lời (Khớp báo cáo):**
  - Thiết lập `temperature` ở mức thấp (0.3) nhằm khống chế tính sáng tạo tự do của mô hình ngôn ngữ lớn (LLM).
  - Điều này buộc Chatbot AI phản hồi ngắn gọn và bám sát hoàn toàn vào dữ liệu rạp chiếu phim thực tế được cung cấp từ CSDL, hạn chế tối đa hiện tượng "ảo giác" (AI tự phịa ra thông tin).

### Câu 27: Ở Mục 5.4, em trình bày về "Cơ chế giới hạn phạm vi nghiệp vụ (System Instruction Boundary)". Cơ chế này hoạt động thế nào trong kịch bản kiểm thử 2 (Mục 5.5.2) khi khách hỏi cách nấu thịt kho tàu hay viết code Java?

- **Trả lời (Khớp báo cáo):**
  - Dựa trên System Instruction cấm trả lời ngoài lề, AI sẽ từ chối khéo léo theo đúng kịch bản thiết lập: _"Em thành thật xin lỗi vì không thể hỗ trợ nội dung này. Em chỉ được trang bị kiến thức để hỗ trợ tra cứu thông tin phim, suất chiếu tại CinemaTick..."_, giúp bảo vệ tài nguyên hệ thống.

### Câu 28: Trong kịch bản kiểm thử 3 (Mục 5.5.3), em có mô tả năng lực xử lý chiến lược bán vé "Upsell" (Sneak Show) đối với phim Kungfu Panda 4. Chiến lược này hoạt động thế nào?

- **Trả lời (Khớp báo cáo):**
  - Khi phim ở trạng thái "Sắp chiếu" nhưng có lịch chiếu sớm (Sneak Show) vào cuối tuần.
  - Chatbot AI dựa vào ngữ cảnh này để tư vấn khách hàng đặt vé xem suất chiếu sớm luôn, giúp gia tăng doanh thu bán vé sớm cho rạp.

### Câu 29: Mã nguồn Client (Angular) xử lý thế nào tại file `chatbot.component.ts` (Mục 5.3.3) khi đường truyền mạng hoặc kết nối tới tổng đài API Gemini bị gián đoạn?

- **Trả lời (Khớp báo cáo):**
  - Hàm `guiTinNhan()` trong `chatbot.component.ts` sử dụng hàm `error` của luồng `subscribe` từ `HttpClient`.
  - Khi gặp sự cố kết nối, Angular tự động bắt ngoại lệ và hiển thị thông báo lỗi lên giao diện chat: _"Dạ, kết nối tới tổng đài AI bị gián đoạn."_ để khách hàng nắm thông tin.

### Câu 30: Ở Mục 5.3.1, mã nguồn Backend cấu hình thế nào để Chatbot không bị chết hoàn toàn khi kết nối API Gemini gặp sự cố (mất mạng hoặc hết hạn API Key)?

- **Trả lời (Khớp báo cáo):**
  - Backend Spring Boot bao bọc tiến trình gọi API trong khối `try-catch`.
  - Nếu API lỗi, hệ thống sẽ bắt ngoại lệ và kích hoạt **Cơ chế Rule-based Fallback ngoại tuyến**. Bộ xử lý này tự quét từ khóa trong câu hỏi (ví dụ: "lịch", "phim") để tự động truy xuất DB và phản hồi câu trả lời mẫu định sẵn.

### Câu 31: Tại sao tỷ lệ lấp đầy phòng chiếu trong Dashboard thống kê của Admin mới chỉ hiển thị tổng hợp chung cho cả phòng chiếu theo ngày? Có hướng phát triển nào để thống kê chi tiết theo từng khung giờ (Time slots) không?

- **Trả lời (Định hướng phản biện chuyên nghiệp):**
  - **Hiện tại (Khớp báo cáo):** Hệ thống đang thống kê tỷ lệ lấp đầy bằng cách lấy `Tổng số vé đã bán / Tổng sức chứa của tất cả suất chiếu` thuộc phòng chiếu đó trong khoảng thời gian được lọc, nhằm cho Admin cái nhìn tổng quan nhất về hiệu quả sử dụng ghế trung bình của từng phòng.
  - **Hướng phát triển theo khung giờ:** Vì mỗi suất chiếu (`SuatChieu`) đều lưu giờ chiếu cụ thể (`gioChieu`), hướng nâng cấp tiếp theo sẽ là phân loại (Group By) các suất chiếu theo 4 khung giờ: _Sáng (8h-12h), Chiều (12h-17h), Giờ vàng (17h-22h), và Khuya (22h-2h)_ để tính tỷ lệ lấp đầy riêng biệt cho từng khung giờ. Việc này giúp ban quản trị nhận diện khung giờ cao điểm để áp dụng chính sách tăng giá vé giờ vàng hoặc giảm giá kích cầu giờ thấp điểm một cách linh hoạt.
