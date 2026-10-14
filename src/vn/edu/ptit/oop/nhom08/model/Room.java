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

public abstract class Room {
    private String roomNumber;
    private int floor;
    private RoomStatus status;
    private boolean cleaningRequested;

    public Room(String roomNumber, int floor, RoomStatus status) {
        setRoomNumber(roomNumber);
        setFloor(floor);
        setStatus(status);
        this.cleaningRequested = false;
    }

    public abstract double getPricePerNight();
    public abstract String getAmenities();
    public abstract String getRoomTypeName();
    public abstract int getMaxOccupancy();

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty())
            throw new IllegalArgumentException("Số phòng không được rỗng!");
        this.roomNumber = roomNumber.trim();
    }

    public int getFloor() { return floor; }
    public void setFloor(int floor) {
        if (floor < 1 || floor > 50) throw new IllegalArgumentException("Tầng phải từ 1 đến 50!");
        this.floor = floor;
    }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = Objects.requireNonNull(status); }

    public boolean isCleaningRequested() { return cleaningRequested; }
    public void setCleaningRequested(boolean cleaningRequested) {
        this.cleaningRequested = cleaningRequested;
    }

    @Override
    public String toString() {
        String cleanTag = cleaningRequested ? " - Cần dọn phòng" : "";
        return String.format("Phòng %s (%s - Tầng %d - %s%s)",
                roomNumber, getRoomTypeName(), floor, status.getDisplayName(), cleanTag);
    }
}
