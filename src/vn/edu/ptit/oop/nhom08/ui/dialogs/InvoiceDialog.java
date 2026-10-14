package vn.edu.ptit.oop.nhom08.ui.dialogs;

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

public class InvoiceDialog extends JDialog {
    private final Booking booking;
    private final BookingService service;
    private final JCheckBox chkMinibar;
    private final JCheckBox chkLaundry;
    private final JCheckBox chkBreakfast;
    private final JCheckBox chkSpa;
    private final InvoicePaperPanel invoicePaperPanel;
    private boolean paid = false;

    public InvoiceDialog(JFrame parent, Booking booking, BookingService service) {
        super(parent, "Check-out & Hóa đơn Đỏ - Phòng " + booking.getRoom().getRoomNumber() + " | Nhóm8HotelBooking", true);
        this.booking = booking;
        this.service = service;
        setSize(760, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        chkMinibar = new JCheckBox("Minibar (Đồ uống & Snack) - 150.000đ");
        chkLaundry = new JCheckBox("Giặt ủi cao cấp - 120.000đ");
        chkBreakfast = new JCheckBox("Buffet Sáng phụ thêm - 200.000đ");
        chkSpa = new JCheckBox("Dịch vụ Spa & Xông hơi - 350.000đ");

        Font chkFont = new Font("Segoe UI", Font.BOLD, 12);
        chkMinibar.setFont(chkFont);
        chkLaundry.setFont(chkFont);
        chkBreakfast.setFont(chkFont);
        chkSpa.setFont(chkFont);

        JPanel checks = new JPanel(new GridLayout(2, 2, 12, 8));
        checks.setBorder(BorderFactory.createTitledBorder("Chọn phụ phí dịch vụ phát sinh (Tích chọn để in Hình ảnh Dịch vụ lên Hóa đơn)"));
        ActionListener al = e -> refreshPreview();
        chkMinibar.addActionListener(al);
        chkLaundry.addActionListener(al);
        chkBreakfast.addActionListener(al);
        chkSpa.addActionListener(al);
        checks.add(chkMinibar);
        checks.add(chkLaundry);
        checks.add(chkBreakfast);
        checks.add(chkSpa);

        invoicePaperPanel = new InvoicePaperPanel();
        JScrollPane scrollPane = new JScrollPane(invoicePaperPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bản xem trước Hóa đơn Giá trị Gia tăng (Có đóng dấu mộc đỏ Nhóm8HotelBooking)"));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton btnExportPng = new JButton("Xuất ảnh Hóa đơn (.PNG)");
        JButton btnPay = new JButton("Xác nhận Thanh toán & Check-out");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPay.setForeground(new Color(180, 20, 20));
        JButton btnClose = new JButton("Đóng");

        btnExportPng.addActionListener(e -> exportInvoiceImage());
        btnPay.addActionListener(e -> {
            service.checkOut(booking, collectCharges());
            paid = true;
            JOptionPane.showMessageDialog(this,
                    "Thanh toán & Đóng dấu hóa đơn thành công!\nPhòng " + booking.getRoom().getRoomNumber()
                            + " đã chuyển về trạng thái Trống và bật cờ Cần dọn phòng.",
                    "Hoàn tất Check-out", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        btnClose.addActionListener(e -> dispose());

        btns.add(btnExportPng);
        btns.add(btnPay);
        btns.add(btnClose);

        add(checks, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        refreshPreview();
    }

    private List<ServiceCharge> collectCharges() {
        List<ServiceCharge> list = new ArrayList<>();
        if (chkMinibar.isSelected())
            list.add(new ServiceCharge("Minibar (Đồ uống & Snack)", 150_000, ServiceType.MINIBAR));
        if (chkLaundry.isSelected())
            list.add(new ServiceCharge("Giặt ủi cao cấp (24h)", 120_000, ServiceType.LAUNDRY));
        if (chkBreakfast.isSelected())
            list.add(new ServiceCharge("Buffet Sáng Á - Âu phụ thêm", 200_000, ServiceType.BREAKFAST));
        if (chkSpa.isSelected())
            list.add(new ServiceCharge("Dịch vụ Spa & Xông hơi thảo dược", 350_000, ServiceType.SPA));
        return list;
    }

    private void refreshPreview() {
        Invoice inv = new Invoice(booking, service.getPricingPolicy());
        collectCharges().forEach(inv::addServiceCharge);
        invoicePaperPanel.setInvoice(inv);
    }

    private void exportInvoiceImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("HoaDon_Nhom8HotelBooking_" + booking.getBookingId() + ".png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = new BufferedImage(
                        invoicePaperPanel.getWidth(),
                        invoicePaperPanel.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                invoicePaperPanel.paint(g2);
                g2.dispose();
                ImageIO.write(img, "png", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Đã lưu ảnh Hóa đơn tại:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất ảnh: " + ex.getMessage());
            }
        }
    }

    public boolean isPaid() { return paid; }
}
