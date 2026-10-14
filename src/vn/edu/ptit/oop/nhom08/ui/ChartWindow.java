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

public class ChartWindow extends JFrame {
    private final BookingService service;
    private final BarChartPanel barChartPanel = new BarChartPanel();
    private final LineChartPanel lineChartPanel = new LineChartPanel();

    public ChartWindow(BookingService service) {
        super("Biểu đồ Thống kê Công suất phòng & Doanh thu 14 ngày - Nhóm8HotelBooking");
        this.service = service;
        setSize(980, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Biểu đồ Cột (BarChart): Công suất phòng (%)", barChartPanel);
        tabs.addTab("2. Biểu đồ Đường (LineChart): Doanh thu 14 ngày gần nhất", lineChartPanel);

        add(tabs, BorderLayout.CENTER);
        refreshCharts();
    }

    public void refreshCharts() {
        barChartPanel.setData(service.calculateOccupancyRateByRoomType());
        lineChartPanel.setData(service.getRevenueLast14Days());
    }
}
