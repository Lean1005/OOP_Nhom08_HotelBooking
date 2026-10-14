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

public class BookingDialog extends JDialog {
    private final BookingService bookingService;
    private final JComboBox<Room> cboRoom;
    private final JTextField txtName = new JTextField();
    private final JComboBox<Gender> cboGender = new JComboBox<>(Gender.values());
    private final JTextField txtCccd = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JSpinner spnIn;
    private final JSpinner spnOut;
    private final JCheckBox chkCleaningRequest = new JCheckBox("Yêu cầu dọn phòng trước / trong khi nhận phòng");
    private final JLabel lblNights = new JLabel("3 đêm");
    private final JLabel lblCost = new JLabel("0 VNĐ");

    private final List<Guest> addedGuests = new ArrayList<>();
    private final DefaultTableModel guestTableModel;
    private final JTable guestTable;

    private boolean confirmed = false;
    private Booking createdBooking;

    public BookingDialog(JFrame parent, BookingService service, Room preselected) {
        super(parent, "Đặt phòng Khách sạn (Hỗ trợ 1 hoặc nhiều người ở) - Nhóm8HotelBooking", true);
        this.bookingService = service;
        setSize(660, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        JPanel topForm = new JPanel(new GridLayout(5, 2, 8, 8));
        topForm.setBorder(BorderFactory.createTitledBorder("1. Thông tin Phòng & Nhập thông tin từng Khách hàng"));

        cboRoom = new JComboBox<>(service.getAllRooms().toArray(new Room[0]));
        if (preselected != null) {
            cboRoom.setSelectedItem(preselected);
            chkCleaningRequest.setSelected(preselected.isCleaningRequested());
        }

        topForm.add(new JLabel("Chọn phòng:"));                topForm.add(cboRoom);
        topForm.add(new JLabel("Họ và tên khách:"));           topForm.add(txtName);
        topForm.add(new JLabel("Giới tính:"));                 topForm.add(cboGender);
        topForm.add(new JLabel("Số CCCD (9-12 số):"));         topForm.add(txtCccd);
        topForm.add(new JLabel("Số điện thoại (10 số):"));     topForm.add(txtPhone);

        JPanel guestListPanel = new JPanel(new BorderLayout(6, 6));
        guestListPanel.setBorder(BorderFactory.createTitledBorder(
                "2. Danh sách Khách cùng lưu trú (Đi 2+ người: Bấm '+ Thêm người vào danh sách' cho từng người)"));

        JPanel guestBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JButton btnAddGuest = new JButton("+ Thêm người vào danh sách ở cùng");
        btnAddGuest.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddGuest.setForeground(new Color(39, 174, 96));
        JButton btnRemoveGuest = new JButton("Xóa người đang chọn");

        btnAddGuest.addActionListener(e -> addCurrentGuestToList());
        btnRemoveGuest.addActionListener(e -> removeSelectedGuest());
        guestBtnBar.add(btnAddGuest);
        guestBtnBar.add(btnRemoveGuest);

        guestTableModel = new DefaultTableModel(new String[]{"STT", "Họ và tên khách", "Giới tính", "Số CCCD", "Số điện thoại"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        guestTable = new JTable(guestTableModel);
        guestTable.setRowHeight(24);
        JScrollPane guestScroll = new JScrollPane(guestTable);
        guestScroll.setPreferredSize(new Dimension(600, 125));

        guestListPanel.add(guestBtnBar, BorderLayout.NORTH);
        guestListPanel.add(guestScroll, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout(6, 6));
        JPanel dateGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        dateGrid.setBorder(BorderFactory.createTitledBorder("3. Thời gian lưu trú & Tùy chọn Dọn phòng"));

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 3);

        spnIn = new JSpinner(new SpinnerDateModel(now, null, null, Calendar.DAY_OF_MONTH));
        spnIn.setEditor(new JSpinner.DateEditor(spnIn, "dd/MM/yyyy"));

        spnOut = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        spnOut.setEditor(new JSpinner.DateEditor(spnOut, "dd/MM/yyyy"));

        lblNights.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCost.setForeground(new Color(192, 57, 43));

        ChangeListener cl = e -> updateLiveCost();
        spnIn.addChangeListener(cl);
        spnOut.addChangeListener(cl);
        cboRoom.addActionListener(e -> updateLiveCost());

        dateGrid.add(new JLabel("Ngày Check-in:"));         dateGrid.add(spnIn);
        dateGrid.add(new JLabel("Ngày Check-out:"));        dateGrid.add(spnOut);
        dateGrid.add(new JLabel("Dịch vụ buồng phòng:"));   dateGrid.add(chkCleaningRequest);
        dateGrid.add(new JLabel("Số đêm lưu trú:"));        dateGrid.add(lblNights);
        dateGrid.add(new JLabel("Tổng tiền dự kiến:"));     dateGrid.add(lblCost);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton btnOk = new JButton("Xác nhận Đặt phòng");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton btnCancel = new JButton("Hủy");
        btnOk.addActionListener(e -> confirm());
        btnCancel.addActionListener(e -> dispose());
        actionBtns.add(btnOk);
        actionBtns.add(btnCancel);

        bottomSection.add(dateGrid, BorderLayout.CENTER);
        bottomSection.add(actionBtns, BorderLayout.SOUTH);

        JPanel mainContent = new JPanel(new BorderLayout(8, 8));
        mainContent.setBorder(new EmptyBorder(8, 12, 8, 12));
        mainContent.add(topForm, BorderLayout.NORTH);
        mainContent.add(guestListPanel, BorderLayout.CENTER);
        mainContent.add(bottomSection, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
        updateLiveCost();
    }

    private void addCurrentGuestToList() {
        try {
            Room r = (Room) cboRoom.getSelectedItem();
            if (r != null && addedGuests.size() >= r.getMaxOccupancy()) {
                JOptionPane.showMessageDialog(this,
                        "Hạng phòng " + r.getRoomTypeName() + " chỉ cho phép tối đa " + r.getMaxOccupancy() + " người ở!",
                        "Vượt số người tối đa", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Guest g = new Guest(
                    txtCccd.getText(),
                    txtName.getText(),
                    (Gender) cboGender.getSelectedItem(),
                    txtPhone.getText()
            );
            addedGuests.add(g);
            refreshGuestTable();
            txtName.setText("");
            txtCccd.setText("");
            txtName.requestFocus();
        } catch (InvalidBookingDataException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông tin khách chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedGuest() {
        int row = guestTable.getSelectedRow();
        if (row >= 0 && row < addedGuests.size()) {
            addedGuests.remove(row);
            refreshGuestTable();
        }
    }

    private void refreshGuestTable() {
        guestTableModel.setRowCount(0);
        for (int i = 0; i < addedGuests.size(); i++) {
            Guest g = addedGuests.get(i);
            guestTableModel.addRow(new Object[]{
                    i + 1, g.getName(), g.getGender().getLabel(), g.getCccd(), g.getPhone()
            });
        }
    }

    private LocalDate toLocalDate(JSpinner spn) {
        return ((Date) spn.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void updateLiveCost() {
        long nights = ChronoUnit.DAYS.between(toLocalDate(spnIn), toLocalDate(spnOut));
        Room r = (Room) cboRoom.getSelectedItem();
        if (nights <= 0 || r == null) {
            lblNights.setText("Ngày không hợp lệ (<= 0 đêm)");
            lblCost.setText("0 VNĐ");
        } else {
            lblNights.setText(nights + " đêm");
            double cost = bookingService.getPricingPolicy().calculatePrice(r, nights);
            lblCost.setText(NumberFormat.getCurrencyInstance(HotelBookingApp.VI_LOCALE).format(cost));
        }
    }

    private void confirm() {
        try {
            if (!txtName.getText().trim().isEmpty() || !txtCccd.getText().trim().isEmpty() || addedGuests.isEmpty()) {
                if (!txtName.getText().trim().isEmpty() || addedGuests.isEmpty()) {
                    Guest g = new Guest(
                            txtCccd.getText(),
                            txtName.getText(),
                            (Gender) cboGender.getSelectedItem(),
                            txtPhone.getText()
                    );
                    addedGuests.add(g);
                    refreshGuestTable();
                    txtName.setText("");
                    txtCccd.setText("");
                }
            }

            Room r = (Room) cboRoom.getSelectedItem();
            if (r != null && addedGuests.size() > r.getMaxOccupancy()) {
                throw new InvalidBookingDataException("Số khách (" + addedGuests.size()
                        + " người) vượt quá sức chứa tối đa của phòng (" + r.getMaxOccupancy() + " người)!");
            }

            createdBooking = bookingService.createBooking(
                    addedGuests, r, toLocalDate(spnIn), toLocalDate(spnOut), chkCleaningRequest.isSelected());
            confirmed = true;
            JOptionPane.showMessageDialog(this,
                    "Đặt phòng thành công!\nMã ĐP: " + createdBooking.getBookingId()
                            + "\nSố lượng khách: " + createdBooking.getGuests().size() + " người",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (RoomNotAvailableException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Trùng lịch Đặt phòng", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidBookingDataException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public Booking getCreatedBooking() { return createdBooking; }
}
