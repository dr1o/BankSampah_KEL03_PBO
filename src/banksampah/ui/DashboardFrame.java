package banksampah.ui;

import banksampah.pengguna.Admin;
import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;
import banksampah.pengguna.Petugas;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Jendela utama setelah login. Memuat panel peran secara dinamis (Polymorphism)
 * dan menampilkan jejak eksekusi OOP secara real-time di bagian bawah.
 */
@SuppressWarnings({"serial", "this-escape"})
public class DashboardFrame extends JFrame {

    private final BankSampahContext context;
    private Pengguna user;
    private RefreshablePanel activePanel;
    private JPanel contentHolder;
    private JLabel lblLatestOop;

    public DashboardFrame(BankSampahContext context, Pengguna user) {
        this.context = context;
        this.user = user;
        setTitle("Sistem Bank Sampah \u2014 " + user.getNama() + " (" + user.getClass().getSimpleName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.COOL_GREY);

        // 1. Header Navy ala app-catalog
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel brandBox = new JPanel(new BorderLayout(0, 2));
        brandBox.setOpaque(false);
        JLabel appTitle = new JLabel("Sistem Bank Sampah Digital");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("Arsitektur OOP Java Murni  \u2022  Database Cloud Neon PostgreSQL");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appSub.setForeground(Color.decode("#93C5FD"));
        brandBox.add(appTitle, BorderLayout.NORTH);
        brandBox.add(appSub, BorderLayout.SOUTH);

        JPanel userControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userControls.setOpaque(false);

        JLabel lblWelcome = new JLabel("Halo, " + user.getNama());
        lblWelcome.setFont(Theme.FONT_BODY_BOLD);
        lblWelcome.setForeground(Color.WHITE);

        JLabel roleBadge = Theme.createBadge(user.getClass().getSimpleName().toUpperCase(), Theme.ICE_BLUE, Theme.TECH_BLUE);

        JButton btnRefresh = Theme.createSecondaryButton("Segarkan Data");
        btnRefresh.addActionListener(e -> handleRefreshFromDb());

        JButton btnPassword = Theme.createSecondaryButton("Ubah Password");
        btnPassword.addActionListener(e -> openChangePasswordDialog());

        JButton btnLogout = Theme.createDangerButton("Logout");
        btnLogout.addActionListener(e -> handleLogout());

        userControls.add(lblWelcome);
        userControls.add(roleBadge);
        userControls.add(btnRefresh);
        userControls.add(btnPassword);
        userControls.add(btnLogout);

        header.add(brandBox, BorderLayout.WEST);
        header.add(userControls, BorderLayout.EAST);

        // 2. Panel Konten Tengah
        contentHolder = new JPanel(new BorderLayout());
        contentHolder.setBackground(Theme.COOL_GREY);
        contentHolder.setBorder(BorderFactory.createEmptyBorder(16, 22, 12, 22));
        mountRolePanel();

        // 3. Footer OOP Live Trace Bar (untuk presentasi PBO)
        JPanel oopBar = new JPanel(new BorderLayout(10, 0));
        oopBar.setBackground(Color.WHITE);
        oopBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.STEEL_GREY),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)
        ));

        String initialTrace = context.getOopLogs().isEmpty()
                ? "Siap mengeksekusi logika OOP..."
                : context.getOopLogs().get(0);
        lblLatestOop = new JLabel("OOP Trace: " + initialTrace);
        lblLatestOop.setFont(Theme.FONT_MONO);
        lblLatestOop.setForeground(Theme.NAVY);

        context.setLogListener(msg -> SwingUtilities.invokeLater(() -> lblLatestOop.setText("OOP Trace: " + msg)));

        JButton btnLogs = Theme.createSecondaryButton("Lihat Semua Log OOP");
        btnLogs.setFont(Theme.FONT_SMALL);
        btnLogs.addActionListener(e -> openOopLogsDialog());

        oopBar.add(lblLatestOop, BorderLayout.CENTER);
        oopBar.add(btnLogs, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(contentHolder, BorderLayout.CENTER);
        root.add(oopBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void mountRolePanel() {
        contentHolder.removeAll();
        JPanel rolePanel;
        if (user instanceof Nasabah) {
            NasabahPanel np = new NasabahPanel(context, (Nasabah) user);
            activePanel = np;
            rolePanel = np;
        } else if (user instanceof Petugas) {
            PetugasPanel pp = new PetugasPanel(context, (Petugas) user);
            activePanel = pp;
            rolePanel = pp;
        } else if (user instanceof Admin) {
            AdminPanel ap = new AdminPanel(context, (Admin) user);
            activePanel = ap;
            rolePanel = ap;
        } else {
            rolePanel = new JPanel();
            activePanel = null;
        }
        contentHolder.add(rolePanel, BorderLayout.CENTER);
        contentHolder.revalidate();
        contentHolder.repaint();
    }

    private void handleRefreshFromDb() {
        try {
            String currentId = user.getIdPengguna();
            context.loadInitialData();
            for (Pengguna p : context.getDaftarPengguna()) {
                if (p.getIdPengguna().equals(currentId)) {
                    this.user = p;
                    break;
                }
            }
            mountRolePanel();
            if (activePanel != null) {
                activePanel.refreshData();
            }
            Theme.showInfo(this, "Data berhasil disinkronkan ulang dari Neon PostgreSQL.");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal memuat ulang data dari Neon: " + ex.getMessage());
        }
    }

    private void handleLogout() {
        context.setLogListener(null);
        context.logout();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(context).setVisible(true);
            dispose();
        });
    }

    private void openChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Ubah Password", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1.0;

        JPasswordField txtLama = new JPasswordField();
        JPasswordField txtBaru = new JPasswordField();

        gc.gridy = 0; gc.gridx = 0; panel.add(new JLabel("Password Lama"), gc);
        gc.gridx = 1; panel.add(txtLama, gc);
        gc.gridy = 1; gc.gridx = 0; panel.add(new JLabel("Password Baru (min. 6)"), gc);
        gc.gridx = 1; panel.add(txtBaru, gc);

        JButton btnSimpan = Theme.createPrimaryButton("Simpan Password");
        btnSimpan.addActionListener(e -> {
            try {
                boolean ok = context.ubahPassword(user,
                        new String(txtLama.getPassword()),
                        new String(txtBaru.getPassword()));
                if (ok) {
                    dialog.dispose();
                    Theme.showInfo(this, "Password berhasil diperbarui dan disimpan ke Neon DB.");
                } else {
                    Theme.showError(dialog, "Password lama salah atau password baru kurang dari 6 karakter.");
                }
            } catch (Exception ex) {
                Theme.showError(dialog, "Gagal mengubah password: " + ex.getMessage());
            }
        });
        gc.gridy = 2; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(12, 4, 4, 4);
        panel.add(btnSimpan, gc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openOopLogsDialog() {
        JDialog dialog = new JDialog(this, "Log Eksekusi Konsep OOP (Live PBO Trace)", false);
        dialog.setSize(760, 420);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(Theme.FONT_MONO);
        area.setBackground(Theme.NAVY);
        area.setForeground(Color.decode("#E2E8F0"));
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        StringBuilder sb = new StringBuilder();
        for (String line : context.getOopLogs()) {
            sb.append(line).append("\n");
        }
        area.setText(sb.toString());

        dialog.setContentPane(new JScrollPane(area));
        dialog.setVisible(true);
    }
}
