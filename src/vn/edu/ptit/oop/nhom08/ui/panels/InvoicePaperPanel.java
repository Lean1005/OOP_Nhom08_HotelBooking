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

public class InvoicePaperPanel extends JPanel {
    private Invoice invoice;

    public InvoicePaperPanel() {
        setBackground(new Color(228, 232, 238));
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        int extraServiceCount = (invoice != null) ? invoice.getServiceCharges().size() : 0;
        int extraGuestLines = (invoice != null) ? Math.max(0, invoice.getBooking().getGuests().size() - 1) : 0;
        int dynamicHeight = 700 + extraGuestLines * 18 + extraServiceCount * 42 + (extraServiceCount > 0 ? 115 : 0);
        setPreferredSize(new Dimension(680, dynamicHeight));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (invoice == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelW = getWidth();
        int paperW = 640;
        int paperX = Math.max(15, (panelW - paperW) / 2);
        int paperY = 16;
        int paperH = getPreferredSize().height - 32;

        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(paperX + 5, paperY + 6, paperW, paperH, 10, 10);
        g2.setColor(new Color(255, 254, 250));
        g2.fillRoundRect(paperX, paperY, paperW, paperH, 10, 10);
        g2.setColor(new Color(180, 175, 165));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(paperX, paperY, paperW, paperH, 10, 10);

        g2.setColor(new Color(25, 55, 109));
        g2.fillRoundRect(paperX, paperY, paperW, 10, 10, 10);

        int curY = paperY + 26;
        int leftX = paperX + 28;
        int rightX = paperX + paperW - 28;

        drawHotelLogo(g2, leftX, curY);
        g2.setColor(new Color(25, 55, 109));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.drawString("CÔNG TY TNHH " + Invoice.COMPANY_NAME, leftX + 58, curY + 18);

        g2.setColor(new Color(90, 95, 105));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.drawString("MST: 0108889999 - Đ/c: Học viện Công nghệ Bưu chính Viễn thông (PTIT)", leftX + 58, curY + 35);
        g2.drawString("Hotline: 1900 8888 | Website: www.nhom8hotelbooking.vn", leftX + 58, curY + 50);

        drawBarcode(g2, rightX - 115, curY + 4, invoice.getBooking().getBookingId());

        curY += 68;
        g2.setColor(new Color(210, 215, 220));
        g2.drawLine(leftX, curY, rightX, curY);

        curY += 28;
        g2.setColor(new Color(185, 28, 28));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 21));
        String title = "HÓA ĐƠN THANH TOÁN DỊCH VỤ KHÁCH SẠN";
        g2.drawString(title, paperX + (paperW - g2.getFontMetrics().stringWidth(title)) / 2, curY);

        curY += 20;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        g2.setColor(new Color(100, 100, 100));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        String subTitle = "Số HĐ: " + invoice.getBooking().getBookingId() + "   |   Ngày lập: " + invoice.getIssuedAt().format(dtf);
        g2.drawString(subTitle, paperX + (paperW - g2.getFontMetrics().stringWidth(subTitle)) / 2, curY);

        curY += 16;
        Booking b = invoice.getBooking();
        List<Guest> guestList = b.getGuests();
        int boxH = 88 + Math.max(0, guestList.size() - 1) * 18;

        g2.setColor(new Color(245, 247, 250));
        g2.fillRoundRect(leftX, curY, paperW - 56, boxH, 8, 8);
        g2.setColor(new Color(215, 220, 228));
        g2.drawRoundRect(leftX, curY, paperW - 56, boxH, 8, 8);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Guest primary = b.getPrimaryGuest();
        g2.setColor(new Color(40, 40, 40));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Khách đại diện:", leftX + 14, curY + 22);
        g2.drawString("Số CCCD / ĐT:", leftX + 14, curY + 44);
        g2.drawString("Chính sách giá:", leftX + 14, curY + 66);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString(primary.getName() + " (" + primary.getGender().getLabel() + ")", leftX + 112, curY + 22);
        g2.drawString(primary.getCccd() + " - " + primary.getPhone(), leftX + 112, curY + 44);
        g2.drawString(invoice.getAppliedPolicy().getPolicyName(), leftX + 112, curY + 66);

        int midX = leftX + 320;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Phòng lưu trú:", midX, curY + 22);
        g2.drawString("Nhận - Trả:", midX, curY + 44);
        g2.drawString("Số lượng khách:", midX, curY + 66);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString("Phòng " + b.getRoom().getRoomNumber() + " (" + b.getRoom().getRoomTypeName() + ")", midX + 100, curY + 22);
        g2.drawString(b.getCheckIn().format(df) + " -> " + b.getCheckOut().format(df), midX + 100, curY + 44);
        g2.drawString(guestList.size() + " khách (" + b.nights() + " đêm)", midX + 100, curY + 66);

        if (guestList.size() > 1) {
            int gy = curY + 86;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(25, 55, 109));
            g2.drawString("Khách ở cùng:", leftX + 14, gy);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 1; i < guestList.size(); i++) {
                Guest acc = guestList.get(i);
                g2.drawString(String.format("• %s (%s - CCCD: %s - SĐT: %s)",
                        acc.getName(), acc.getGender().getLabel(), acc.getCccd(), acc.getPhone()), leftX + 112, gy);
                gy += 18;
            }
        }

        curY += boxH + 16;
        int tableW = paperW - 56;
        g2.setColor(new Color(25, 55, 109));
        g2.fillRoundRect(leftX, curY, tableW, 30, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("STT", leftX + 12, curY + 20);
        g2.drawString("HÌNH", leftX + 48, curY + 20);
        g2.drawString("NỘI DUNG HẠNG MỤC / DỊCH VỤ", leftX + 98, curY + 20);
        g2.drawString("SL", leftX + 375, curY + 20);
        g2.drawString("THÀNH TIỀN (VNĐ)", leftX + 455, curY + 20);

        curY += 30;
        NumberFormat vnd = NumberFormat.getNumberInstance(HotelBookingApp.VI_LOCALE);

        drawTableRow(g2, leftX, curY, tableW, 1, null,
                "Tiền thuê Phòng " + b.getRoom().getRoomNumber() + " (" + b.getRoom().getRoomTypeName() + " - " + guestList.size() + " khách)",
                b.nights() + " đêm", vnd.format(invoice.getRoomCharge()) + " đ");
        curY += 42;

        List<ServiceCharge> charges = invoice.getServiceCharges();
        int stt = 2;
        for (ServiceCharge sc : charges) {
            drawTableRow(g2, leftX, curY, tableW, stt++, sc.getType(),
                    sc.getServiceName(), "01 gói", vnd.format(sc.getAmount()) + " đ");
            curY += 42;
        }

        if (!charges.isEmpty()) {
            curY += 10;
            g2.setColor(new Color(25, 55, 109));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("HÌNH ẢNH XÁC NHẬN DỊCH VỤ ĐÃ SỬ DỤNG:", leftX, curY + 14);
            curY += 22;

            int cardW = 136, cardH = 78, cardGap = 12, cardX = leftX;
            for (ServiceCharge sc : charges) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cardX, curY, cardW, cardH, 10, 10);
                g2.setColor(sc.getType().getThemeColor());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cardX, curY, cardW, cardH, 10, 10);

                drawServiceIllustration(g2, sc.getType(), cardX + (cardW - 36) / 2, curY + 6, 36);

                g2.setColor(new Color(40, 40, 40));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String label = sc.getType().getShortTitle();
                g2.drawString(label, cardX + (cardW - g2.getFontMetrics().stringWidth(label)) / 2, curY + 56);

                g2.setColor(new Color(185, 28, 28));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String priceStr = "+" + vnd.format(sc.getAmount()) + " đ";
                g2.drawString(priceStr, cardX + (cardW - g2.getFontMetrics().stringWidth(priceStr)) / 2, curY + 71);

                cardX += cardW + cardGap;
            }
            curY += cardH + 8;
        }

        curY += 12;
        g2.setColor(new Color(250, 242, 242));
        g2.fillRoundRect(leftX, curY, tableW, 66, 8, 8);
        g2.setColor(new Color(220, 180, 180));
        g2.drawRoundRect(leftX, curY, tableW, 66, 8, 8);

        g2.setColor(new Color(70, 70, 70));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString("Cộng tiền phòng: " + vnd.format(invoice.getRoomCharge()) + " đ   |   Cộng phí dịch vụ: "
                + vnd.format(invoice.getTotalServiceCharges()) + " đ", leftX + 14, curY + 24);

        g2.setColor(new Color(185, 28, 28));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.drawString("TỔNG CỘNG THANH TOÁN:", leftX + 14, curY + 52);
        String grandTotalStr = vnd.format(invoice.getGrandTotal()) + " VNĐ";
        g2.drawString(grandTotalStr, rightX - g2.getFontMetrics().stringWidth(grandTotalStr) - 16, curY + 52);

        curY += 86;
        g2.setColor(new Color(60, 60, 60));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("KHÁCH HÀNG ĐẠI DIỆN", leftX + 35, curY);
        g2.drawString("ĐẠI DIỆN NHÓM8HOTELBOOKING", rightX - 215, curY);

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g2.setColor(Color.GRAY);
        g2.drawString("(Ký, ghi rõ họ tên)", leftX + 45, curY + 16);
        g2.drawString("(Ký, đóng dấu mộc đỏ)", rightX - 182, curY + 16);

        g2.setColor(new Color(30, 60, 140));
        g2.setFont(new Font("Segoe Script", Font.ITALIC, 15));
        g2.drawString(primary.getName(), leftX + 28, curY + 68);

        drawRedCompanyStamp(g2, rightX - 195, curY + 8);
        g2.dispose();
    }

    private void drawTableRow(Graphics2D g2, int x, int y, int w, int stt, ServiceType type,
                              String name, String qty, String amount) {
        g2.setColor(stt % 2 == 0 ? new Color(249, 250, 252) : Color.WHITE);
        g2.fillRect(x, y, w, 42);
        g2.setColor(new Color(230, 232, 236));
        g2.drawLine(x, y + 42, x + w, y + 42);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(String.format("%02d", stt), x + 14, y + 26);

        drawServiceIllustration(g2, type, x + 46, y + 5, 32);

        g2.setColor(new Color(30, 35, 45));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString(name, x + 98, y + 26);
        g2.drawString(qty, x + 370, y + 26);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(amount, x + w - g2.getFontMetrics().stringWidth(amount) - 14, y + 26);
    }

    private void drawServiceIllustration(Graphics2D g2, ServiceType type, int x, int y, int size) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (type == null) {
            g.setColor(new Color(52, 152, 219, 35));
            g.fillRoundRect(x, y, size, size, 8, 8);
            g.setColor(new Color(41, 128, 185));
            g.fillRoundRect(x + 4, y + size / 2, size - 8, size / 4, 4, 4);
            g.fillRect(x + 4, y + size / 3, 4, size / 2);
            g.setColor(new Color(236, 240, 241));
            g.fillRoundRect(x + 9, y + size / 2 - 4, 8, 5, 3, 3);
        } else {
            Color c = type.getThemeColor();
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35));
            g.fillRoundRect(x, y, size, size, 8, 8);
            g.setColor(c);
            g.setStroke(new BasicStroke(1.8f));

            switch (type) {
                case MINIBAR:
                    int cx = x + size / 2;
                    int[] px = {x + 7, x + size - 7, cx};
                    int[] py = {y + 9, y + 9, y + size / 2 + 3};
                    g.fillPolygon(px, py, 3);
                    g.drawLine(cx, y + size / 2 + 2, cx, y + size - 6);
                    g.drawLine(cx - 6, y + size - 6, cx + 6, y + size - 6);
                    g.setColor(new Color(231, 76, 60));
                    g.drawLine(cx + 1, y + 11, x + size - 6, y + 4);
                    break;
                case LAUNDRY:
                    int mx = x + size / 2;
                    g.drawArc(mx - 4, y + 4, 8, 6, 0, 180);
                    int[] sx = {mx - 10, mx - 4, mx + 4, mx + 10, mx + 7, mx + 7, mx - 7, mx - 7};
                    int[] sy = {y + 13, y + 9, y + 9, y + 13, y + 17, y + size - 5, y + size - 5, y + 17};
                    g.fillPolygon(sx, sy, 8);
                    g.setColor(Color.WHITE);
                    g.fillOval(mx - 2, y + 15, 4, 4);
                    break;
                case BREAKFAST:
                    g.fillRoundRect(x + 7, y + 13, size - 16, size - 19, 6, 6);
                    g.drawOval(x + size - 13, y + 15, 6, 8);
                    g.drawLine(x + 5, y + size - 5, x + size - 7, y + size - 5);
                    g.drawArc(x + 10, y + 5, 4, 6, 45, 180);
                    g.drawArc(x + 16, y + 5, 4, 6, 45, 180);
                    break;
                case SPA:
                    int scx = x + size / 2;
                    int scy = y + size / 2 + 2;
                    g.fillOval(scx - 4, scy - 11, 8, 14);
                    g.fillOval(scx - 11, scy - 7, 9, 11);
                    g.fillOval(scx + 2, scy - 7, 9, 11);
                    g.setColor(new Color(100, 100, 110));
                    g.fillRoundRect(scx - 9, scy + 5, 18, 5, 4, 4);
                    break;
            }
        }
        g.dispose();
    }

    private void drawRedCompanyStamp(Graphics2D g2, int x, int y) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(-11), x + 65, y + 60);

        Color stampRed = new Color(210, 22, 22, 205);
        g.setColor(stampRed);

        int d = 116;
        g.setStroke(new BasicStroke(3.2f));
        g.drawOval(x, y, d, d);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(x + 6, y + 6, d - 12, d - 12);
        g.drawOval(x + 26, y + 26, d - 52, d - 52);

        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        String topText = "CÔNG TY TNHH";
        g.drawString(topText, x + (d - g.getFontMetrics().stringWidth(topText)) / 2, y + 19);

        String botText = "M.S.D.N: 0108889999";
        g.drawString(botText, x + (d - g.getFontMetrics().stringWidth(botText)) / 2, y + d - 11);

        g.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        g.drawString("★", x + 10, y + d / 2 + 4);
        g.drawString("★", x + d - 21, y + d / 2 + 4);

        g.setColor(new Color(255, 250, 250, 235));
        g.fillRoundRect(x + 4, y + d / 2 - 14, d - 8, 28, 6, 6);
        g.setColor(stampRed);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(x + 4, y + d / 2 - 14, d - 8, 28, 6, 6);

        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        String centerName = Invoice.COMPANY_NAME;
        g.drawString(centerName, x + (d - g.getFontMetrics().stringWidth(centerName)) / 2, y + d / 2 - 1);

        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        String paidLabel = "ĐÃ THU TIỀN";
        g.drawString(paidLabel, x + (d - g.getFontMetrics().stringWidth(paidLabel)) / 2, y + d / 2 + 11);

        g.setTransform(old);

        g.setColor(new Color(20, 50, 140, 210));
        g.setFont(new Font("Segoe Script", Font.BOLD | Font.ITALIC, 15));
        g.drawString("Nhóm 8 Hotel", x + 15, y + d + 6);

        g.dispose();
    }

    private void drawHotelLogo(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(25, 55, 109));
        g2.fillRoundRect(x, y + 4, 46, 46, 12, 12);
        g2.setColor(new Color(241, 196, 15));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.drawString("N8", x + 8, y + 35);
    }

    private void drawBarcode(Graphics2D g2, int x, int y, String code) {
        g2.setColor(Color.BLACK);
        int curX = x;
        for (int i = 0; i < 24; i++) {
            int barW = (i % 3 == 0) ? 3 : 1;
            g2.fillRect(curX, y, barW, 28);
            curX += barW + ((i % 2 == 0) ? 2 : 3);
        }
        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2.drawString("* " + code + " *", x + 12, y + 40);
    }
}
