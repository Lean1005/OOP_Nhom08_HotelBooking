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

public class RoomFilterDialog extends JDialog {
    private final List<Room> allRooms;
    private final JComboBox<String> cboType = new JComboBox<>(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"Tất cả", "Trống", "Đang có khách", "Bảo trì", "Cần dọn phòng"});
    private final DefaultTableModel model;
    private List<Room> currentList;

    public RoomFilterDialog(JFrame parent, List<Room> allRooms, Consumer<Room> onPick) {
        super(parent, "Tra cứu & Lọc Phòng", true);
        this.allRooms = allRooms;
        setSize(680, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Hạng phòng:")); top.add(cboType);
        top.add(new JLabel("Trạng thái:")); top.add(cboStatus);
        cboType.addActionListener(e -> filter());
        cboStatus.addActionListener(e -> filter());

        model = new DefaultTableModel(new String[]{"Số Phòng", "Tầng", "Hạng Phòng", "Giá/Đêm", "Trạng Thái", "Dọn phòng"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JButton btnSelect = new JButton("Chọn phòng này trên Sơ đồ");
        btnSelect.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0 && r < currentList.size()) {
                onPick.accept(currentList.get(r));
                dispose();
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnSelect);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        filter();
    }

    private void filter() {
        String t = (String) cboType.getSelectedItem();
        String s = (String) cboStatus.getSelectedItem();
        currentList = allRooms.stream()
                .filter(r -> "Tất cả".equals(t) || r.getRoomTypeName().equalsIgnoreCase(t))
                .filter(r -> {
                    if ("Cần dọn phòng".equals(s)) return r.isCleaningRequested();
                    return "Tất cả".equals(s) || r.getStatus().getDisplayName().equalsIgnoreCase(s);
                })
                .collect(Collectors.toList());
        model.setRowCount(0);
        NumberFormat vnd = NumberFormat.getNumberInstance(HotelBookingApp.VI_LOCALE);
        for (Room r : currentList) {
            model.addRow(new Object[]{
                    r.getRoomNumber(), "Tầng " + r.getFloor(), r.getRoomTypeName(),
                    vnd.format(r.getPricePerNight()) + " đ", r.getStatus().getDisplayName(),
                    r.isCleaningRequested() ? "Đang yêu cầu dọn" : "Đã sạch"
            });
        }
    }
}
