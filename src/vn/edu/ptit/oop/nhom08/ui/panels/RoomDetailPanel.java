package vn.edu.ptit.oop.nhom08.ui.panels;

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

public class RoomDetailPanel extends JPanel {
    private final JLabel lblRoomNo = new JLabel("-");
    private final JLabel lblType = new JLabel("-");
    private final JLabel lblFloor = new JLabel("-");
    private final JLabel lblStatus = new JLabel("-");
    private final JLabel lblCleaning = new JLabel("-");
    private final JLabel lblBasePrice = new JLabel("-");
    private final JLabel lblPolicyPrice = new JLabel("-");
    private final JTextArea txtAmenitiesAndGuests = new JTextArea(5, 20);
    private final JButton btnToggleCleaning;

    public RoomDetailPanel(Runnable onBook, Runnable onToggleCleaning, Runnable onMaint) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder("Chi tiết Phòng đang chọn"));
        setPreferredSize(new Dimension(370, 340));

        JPanel grid = new JPanel(new GridLayout(7, 2, 6, 6));
        grid.setBorder(new EmptyBorder(6, 12, 6, 12));
        Font bold = new Font("Segoe UI", Font.BOLD, 13);
        lblRoomNo.setFont(bold);
        lblCleaning.setFont(bold);
        lblPolicyPrice.setFont(bold);
        lblPolicyPrice.setForeground(new Color(192, 57, 43));

        grid.add(new JLabel("Số phòng:"));              grid.add(lblRoomNo);
        grid.add(new JLabel("Hạng phòng:"));            grid.add(lblType);
        grid.add(new JLabel("Tầng:"));                  grid.add(lblFloor);
        grid.add(new JLabel("Trạng thái:"));            grid.add(lblStatus);
        grid.add(new JLabel("Yêu cầu dọn phòng:"));     grid.add(lblCleaning);
        grid.add(new JLabel("Giá gốc niêm yết:"));      grid.add(lblBasePrice);
        grid.add(new JLabel("Giá theo CS hiện tại:"));  grid.add(lblPolicyPrice);

        txtAmenitiesAndGuests.setLineWrap(true);
        txtAmenitiesAndGuests.setWrapStyleWord(true);
        txtAmenitiesAndGuests.setEditable(false);
        txtAmenitiesAndGuests.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtAmenitiesAndGuests);
        scroll.setBorder(BorderFactory.createTitledBorder("Tiện nghi & Khách đang lưu trú trong phòng"));

        JPanel btns = new JPanel(new GridLayout(3, 1, 5, 5));
        btns.setBorder(new EmptyBorder(4, 10, 8, 10));

        JButton btnBook = new JButton("Đặt phòng này ngay");
        btnBook.setFont(bold);
        btnBook.addActionListener(e -> onBook.run());

        btnToggleCleaning = new JButton("Yêu cầu Dọn phòng / Hoàn tất dọn");
        btnToggleCleaning.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleCleaning.setForeground(new Color(25, 80, 150));
        btnToggleCleaning.addActionListener(e -> onToggleCleaning.run());

        JButton btnMaint = new JButton("Đổi trạng thái Bảo trì / Trống");
        btnMaint.addActionListener(e -> onMaint.run());

        btns.add(btnBook);
        btns.add(btnToggleCleaning);
        btns.add(btnMaint);

        add(grid, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }

    public void displayRoom(Room room, PricingPolicy policy, Booking activeBooking) {
        if (room == null) return;
        NumberFormat vnd = NumberFormat.getCurrencyInstance(HotelBookingApp.VI_LOCALE);
        lblRoomNo.setText("Phòng " + room.getRoomNumber());
        lblType.setText(room.getRoomTypeName() + " (Tối đa " + room.getMaxOccupancy() + " khách)");
        lblFloor.setText("Tầng " + room.getFloor());
        lblStatus.setText(room.getStatus().getDisplayName());

        if (room.isCleaningRequested()) {
            lblCleaning.setText("Đang yêu cầu dọn phòng!");
            lblCleaning.setForeground(new Color(41, 128, 185));
            btnToggleCleaning.setText("Xác nhận Đã dọn phòng xong");
        } else {
            lblCleaning.setText("Đã dọn sạch sẽ");
            lblCleaning.setForeground(new Color(39, 174, 96));
            btnToggleCleaning.setText("Gửi Yêu cầu Dọn phòng");
        }

        lblBasePrice.setText(vnd.format(room.getPricePerNight()) + " / đêm");
        lblPolicyPrice.setText(vnd.format(policy.calculatePrice(room, 1)) + " / đêm");

        StringBuilder info = new StringBuilder();
        info.append("• Tiện nghi: ").append(room.getAmenities()).append("\n");
        if (activeBooking != null) {
            info.append("• Đoàn khách đang ở (").append(activeBooking.getGuests().size()).append(" người):\n");
            int idx = 1;
            for (Guest g : activeBooking.getGuests()) {
                info.append("   ").append(idx++).append(". ").append(g.getName())
                    .append(" (").append(g.getGender().getLabel())
                    .append(" - CCCD: ").append(g.getCccd()).append(")\n");
            }
        } else {
            info.append("• Hiện chưa có khách lưu trú.");
        }
        txtAmenitiesAndGuests.setText(info.toString());
        txtAmenitiesAndGuests.setCaretPosition(0);
    }
}
