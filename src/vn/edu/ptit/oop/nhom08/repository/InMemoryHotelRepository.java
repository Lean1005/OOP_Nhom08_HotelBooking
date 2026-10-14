package vn.edu.ptit.oop.nhom08.repository;

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

public class InMemoryHotelRepository implements HotelRepository {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<LocalDate, Double> revenueByDate = new LinkedHashMap<>();

    public InMemoryHotelRepository() {
        for (int floor = 1; floor <= 4; floor++) {
            for (int idx = 1; idx <= 6; idx++) {
                String roomNo = String.format("%d%02d", floor, idx);
                if (floor <= 2) rooms.add(new StandardRoom(roomNo, floor, RoomStatus.AVAILABLE, (idx % 2) + 1));
                else if (floor == 3) rooms.add(new DeluxeRoom(roomNo, floor, RoomStatus.AVAILABLE, idx <= 3 ? "Hướng Biển" : "Hướng Phố"));
                else rooms.add(new SuiteRoom(roomNo, floor, RoomStatus.AVAILABLE, idx % 2 == 0));
            }
        }
        findRoomByNumber("206").setStatus(RoomStatus.MAINTENANCE);

        try {
            LocalDate today = LocalDate.now();
            Room r102 = findRoomByNumber("102");
            Room r301 = findRoomByNumber("301");
            Room r402 = findRoomByNumber("402");

            bookings.add(new Booking("BK-1001",
                    List.of(new Guest("001204012345", "Nguyễn Văn Minh", Gender.NAM, "0901234567")),
                    r102, today.minusDays(1), today.plusDays(2)));
            r102.setStatus(RoomStatus.OCCUPIED);

            bookings.add(new Booking("BK-1002",
                    List.of(
                            new Guest("079203009876", "Trần Thị Ngọc Lan", Gender.NU, "0912345678"),
                            new Guest("079201005566", "Phạm Quốc Bảo", Gender.NAM, "0918889999")
                    ),
                    r301, today, today.plusDays(3)));
            r301.setStatus(RoomStatus.OCCUPIED);

            bookings.add(new Booking("BK-1003",
                    List.of(
                            new Guest("036205005432", "Lê Hoàng Nam", Gender.NAM, "0987654321"),
                            new Guest("036206008899", "Nguyễn Mai Phương", Gender.NU, "0986667777"),
                            new Guest("036212001122", "Lê Hoàng Quân", Gender.NAM, "0985554444")
                    ),
                    r402, today.minusDays(2), today.plusDays(1)));
            r402.setStatus(RoomStatus.OCCUPIED);
            r402.setCleaningRequested(true);
        } catch (Exception ignored) {
        }

        LocalDate today = LocalDate.now();
        double[] sampleMillions = {3.5, 4.2, 3.0, 5.4, 6.2, 7.8, 6.5, 4.1, 4.9, 6.3, 8.4, 7.2, 5.6, 4.5};
        for (int i = 13; i >= 0; i--) {
            revenueByDate.put(today.minusDays(i), sampleMillions[13 - i] * 1_000_000);
        }
    }

    @Override public List<Room> findAllRooms() { return Collections.unmodifiableList(rooms); }
    @Override public Room findRoomByNumber(String no) {
        return rooms.stream().filter(r -> r.getRoomNumber().equalsIgnoreCase(no)).findFirst().orElse(null);
    }
    @Override public void addRoom(Room room) {
        if (room != null) rooms.add(room);
    }
    @Override public List<Booking> findAllBookings() { return Collections.unmodifiableList(bookings); }
    @Override public void addBooking(Booking b) { bookings.add(b); }
    @Override public Map<LocalDate, Double> getRevenueLast14Days() { return Collections.unmodifiableMap(revenueByDate); }
    @Override public void addRevenue(LocalDate date, double amount) { revenueByDate.merge(date, amount, Double::sum); }
}
