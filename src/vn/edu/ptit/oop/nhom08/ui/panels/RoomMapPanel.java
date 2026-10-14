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

public class RoomMapPanel extends JPanel {
    private final List<Room> rooms;
    private final Consumer<Room> onRoomSelected;
    private final Map<Room, Rectangle> roomRects = new HashMap<>();
    private Room selectedRoom;

    private static final Color COLOR_AVAILABLE = new Color(46, 204, 113);
    private static final Color COLOR_OCCUPIED = new Color(231, 76, 60);
    private static final Color COLOR_MAINTENANCE = new Color(241, 196, 15);
    private static final Color COLOR_CLEANING_BADGE = new Color(41, 128, 185);

    public RoomMapPanel(List<Room> rooms, Consumer<Room> onRoomSelected) {
        this.rooms = rooms;
        this.onRoomSelected = onRoomSelected;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<Room, Rectangle> entry : roomRects.entrySet()) {
                    if (entry.getValue().contains(e.getPoint())) {
                        selectedRoom = entry.getKey();
                        repaint();
                        if (onRoomSelected != null) onRoomSelected.accept(selectedRoom);
                        break;
                    }
                }
            }
        });
    }

    public Room getSelectedRoom() { return selectedRoom; }
    public void setSelectedRoom(Room room) { this.selectedRoom = room; repaint(); }

    private Map<Integer, List<Room>> groupRoomsByFloorDescending() {
        Map<Integer, List<Room>> map = new TreeMap<>(Collections.reverseOrder());
        for (Room r : rooms) {
            map.computeIfAbsent(r.getFloor(), k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    @Override
    public Dimension getPreferredSize() {
        Map<Integer, List<Room>> byFloor = groupRoomsByFloorDescending();
        int rows = Math.max(4, byFloor.size());
        int cols = byFloor.values().stream().mapToInt(List::size).max().orElse(6);
        int prefW = Math.max(780, 80 + cols * 122);
        int prefH = Math.max(380, 60 + rows * 82);
        return new Dimension(prefW, prefH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        roomRects.clear();
        drawLegend(g2, 16, 14);

        Map<Integer, List<Room>> byFloor = groupRoomsByFloorDescending();
        if (byFloor.isEmpty()) {
            g2.dispose();
            return;
        }

        int topOffset = 46, leftWidth = 76, gap = 12;
        int rows = byFloor.size();
        int maxCols = byFloor.values().stream().mapToInt(List::size).max().orElse(6);

        int availW = getWidth() - leftWidth - 20;
        int availH = getHeight() - topOffset - 15;
        int cellW = Math.max(92, (availW - (maxCols - 1) * gap) / Math.max(1, maxCols));
        int cellH = Math.max(64, (availH - (rows - 1) * gap) / Math.max(1, rows));
        NumberFormat vnd = NumberFormat.getNumberInstance(HotelBookingApp.VI_LOCALE);

        int rowIdx = 0;
        for (Map.Entry<Integer, List<Room>> floorEntry : byFloor.entrySet()) {
            int floorNo = floorEntry.getKey();
            List<Room> floorRooms = floorEntry.getValue();
            int y = topOffset + rowIdx * (cellH + gap);

            // Vẽ nhãn Tầng ở đầu hàng
            g2.setColor(new Color(44, 62, 80));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Tầng " + floorNo, 12, y + cellH / 2 + 5);

            for (int colIdx = 0; colIdx < floorRooms.size(); colIdx++) {
                Room room = floorRooms.get(colIdx);
                int x = leftWidth + colIdx * (cellW + gap);

                roomRects.put(room, new Rectangle(x, y, cellW, cellH));
                g2.setColor(room.getStatus() == RoomStatus.AVAILABLE ? COLOR_AVAILABLE :
                        room.getStatus() == RoomStatus.OCCUPIED ? COLOR_OCCUPIED : COLOR_MAINTENANCE);
                g2.fillRoundRect(x, y, cellW, cellH, 14, 14);

                if (room.equals(selectedRoom)) {
                    g2.setColor(new Color(25, 42, 86));
                    g2.setStroke(new BasicStroke(3.5f));
                } else {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setStroke(new BasicStroke(1.2f));
                }
                g2.drawRoundRect(x, y, cellW, cellH, 14, 14);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("P." + room.getRoomNumber(), x + 9, y + 20);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(room.getRoomTypeName(), x + 9, y + 35);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                g2.drawString(vnd.format(room.getPricePerNight() / 1000) + "k/đêm", x + 9, y + 49);

                if (room.isCleaningRequested()) {
                    int badgeW = cellW - 12;
                    int badgeH = 15;
                    int bx = x + 6;
                    int by = y + cellH - badgeH - 4;
                    g2.setColor(COLOR_CLEANING_BADGE);
                    g2.fillRoundRect(bx, by, badgeW, badgeH, 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String tag = "CẦN DỌN PHÒNG";
                    g2.drawString(tag, bx + (badgeW - g2.getFontMetrics().stringWidth(tag)) / 2, by + 11);
                }
            }
            rowIdx++;
        }
        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        Object[][] items = {
                {COLOR_AVAILABLE, "Trống (Available)"},
                {COLOR_OCCUPIED, "Đang có khách (Occupied)"},
                {COLOR_MAINTENANCE, "Bảo trì (Maintenance)"},
                {COLOR_CLEANING_BADGE, "Yêu cầu dọn phòng"}
        };
        int curX = x;
        for (Object[] it : items) {
            g2.setColor((Color) it[0]);
            g2.fillRoundRect(curX, y, 16, 16, 5, 5);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString((String) it[1], curX + 22, y + 13);
            curX += 172;
        }
    }
}
