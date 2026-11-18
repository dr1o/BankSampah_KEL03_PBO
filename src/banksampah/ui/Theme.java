package banksampah.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Palet warna dan komponen visual modern yang terinspirasi dari app-catalog
 * (Navy #0F294A, Tech Blue #1D4ED8, Ice Blue #EFF6FF, Cool Grey #F1F5F9).
 */
public final class Theme {

    public static final Color NAVY = Color.decode("#0F294A");
    public static final Color TECH_BLUE = Color.decode("#1D4ED8");
    public static final Color TECH_BLUE_HOVER = Color.decode("#1E40AF");
    public static final Color ICE_BLUE = Color.decode("#EFF6FF");
    public static final Color COOL_GREY = Color.decode("#F1F5F9");
    public static final Color STEEL_GREY = Color.decode("#E2E8F0");
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT_DARK = Color.decode("#0F172A");
    public static final Color TEXT_MUTED = Color.decode("#64748B");

    public static final Color SUCCESS_BG = Color.decode("#DCFCE7");
    public static final Color SUCCESS_FG = Color.decode("#166534");
    public static final Color WARN_BG = Color.decode("#FEF9C3");
    public static final Color WARN_FG = Color.decode("#854D0E");
    public static final Color DANGER_BG = Color.decode("#FEE2E2");
    public static final Color DANGER_FG = Color.decode("#991B1B");
    public static final Color INFO_BG = Color.decode("#DBEAFE");
    public static final Color INFO_FG = Color.decode("#1E40AF");
    public static final Color PURPLE_BG = Color.decode("#EDE9FE");
    public static final Color PURPLE_FG = Color.decode("#5B21B6");

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 12);
    public static final Font FONT_STAT = new Font("Segoe UI", Font.BOLD, 22);

    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");

    private Theme() {
    }

    public static String formatRupiah(double nominal) {
        NumberFormat nf = NumberFormat.getNumberInstance(ID_LOCALE);
        nf.setMaximumFractionDigits(0);
        return "Rp" + nf.format(Math.round(nominal));
    }

    public static String formatTanggal(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("dd MMM yyyy HH:mm", ID_LOCALE).format(date);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(TECH_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JButton createNavyButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(NAVY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(NAVY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(STEEL_GREY, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(DANGER_BG);
        btn.setForeground(DANGER_FG);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }

    public static JLabel createStatusBadge(String status) {
        if (status == null) {
            return createBadge("-", COOL_GREY, TEXT_MUTED);
        }
        switch (status) {
            case "SELESAI":
                return createBadge("SELESAI", SUCCESS_BG, SUCCESS_FG);
            case "DITIMBANG":
                return createBadge("DITIMBANG", INFO_BG, INFO_FG);
            case "DIVERIFIKASI":
                return createBadge("DIVERIFIKASI", PURPLE_BG, PURPLE_FG);
            case "DIAJUKAN":
                return createBadge("DIAJUKAN", WARN_BG, WARN_FG);
            case "DITOLAK":
                return createBadge("DITOLAK", DANGER_BG, DANGER_FG);
            default:
                return createBadge(status, COOL_GREY, TEXT_MUTED);
        }
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(STEEL_GREY);
        table.setSelectionBackground(ICE_BLUE);
        table.setSelectionForeground(NAVY);
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(COOL_GREY);
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static DefaultTableCellRenderer createStatusCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected) {
                    switch (status) {
                        case "SELESAI":
                            label.setBackground(SUCCESS_BG);
                            label.setForeground(SUCCESS_FG);
                            break;
                        case "DITIMBANG":
                            label.setBackground(INFO_BG);
                            label.setForeground(INFO_FG);
                            break;
                        case "DIAJUKAN":
                            label.setBackground(WARN_BG);
                            label.setForeground(WARN_FG);
                            break;
                        case "DITOLAK":
                            label.setBackground(DANGER_BG);
                            label.setForeground(DANGER_FG);
                            break;
                        default:
                            label.setBackground(Color.WHITE);
                            label.setForeground(TEXT_DARK);
                    }
                }
                return label;
            }
        };
    }

    public static Border cardPadding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Terjadi Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Panel kartu dengan sudut membulat, border Steel Grey, dan garis aksen atas 3px
     * menyerupai ApplicationCard pada app-catalog.
     */
    @SuppressWarnings({"serial", "this-escape"})
    public static class CardPanel extends JPanel {
        private final Color accentColor;
        private final int radius = 14;

        public CardPanel() {
            this(NAVY);
        }

        public CardPanel(Color accentColor) {
            this.accentColor = accentColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Latar putih membulat
            g2.setColor(SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            // Garis aksen atas 4px
            if (accentColor != null) {
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 4);
                g2.setClip(null);
            }

            // Border halus
            g2.setColor(STEEL_GREY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
