package com.example.library.database;

import android.content.Context;
import com.example.library.models.Book;
import com.example.library.models.BorrowingRecord;
import com.example.library.models.User;

public class DataInitializer {
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private BorrowingDAO borrowingDAO;
    
    public DataInitializer(Context context) {
        this.userDAO = new UserDAO(context);
        this.bookDAO = new BookDAO(context);
        this.borrowingDAO = new BorrowingDAO(context);
    }
    
    public void initializeSampleData() {
        // Only add data if database is empty
        if (bookDAO.getTotalBookCount() == 0) {
            // Add admin user
            User admin = new User();
            admin.setName("Thủ thư viện");
            admin.setEmail("admin@lib.com");
            admin.setPassword("admin1");
            admin.setRole("admin");
            userDAO.addUser(admin);
            
            // Add sample students
            addStudentUsers();
            
            // Add sample books
            addSampleBooks();
            
            // Add sample borrowing records
            addSampleBorrowingRecords();
        }
        
        // FORCE ADD TEST DATA for MSSV0009 and MSSV0010 (always add regardless of database state)
        addOverdueTestData();
    }
    
    private void addStudentUsers() {
        // Sample students with MSSV
        String[][] students = {
            {"Nguyễn Văn An", "an.nv@student.edu.vn", "0901234567", "MSSV0001"},
            {"Trần Thị Bình", "binh.tt@student.edu.vn", "0912345678", "MSSV0002"},
            {"Lê Minh Cường", "cuong.lm@student.edu.vn", "0923456789", "MSSV0003"},
            {"Phạm Thu Dung", "dung.pt@student.edu.vn", "0934567890", "MSSV0004"},
            {"Hoàng Văn Em", "em.hv@student.edu.vn", "0945678901", "MSSV0005"}
        };
        
        for (String[] studentData : students) {
            User student = new User();
            student.setName(studentData[0]);
            student.setEmail(studentData[1]);
            student.setPassword("123456");
            student.setPhone(studentData[2]);
            student.setStudentId(studentData[3]);
            student.setRole("user");
            userDAO.addUser(student);
        }
        
        // Add remaining students 6-10
        for (int i = 6; i <= 10; i++) {
            User student = new User();
            student.setName("Student " + i);
            student.setEmail("student" + i + "@edu.vn");
            student.setPassword("123456");
            student.setPhone("090123456" + i);
            student.setStudentId("MSSV000" + i);
            student.setRole("user");
            userDAO.addUser(student);
        }
    }
    
    private void addSampleBooks() {
        String[][] booksData = {
            {"Mắt Biếc", "Nguyễn Nhật Ánh", "Thiếu nhi", "978-2828282828", "6", "6", "mat_biec.jpg", "Tác phẩm nổi tiếng về tình yêu đầu đời"},
            {"Hoàng Tử Bé", "Antoine de Saint-Exupéry", "Thiếu nhi", "978-2323232323", "6", "6", "hoang_tu_be.jpg", "Tác phẩm kinh điển thế giới cho trẻ em"},
            {"Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Thiếu nhi", "978-1616161616", "5", "5", "de_men_phieu_luu_ky.jpg", "Tác phẩm thiếu nhi kinh điển"},
            {"Cho Tôi Một Vé Đi Tuổi Thơ", "Nguyễn Nhật Ánh", "Thiếu nhi", "978-1010101010", "5", "5", "cho_toi_mot_ve_di_tuoi_tho.jpg", "Hồi ức tuổi thơ đẹp"},
            {"Chí Phèo", "Nam Cao", "Văn học cổ điển", "978-8888888888", "3", "3", "chi_pheo.jpg", "Tác phẩm kinh điển về số phận con người"},
            {"Lão Hạc", "Nam Cao", "Văn học cổ điển", "978-2626262626", "3", "3", "lao_hac.jpg", "Tác phẩm kinh điển về tình người"},
            {"Số Đỏ", "Vũ Trọng Phụng", "Văn học cổ điển", "978-3939393939", "2", "2", "so_do.jpg", "Tiểu thuyết hiện thực phê phán"},
            {"Tắt Đèn", "Ngô Tất Tố", "Văn học cổ điển", "978-4242424242", "2", "2", "tat_den.jpg", "Tác phẩm về nông thôn Việt Nam"},
            {"Mindset", "Carol Dweck", "Phát triển bản thân", "978-3030303030", "4", "4", "mindset.jpg", "Về tư duy phát triển"},
            {"Tuổi Trẻ Không Trì Hoãn", "Lý Thượng Long", "Phát triển bản thân", "978-5252525252", "3", "3", "tuoi_tre_khong_tri_hoan.jpg", "Về việc nắm bắt cơ hội tuổi trẻ"}
        };
        
        for (String[] bookData : booksData) {
            Book book = new Book();
            book.setTitle(bookData[0]);
            book.setAuthor(bookData[1]);
            book.setCategory(bookData[2]);
            book.setIsbn(bookData[3]);
            book.setQuantity(Integer.parseInt(bookData[4]));
            book.setAvailable(Integer.parseInt(bookData[5]));
            book.setImageName(bookData[6]);
            book.setDescription(bookData[7]);
            bookDAO.addBook(book);
        }
    }
    
    private void addSampleBorrowingRecords() {
        // Add some sample borrowing records for testing
        try {
            String currentDate = getCurrentDate();
            
            // Sample borrowing records for regular students
            addBorrowingRecordDirect(new BorrowingRecord(2, 1, "2024-12-01", "2024-12-31", "returned", "2024-12-25"));
            addBorrowingRecordDirect(new BorrowingRecord(2, 2, currentDate, getDueDateAfterDays(30), "borrowed"));
            addBorrowingRecordDirect(new BorrowingRecord(3, 3, "2024-11-15", "2024-12-15", "borrowed"));
            addBorrowingRecordDirect(new BorrowingRecord(4, 4, "2024-11-20", "2024-12-20", "returned", "2024-12-18"));
            addBorrowingRecordDirect(new BorrowingRecord(5, 5, currentDate, getDueDateAfterDays(30), "borrowed"));
            addBorrowingRecordDirect(new BorrowingRecord(6, 6, currentDate, getDueDateAfterDays(30), "pending"));
            
            // =========================== TEST DATA FOR ADMIN ===========================
            // Create OVERDUE data for MSSV0009 (userId = 10) - Multiple overdue books + debt
            // Book 1: Very overdue (30 days late) - fined
            addBorrowingRecordDirect(new BorrowingRecord(10, 7, "2024-11-01", "2024-12-01", "borrowed", null)); // 20+ days overdue
            // Book 2: Recently overdue (5 days late)
            addBorrowingRecordDirect(new BorrowingRecord(10, 8, "2024-12-10", "2025-01-10", "borrowed", null)); // 11+ days overdue
            // Book 3: Pending request
            addBorrowingRecordDirect(new BorrowingRecord(10, 9, currentDate, getDueDateAfterDays(30), "pending", null));
            // History: Previously returned very late - generated fine
            addBorrowingRecordDirect(new BorrowingRecord(10, 1, "2024-10-01", "2024-11-01", "returned", "2024-11-15")); // 14 days late
            addBorrowingRecordDirect(new BorrowingRecord(10, 2, "2024-09-01", "2024-10-01", "returned", "2024-10-20")); // 19 days late
            
            // Create OVERDUE data for MSSV0010 (userId = 11) - Also overdue + debt
            // Book 1: Moderately overdue (10 days late)
            addBorrowingRecordDirect(new BorrowingRecord(11, 3, "2024-12-01", "2025-01-01", "borrowed", null)); // 20+ days overdue
            // Book 2: Recently overdue (3 days late)
            addBorrowingRecordDirect(new BorrowingRecord(11, 4, "2024-12-15", "2025-01-15", "borrowed", null)); // 6+ days overdue
            // Pending request
            addBorrowingRecordDirect(new BorrowingRecord(11, 5, currentDate, getDueDateAfterDays(30), "pending", null));
            // History: Multiple late returns
            addBorrowingRecordDirect(new BorrowingRecord(11, 6, "2024-08-01", "2024-09-01", "returned", "2024-09-25")); // 24 days late
            addBorrowingRecordDirect(new BorrowingRecord(11, 1, "2024-07-01", "2024-08-01", "returned", "2024-08-10")); // 9 days late
            
        } catch (Exception e) {
            // Ignore errors in sample data
        }
    }
    
    private void addBorrowingRecordDirect(BorrowingRecord record) {
        borrowingDAO.addBorrowingRecord(record);
    }
    
    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }
    
    private String getDueDateAfterDays(int days) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.getTime());
    }
    
    /**
     * Force add overdue test data for MSSV0009 and MSSV0010 - always runs
     */
    private void addOverdueTestData() {
        try {
            // Remove existing test data first to avoid duplicates
            // Get userIds for MSSV0009 and MSSV0010
            User user0009 = userDAO.getUserByStudentId("MSSV0009");
            User user0010 = userDAO.getUserByStudentId("MSSV0010");
            
            if (user0009 != null && user0010 != null) {
                String currentDate = getCurrentDate();
                
                // =========================== TEST DATA FOR MSSV0009 ===========================
                // Very overdue books with high debt
                addBorrowingRecordDirect(new BorrowingRecord(user0009.getId(), 1, "2024-11-01", "2024-12-01", "borrowed", null)); // 50+ days overdue
                addBorrowingRecordDirect(new BorrowingRecord(user0009.getId(), 2, "2024-12-01", "2024-12-31", "borrowed", null)); // 20+ days overdue
                // Pending request
                addBorrowingRecordDirect(new BorrowingRecord(user0009.getId(), 3, currentDate, getDueDateAfterDays(30), "pending", null));
                // Historical late returns
                addBorrowingRecordDirect(new BorrowingRecord(user0009.getId(), 4, "2024-09-01", "2024-10-01", "returned", "2024-10-20")); // 19 days late
                addBorrowingRecordDirect(new BorrowingRecord(user0009.getId(), 5, "2024-08-01", "2024-09-01", "returned", "2024-09-25")); // 24 days late
                
                // =========================== TEST DATA FOR MSSV0010 ===========================
                // Moderately overdue books
                addBorrowingRecordDirect(new BorrowingRecord(user0010.getId(), 6, "2024-12-01", "2025-01-01", "borrowed", null)); // 20+ days overdue
                addBorrowingRecordDirect(new BorrowingRecord(user0010.getId(), 7, "2024-12-10", "2025-01-10", "borrowed", null)); // 10+ days overdue
                // Pending request
                addBorrowingRecordDirect(new BorrowingRecord(user0010.getId(), 8, currentDate, getDueDateAfterDays(30), "pending", null));
                // Historical late returns
                addBorrowingRecordDirect(new BorrowingRecord(user0010.getId(), 9, "2024-10-01", "2024-11-01", "returned", "2024-11-10")); // 9 days late
                addBorrowingRecordDirect(new BorrowingRecord(user0010.getId(), 10, "2024-09-01", "2024-10-01", "returned", "2024-10-15")); // 14 days late
            }
            
        } catch (Exception e) {
            // Ignore errors but log them
            android.util.Log.e("DataInitializer", "Error adding test data: ", e);
        }
    }
}
