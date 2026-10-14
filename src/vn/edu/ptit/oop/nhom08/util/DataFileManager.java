package vn.edu.ptit.oop.nhom08.util;

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

public class DataFileManager {
    public static void exportBookingsToCsv(List<Booking> bookings, PricingPolicy policy, java.nio.file.Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write('\ufeff');
            w.write("Mã ĐP,Phòng,Hạng Phòng,Số Khách,Khách Đại Diện & Đi Cùng,Giới Tính,CCCD Chính,SĐT Chính,Check-In,Check-Out,Số Đêm,Thành Tiền,Trạng Thái\n");
            for (Booking b : bookings) {
                Guest p = b.getPrimaryGuest();
                w.write(String.format("%s,%s,%s,%d,\"%s\",\"%s\",%s,%s,%s,%s,%d,%.0f,%s\n",
                        b.getBookingId(), b.getRoom().getRoomNumber(), b.getRoom().getRoomTypeName(),
                        b.getGuests().size(), b.getGuestsSummary(), b.getAllGendersSummary(),
                        p.getCccd(), p.getPhone(),
                        b.getCheckIn(), b.getCheckOut(), b.nights(), b.totalCost(policy),
                        b.isCheckedOut() ? "Đã Check-out" : "Đang lưu trú"));
            }
        }
    }
}
