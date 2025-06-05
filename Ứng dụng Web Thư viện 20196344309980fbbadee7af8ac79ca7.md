# Ứng dụng Web Thư viện

## Tài liệu Phân tích Nghiệp vụ: Ứng dụng Web Thư viện

### 1. Giới thiệu

### 1.1. Mục đích

Tài liệu này mô tả các yêu cầu nghiệp vụ và chức năng cho ứng dụng web quản lý thư viện đa năng. Ứng dụng cho phép người dùng tìm kiếm, mượn, mua sách trực tuyến, xem tài liệu đính kèm và giúp thủ thư quản lý kho sách, tài liệu, người dùng, đơn hàng một cách hiệu quả.

### 1.2. Phạm vi

- **Trong phạm vi:**
    - Quản lý sách (thêm, sửa, xóa, tìm kiếm thông tin chi tiết, bao gồm cả thông tin cho mượn và bán).
    - Quản lý người dùng (đăng ký, đăng nhập, quản lý thông tin cá nhân, phân quyền).
    - Quản lý mượn/trả sách (tạo phiếu mượn, ghi nhận trả sách, theo dõi tình trạng mượn, quản lý số lượng cho mượn).
    - Chức năng mua sách:
        - Hiển thị sách có bán và giá.
        - Quản lý giỏ hàng (thêm, sửa, xóa sản phẩm).
        - Quy trình đặt hàng và thanh toán (có thể tích hợp cổng thanh toán bên thứ ba).
        - Quản lý đơn hàng cho cả người dùng và thủ thư.
        - Quản lý số lượng tồn kho cho sách bán.
    - Quản lý và xem tài liệu trực tuyến:
        - Thủ thư tải lên các tệp tài liệu (ví dụ: PDF, DOCX, EPUB) lên hệ thống lưu trữ đối tượng MinIO.
        - Quản lý metadata cho tài liệu (tên, mô tả, liên kết với sách nếu có).
        - Người dùng tìm kiếm và xem nội dung tài liệu trực tuyến trong trình duyệt mà không cần tải về.
        - Kiểm soát truy cập tài liệu.
    - Tìm kiếm và lọc sách/tài liệu theo nhiều tiêu chí (tên, tác giả, thể loại, ISBN, v.v.).
    - Phân quyền người dùng (người dùng thường/độc giả, thủ thư/quản trị viên).
- **Ngoài phạm vi (có thể phát triển trong tương lai):**
    - Chức năng đặt trước sách (pre-order cho sách sắp phát hành hoặc sách đang được mượn).
    - Hệ thống gợi ý sách/tài liệu dựa trên hành vi người dùng.
    - Thông báo nâng cao qua email/SMS (nhắc lịch trả sách, xác nhận đơn hàng, thông báo sách mới).
    - Chức năng đánh giá, bình luận sách/tài liệu.
    - Quản lý kho vận chi tiết cho việc giao sách đã mua.
    - Các chương trình khuyến mãi, mã giảm giá.

### 1.3. Đối tượng sử dụng

- **Người dùng (Độc giả/Khách hàng):** Những người muốn tìm kiếm, xem thông tin, mượn sách, mua sách và xem các tài liệu trực tuyến.
- **Thủ thư/Quản trị viên (Admin):** Những người chịu trách nhiệm quản lý kho sách (cho mượn và bán), quản lý tài liệu số, quản lý người dùng, quản lý các hoạt động mượn/trả sách, và xử lý đơn hàng.

### 2. Yêu cầu nghiệp vụ

### 2.1. Yêu cầu chức năng

- **UC1: Quản lý Sách**
    - **UC1.1:** Thủ thư có thể thêm sách mới vào hệ thống với các thông tin: tên sách, tác giả, nhà xuất bản, năm xuất bản, thể loại, ISBN, mô tả, ảnh bìa, số lượng bản cho mượn, số lượng bản để bán, giá bán (nếu có), liên kết tài liệu (nếu có).
    - **UC1.2:** Thủ thư có thể cập nhật thông tin sách (bao gồm cả số lượng và giá cả).
    - **UC1.3:** Thủ thư có thể xóa sách khỏi hệ thống (nếu sách không đang được mượn và không có trong đơn hàng đang xử lý).
    - **UC1.4:** Người dùng và Thủ thư có thể tìm kiếm sách theo tên sách, tác giả, thể loại, ISBN.
    - **UC1.5:** Người dùng và Thủ thư có thể xem chi tiết thông tin sách, bao gồm tình trạng sẵn có cho mượn và/hoặc để bán.
    - **UC1.6:** Hệ thống hiển thị số lượng sách còn lại có sẵn để mượn và số lượng tồn kho để bán.
- **UC2: Quản lý Người dùng**
    - **UC2.1:** Người dùng có thể đăng ký tài khoản mới (cung cấp tên, email, mật khẩu, thông tin liên hệ).
    - **UC2.2:** Người dùng có thể đăng nhập vào hệ thống.
    - **UC2.3:** Người dùng có thể xem và cập nhật thông tin cá nhân.
    - **UC2.4:** Người dùng có thể xem lịch sử mượn sách và lịch sử mua hàng của mình.
    - **UC2.5:** Thủ thư có thể xem danh sách người dùng.
    - **UC2.6:** Thủ thư có thể kích hoạt/vô hiệu hóa tài khoản người dùng.
- **UC3: Quản lý Mượn/Trả sách**
    - **UC3.1:** Người dùng (đã đăng nhập) có thể yêu cầu mượn sách (nếu sách còn bản cho mượn và người dùng không vi phạm quy định).
    - **UC3.2:** Thủ thư xác nhận yêu cầu mượn sách và tạo phiếu mượn (ghi nhận ngày mượn, ngày dự kiến trả).
    - **UC3.3:** Thủ thư ghi nhận khi người dùng trả sách (cập nhật trạng thái phiếu mượn, cập nhật số lượng sách cho mượn).
    - **UC3.4:** Hệ thống tự động tính toán phí phạt nếu trả sách muộn (nếu có quy định).
    - **UC3.5:** Thủ thư có thể xem danh sách các sách đang được mượn, sách quá hạn.
    - **UC3.6:** Hệ thống giảm số lượng sách có sẵn cho mượn khi sách được mượn và tăng lại khi sách được trả.
- **UC4: Phân quyền**
    - **UC4.1:** Hệ thống có hai vai trò chính: Người dùng (USER) và Thủ thư/Quản trị viên (LIBRARIAN/ADMIN).
    - **UC4.2:** Chức năng quản lý sách, quản lý tài liệu, quản lý người dùng, xác nhận mượn/trả, quản lý đơn hàng chỉ dành cho Thủ thư.
    - **UC4.3:** Chức năng tìm kiếm, xem thông tin sách/tài liệu, yêu cầu mượn, mua sách dành cho Người dùng.
- **UC5: Quản lý Mua Sách**
    - **UC5.1:** Người dùng (đã đăng nhập) có thể thêm sách có bán vào giỏ hàng.
    - **UC5.2:** Người dùng có thể xem giỏ hàng, thay đổi số lượng hoặc xóa sách khỏi giỏ.
    - **UC5.3:** Người dùng có thể tiến hành thanh toán cho các sách trong giỏ hàng (bao gồm nhập thông tin giao hàng nếu cần, chọn phương thức thanh toán).
    - **UC5.4:** Hệ thống ghi nhận đơn hàng sau khi thanh toán thành công (hoặc chờ xử lý tùy phương thức) và gửi thông báo xác nhận cho người dùng.
    - **UC5.5:** Người dùng có thể xem lịch sử đơn hàng và trạng thái của từng đơn hàng.
    - **UC5.6:** Thủ thư có thể xem và quản lý danh sách đơn hàng (ví dụ: xác nhận đơn, cập nhật trạng thái thanh toán, trạng thái giao hàng).
    - **UC5.7:** Hệ thống cập nhật số lượng tồn kho bán của sách sau khi đơn hàng được xác nhận/hoàn tất.
- **UC6: Quản lý và Xem Tài liệu Trực tuyến**
    - **UC6.1:** Thủ thư có thể tải lên các tệp tài liệu (PDF, DOCX, EPUB, v.v.) lên hệ thống (lưu trữ trên MinIO).
    - **UC6.2:** Thủ thư có thể nhập thông tin metadata cho tài liệu (tên, mô tả, thể loại, tác giả tài liệu, liên kết với một đầu sách cụ thể nếu có, mức độ truy cập).
    - **UC6.3:** Thủ thư có thể cập nhật thông tin metadata hoặc thay thế tệp tài liệu.
    - **UC6.4:** Thủ thư có thể xóa tài liệu (xóa cả metadata và tệp trên MinIO).
    - **UC6.5:** Người dùng và Thủ thư có thể tìm kiếm tài liệu theo tên, thể loại, hoặc từ khóa trong mô tả.
    - **UC6.6:** Người dùng (tùy theo quyền truy cập của tài liệu) có thể xem nội dung tài liệu trực tuyến trong trình duyệt mà không cần tải về.
    - **UC6.7:** Hệ thống đảm bảo quyền truy cập phù hợp đối với các tài liệu (ví dụ: công khai, chỉ người dùng đăng nhập, chỉ người đã mua/mượn sách liên quan, hoặc chỉ người dùng đã mua tài liệu đó nếu tài liệu được bán riêng).

### 2.2. Yêu cầu phi chức năng

- **PN1: Hiệu năng:**
    - Thời gian phản hồi của hệ thống cho các thao tác tìm kiếm, xem chi tiết sách/tài liệu phải dưới 2-3 giây.
    - Thời gian tải và hiển thị trang đầu của tài liệu trực tuyến phải hợp lý (ví dụ, dưới 5 giây cho tài liệu kích thước trung bình).
    - Hệ thống có khả năng phục vụ đồng thời ít nhất 100-200 người dùng hoạt động.
    - Quá trình thanh toán phải nhanh chóng và đáng tin cậy.
- **PN2: Bảo mật:**
    - Mật khẩu người dùng phải được mã hóa bằng thuật toán mạnh (ví dụ: bcrypt).
    - Chống các tấn công web phổ biến (XSS, SQL Injection, CSRF).
    - Phân quyền truy cập chặt chẽ dựa trên vai trò người dùng.
    - Bảo mật giao dịch thanh toán, tuân thủ các tiêu chuẩn nếu xử lý thông tin thẻ (ưu tiên tích hợp cổng thanh toán tuân thủ PCI DSS).
    - Kiểm soát truy cập an toàn tới các tài liệu trên MinIO (ví dụ: sử dụng pre-signed URLs, không lộ thông tin credentials của MinIO).
    - Sử dụng HTTPS cho toàn bộ ứng dụng.
- **PN3: Tính khả dụng:**
    - Giao diện người dùng thân thiện, dễ sử dụng, responsive trên các trình duyệt web phổ biến (Chrome, Firefox, Safari, Edge) và các thiết bị (desktop, tablet, mobile).
    - Hệ thống phải hoạt động ổn định 24/7 (ngoại trừ thời gian bảo trì theo kế hoạch, cần thông báo trước).
- **PN4: Khả năng bảo trì:**
    - Mã nguồn được tổ chức rõ ràng, có cấu trúc, dễ hiểu, có tài liệu (comments, Javadoc).
    - Dễ dàng cập nhật, sửa lỗi và mở rộng chức năng trong tương lai.
    - Có hệ thống logging đầy đủ để theo dõi và chẩn đoán lỗi.
- **PN5: Khả năng mở rộng:**
    - Hệ thống có thể dễ dàng mở rộng để xử lý lượng dữ liệu (sách, tài liệu, người dùng, đơn hàng) và lượng truy cập người dùng lớn hơn trong tương lai (cả theo chiều dọc và chiều ngang).
- **PN6: Tích hợp:**
    - Hệ thống có khả năng tích hợp mượt mà với cổng thanh toán đã chọn.
    - Hệ thống tích hợp chặt chẽ và hiệu quả với dịch vụ lưu trữ đối tượng MinIO.

### 3. Quy tắc nghiệp vụ

- **BR1:** Mỗi người dùng chỉ được mượn tối đa X cuốn sách cùng một lúc (ví dụ: 3-5 cuốn), tùy thuộc vào loại tài khoản hoặc chính sách thư viện.
- **BR2:** Thời gian mượn tối đa cho mỗi cuốn sách là Y ngày (ví dụ: 7-30 ngày).
- **BR3:** Nếu trả sách muộn, người dùng có thể bị tính phí Z đồng/ngày/cuốn hoặc tạm khóa chức năng mượn sách.
- **BR4:** Sách chỉ có thể được xóa nếu không có ai đang mượn và không nằm trong các đơn hàng chưa hoàn thành.
- **BR5:** Thông tin ISBN là duy nhất cho mỗi đầu sách.
- **BR6:** Email đăng ký tài khoản là duy nhất.
- **BR7:** Một cuốn sách có thể đồng thời vừa cho mượn vừa để bán, hoặc chỉ một trong hai. Số lượng cho mượn và số lượng để bán được quản lý riêng.
- **BR8:** Giá bán của sách do Thủ thư thiết lập và có thể được cập nhật.
- **BR9:** Đơn hàng chỉ được coi là hợp lệ và được xử lý sau khi thanh toán được xác nhận (trừ trường hợp thanh toán khi nhận hàng - COD, nếu được hỗ trợ).
- **BR10:** Tài liệu có thể có các mức truy cập khác nhau: công khai, yêu cầu đăng nhập, chỉ dành cho người đã mượn/mua sách liên quan, hoặc bán riêng lẻ.
- **BR11:** Số lượng sách tồn kho để bán sẽ giảm khi đơn hàng được xác nhận/giao hàng thành công.
- **BR12:** Người dùng không thể đặt mua số lượng sách vượt quá số lượng tồn kho hiện có để bán.
- **BR13:** Pre-signed URL cho tài liệu trên MinIO phải có thời gian hết hạn ngắn để đảm bảo an toàn.

### 4. Mô hình hóa (Gợi ý)

- **Sơ đồ Use Case tổng quát:** Mô tả trực quan các tương tác chính của các Actor (Người dùng, Thủ thư) với các chức năng chính của hệ thống (Quản lý Sách, Quản lý Mượn/Trả, Quản lý Mua Sách, Quản lý Tài liệu, Quản lý Người dùng).
- **Mô hình dữ liệu khái niệm (Conceptual Data Model):** Xác định các thực thể chính (Sách, Người Dùng, Phiếu Mượn, Đơn Hàng, Chi Tiết Đơn Hàng, Tài Liệu, Tác Giả, Thể Loại, Nhà Xuất Bản) và các mối quan hệ cơ bản giữa chúng.
- **Sơ đồ luồng quy trình nghiệp vụ (Business Process Flow Diagram):** Mô tả các bước trong các quy trình quan trọng như quy trình mượn sách, quy trình mua sách, quy trình tải lên và xem tài liệu.

---

## Tài liệu Phân tích Thiết kế Hệ thống: Ứng dụng Web Thư viện (Phiên bản đầy đủ)

### 1. Giới thiệu

### 1.1. Mục đích

Tài liệu này mô tả kiến trúc và thiết kế chi tiết của ứng dụng web thư viện đa năng, bao gồm cả Frontend và Backend (sử dụng Java Spring Boot), tích hợp chức năng mua sách và lưu trữ/xem tài liệu trực tuyến từ MinIO.

### 1.2. Công nghệ sử dụng

- **Backend:**
    - Ngôn ngữ: Java (phiên bản 11, 17 hoặc mới hơn)
    - Framework: Spring Boot (bao gồm Spring MVC, Spring Data JPA, Spring Security, Spring WebFlux nếu cần cho các tác vụ bất đồng bộ)
    - Cơ sở dữ liệu: PostgreSQL (ưu tiên) hoặc MySQL. H2 cho môi trường phát triển và kiểm thử.
    - Build tool: Maven hoặc Gradle
    - API: RESTful APIs (sử dụng JSON)
    - Thư viện MinIO Java SDK: Để tương tác với MinIO server.
    - Thư viện tích hợp cổng thanh toán: Ví dụ Stripe SDK, PayPal SDK, hoặc SDK của các cổng thanh toán nội địa (VNPay, MoMo).
    - ORM: Hibernate (thông qua Spring Data JPA).
    - Messaging Queue (Tùy chọn, cho khả năng mở rộng): RabbitMQ/Kafka cho các tác vụ bất đồng bộ như gửi email thông báo, xử lý đơn hàng phức tạp.
- **Frontend:**
    - Ngôn ngữ: JavaScript/TypeScript
    - Framework/Thư viện: React.js (khuyến nghị) hoặc Angular, Vue.js.
    - Quản lý state: Redux Toolkit, Zustand, hoặc Context API (cho React).
    - Styling: CSS Modules, Styled Components, Tailwind CSS, hoặc thư viện UI như Material UI, Ant Design.
    - Build tool: npm/yarn, Webpack/Vite.
    - Thư viện xem tài liệu:
        - PDF: PDF.js, React-PDF.
        - DOCX/EPUB: Có thể sử dụng `<iframe>` với Google Docs Viewer (cho các định dạng phổ biến, có giới hạn), hoặc các thư viện thương mại (PSPDFKit, Apryse) nếu cần trải nghiệm xem và chú thích nâng cao.
- **Lưu trữ Đối tượng (Object Storage):**
    - MinIO Server: Cài đặt tự host hoặc sử dụng dịch vụ tương thích S3.
- **Khác:**
    - Version Control: Git, GitHub/GitLab/Bitbucket.
    - Containerization: Docker, Docker Compose.
    - CI/CD: Jenkins, GitLab CI, GitHub Actions.
    - Web Server/Reverse Proxy: Nginx hoặc Apache HTTP Server.
    - Caching: Redis hoặc Ehcache (cho cả backend và có thể frontend).

### 2. Kiến trúc hệ thống

### 2.1. Kiến trúc tổng quan

Ứng dụng theo kiến trúc Client-Server, mở rộng với các dịch vụ bên ngoài:

- **Client (Frontend):** Giao diện người dùng trên trình duyệt web, tương tác với người dùng và gửi yêu cầu đến Backend API.
- **Server (Backend - Java Spring Boot):**
    - Xử lý logic nghiệp vụ chính.
    - Quản lý CSDL (PostgreSQL).
    - Tương tác với MinIO để lưu trữ và truy xuất tài liệu.
    - Tích hợp với Cổng thanh toán để xử lý giao dịch mua sách.
    - Cung cấp RESTful APIs cho Frontend.
- **MinIO Service:** Dịch vụ lưu trữ đối tượng, nơi các tệp tài liệu được lưu trữ.
- **Payment Gateway Service:** Dịch vụ của bên thứ ba xử lý các giao dịch thanh toán.

*(Lưu ý: Đây là hình ảnh minh họa, sơ đồ thực tế có thể chi tiết hơn)*

### 2.2. Kiến trúc Backend (Java Spring Boot)

Sử dụng kiến trúc phân lớp (Layered Architecture) kết hợp với các patterns phù hợp:

- **Controller Layer (API Layer):** (`@RestController`)
    - Tiếp nhận HTTP request từ client, xác thực đầu vào cơ bản.
    - Gọi các service tương ứng để xử lý.
    - Chuyển đổi DTO (Data Transfer Objects) và trả về HTTP response (JSON).
    - Ví dụ: `BookController`, `UserController`, `LoanController`, `OrderController`, `DocumentController`, `PaymentWebhookController`.
- **Service Layer (Business Logic Layer):** (`@Service`)
    - Chứa logic nghiệp vụ chính, phức tạp của ứng dụng.
    - Điều phối các tương tác giữa Repository và các service khác (nếu có).
    - Quản lý transaction.
    - Ví dụ: `BookService`, `UserService`, `LoanService`, `OrderService` (bao gồm logic giỏ hàng), `PaymentService` (tương tác cổng thanh toán), `MinioService` (tương tác MinIO), `DocumentService`.
- **Repository Layer (Data Access Layer):** (`@Repository`)
    - Sử dụng Spring Data JPA để định nghĩa các interface mở rộng từ `JpaRepository` hoặc `CrudRepository`.
    - Chịu trách nhiệm tương tác trực tiếp với cơ sở dữ liệu (thực hiện các thao tác CRUD và các query phức tạp hơn nếu cần).
    - Ví dụ: `BookRepository`, `UserRepository`, `LoanRepository`, `OrderRepository`, `OrderItemRepository`, `DocumentRepository`.
- **Domain/Entity Layer:** (`@Entity`)
    - Định nghĩa các đối tượng Java (POJO) được ánh xạ với các bảng trong cơ sở dữ liệu bằng JPA annotations.
    - Ví dụ: `Book`, `User`, `Role`, `Loan`, `Order`, `OrderItem`, `Document`, `Category`, `Author`, `Publisher`.
- **DTO (Data Transfer Object) Layer:** Các POJO đơn giản dùng để truyền dữ liệu giữa các lớp, đặc biệt là giữa Controller và Service, và trong API responses/requests. Giúp tách biệt API contract với domain model.
- **Configuration Layer:** (`@Configuration`)
    - Cấu hình Spring Security, CORS, MinIO client, DataSource, Swagger/OpenAPI, v.v.
- **Security Component:** (Sử dụng Spring Security)
    - Xử lý Authentication (JWT hoặc Session-based) và Authorization (dựa trên role, permission).
- **Utils/Helpers:** Các lớp tiện ích dùng chung (ví dụ: xử lý ngày tháng, chuỗi, tạo slug).

### 2.3. Kiến trúc Frontend

Sử dụng kiến trúc dựa trên Component (Component-Based Architecture):

- **Components (Thành phần):** Các khối giao diện người dùng độc lập, có thể tái sử dụng.
    - **UI Components (Presentational):** Chỉ chịu trách nhiệm hiển thị dữ liệu và giao diện (`Button`, `Card`, `Input`, `Modal`).
    - **Container Components (Smart):** Chứa logic, quản lý state và tương tác với services để lấy/gửi dữ liệu, sau đó truyền xuống cho UI components.
    - Ví dụ: `BookCard`, `SearchBar`, `LoginForm`, `BookList`, `ShoppingCartIcon`, `CartItem`, `CheckoutForm`, `OrderItemCard`, `DocumentViewerWrapper`, `DocumentListItem`.
- **Pages/Views (Trang):** Tập hợp các component để tạo thành các trang hoàn chỉnh, tương ứng với các route của ứng dụng.
    - Ví dụ: `HomePage`, `BookDetailPage`, `LoginPage`, `RegisterPage`, `AdminDashboardPage`, `ShoppingCartPage`, `CheckoutPage`, `OrderHistoryPage`, `DocumentListingPage`, `DocumentViewPage`.
- **Services/API Clients (Dịch vụ):** Các module chịu trách nhiệm thực hiện các lời gọi API đến Backend (sử dụng `fetch` hoặc `axios`).
    - Ví dụ: `authService`, `bookService`, `cartService`, `orderService`, `documentService`.
- **State Management (Quản lý trạng thái):**
    - **Global State:** Sử dụng Redux Toolkit, Zustand, hoặc Context API + useReducer cho các trạng thái dùng chung toàn ứng dụng (thông tin người dùng đăng nhập, giỏ hàng, cài đặt UI).
    - **Local State:** Sử dụng `useState`, `useReducer` cho trạng thái riêng của từng component.
- **Routing (Điều hướng):** Sử dụng thư viện routing của framework (ví dụ: `react-router-dom` cho React) để quản lý việc điều hướng giữa các trang.
- **Layouts:** Các component khung chính cho các nhóm trang có bố cục tương tự (ví dụ: `MainLayout`, `AdminLayout`).
- **Assets:** Hình ảnh, fonts, file CSS tĩnh.

### 3. Thiết kế cơ sở dữ liệu (PostgreSQL)

### 3.1. Sơ đồ quan hệ thực thể (ERD) - Chi tiết

- **Bảng `roles` (Vai trò)**
    - `id` (SERIAL, PK)
    - `name` (VARCHAR(20), Unique, Not Null) - Ví dụ: 'ROLE_USER', 'ROLE_LIBRARIAN', 'ROLE_ADMIN'
- **Bảng `users` (Người dùng)**
    - `id` (BIGSERIAL, PK)
    - `username` (VARCHAR(50), Unique, Not Null)
    - `password` (VARCHAR(255), Not Null) - Lưu trữ dạng mã hóa (bcrypt)
    - `email` (VARCHAR(100), Unique, Not Null)
    - `full_name` (VARCHAR(100))
    - `phone_number` (VARCHAR(20))
    - `address` (TEXT)
    - `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `is_active` (BOOLEAN, Default: true)
- **Bảng `user_roles` (Phân quyền người dùng)**
    - `user_id` (BIGINT, PK, FK references `users(id)`)
    - `role_id` (INT, PK, FK references `roles(id)`)
- **Bảng `categories` (Thể loại sách/tài liệu)**
    - `id` (SERIAL, PK)
    - `name` (VARCHAR(100), Unique, Not Null)
    - `description` (TEXT)
    - `parent_category_id` (INT, FK references `categories(id)`, Nullable) - Cho cấu trúc đa cấp
- **Bảng `authors` (Tác giả)**
    - `id` (SERIAL, PK)
    - `name` (VARCHAR(100), Not Null)
    - `biography` (TEXT)
- **Bảng `publishers` (Nhà xuất bản)**
    - `id` (SERIAL, PK)
    - `name` (VARCHAR(100), Not Null)
    - `address` (TEXT)
    - `contact_info` (VARCHAR(255))
- **Bảng `books` (Sách)**
    - `id` (BIGSERIAL, PK)
    - `title` (VARCHAR(255), Not Null)
    - `isbn` (VARCHAR(20), Unique, Not Null)
    - `publication_year` (INT)
    - `description` (TEXT)
    - `cover_image_url` (VARCHAR(255))
    - `language` (VARCHAR(50))
    - `number_of_pages` (INT)
    - `total_copies_for_loan` (INT, Not Null, Default: 0)
    - `available_copies_for_loan` (INT, Not Null, Default: 0)
    - `price` (DECIMAL(12,2), Nullable) - Giá bán
    - `stock_for_sale` (INT, Default: 0) - Số lượng tồn kho để bán
    - `category_id` (INT, FK references `categories(id)`)
    - `publisher_id` (INT, FK references `publishers(id)`)
    - `edition` (VARCHAR(50))
    - `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `is_sellable` (BOOLEAN, Default: false)
    - `is_lendable` (BOOLEAN, Default: true)
- **Bảng `book_authors` (Liên kết Sách - Tác giả - Mối quan hệ nhiều-nhiều)**
    - `book_id` (BIGINT, PK, FK references `books(id)`)
    - `author_id` (INT, PK, FK references `authors(id)`)
- **Bảng `loans` (Phiếu mượn)**
    - `id` (BIGSERIAL, PK)
    - `user_id` (BIGINT, Not Null, FK references `users(id)`)
    - `book_id` (BIGINT, Not Null, FK references `books(id)`)
    - `loan_date` (TIMESTAMP, Not Null, Default: CURRENT_TIMESTAMP) - Ngày mượn
    - `due_date` (TIMESTAMP, Not Null) - Ngày dự kiến trả
    - `return_date` (TIMESTAMP, Nullable) - Ngày trả thực tế
    - `status` (VARCHAR(20), Not Null) - Ví dụ: 'REQUESTED', 'BORROWED', 'RETURNED', 'OVERDUE', 'CANCELLED'
    - `notes_by_librarian` (TEXT) - Ghi chú của thủ thư
    - `fine_amount` (DECIMAL(10,2), Default: 0) - Tiền phạt (nếu có)
    - `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
- **Bảng `documents` (Tài liệu số)**
    - `id` (BIGSERIAL, PK)
    - `title` (VARCHAR(255), Not Null)
    - `description` (TEXT)
    - `file_name_original` (VARCHAR(255), Not Null) - Tên file gốc khi tải lên
    - `minio_object_name` (VARCHAR(255), Not Null, Unique) - Tên đối tượng (key) trong MinIO (ví dụ: UUID)
    - `file_type` (VARCHAR(100)) - MIME type
    - `size_in_bytes` (BIGINT)
    - `book_id` (BIGINT, FK references `books(id)`, Nullable) - Liên kết với sách nếu là tài liệu đính kèm
    - `uploader_id` (BIGINT, FK references `users(id)`) - Người tải lên (thường là Thủ thư)
    - `access_level` (VARCHAR(20), Default: 'PUBLIC') - Ví dụ: 'PUBLIC', 'LOGGED_IN_USER', 'RESTRICTED_BY_BOOK_OWNERSHIP'
    - `version` (INT, Default: 1)
    - `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
- **Bảng `orders` (Đơn hàng)**
    - `id` (BIGSERIAL, PK)
    - `user_id` (BIGINT, Not Null, FK references `users(id)`)
    - `order_code` (VARCHAR(50), Unique, Not Null) - Mã đơn hàng tự sinh
    - `order_date` (TIMESTAMP, Not Null, Default: CURRENT_TIMESTAMP)
    - `sub_total_amount` (DECIMAL(12,2), Not Null) - Tổng tiền hàng
    - `shipping_fee` (DECIMAL(10,2), Default: 0)
    - `discount_amount` (DECIMAL(10,2), Default: 0)
    - `total_amount` (DECIMAL(12,2), Not Null) - Tổng tiền cuối cùng
    - `status` (VARCHAR(50), Not Null) - Ví dụ: 'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    - `shipping_address_line1` (VARCHAR(255))
    - `shipping_address_line2` (VARCHAR(255))
    - `shipping_city` (VARCHAR(100))
    - `shipping_postal_code` (VARCHAR(20))
    - `shipping_country` (VARCHAR(50))
    - `customer_note` (TEXT)
    - `payment_method` (VARCHAR(50))
    - `payment_status` (VARCHAR(30), Default: 'UNPAID')
    - `payment_transaction_id` (VARCHAR(100), Nullable) - ID giao dịch từ cổng thanh toán
    - `created_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
    - `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP)
- **Bảng `order_items` (Chi tiết đơn hàng)**
    - `id` (BIGSERIAL, PK)
    - `order_id` (BIGINT, Not Null, FK references `orders(id)` ON DELETE CASCADE)
    - `book_id` (BIGINT, Not Null, FK references `books(id)`)
    - `quantity` (INT, Not Null, CHECK (quantity > 0))
    - `price_per_unit` (DECIMAL(12,2), Not Null) - Giá tại thời điểm mua
    - `item_total_price` (DECIMAL(12,2), Not Null) - (quantity * price_per_unit)

### 4. Thiết kế API (RESTful)

### 4.1. Nguyên tắc chung

- Sử dụng các phương thức HTTP chuẩn: GET, POST, PUT, DELETE, PATCH.
- Sử dụng JSON làm định dạng dữ liệu trao đổi chính.
- Sử dụng mã trạng thái HTTP để biểu thị kết quả của yêu cầu (200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error).
- Thiết kế URL thân thiện, dễ hiểu, dựa trên tài nguyên.
- Sử dụng versioning cho API (ví dụ: `/api/v1/...`).
- Sử dụng DTOs (Data Transfer Objects) cho request bodies và response payloads.
- Triển khai HATEOAS (Hypermedia as the Engine of Application State) nếu cần thiết để API dễ khám phá hơn.
- Sử dụng phân trang (pagination), sắp xếp (sorting), và lọc (filtering) cho các API trả về danh sách.

### 4.2. Danh sách Endpoint (Ví dụ chi tiết hơn)

- **Authentication & Users (`/api/v1/auth`, `/api/v1/users`)**
    - `POST /api/v1/auth/register`: Đăng ký người dùng mới.
    - `POST /api/v1/auth/login`: Đăng nhập, trả về JWT.
    - `POST /api/v1/auth/refresh-token`: Làm mới JWT.
    - `GET /api/v1/users/me`: Lấy thông tin người dùng đang đăng nhập.
    - `PUT /api/v1/users/me`: Cập nhật thông tin người dùng đang đăng nhập.
    - `PUT /api/v1/users/me/change-password`: Đổi mật khẩu.
    - (Admin) `GET /api/v1/admin/users`: Lấy danh sách người dùng (phân trang, lọc).
    - (Admin) `GET /api/v1/admin/users/{userId}`: Lấy chi tiết người dùng.
    - (Admin) `PUT /api/v1/admin/users/{userId}`: Cập nhật thông tin người dùng (Admin).
    - (Admin) `PUT /api/v1/admin/users/{userId}/status`: Kích hoạt/Vô hiệu hóa tài khoản người dùng.
- **Books (`/api/v1/books`, `/api/v1/admin/books`)**
    - `GET /api/v1/books`: Lấy danh sách sách (phân trang, lọc theo thể loại, tác giả, tên, ISBN, trạng thái bán/cho mượn, sắp xếp).
    - `GET /api/v1/books/{bookIdOrIsbn}`: Lấy chi tiết sách theo ID hoặc ISBN.
    - (Admin) `POST /api/v1/admin/books`: Thêm sách mới.
    - (Admin) `PUT /api/v1/admin/books/{bookId}`: Cập nhật thông tin sách.
    - (Admin) `DELETE /api/v1/admin/books/{bookId}`: Xóa sách.
- **Categories, Authors, Publishers (Tương tự, ví dụ `/api/v1/categories`)**
    - `GET /api/v1/categories`: Lấy danh sách thể loại.
    - `GET /api/v1/categories/{categoryId}`: Lấy chi tiết thể loại.
    - (Admin) `POST /api/v1/admin/categories`: Thêm thể loại mới.
    - (Admin) `PUT /api/v1/admin/categories/{categoryId}`: Cập nhật thể loại.
    - (Admin) `DELETE /api/v1/admin/categories/{categoryId}`: Xóa thể loại.
- **Loans (Mượn sách - `/api/v1/loans`, `/api/v1/admin/loans`)**
    - `POST /api/v1/loans/request`: Người dùng yêu cầu mượn sách. Request body: `{ "bookId": ... }`.
    - `GET /api/v1/loans/my-history`: Người dùng xem lịch sử mượn của mình (phân trang).
    - `GET /api/v1/loans/my-current`: Người dùng xem các sách đang mượn.
    - (Admin) `GET /api/v1/admin/loans`: Xem tất cả các phiếu mượn (phân trang, lọc theo trạng thái, người dùng, sách).
    - (Admin) `POST /api/v1/admin/loans/{loanId}/approve`: Thủ thư xác nhận yêu cầu mượn.
    - (Admin) `POST /api/v1/admin/loans/{loanId}/reject`: Thủ thư từ chối yêu cầu mượn.
    - (Admin) `POST /api/v1/admin/loans/{loanId}/return`: Thủ thư ghi nhận trả sách.
    - (Admin) `PUT /api/v1/admin/loans/{loanId}`: Thủ thư cập nhật thông tin phiếu mượn (ví dụ: gia hạn, ghi chú).
- **Cart (Giỏ hàng - `/api/v1/cart`)**
    - `GET /api/v1/cart`: Lấy thông tin giỏ hàng của người dùng hiện tại.
    - `POST /api/v1/cart/items`: Thêm sách vào giỏ hàng. Request body: `{ "bookId": ..., "quantity": ... }`.
    - `PUT /api/v1/cart/items/{bookId}`: Cập nhật số lượng sách trong giỏ. Request body: `{ "quantity": ... }`.
    - `DELETE /api/v1/cart/items/{bookId}`: Xóa một mục sách khỏi giỏ hàng.
    - `DELETE /api/v1/cart`: Xóa toàn bộ giỏ hàng (clear cart).
- **Orders (Đơn hàng - `/api/v1/orders`, `/api/v1/admin/orders`)**
    - `POST /api/v1/orders/checkout`: Người dùng đặt hàng từ giỏ hàng. Request body: `{ "shippingAddress": {...}, "paymentMethod": "...", "customerNote": "..." }`. Backend sẽ tạo Payment Intent nếu cần.
    - `GET /api/v1/orders`: Lấy lịch sử đơn hàng của người dùng hiện tại (phân trang).
    - `GET /api/v1/orders/{orderCode}`: Lấy chi tiết đơn hàng theo mã đơn hàng.
    - (Admin) `GET /api/v1/admin/orders`: (Librarian only) Lấy danh sách tất cả đơn hàng (phân trang, lọc theo trạng thái, người dùng).
    - (Admin) `GET /api/v1/admin/orders/{orderCode}`: (Librarian only) Lấy chi tiết đơn hàng.
    - (Admin) `PUT /api/v1/admin/orders/{orderCode}/status`: (Librarian only) Cập nhật trạng thái đơn hàng (ví dụ: 'PROCESSING', 'SHIPPED').
- **Payments (Thanh toán - `/api/v1/payments`)**
    - `POST /api/v1/payments/create-payment-intent`: (Nếu cần) Tạo một Payment Intent với cổng thanh toán. Request body: `{ "orderId": ... }` hoặc `{ "cartItems": [...] }`. Trả về `clientSecret`.
    - `POST /api/v1/payments/webhook/{gatewayName}` (Ví dụ: `/stripe`): Endpoint để cổng thanh toán gửi thông báo (webhook) kết quả giao dịch. Backend xử lý và cập nhật trạng thái đơn hàng.
- **Documents (Tài liệu - `/api/v1/documents`, `/api/v1/admin/documents`)**
    - `GET /api/v1/documents`: Lấy danh sách metadata tài liệu (phân trang, lọc theo thể loại, sách liên quan, tên).
    - `GET /api/v1/documents/{documentId}`: Lấy metadata chi tiết của tài liệu.
    - `GET /api/v1/documents/{documentId}/view-url`: Lấy URL an toàn (pre-signed URL từ MinIO) để xem/tải tài liệu. Backend kiểm tra quyền truy cập trước khi tạo URL.
    - (Admin) `POST /api/v1/admin/documents/upload`: Tải lên tài liệu mới. Request: `multipart/form-data` (file, title, description, bookId (optional), accessLevel).
    - (Admin) `PUT /api/v1/admin/documents/{documentId}`: Cập nhật metadata tài liệu.
    - (Admin) `POST /api/v1/admin/documents/{documentId}/replace-file`: Thay thế tệp của tài liệu hiện có.
    - (Admin) `DELETE /api/v1/admin/documents/{documentId}`: Xóa tài liệu (cả metadata và file trên MinIO).

### 5. Thiết kế Giao diện Người dùng (Frontend) - Sơ lược

### 5.1. Các trang chính

- **Trang công cộng:**
    - Trang chủ (Home Page): Thanh tìm kiếm nổi bật, danh sách sách mới, sách bán chạy, sách mượn nhiều, tài liệu nổi bật, thể loại.
    - Trang Danh sách Sách/Tìm kiếm Sách (Book Listing/Search Page): Kết quả tìm kiếm, bộ lọc (thể loại, tác giả, giá, năm XB, ngôn ngữ), sắp xếp, phân trang.
    - Trang Chi tiết Sách (Book Detail Page): Thông tin đầy đủ (ảnh bìa, mô tả, tác giả, NXB, ISBN, số trang, ngôn ngữ, đánh giá nếu có), tình trạng (còn cho mượn, còn hàng bán, giá), nút "Mượn sách" hoặc "Thêm vào giỏ hàng", danh sách tài liệu liên quan.
    - Trang Danh sách Tài liệu (Document Listing Page): Tìm kiếm, lọc tài liệu.
    - Trang Xem Tài liệu (Document View Page): Nhúng trình xem tài liệu.
    - Trang Đăng nhập (Login Page).
    - Trang Đăng ký (Register Page).
    - Trang Quên mật khẩu (Forgot Password Page).
- **Trang người dùng (Yêu cầu đăng nhập):**
    - Trang Thông tin cá nhân (User Profile Page): Xem/Chỉnh sửa thông tin cá nhân, đổi mật khẩu.
    - Trang Lịch sử mượn sách (My Loan History).
    - Trang Sách đang mượn (My Current Loans).
    - Trang Giỏ hàng (Shopping Cart Page): Danh sách sản phẩm, cập nhật số lượng, xóa sản phẩm, tổng tiền, nút "Tiến hành thanh toán".
    - Trang Thanh toán (Checkout Page): Nhập địa chỉ giao hàng, chọn phương thức thanh toán, xác nhận đơn hàng.
    - Trang Lịch sử Đơn hàng (My Order History): Danh sách đơn hàng đã đặt, trạng thái, chi tiết đơn hàng.
- **Trang Quản trị viên/Thủ thư (Admin Dashboard - Yêu cầu quyền Admin/Librarian):**
    - Tổng quan (Dashboard): Thống kê nhanh.
    - Quản lý Sách: Danh sách sách, thêm, sửa, xóa.
    - Quản lý Tác giả/Thể loại/NXB: Thêm, sửa, xóa.
    - Quản lý Người dùng: Danh sách người dùng, xem chi tiết, kích hoạt/vô hiệu hóa.
    - Quản lý Mượn/Trả:
        - Danh sách yêu cầu mượn (chờ duyệt).
        - Danh sách sách đang được mượn, sách quá hạn.
        - Chức năng xác nhận mượn, ghi nhận trả.
    - Quản lý Đơn hàng: Danh sách đơn hàng, xem chi tiết, cập nhật trạng thái (thanh toán, giao hàng).
    - Quản lý Tài liệu số: Tải lên, danh sách tài liệu, sửa metadata, xóa.
    - Cấu hình hệ thống (nếu có).

### 5.2. Các thành phần (Components) chính (Tái sử dụng)

- Thanh điều hướng (Navbar): Logo, link, tìm kiếm, icon giỏ hàng, menu người dùng/login.
- Chân trang (Footer): Thông tin bản quyền, liên kết hữu ích.
- Thanh tìm kiếm (SearchBar): Tích hợp gợi ý.
- Thẻ sách (BookCard): Hiển thị tóm tắt thông tin sách.
- Thẻ tài liệu (DocumentCard/ListItem): Hiển thị tóm tắt thông tin tài liệu.
- Form (LoginForm, RegisterForm, BookForm, ProfileForm, CheckoutForm).
- Bảng dữ liệu (DataTable): Dùng cho các trang quản lý, hỗ trợ sắp xếp, lọc, phân trang.
- Modal/Popup: Xác nhận hành động, hiển thị thông báo, form nhanh.
- Component phân trang (Pagination).
- Trình xem tài liệu nhúng (DocumentViewer: PDFViewer, DocxViewer wrapper).
- Mục trong giỏ hàng (CartItem).
- Thẻ thông tin đơn hàng (OrderSummaryCard, OrderItemCard).
- Loading Spinners/Progress Bars.
- Alerts/Notifications.

### 6. Bảo mật

- **Xác thực (Authentication):**
    - Spring Security với JWT (JSON Web Tokens).
    - Token được lưu trữ an toàn ở client (ví dụ: `HttpOnly` cookie hoặc Local Storage với các biện pháp bảo vệ XSS).
    - Triển khai cơ chế refresh token để duy trì session mà không cần đăng nhập lại thường xuyên.
- **Ủy quyền (Authorization):**
    - Spring Security với phân quyền dựa trên vai trò (Role-Based Access Control - RBAC) và có thể cả quyền hạn chi tiết (Permission-based).
    - Sử dụng annotations `@PreAuthorize`, `@PostAuthorize`, `@Secured` trên các method ở Controller hoặc Service.
- **Bảo vệ chống tấn công phổ biến:**
    - **XSS (Cross-Site Scripting):** Escape dữ liệu đầu ra ở Frontend. Sử dụng Content Security Policy (CSP).
    - **SQL Injection:** Sử dụng Prepared Statements (Spring Data JPA mặc định làm điều này). Validate và sanitize dữ liệu đầu vào.
    - **CSRF (Cross-Site Request Forgery):** Spring Security cung cấp cơ chế chống CSRF (thường dùng cho form-based login, cần cân nhắc với API stateless dùng JWT).
    - **Input Validation:** Validate tất cả dữ liệu đầu vào từ người dùng ở cả Frontend và Backend (sử dụng Bean Validation API - JSR 380).
- **Mã hóa mật khẩu:** Sử dụng `BCryptPasswordEncoder` của Spring Security.
- **HTTPS:** Bắt buộc sử dụng HTTPS cho toàn bộ traffic.
- **Bảo mật API:**
    - Rate limiting để chống brute-force.
    - Giám sát và ghi log các truy cập API đáng ngờ.
- **Bảo vệ truy cập MinIO:**
    - Backend tạo pre-signed URL có thời hạn ngắn cho việc truy cập (GET) hoặc tải lên (PUT) tài liệu.
    - MinIO server không nên public access key/secret key. Backend sử dụng credentials được cấu hình an toàn.
    - Cấu hình Bucket Policies và User Policies trên MinIO chặt chẽ.
- **Bảo mật thanh toán:**
    - Không lưu trữ thông tin thẻ tín dụng nhạy cảm trên server (trừ khi tuân thủ đầy đủ PCI DSS).
    - Sử dụng giải pháp tích hợp của cổng thanh toán (ví dụ: Stripe Elements, PayPal Hosted Fields) để thông tin thẻ được gửi trực tiếp đến cổng thanh toán.
    - Xác thực kỹ lưỡng các webhook từ cổng thanh toán (kiểm tra chữ ký, nguồn gốc).
- **Headers bảo mật:** Sử dụng các HTTP security headers như `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`.

### 7. Tương tác với MinIO

- **Cấu hình (`application.properties` hoặc `application.yml` trong Spring Boot):**
    - `minio.endpoint`
    - `minio.access-key`
    - `minio.secret-key`
    - `minio.bucket-name` (Tên bucket chính cho tài liệu)
- **MinioService (trong Backend):**
    - Khởi tạo MinioClient.
    - **Tải lên (Upload):**
        - Nhận `MultipartFile` từ controller.
        - Tạo tên object duy nhất cho MinIO (ví dụ: `UUID.randomUUID().toString() + "-" + originalFilename`).
        - Sử dụng `minioClient.putObject()` để tải file lên bucket.
        - Lưu metadata (bao gồm `minio_object_name`, `file_name_original`, `file_type`, `size_in_bytes`) vào bảng `documents` trong CSDL.
    - **Tạo Pre-signed URL (cho xem/tải xuống):**
        - Nhận yêu cầu xem tài liệu (ví dụ: theo `documentId`).
        - Kiểm tra quyền truy cập của người dùng đối với tài liệu này.
        - Nếu được phép, sử dụng `minioClient.getPresignedObjectUrl()` hoặc `minioClient.presignedGetObject()` (tùy phiên bản SDK) với phương thức `GET` và thời gian hết hạn ngắn (ví dụ: 5-15 phút).
        - Trả URL này về cho Frontend.
    - **Xóa (Delete):**
        - Khi xóa tài liệu từ CSDL, đồng thời gọi `minioClient.removeObject()` để xóa file tương ứng trên MinIO. Cần xử lý transaction cẩn thận.
    - **Kiểm tra sự tồn tại, lấy metadata file từ MinIO (nếu cần).**

### 8. Tích hợp Cổng thanh toán (Ví dụ với Stripe)

- **Cấu hình (`application.properties` hoặc `application.yml`):**
    - `stripe.secret-key`
    - `stripe.public-key` (dùng ở Frontend)
    - `stripe.webhook-secret` (để xác thực webhook)
- **PaymentService (trong Backend):**
    - **Tạo Payment Intent:**
        - Khi người dùng tiến hành checkout, Backend nhận thông tin đơn hàng (tổng tiền, loại tiền tệ).
        - Gọi API của Stripe (`PaymentIntent.create()`) để tạo một Payment Intent. Lưu ID của Payment Intent này liên kết với đơn hàng trong CSDL.
        - Trả về `client_secret` của Payment Intent cho Frontend.
    - **Xử lý Webhook:**
        - Tạo một endpoint (`@PostMapping("/api/v1/payments/webhook/stripe")`) để nhận sự kiện từ Stripe.
        - Xác thực chữ ký của webhook bằng `stripe.webhook-secret` để đảm bảo request đến từ Stripe.
        - Xử lý các loại sự kiện quan trọng:
            - `payment_intent.succeeded`: Thanh toán thành công. Cập nhật trạng thái đơn hàng sang "PAID" hoặc "PROCESSING", giảm tồn kho, gửi email xác nhận.
            - `payment_intent.payment_failed`: Thanh toán thất bại. Cập nhật trạng thái đơn hàng, thông báo cho người dùng.
            - Các sự kiện khác: `charge.refunded`, ...
- **Frontend:**
    - Sử dụng Stripe.js và Stripe Elements để tạo form nhập thông tin thẻ an toàn.
    - Khi người dùng submit form thanh toán, gọi `stripe.confirmCardPayment(clientSecret, { payment_method: { card: cardElement } })`.
    - Xử lý kết quả trả về từ Stripe (thành công/thất bại) và hiển thị thông báo cho người dùng.
    - Frontend có thể không cần đợi webhook mà cập nhật UI tạm thời, nhưng trạng thái cuối cùng của đơn hàng phải dựa trên xác nhận từ Backend (thông qua webhook hoặc polling).

### 9. Triển khai (Deployment) - Gợi ý

- **Backend (Spring Boot App):**
    - Đóng gói thành file JAR (hoặc WAR nếu triển khai trên Application Server truyền thống).
    - Container hóa bằng Docker: Tạo `Dockerfile` để build image.
    - Triển khai lên server (VPS, Dedicated Server, Cloud VM như AWS EC2, Google Compute Engine) hoặc PaaS (AWS Elastic Beanstalk, Heroku, Google App Engine).
    - Sử dụng Nginx làm reverse proxy phía trước ứng dụng Spring Boot để xử lý SSL termination, load balancing (nếu có nhiều instance), phục vụ static content (nếu Frontend được bundle cùng).
- **Frontend (React/Angular/Vue App):**
    - Build ra các file tĩnh (HTML, CSS, JS).
    - Phục vụ bằng Nginx (có thể cùng Nginx với Backend).
    - Hoặc triển khai lên các dịch vụ hosting tĩnh (Netlify, Vercel, AWS S3 + CloudFront, GitHub Pages).
- **Cơ sở dữ liệu (PostgreSQL):**
    - Cài đặt trên server riêng.
    - Hoặc sử dụng dịch vụ CSDL được quản lý (Managed Database Service) như AWS RDS, Google Cloud SQL, Azure Database for PostgreSQL.
- **MinIO Server:**
    - Cài đặt tự host trên server riêng (sử dụng Docker image của MinIO là một lựa chọn tốt).
    - Hoặc sử dụng dịch vụ lưu trữ tương thích S3.
    - Đảm bảo cấu hình lưu trữ bền vững (persistent storage) cho MinIO.
- **CI/CD Pipeline:**
    - Sử dụng Jenkins, GitLab CI, hoặc GitHub Actions để tự động hóa quá trình build, test, và deploy mỗi khi có thay đổi code.
- **Monitoring & Logging:**
    - Sử dụng Spring Boot Actuator cho health checks và metrics.
    - Tích hợp với hệ thống logging tập trung (ELK Stack - Elasticsearch, Logstash, Kibana; hoặc Grafana Loki).
    - Sử dụng công cụ monitoring (Prometheus + Grafana, Datadog, New Relic).