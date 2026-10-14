package vn.edu.ptit.oop.nhom08.model;

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

public class Booking {
    private final String bookingId;
    private final List<Guest> guests = new ArrayList<>();
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private boolean checkedOut;

    public Booking(String bookingId, List<Guest> guestList, Room room, LocalDate checkIn, LocalDate checkOut)
            throws InvalidBookingDataException {
        if (guestList == null || guestList.isEmpty())
            throw new InvalidBookingDataException("Phải có ít nhất 1 khách hàng trong danh sách đặt phòng!");
        if (room == null)
            throw new InvalidBookingDataException("Phòng không được để trống!");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new InvalidBookingDataException("Ngày Check-out phải sau ngày Check-in ít nhất 1 đêm!");
        this.bookingId = bookingId;
        this.guests.addAll(guestList);
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkedOut = false;
    }

    public Guest getPrimaryGuest() {
        return guests.get(0);
    }

    public List<Guest> getGuests() {
        return Collections.unmodifiableList(guests);
    }

    public String getGuestsSummary() {
        if (guests.size() == 1) {
            Guest g = guests.get(0);
            return g.getName() + " (" + g.getGender().getLabel() + ")";
        }
        return getPrimaryGuest().getName() + " (+" + (guests.size() - 1) + " người đi cùng)";
    }

    public String getAllGendersSummary() {
        return guests.stream().map(g -> g.getGender().getLabel()).collect(Collectors.joining(", "));
    }

    public long nights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }
    public double totalCost() { return nights() * room.getPricePerNight(); }
    public double totalCost(PricingPolicy policy) {
        return policy != null ? policy.calculatePrice(room, nights()) : totalCost();
    }

    public String getBookingId() { return bookingId; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public boolean isCheckedOut() { return checkedOut; }
    public void setCheckedOut(boolean checkedOut) { this.checkedOut = checkedOut; }
}
