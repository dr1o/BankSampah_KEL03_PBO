package banksampah.ui;

import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
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
 * Halaman login dan pendaftaran nasabah dengan desain modern terinspirasi app-catalog.
 */
@SuppressWarnings({"serial", "this-escape"})
public class LoginFrame extends JFrame {

    private final BankSampahContext context;
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame(BankSampahContext context) {
        this.context = context;
        setTitle("Sistem Bank Sampah \u2014 Login (PBO + Neon DB)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 620);
        setMinimumSize(new Dimension(480, 580));
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.COOL_GREY);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Hero Header Card (Navy)
        JPanel hero = new JPanel(new BorderLayout(0, 6));
        hero.setBackground(Theme.NAVY);
        hero.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel eyebrow = new JLabel("SISTEM INFORMASI PBO \u2022 KELOMPOK 03");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(Color.decode("#93C5FD"));
        topRow.add(eyebrow, BorderLayout.WEST);
        topRow.add(Theme.createBadge("\u25CF Neon PostgreSQL", Theme.SUCCESS_BG, Theme.SUCCESS_FG), BorderLayout.EAST);

        JLabel title = new JLabel("Bank Sampah Digital");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("<html>Setor sampah terpilah, kelola dompet saldo & poin, dan tukar hadiah dengan arsitektur OOP Java murni.</html>");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Color.decode("#CBD5E1"));

        hero.add(topRow, BorderLayout.NORTH);
        hero.add(title, BorderLayout.CENTER);
        hero.add(subtitle, BorderLayout.SOUTH);

        // Card Utama dengan Tab Login & Daftar
        Theme.CardPanel card = new Theme.CardPanel(Theme.TECH_BLUE);
        card.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY_BOLD);
        tabs.addTab("Masuk Akun", createLoginPanel());
        tabs.addTab("Daftar Nasabah Baru", createRegisterPanel());
        card.add(tabs, BorderLayout.CENTER);

        root.add(hero, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 12, 10, 12));

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(Theme.FONT_BODY_BOLD);
        lblEmail.setAlignmentX(LEFT_ALIGNMENT);

        emailField = new JTextField();
        emailField.setFont(Theme.FONT_BODY);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailField.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(Theme.FONT_BODY_BOLD);
        lblPass.setAlignmentX(LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.addActionListener(e -> handleLogin());

        JButton btnLogin = Theme.createPrimaryButton("Masuk ke Aplikasi");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(6));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lblPass);
        panel.add(Box.createVerticalStrut(6));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(22));

        // Bagian Demo Cepat untuk Presentasi
        JPanel demoBox = new JPanel(new BorderLayout(0, 8));
        demoBox.setBackground(Theme.ICE_BLUE);
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#BFDBFE"), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        demoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        demoBox.setAlignmentX(LEFT_ALIGNMENT);

        JLabel demoTitle = new JLabel("Isi Cepat Akun Demo (Untuk Presentasi Kelas):");
        demoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        demoTitle.setForeground(Theme.NAVY);

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoBtns.setOpaque(false);
        demoBtns.add(createDemoButton("Nasabah (Budi)", "budi@mail.com", "rahasia1"));
        demoBtns.add(createDemoButton("Petugas (Sari)", "sari@mail.com", "petugas1"));
        demoBtns.add(createDemoButton("Admin (Dewi)", "dewi@mail.com", "admin123"));

        demoBox.add(demoTitle, BorderLayout.NORTH);
        demoBox.add(demoBtns, BorderLayout.CENTER);

        panel.add(demoBox);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JButton createDemoButton(String label, String email, String pass) {
        JButton btn = Theme.createSecondaryButton(label);
        btn.setFont(Theme.FONT_SMALL);
        btn.addActionListener(e -> {
            emailField.setText(email);
            passwordField.setText(pass);
        });
        return btn;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 12, 10, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 4, 5, 4);
        gc.weightx = 1.0;

        JTextField txtNama = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtHp = new JTextField();
        JTextField txtAlamat = new JTextField();

        addFormRow(panel, gc, 0, "Nama Lengkap", txtNama);
        addFormRow(panel, gc, 1, "Email", txtEmail);
        addFormRow(panel, gc, 2, "Password (min. 6 karakter)", txtPass);
        addFormRow(panel, gc, 3, "No. HP", txtHp);
        addFormRow(panel, gc, 4, "Alamat", txtAlamat);

        gc.gridx = 0;
        gc.gridy = 5;
        gc.gridwidth = 2;
        gc.insets = new Insets(14, 4, 4, 4);
        JButton btnDaftar = Theme.createNavyButton("Daftar & Simpan ke Neon DB");
        btnDaftar.setPreferredSize(new Dimension(200, 38));
        btnDaftar.addActionListener(e -> {
            try {
                Nasabah baru = context.registerNasabah(
                        txtNama.getText().trim(),
                        txtEmail.getText().trim(),
                        new String(txtPass.getPassword()),
                        txtHp.getText().trim(),
                        txtAlamat.getText().trim()
                );
                Theme.showInfo(this, "Pendaftaran berhasil!\nID Nasabah: " + baru.getIdPengguna()
                        + "\nDompet: " + baru.getDompet().getIdDompet() + "\nSilakan masuk dengan akun baru Anda.");
                emailField.setText(baru.getEmail());
                passwordField.setText("");
                txtNama.setText("");
                txtEmail.setText("");
                txtPass.setText("");
                txtHp.setText("");
                txtAlamat.setText("");
            } catch (Exception ex) {
                Theme.showError(this, ex.getMessage());
            }
        });
        panel.add(btnDaftar, gc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        panel.add(lbl, gc);

        gc.gridx = 1;
        gc.weightx = 0.65;
        field.setFont(Theme.FONT_BODY);
        field.setPreferredSize(new Dimension(200, 32));
        panel.add(field, gc);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            Theme.showError(this, "Email dan password harus diisi.");
            return;
        }
        Pengguna user = context.login(email, pass);
        if (user == null) {
            Theme.showError(this, "Email atau password salah.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(context, user).setVisible(true);
            dispose();
        });
    }
}
