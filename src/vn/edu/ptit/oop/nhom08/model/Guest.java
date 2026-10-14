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

public class Guest {
    private String cccd;
    private String name;
    private Gender gender;
    private String phone;

    public Guest(String cccd, String name, Gender gender, String phone) throws InvalidBookingDataException {
        setCccd(cccd);
        setName(name);
        setGender(gender);
        setPhone(phone);
    }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) throws InvalidBookingDataException {
        if (cccd == null || !cccd.trim().matches("\\d{9,12}"))
            throw new InvalidBookingDataException("Số CCCD của khách '" + (name != null ? name : "") + "' phải gồm từ 9 đến 12 chữ số!");
        this.cccd = cccd.trim();
    }

    public String getName() { return name; }
    public void setName(String name) throws InvalidBookingDataException {
        if (name == null || name.trim().length() < 2)
            throw new InvalidBookingDataException("Họ tên khách hàng không được để trống (tối thiểu 2 ký tự)!");
        this.name = name.trim();
    }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) {
        this.gender = (gender == null) ? Gender.NAM : gender;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) throws InvalidBookingDataException {
        if (phone == null || !phone.trim().matches("0\\d{9}"))
            throw new InvalidBookingDataException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        this.phone = phone.trim();
    }

    @Override
    public String toString() {
        return String.format("%s (%s - CCCD: %s - SĐT: %s)", name, gender.getLabel(), cccd, phone);
    }
}
