package vn.edu.ptit.oop.nhom08.common;

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

public abstract class BaseWindow extends JFrame {
    protected JMenuBar menuBar;
    protected JMenu fileMenu, viewMenu, helpMenu;
    protected JToolBar toolBar;
    protected JPanel statusBar;
    protected JLabel statusLabel;

    public BaseWindow(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setIconImage(createColorIcon(32, new Color(41, 128, 185), "H").getImage());
        initMenuBar();
        initToolBar();
        initStatusBar();
        initUI();
    }

    protected abstract void initUI();

    protected void showAbout() {
        JOptionPane.showMessageDialog(this, getTitle(), "About", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void setStatus(String msg) {
        statusLabel.setText(" " + msg);
    }

    protected static ImageIcon createColorIcon(int size, Color bg, String symbol) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, size, size, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size == 32 ? 20 : 11));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(symbol)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(symbol, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }

    protected JButton addToolButton(Color color, String symbol, String text, String tooltip, ActionListener action) {
        JButton btn = new JButton(text, createColorIcon(16, color, symbol));
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.addActionListener(action);
        toolBar.add(btn);
        return btn;
    }

    private void initMenuBar() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File"); fileMenu.setMnemonic(KeyEvent.VK_F);
        viewMenu = new JMenu("View"); viewMenu.setMnemonic(KeyEvent.VK_V);
        helpMenu = new JMenu("Help"); helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        fileMenu.add(exitItem);

        JMenuItem aboutItem = new JMenuItem("About...");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);
    }

    private void initStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusLabel = new JLabel(" Sẵn sàng");
        statusLabel.setBorder(new EmptyBorder(4, 6, 4, 6));
        statusBar.add(statusLabel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
}
