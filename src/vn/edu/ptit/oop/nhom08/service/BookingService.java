package vn.edu.ptit.oop.nhom08.service;

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

public class BookingService {
    private final HotelRepository repository;
    private PricingPolicy pricingPolicy = new StandardPricing();
    private int seq = 1004;

    public BookingService(HotelRepository repository) { this.repository = repository; }
    public PricingPolicy getPricingPolicy() { return pricingPolicy; }
    public void setPricingPolicy(PricingPolicy policy) { if (policy != null) this.pricingPolicy = policy; }
    public List<Room> getAllRooms() { return repository.findAllRooms(); }

    public List<Integer> getExistingFloors() {
        return repository.findAllRooms().stream()
                .map(Room::getFloor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getNextNewFloorNumber() {
        return repository.findAllRooms().stream()
                .mapToInt(Room::getFloor)
                .max().orElse(0) + 1;
    }

    /** Tự động gợi ý mã số phòng tiếp theo trên tầng được chọn (VD: Tầng 4 đang có 6 phòng -> 407). */
    public String suggestNextRoomNumber(int floor) {
        long countOnFloor = repository.findAllRooms().stream()
                .filter(r -> r.getFloor() == floor)
                .count();
        int nextIndex = (int) countOnFloor + 1;
        String candidate = String.format("%d%02d", floor, nextIndex);
        while (repository.findRoomByNumber(candidate) != null) {
            nextIndex++;
            candidate = String.format("%d%02d", floor, nextIndex);
        }
        return candidate;
    }

    /** Thêm một phòng lẻ vào tầng bất kỳ (kiểm tra trùng mã số phòng). */
    public Room addSingleRoom(String roomNo, int floor, String type, int bedCount, String viewDir, boolean hasJacuzzi)
            throws InvalidBookingDataException {
        if (roomNo == null || roomNo.trim().isEmpty()) {
            throw new InvalidBookingDataException("Mã số phòng không được để trống!");
        }
        String cleanNo = roomNo.trim();
        if (repository.findRoomByNumber(cleanNo) != null) {
            throw new InvalidBookingDataException("Số phòng '" + cleanNo + "' đã tồn tại trong khách sạn!");
        }
        Room newRoom = instantiateRoom(cleanNo, floor, type, bedCount, viewDir, hasJacuzzi);
        repository.addRoom(newRoom);
        return newRoom;
    }

    /** Xây thêm một tầng mới với số lượng phòng và hạng phòng tùy chọn. */
    public List<Room> addNewFloor(int floor, int roomCount, String floorTypeOption)
            throws InvalidBookingDataException {
        if (roomCount < 1 || roomCount > 15) {
            throw new InvalidBookingDataException("Số lượng phòng mỗi tầng phải từ 1 đến 15!");
        }
        List<Room> created = new ArrayList<>();
        for (int idx = 1; idx <= roomCount; idx++) {
            String roomNo = suggestNextRoomNumber(floor);
            String type = floorTypeOption;
            if ("Hỗn hợp (Standard + Deluxe + Suite)".equals(floorTypeOption)) {
                if (idx <= 2) type = "Standard";
                else if (idx <= 4) type = "Deluxe";
                else type = "Suite";
            }
            Room r = instantiateRoom(
                    roomNo, floor, type,
                    (idx % 2) + 1,
                    idx % 2 == 0 ? "Hướng Biển" : "Hướng Thành phố",
                    true
            );
            repository.addRoom(r);
            created.add(r);
        }
        return created;
    }

    private Room instantiateRoom(String roomNo, int floor, String type, int bedCount, String viewDir, boolean hasJacuzzi) {
        if ("Deluxe".equalsIgnoreCase(type)) {
            return new DeluxeRoom(roomNo, floor, RoomStatus.AVAILABLE, viewDir);
        } else if ("Suite".equalsIgnoreCase(type)) {
            return new SuiteRoom(roomNo, floor, RoomStatus.AVAILABLE, hasJacuzzi);
        }
        return new StandardRoom(roomNo, floor, RoomStatus.AVAILABLE, bedCount);
    }

    public Booking findActiveBookingByRoom(Room room) {
        if (room == null) return null;
        return repository.findAllBookings().stream()
                .filter(b -> !b.isCheckedOut() && b.getRoom().getRoomNumber().equals(room.getRoomNumber()))
                .findFirst().orElse(null);
    }

    public Booking createBooking(List<Guest> guests, Room room, LocalDate checkIn, LocalDate checkOut, boolean requestCleaning)
            throws RoomNotAvailableException, InvalidBookingDataException {
        if (room == null) throw new InvalidBookingDataException("Vui lòng chọn phòng!");
        if (guests == null || guests.isEmpty())
            throw new InvalidBookingDataException("Vui lòng nhập ít nhất 1 khách hàng!");
        if (room.getStatus() == RoomStatus.MAINTENANCE)
            throw new RoomNotAvailableException("Phòng " + room.getRoomNumber() + " đang bảo trì, không thể đặt!");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new InvalidBookingDataException("Ngày Check-out phải sau ngày Check-in ít nhất 1 đêm!");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Booking existing : repository.findAllBookings()) {
            if (!existing.isCheckedOut() && existing.getRoom().getRoomNumber().equals(room.getRoomNumber())) {
                if (checkIn.isBefore(existing.getCheckOut()) && checkOut.isAfter(existing.getCheckIn())) {
                    throw new RoomNotAvailableException(String.format(
                            "TRÙNG LỊCH ĐẶT PHÒNG!\nPhòng %s đã có lịch của đoàn khách '%s' từ %s đến %s.",
                            room.getRoomNumber(), existing.getPrimaryGuest().getName(),
                            existing.getCheckIn().format(fmt), existing.getCheckOut().format(fmt)));
                }
            }
        }

        Booking b = new Booking("BK-" + (seq++), guests, room, checkIn, checkOut);
        repository.addBooking(b);
        room.setStatus(RoomStatus.OCCUPIED);
        room.setCleaningRequested(requestCleaning);
        return b;
    }

    public Invoice checkOut(Booking booking, List<ServiceCharge> charges) {
        Invoice invoice = new Invoice(booking, pricingPolicy);
        if (charges != null) charges.forEach(invoice::addServiceCharge);
        booking.setCheckedOut(true);

        boolean stillActive = repository.findAllBookings().stream()
                .anyMatch(b -> !b.isCheckedOut() && b.getRoom().getRoomNumber().equals(booking.getRoom().getRoomNumber()));
        if (!stillActive) {
            booking.getRoom().setStatus(RoomStatus.AVAILABLE);
            booking.getRoom().setCleaningRequested(true);
        }

        repository.addRevenue(LocalDate.now(), invoice.getGrandTotal());
        return invoice;
    }

    public Map<String, Double> calculateOccupancyRateByRoomType() {
        Map<String, Integer> total = new LinkedHashMap<>();
        Map<String, Integer> occ = new LinkedHashMap<>();
        for (Room r : repository.findAllRooms()) {
            total.merge(r.getRoomTypeName(), 1, Integer::sum);
            if (r.getStatus() == RoomStatus.OCCUPIED) occ.merge(r.getRoomTypeName(), 1, Integer::sum);
        }
        Map<String, Double> rates = new LinkedHashMap<>();
        for (String type : total.keySet()) {
            rates.put(type, occ.getOrDefault(type, 0) * 100.0 / total.get(type));
        }
        return rates;
    }

    public Map<LocalDate, Double> getRevenueLast14Days() { return repository.getRevenueLast14Days(); }
}
