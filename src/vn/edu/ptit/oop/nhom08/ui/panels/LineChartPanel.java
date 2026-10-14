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

public class LineChartPanel extends JPanel {
    private Map<LocalDate, Double> data = Collections.emptyMap();

    public void setData(Map<LocalDate, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String title = "Biến động Doanh thu 14 Ngày gần nhất (Triệu VNĐ)";
        g2.drawString(title, (w - g2.getFontMetrics().stringWidth(title)) / 2, 36);

        int left = 80, right = 45, top = 70, bottom = 75;
        int chartW = w - left - right, chartH = h - top - bottom;

        double maxMillion = data.values().stream().mapToDouble(v -> v / 1_000_000.0).max().orElse(10.0);
        maxMillion = Math.ceil(maxMillion / 2.0) * 2.0;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            double labelVal = maxMillion * i / 5.0;
            int y = top + chartH - (int) (chartH * (labelVal / maxMillion));
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(left, y, left + chartW, y);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.1f tr", labelVal), left - 55, y + 5);
        }

        g2.setColor(Color.BLACK);
        g2.drawLine(left, top, left, top + chartH);
        g2.drawLine(left, top + chartH, left + chartW, top + chartH);

        if (data.size() > 1) {
            List<Map.Entry<LocalDate, Double>> list = new ArrayList<>(data.entrySet());
            int n = list.size();
            int[] px = new int[n];
            int[] py = new int[n];
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

            for (int i = 0; i < n; i++) {
                double mVal = list.get(i).getValue() / 1_000_000.0;
                px[i] = left + (i * chartW) / (n - 1);
                py[i] = top + chartH - (int) ((mVal / maxMillion) * chartH);
            }

            g2.setColor(new Color(231, 76, 60));
            g2.setStroke(new BasicStroke(2.8f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
            }

            for (int i = 0; i < n; i++) {
                g2.setColor(new Color(192, 57, 43));
                g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(list.get(i).getKey().format(fmt), px[i] - 16, top + chartH + 22);
                g2.drawString(String.format("%.1f", list.get(i).getValue() / 1_000_000.0), px[i] - 10, py[i] - 10);
            }
        }
        g2.dispose();
    }
}
