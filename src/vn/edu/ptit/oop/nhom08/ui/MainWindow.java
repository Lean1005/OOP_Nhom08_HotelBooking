package vn.edu.ptit.oop.nhom08.ui;

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

public class MainWindow extends BaseWindow {
    private HotelRepository repository;
    private BookingService bookingService;
    private RoomMapPanel roomMapPanel;
    private RoomDetailPanel roomDetailPanel;
    private BookingTableModel bookingTableModel;
    private JTable bookingTable;
    private ChartWindow chartWindow;

    public MainWindow() {
        super("Nhóm8HotelBooking - Hệ thống Quản lý Đặt phòng Khách sạn", 1280, 780);
    }

    @Override
    protected void initUI() {
        repository = new InMemoryHotelRepository();
        bookingService = new BookingService(repository);

        setupToolbarAndMenus();

        roomMapPanel = new RoomMapPanel(repository.findAllRooms(), this::onRoomSelected);
        JScrollPane mapScrollPane = new JScrollPane(roomMapPanel);
        mapScrollPane.setBorder(BorderFactory.createTitledBorder(
                "Sơ đồ Phòng Trực quan (Tự động co giãn khi Thêm Phòng / Thêm Tầng - Click chuột để chọn phòng)"));
        mapScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mapScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        roomDetailPanel = new RoomDetailPanel(
                () -> openBookingDialog(roomMapPanel.getSelectedRoom()),
                this::toggleCleaningRequestStatus,
                this::toggleMaintenanceStatus
        );

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScrollPane, roomDetailPanel);
        topSplit.setResizeWeight(0.71);
        topSplit.setDividerLocation(855);

        JPanel bottomPanel = createBookingTableSection();

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, bottomPanel);
        mainSplit.setResizeWeight(0.58);
        mainSplit.setDividerLocation(435);

        add(mainSplit, BorderLayout.CENTER);

        Room defaultRoom = repository.findRoomByNumber("402");
        if (defaultRoom == null && !repository.findAllRooms().isEmpty()) {
            defaultRoom = repository.findAllRooms().get(0);
        }
        if (defaultRoom != null) {
            roomMapPanel.setSelectedRoom(defaultRoom);
            updateRoomDetailView(defaultRoom);
        }
        updateStatusBar();
    }

    private void setupToolbarAndMenus() {
        addToolButton(new Color(39, 174, 96), "+", "Đặt phòng mới", "Mở hộp thoại đặt phòng (hỗ trợ nhiều khách)",
                e -> openBookingDialog(roomMapPanel.getSelectedRoom()));
        addToolButton(new Color(22, 160, 133), "R", "+ Thêm Phòng", "Thêm một phòng mới vào tầng bất kỳ",
                e -> openAddRoomOrFloorDialog(0));
        addToolButton(new Color(41, 128, 185), "T", "+ Thêm Tầng", "Xây thêm một tầng mới với nhiều phòng",
                e -> openAddRoomOrFloorDialog(1));
        addToolButton(new Color(230, 126, 34), "$", "Check-out & Hóa đơn", "Thanh toán trả phòng và in hóa đơn có mộc đỏ",
                e -> openCheckOutDialog());
        addToolButton(new Color(52, 152, 219), "D", "Yêu cầu Dọn phòng", "Bật/Tắt trạng thái yêu cầu dọn phòng",
                e -> toggleCleaningRequestStatus());
        addToolButton(new Color(142, 68, 173), "F", "Lọc phòng", "Tra cứu lọc phòng theo tiêu chí",
                e -> openRoomFilterDialog());
        addToolButton(new Color(192, 57, 43), "C", "Biểu đồ Thống kê", "Xem biểu đồ Công suất & Doanh thu 14 ngày",
                e -> openChartWindow());
        addToolButton(new Color(44, 62, 80), "S", "Xuất CSV", "Xuất danh sách đặt phòng ra file CSV",
                e -> exportBookingsToCsv());

        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(new JLabel(" Chính sách giá: "));

        PricingPolicy[] policies = { new StandardPricing(), new WeekendPricing(), new HolidayPricing() };
        JComboBox<PricingPolicy> policyCombo = new JComboBox<>(policies);
        policyCombo.setMaximumSize(new Dimension(215, 28));
        policyCombo.addActionListener(e -> {
            PricingPolicy selected = (PricingPolicy) policyCombo.getSelectedItem();
            if (selected != null) {
                bookingService.setPricingPolicy(selected);
                updateRoomDetailView(roomMapPanel.getSelectedRoom());
                bookingTableModel.fireTableDataChanged();
                setStatus("Đã đổi chính sách giá: " + selected.getPolicyName());
            }
        });
        toolBar.add(policyCombo);

        // Menu File bổ sung Thêm Phòng & Thêm Tầng
        JMenuItem addRoomMenu = new JMenuItem("Thêm Phòng mới...");
        addRoomMenu.addActionListener(e -> openAddRoomOrFloorDialog(0));
        JMenuItem addFloorMenu = new JMenuItem("Thêm Tầng mới...");
        addFloorMenu.addActionListener(e -> openAddRoomOrFloorDialog(1));
        fileMenu.insert(addRoomMenu, 0);
        fileMenu.insert(addFloorMenu, 1);
        fileMenu.insertSeparator(2);

        JMenuItem showChartItem = new JMenuItem("Mở cửa sổ Biểu đồ Thống kê");
        showChartItem.addActionListener(e -> openChartWindow());
        viewMenu.add(showChartItem);
    }

    private JPanel createBookingTableSection() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Đặt phòng (Hiển thị đầy đủ số lượng khách, giới tính & trạng thái Check-out)"));

        bookingTableModel = new BookingTableModel(repository.findAllBookings(), bookingService);
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(26);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        BookingStatusCellRenderer renderer = new BookingStatusCellRenderer();
        for (int i = 0; i < bookingTable.getColumnCount(); i++) {
            bookingTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        TableRowSorter<BookingTableModel> sorter = new TableRowSorter<>(bookingTableModel);
        bookingTable.setRowSorter(sorter);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topBar.add(new JLabel("Tìm nhanh (Số phòng / Tên khách / CCCD):"));
        JTextField txtSearch = new JTextField(22);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String kw = txtSearch.getText().trim();
                sorter.setRowFilter(kw.isEmpty() ? null :
                        RowFilter.regexFilter("(?i)" + Pattern.quote(kw), 1, 2, 4));
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        topBar.add(txtSearch);

        JButton btnCheckOut = new JButton("Check-out & Lập Hóa đơn dòng đang chọn");
        btnCheckOut.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCheckOut.addActionListener(e -> openCheckOutDialog());
        topBar.add(btnCheckOut);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        return panel;
    }

    private void updateRoomDetailView(Room room) {
        if (room == null) return;
        Booking activeBooking = bookingService.findActiveBookingByRoom(room);
        roomDetailPanel.displayRoom(room, bookingService.getPricingPolicy(), activeBooking);
    }

    private void onRoomSelected(Room room) {
        updateRoomDetailView(room);
        String cleanNote = room.isCleaningRequested() ? " | [ĐANG YÊU CẦU DỌN PHÒNG]" : "";
        setStatus("Đang chọn Phòng " + room.getRoomNumber() + " (" + room.getRoomTypeName()
                + " - " + room.getStatus().getDisplayName() + ")" + cleanNote);
    }

    private void openAddRoomOrFloorDialog(int defaultTabIndex) {
        int currentFloor = (roomMapPanel.getSelectedRoom() != null)
                ? roomMapPanel.getSelectedRoom().getFloor() : 1;
        AddRoomFloorDialog dialog = new AddRoomFloorDialog(this, bookingService, currentFloor, defaultTabIndex);
        dialog.setVisible(true);
        if (dialog.isChanged()) {
            Room newlyAdded = dialog.getLastAddedRoom();
            if (newlyAdded != null) {
                roomMapPanel.setSelectedRoom(newlyAdded);
            }
            roomMapPanel.revalidate();
            refreshAllViews();
            setStatus(dialog.getActionSummaryMessage());
        }
    }

    private void toggleCleaningRequestStatus() {
        Room selected = roomMapPanel.getSelectedRoom();
        if (selected == null) return;
        selected.setCleaningRequested(!selected.isCleaningRequested());
        refreshAllViews();
        if (selected.isCleaningRequested()) {
            setStatus("Đã gửi YÊU CẦU DỌN PHÒNG cho Phòng " + selected.getRoomNumber());
        } else {
            setStatus("Đã xác nhận DỌN PHÒNG XONG cho Phòng " + selected.getRoomNumber());
        }
    }

    private void toggleMaintenanceStatus() {
        Room selected = roomMapPanel.getSelectedRoom();
        if (selected == null) return;
        if (selected.getStatus() == RoomStatus.OCCUPIED) {
            JOptionPane.showMessageDialog(this, "Phòng đang có khách ở, không thể chuyển sang Bảo trì!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selected.setStatus(selected.getStatus() == RoomStatus.MAINTENANCE
                ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE);
        refreshAllViews();
    }

    private void openBookingDialog(Room preselectedRoom) {
        BookingDialog dialog = new BookingDialog(this, bookingService, preselectedRoom);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Booking created = dialog.getCreatedBooking();
            roomMapPanel.setSelectedRoom(created.getRoom());
            refreshAllViews();
            setStatus("Đã đặt thành công phòng " + created.getRoom().getRoomNumber()
                    + " cho " + created.getGuests().size() + " khách lưu trú!");
        }
    }

    private void openCheckOutDialog() {
        int viewRow = bookingTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng đặt phòng trong bảng bên dưới để Check-out!",
                    "Chưa chọn đơn đặt phòng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = bookingTable.convertRowIndexToModel(viewRow);
        Booking selectedBooking = bookingTableModel.getBookingAt(modelRow);

        if (selectedBooking.isCheckedOut()) {
            JOptionPane.showMessageDialog(this, "Đơn đặt phòng này đã được Check-out trước đó!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InvoiceDialog dialog = new InvoiceDialog(this, selectedBooking, bookingService);
        dialog.setVisible(true);
        if (dialog.isPaid()) {
            refreshAllViews();
            setStatus("Đã Check-out thành công phòng " + selectedBooking.getRoom().getRoomNumber()
                    + " (Phòng đã tự động bật cờ Yêu cầu dọn phòng)");
        }
    }

    private void openRoomFilterDialog() {
        RoomFilterDialog dialog = new RoomFilterDialog(this, repository.findAllRooms(), room -> {
            roomMapPanel.setSelectedRoom(room);
            onRoomSelected(room);
        });
        dialog.setVisible(true);
    }

    private void openChartWindow() {
        if (chartWindow == null || !chartWindow.isDisplayable()) {
            chartWindow = new ChartWindow(bookingService);
        }
        chartWindow.refreshCharts();
        chartWindow.setVisible(true);
        chartWindow.toFront();
    }

    private void exportBookingsToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("DanhSachDatPhong_Nhom8HotelBooking.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataFileManager.exportBookingsToCsv(repository.findAllBookings(),
                        bookingService.getPricingPolicy(), chooser.getSelectedFile().toPath());
                JOptionPane.showMessageDialog(this, "Đã xuất file CSV tại:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi ghi file: " + ex.getMessage());
            }
        }
    }

    private void refreshAllViews() {
        roomMapPanel.repaint();
        updateRoomDetailView(roomMapPanel.getSelectedRoom());
        bookingTableModel.fireTableDataChanged();
        if (chartWindow != null && chartWindow.isDisplayable()) {
            chartWindow.refreshCharts();
        }
        updateStatusBar();
    }

    private void updateStatusBar() {
        long floors = repository.findAllRooms().stream().map(Room::getFloor).distinct().count();
        long avail = repository.findAllRooms().stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        long occ = repository.findAllRooms().stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        long needClean = repository.findAllRooms().stream().filter(Room::isCleaningRequested).count();
        setStatus(String.format("Quy mô: %d tầng - %d phòng | Trống: %d | Đang có khách: %d | Cần dọn phòng: %d | Tổng đơn: %d | Chính sách: %s",
                floors, repository.findAllRooms().size(), avail, occ, needClean,
                repository.findAllBookings().size(), bookingService.getPricingPolicy().getPolicyName()));
    }

    @Override
    protected void showAbout() {
        new AboutDialog(this).setVisible(true);
    }
}
