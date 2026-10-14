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

public class BarChartPanel extends JPanel {
    private Map<String, Double> data = Collections.emptyMap();

    public void setData(Map<String, Double> data) {
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
        String title = "Tỷ lệ Lấp đầy (Công suất %) theo Hạng phòng";
        g2.drawString(title, (w - g2.getFontMetrics().stringWidth(title)) / 2, 36);

        int left = 80, right = 50, top = 70, bottom = 70;
        int chartW = w - left - right, chartH = h - top - bottom;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int pct = 0; pct <= 100; pct += 20) {
            int y = top + chartH - (pct * chartH / 100);
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(left, y, left + chartW, y);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(pct + "%", left - 42, y + 5);
        }

        g2.setColor(Color.BLACK);
        g2.drawLine(left, top, left, top + chartH);
        g2.drawLine(left, top + chartH, left + chartW, top + chartH);

        if (!data.isEmpty()) {
            int n = data.size();
            int slotW = chartW / n;
            int barW = Math.min(110, slotW / 2);
            Color[] colors = {new Color(52, 152, 219), new Color(46, 204, 113), new Color(155, 89, 182)};
            int idx = 0;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double val = Math.min(100.0, entry.getValue());
                int barH = (int) Math.round((val / 100.0) * chartH);
                int x = left + idx * slotW + (slotW - barW) / 2;
                int y = top + chartH - barH;

                g2.setColor(colors[idx % colors.length]);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String valStr = String.format("%.1f%%", val);
                g2.drawString(valStr, x + (barW - g2.getFontMetrics().stringWidth(valStr)) / 2, y - 8);
                g2.drawString(entry.getKey(), x + (barW - g2.getFontMetrics().stringWidth(entry.getKey())) / 2, top + chartH + 25);
                idx++;
            }
        }
        g2.dispose();
    }
}
