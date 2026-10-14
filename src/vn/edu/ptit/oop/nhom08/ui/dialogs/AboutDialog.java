package vn.edu.ptit.oop.nhom08.ui.dialogs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import vn.edu.ptit.oop.nhom08.*;
import vn.edu.ptit.oop.nhom08.common.*;
import vn.edu.ptit.oop.nhom08.exception.*;
import vn.edu.ptit.oop.nhom08.model.*;
import vn.edu.ptit.oop.nhom08.repository.*;
import vn.edu.ptit.oop.nhom08.service.*;
import vn.edu.ptit.oop.nhom08.ui.*;
import vn.edu.ptit.oop.nhom08.ui.dialogs.*;
import vn.edu.ptit.oop.nhom08.ui.panels.*;
import vn.edu.ptit.oop.nhom08.util.*;

public class AboutDialog extends JDialog {
    public AboutDialog(JFrame parent) {
        super(parent, "Thông tin Nhóm8HotelBooking", true);
        setSize(620, 440);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JTextArea txt = new JTextArea(
                "HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG (PTIT)\n" +
                "Môn học: INT1332 - Lập trình Hướng đối tượng\n" +
                "Đơn vị: CÔNG TY TNHH NHÓM8HOTELBOOKING\n\n" +
                "PHÂN CÔNG NHIỆM VỤ 9 THÀNH VIÊN:\n" +
                "1. Thành viên 1 (Nhóm trưởng): HotelBookingApp, BaseWindow, MainWindow\n" +
                "2. Thành viên 2: Room (abstract + cờ dọn phòng), RoomStatus, StandardRoom, DeluxeRoom, SuiteRoom\n" +
                "3. Thành viên 3: Guest (có Giới tính), Booking (nhiều khách), RoomNotAvailableException\n" +
                "4. Thành viên 4: PricingPolicy (Strategy BONUS), ServiceCharge, Invoice\n" +
                "5. Thành viên 5: HotelRepository, InMemoryHotelRepository, BookingService (Thêm Phòng/Tầng)\n" +
                "6. Thành viên 6: RoomMapPanel (Sơ đồ 2D co giãn động), RoomDetailPanel (Yêu cầu dọn phòng)\n" +
                "7. Thành viên 7: BookingDialog (Nhiều khách + Giới tính), AddRoomFloorDialog, BookingTableModel\n" +
                "8. Thành viên 8: InvoiceDialog, InvoicePaperPanel (Mộc đỏ & Hình ảnh DV), RoomFilterDialog\n" +
                "9. Thành viên 9: ChartWindow, BarChartPanel, LineChartPanel (Biểu đồ Công suất & Doanh thu)\n"
        );
        txt.setEditable(false);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(new EmptyBorder(12, 16, 12, 16));

        JButton btn = new JButton("Đóng");
        btn.addActionListener(e -> dispose());
        JPanel p = new JPanel(); p.add(btn);

        add(new JScrollPane(txt), BorderLayout.CENTER);
        add(p, BorderLayout.SOUTH);
    }
}
