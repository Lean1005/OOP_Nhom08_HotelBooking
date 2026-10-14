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

public class AddRoomFloorDialog extends JDialog {
    private final BookingService bookingService;
    private boolean changed = false;
    private Room lastAddedRoom;
    private String actionSummaryMessage = "";

    public AddRoomFloorDialog(JFrame parent, BookingService bookingService, int selectedFloor, int initialTab) {
        super(parent, "Quản lý Mở rộng Khách sạn: Thêm Phòng & Thêm Tầng - Nhóm8HotelBooking", true);
        this.bookingService = bookingService;
        setSize(540, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.addTab("1. Thêm Phòng mới vào Tầng", createAddSingleRoomPanel(selectedFloor));
        tabbedPane.addTab("2. Thêm Tầng mới (Tạo hàng loạt phòng)", createAddFloorPanel());

        if (initialTab >= 0 && initialTab < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(initialTab);
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAddSingleRoomPanel(int defaultFloor) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(14, 16, 12, 16));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));

        List<Integer> floors = bookingService.getExistingFloors();
        JComboBox<Integer> cboFloor = new JComboBox<>(floors.toArray(new Integer[0]));
        cboFloor.setSelectedItem(defaultFloor);

        JTextField txtRoomNo = new JTextField(bookingService.suggestNextRoomNumber(defaultFloor));
        txtRoomNo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JComboBox<String> cboType = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite"});
        JSpinner spnBeds = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        JComboBox<String> cboView = new JComboBox<>(new String[]{"Hướng Biển", "Hướng Thành phố", "Hướng Hồ bơi"});
        JCheckBox chkJacuzzi = new JCheckBox("Trang bị bể sục Jacuzzi VIP", true);

        cboFloor.addActionListener(e -> {
            Integer fl = (Integer) cboFloor.getSelectedItem();
            if (fl != null) {
                txtRoomNo.setText(bookingService.suggestNextRoomNumber(fl));
            }
        });

        form.add(new JLabel("Chọn Tầng muốn thêm phòng:"));  form.add(cboFloor);
        form.add(new JLabel("Số phòng mới (tự động gợi ý):")); form.add(txtRoomNo);
        form.add(new JLabel("Chọn Hạng phòng:"));            form.add(cboType);
        form.add(new JLabel("Số giường (nếu là Standard):")); form.add(spnBeds);
        form.add(new JLabel("Hướng ban công (nếu là Deluxe):")); form.add(cboView);
        form.add(new JLabel("Tiện ích VIP (nếu là Suite):")); form.add(chkJacuzzi);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddRoom = new JButton("Xác nhận Thêm Phòng");
        btnAddRoom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddRoom.setForeground(new Color(22, 160, 133));
        JButton btnClose = new JButton("Đóng");

        btnAddRoom.addActionListener(e -> {
            try {
                int fl = (Integer) cboFloor.getSelectedItem();
                String roomNo = txtRoomNo.getText();
                String type = (String) cboType.getSelectedItem();
                int beds = (Integer) spnBeds.getValue();
                String view = (String) cboView.getSelectedItem();
                boolean jacuzzi = chkJacuzzi.isSelected();

                lastAddedRoom = bookingService.addSingleRoom(roomNo, fl, type, beds, view, jacuzzi);
                changed = true;
                actionSummaryMessage = "Đã thêm thành công Phòng " + lastAddedRoom.getRoomNumber()
                        + " (" + lastAddedRoom.getRoomTypeName() + ") vào Tầng " + fl + "!";
                JOptionPane.showMessageDialog(this, actionSummaryMessage, "Thêm Phòng thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (InvalidBookingDataException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dispose());

        btnBar.add(btnAddRoom);
        btnBar.add(btnClose);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAddFloorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(14, 16, 12, 16));

        int nextFloor = bookingService.getNextNewFloorNumber();
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));

        JSpinner spnNewFloor = new JSpinner(new SpinnerNumberModel(nextFloor, 1, 30, 1));
        spnNewFloor.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JSpinner spnRoomCount = new JSpinner(new SpinnerNumberModel(6, 1, 12, 1));
        JComboBox<String> cboFloorType = new JComboBox<>(new String[]{
                "Hỗn hợp (Standard + Deluxe + Suite)",
                "Standard",
                "Deluxe",
                "Suite"
        });
        JLabel lblPreview = new JLabel();
        lblPreview.setForeground(new Color(41, 128, 185));
        lblPreview.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        Runnable updatePreview = () -> {
            int fl = (Integer) spnNewFloor.getValue();
            int cnt = (Integer) spnRoomCount.getValue();
            lblPreview.setText(String.format("Sẽ tự động tạo %d phòng: từ P.%d01 đến P.%d%02d", cnt, fl, fl, cnt));
        };
        spnNewFloor.addChangeListener(e -> updatePreview.run());
        spnRoomCount.addChangeListener(e -> updatePreview.run());
        updatePreview.run();

        form.add(new JLabel("Số thứ tự Tầng mới cần xây:"));  form.add(spnNewFloor);
        form.add(new JLabel("Số lượng phòng trên Tầng mới:")); form.add(spnRoomCount);
        form.add(new JLabel("Cấu hình Hạng phòng:"));         form.add(cboFloorType);
        form.add(new JLabel("Dự kiến các phòng được tạo:"));  form.add(lblPreview);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddFloor = new JButton("Xác nhận Xây Thêm Tầng");
        btnAddFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddFloor.setForeground(new Color(41, 128, 185));
        JButton btnClose = new JButton("Đóng");

        btnAddFloor.addActionListener(e -> {
            try {
                int fl = (Integer) spnNewFloor.getValue();
                int cnt = (Integer) spnRoomCount.getValue();
                String opt = (String) cboFloorType.getSelectedItem();

                List<Room> created = bookingService.addNewFloor(fl, cnt, opt);
                if (!created.isEmpty()) {
                    lastAddedRoom = created.get(0);
                }
                changed = true;
                actionSummaryMessage = "Đã xây thêm Tầng " + fl + " với " + created.size() + " phòng mới thành công!";
                JOptionPane.showMessageDialog(this, actionSummaryMessage, "Thêm Tầng thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (InvalidBookingDataException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dispose());

        btnBar.add(btnAddFloor);
        btnBar.add(btnClose);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    public boolean isChanged() { return changed; }
    public Room getLastAddedRoom() { return lastAddedRoom; }
    public String getActionSummaryMessage() { return actionSummaryMessage; }
}
