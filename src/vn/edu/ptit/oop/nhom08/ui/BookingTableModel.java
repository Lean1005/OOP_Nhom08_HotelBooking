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

public class BookingTableModel extends AbstractTableModel {
    private final String[] cols = {
            "Mã ĐP", "Phòng", "Khách lưu trú", "Số khách", "Giới tính",
            "CCCD đại diện", "SĐT", "Check-in", "Check-out", "Số đêm", "Tiền phòng (VNĐ)", "Trạng thái"
    };
    private final List<Booking> bookings;
    private final BookingService service;
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat vnd = NumberFormat.getNumberInstance(HotelBookingApp.VI_LOCALE);

    public BookingTableModel(List<Booking> bookings, BookingService service) {
        this.bookings = bookings;
        this.service = service;
    }

    @Override public int getRowCount() { return bookings.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        Booking b = bookings.get(r);
        Guest p = b.getPrimaryGuest();
        switch (c) {
            case 0: return b.getBookingId();
            case 1: return "P." + b.getRoom().getRoomNumber() + " (" + b.getRoom().getRoomTypeName() + ")";
            case 2: return b.getGuestsSummary();
            case 3: return b.getGuests().size() + " người";
            case 4: return b.getAllGendersSummary();
            case 5: return p.getCccd();
            case 6: return p.getPhone();
            case 7: return b.getCheckIn().format(df);
            case 8: return b.getCheckOut().format(df);
            case 9: return b.nights();
            case 10: return vnd.format(b.totalCost(service.getPricingPolicy()));
            default: return b.isCheckedOut() ? "Đã Check-out" : "Đang lưu trú";
        }
    }

    public Booking getBookingAt(int row) { return bookings.get(row); }
}
