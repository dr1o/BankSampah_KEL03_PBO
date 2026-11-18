package banksampah.ui;

import banksampah.hadiah.Hadiah;
import banksampah.pengguna.Admin;
import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;
import banksampah.pengguna.Petugas;
import banksampah.sampah.Kertas;
import banksampah.sampah.Logam;
import banksampah.sampah.Plastik;
import banksampah.sampah.Sampah;
import banksampah.setoran.Setoran;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Dashboard khusus peran Admin:
 * Kelola Jenis Sampah (Plastik/Kertas/Logam), Kelola Hadiah, Kelola Pengguna,
 * dan Laporan Sistem Bank Sampah.
 */
@SuppressWarnings({"serial", "this-escape"})
public class AdminPanel extends JPanel implements RefreshablePanel {

    private final BankSampahContext context;
    private final Admin admin;

    private JLabel lblTotalPengguna;
    private JLabel lblTotalSampah;
    private JLabel lblTotalHadiah;
    private JLabel lblTotalOmzet;

    private DefaultTableModel sampahModel;
    private JTable tblSampah;

    private DefaultTableModel hadiahModel;
    private JTable tblHadiah;

    private DefaultTableModel penggunaModel;
    private JTable tblPengguna;

    private DefaultTableModel laporanModel;
    private JTable tblLaporan;
    private JLabel lblStatusSummary;

    public AdminPanel(BankSampahContext context, Admin admin) {
        this.context = context;
        this.admin = admin;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.COOL_GREY);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel statsStrip = new JPanel(new GridLayout(1, 4, 12, 0));
        statsStrip.setOpaque(false);

        Theme.CardPanel c1 = new Theme.CardPanel(Theme.NAVY);
        c1.setLayout(new BorderLayout(0, 4));
        JLabel t1 = new JLabel("TOTAL PENGGUNA");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t1.setForeground(Theme.TEXT_MUTED);
        lblTotalPengguna = new JLabel("0");
        lblTotalPengguna.setFont(Theme.FONT_STAT);
        lblTotalPengguna.setForeground(Theme.NAVY);
        c1.add(t1, BorderLayout.NORTH);
        c1.add(lblTotalPengguna, BorderLayout.CENTER);

        Theme.CardPanel c2 = new Theme.CardPanel(Theme.TECH_BLUE);
        c2.setLayout(new BorderLayout(0, 4));
        JLabel t2 = new JLabel("JENIS SAMPAH");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t2.setForeground(Theme.TEXT_MUTED);
        lblTotalSampah = new JLabel("0");
        lblTotalSampah.setFont(Theme.FONT_STAT);
        lblTotalSampah.setForeground(Theme.TECH_BLUE);
        c2.add(t2, BorderLayout.NORTH);
        c2.add(lblTotalSampah, BorderLayout.CENTER);

        Theme.CardPanel c3 = new Theme.CardPanel(Theme.PURPLE_FG);
        c3.setLayout(new BorderLayout(0, 4));
        JLabel t3 = new JLabel("KATALOG HADIAH");
        t3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t3.setForeground(Theme.TEXT_MUTED);
        lblTotalHadiah = new JLabel("0");
        lblTotalHadiah.setFont(Theme.FONT_STAT);
        lblTotalHadiah.setForeground(Theme.PURPLE_FG);
        c3.add(t3, BorderLayout.NORTH);
        c3.add(lblTotalHadiah, BorderLayout.CENTER);

        Theme.CardPanel c4 = new Theme.CardPanel(Theme.SUCCESS_FG);
        c4.setLayout(new BorderLayout(0, 4));
        JLabel t4 = new JLabel("NILAI SETORAN SELESAI");
        t4.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t4.setForeground(Theme.TEXT_MUTED);
        lblTotalOmzet = new JLabel("Rp0");
        lblTotalOmzet.setFont(Theme.FONT_STAT);
        lblTotalOmzet.setForeground(Theme.SUCCESS_FG);
        c4.add(t4, BorderLayout.NORTH);
        c4.add(lblTotalOmzet, BorderLayout.CENTER);

        statsStrip.add(c1);
        statsStrip.add(c2);
        statsStrip.add(c3);
        statsStrip.add(c4);
        add(statsStrip, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY_BOLD);
        tabs.addTab("Kelola Jenis Sampah", createSampahTab());
        tabs.addTab("Kelola Hadiah", createHadiahTab());
        tabs.addTab("Kelola Pengguna", createPenggunaTab());
        tabs.addTab("Laporan Bank Sampah", createLaporanTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createSampahTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.TECH_BLUE);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Daftar Jenis Sampah (Hierarki Sampah -> Plastik / Kertas / Logam)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnUbahHarga = Theme.createSecondaryButton("Ubah Harga/kg");
        btnUbahHarga.addActionListener(e -> handleUbahHarga());
        JButton btnTambah = Theme.createPrimaryButton("+ Tambah Jenis Sampah");
        btnTambah.addActionListener(e -> openTambahSampahDialog());
        btns.add(btnUbahHarga);
        btns.add(btnTambah);

        top.add(title, BorderLayout.WEST);
        top.add(btns, BorderLayout.EAST);

        sampahModel = new DefaultTableModel(
                new String[]{"ID", "Nama Sampah", "Subkelas", "Detail Kondisi", "Harga per Kg"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblSampah = new JTable(sampahModel);
        Theme.styleTable(tblSampah);

        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(tblSampah), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createHadiahTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.PURPLE_FG);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Katalog Hadiah Penukaran Poin");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnRestok = Theme.createSecondaryButton("+ Tambah Stok");
        btnRestok.addActionListener(e -> handleTambahStokHadiah());
        JButton btnTambah = Theme.createPrimaryButton("+ Tambah Hadiah Baru");
        btnTambah.addActionListener(e -> openTambahHadiahDialog());
        btns.add(btnRestok);
        btns.add(btnTambah);

        top.add(title, BorderLayout.WEST);
        top.add(btns, BorderLayout.EAST);

        hadiahModel = new DefaultTableModel(
                new String[]{"ID Hadiah", "Nama Hadiah", "Poin Dibutuhkan", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblHadiah = new JTable(hadiahModel);
        Theme.styleTable(tblHadiah);

        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(tblHadiah), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPenggunaTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.NAVY);
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Daftar Pengguna Terdaftar (Akses Level: " + admin.getLevelAkses() + ")");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        penggunaModel = new DefaultTableModel(
                new String[]{"ID", "Peran", "Nama", "Email", "No. HP", "Detail Khusus Peran"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblPengguna = new JTable(penggunaModel);
        Theme.styleTable(tblPengguna);

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(tblPengguna), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createLaporanTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.SUCCESS_FG);
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Laporan Aktivitas Bank Sampah (Admin.buatLaporan)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        lblStatusSummary = new JLabel("-");
        lblStatusSummary.setFont(Theme.FONT_BODY_BOLD);
        lblStatusSummary.setForeground(Theme.TECH_BLUE);

        header.add(title, BorderLayout.NORTH);
        header.add(lblStatusSummary, BorderLayout.SOUTH);

        laporanModel = new DefaultTableModel(
                new String[]{"ID Setoran", "Nasabah", "Tanggal", "Item", "Total Nilai", "Poin", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblLaporan = new JTable(laporanModel);
        Theme.styleTable(tblLaporan);
        tblLaporan.getColumnModel().getColumn(6).setCellRenderer(Theme.createStatusCellRenderer());

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(tblLaporan), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    @Override
    public void refreshData() {
        List<Pengguna> users = context.getDaftarPengguna();
        List<Sampah> sampahs = context.getDaftarSampah();
        List<Hadiah> hadiahs = context.getDaftarHadiah();
        List<Setoran> setorans = context.getDaftarSetoran();

        int cNasabah = 0, cPetugas = 0, cAdmin = 0;
        penggunaModel.setRowCount(0);
        for (Pengguna p : users) {
            String extra = "-";
            if (p instanceof Nasabah) {
                cNasabah++;
                Nasabah n = (Nasabah) p;
                extra = "Alamat: " + n.getAlamat() + " | Saldo: " + Theme.formatRupiah(n.getDompet().getSaldoRupiah());
            } else if (p instanceof Petugas) {
                cPetugas++;
                Petugas pt = (Petugas) p;
                extra = pt.getIdPetugas() + " | Wilayah: " + pt.getWilayahTugas() + " | Verifikasi: " + pt.getDaftarSetoran().size();
            } else if (p instanceof Admin) {
                cAdmin++;
                extra = "Level Akses: " + ((Admin) p).getLevelAkses();
            }
            penggunaModel.addRow(new Object[]{
                    p.getIdPengguna(), p.getClass().getSimpleName(), p.getNama(), p.getEmail(), p.getNoHp(), extra
            });
        }
        lblTotalPengguna.setText(users.size() + " (" + cNasabah + "N / " + cPetugas + "P / " + cAdmin + "A)");
        lblTotalSampah.setText(String.valueOf(sampahs.size()));
        lblTotalHadiah.setText(String.valueOf(hadiahs.size()));

        sampahModel.setRowCount(0);
        for (Sampah s : sampahs) {
            String detail = "-";
            if (s instanceof Plastik) {
                Plastik p = (Plastik) s;
                detail = p.getJenisPlastik() + ", " + (p.isBersih() ? "bersih" : "kotor (-20%)");
            } else if (s instanceof Kertas) {
                Kertas k = (Kertas) s;
                detail = k.getJenisKertas() + ", " + (k.isKering() ? "kering" : "basah (-30%)");
            } else if (s instanceof Logam) {
                Logam l = (Logam) s;
                detail = l.getJenisLogam() + ", kemurnian " + (int) Math.round(l.getKadarKemurnian() * 100) + "%";
            }
            sampahModel.addRow(new Object[]{
                    s.getIdSampah(), s.getNamaSampah(), s.getClass().getSimpleName(), detail, Theme.formatRupiah(s.getHargaPerKg())
            });
        }

        hadiahModel.setRowCount(0);
        for (Hadiah h : hadiahs) {
            hadiahModel.addRow(new Object[]{
                    h.getIdHadiah(), h.getNamaHadiah(), h.getPoinDibutuhkan() + " poin", h.getStok()
            });
        }

        int diajukan = 0, ditimbang = 0, diverifikasi = 0, selesai = 0, ditolak = 0;
        double totalOmzet = 0;
        laporanModel.setRowCount(0);
        for (Setoran s : setorans) {
            switch (s.getStatus()) {
                case "DIAJUKAN": diajukan++; break;
                case "DITIMBANG": ditimbang++; break;
                case "DIVERIFIKASI": diverifikasi++; break;
                case "SELESAI":
                    selesai++;
                    totalOmzet += s.getTotalNilai();
                    break;
                case "DITOLAK": ditolak++; break;
            }
            Nasabah penyetor = context.findPenyetor(s);
            laporanModel.addRow(new Object[]{
                    s.getIdSetoran(),
                    penyetor != null ? penyetor.getNama() : "-",
                    Theme.formatTanggal(s.getTanggalSetor()),
                    s.getDaftarDetail().size() + " item",
                    Theme.formatRupiah(s.getTotalNilai()),
                    s.getTotalPoin() + " poin",
                    s.getStatus()
            });
        }

        lblTotalOmzet.setText(Theme.formatRupiah(totalOmzet));
        lblStatusSummary.setText("Total " + setorans.size() + " setoran  |  DIAJUKAN: " + diajukan
                + "  \u2022  DITIMBANG: " + ditimbang
                + "  \u2022  DIVERIFIKASI: " + diverifikasi
                + "  \u2022  SELESAI: " + selesai
                + "  \u2022  DITOLAK: " + ditolak);
    }

    private void handleUbahHarga() {
        int row = tblSampah.getSelectedRow();
        if (row < 0 || row >= context.getDaftarSampah().size()) {
            Theme.showError(this, "Pilih jenis sampah dari tabel yang ingin diubah harganya.");
            return;
        }
        Sampah s = context.getDaftarSampah().get(row);
        String input = JOptionPane.showInputDialog(this,
                "Harga baru per kg untuk " + s.getNamaSampah() + " (saat ini: " + Theme.formatRupiah(s.getHargaPerKg()) + "):",
                String.valueOf((long) s.getHargaPerKg()));
        if (input == null) {
            return;
        }
        try {
            double hargaBaru = Double.parseDouble(input.trim());
            context.ubahHargaSampah(s, hargaBaru);
            refreshData();
            Theme.showInfo(this, "Harga " + s.getNamaSampah() + " berhasil diperbarui menjadi " + Theme.formatRupiah(hargaBaru) + "/kg.");
        } catch (NumberFormatException ex) {
            Theme.showError(this, "Harga harus berupa angka valid.");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal memperbarui harga: " + ex.getMessage());
        }
    }

    private void openTambahSampahDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tambah Jenis Sampah Baru", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1.0;

        JComboBox<String> cmbKategori = new JComboBox<>(new String[]{"Plastik", "Kertas", "Logam"});
        JTextField txtNama = new JTextField();
        JTextField txtHarga = new JTextField();
        JLabel lblJenis = new JLabel("Jenis Plastik (mis. PET / HDPE)");
        JTextField txtJenis = new JTextField();
        JCheckBox chkKondisi = new JCheckBox("Plastik bersih (tidak dipotong 20%)", true);
        chkKondisi.setOpaque(false);
        JLabel lblKemurnian = new JLabel("Kadar Kemurnian (0.0 - 1.0)");
        JTextField txtKemurnian = new JTextField("0.90");
        lblKemurnian.setVisible(false);
        txtKemurnian.setVisible(false);

        cmbKategori.addActionListener(e -> {
            String kat = (String) cmbKategori.getSelectedItem();
            if ("Plastik".equals(kat)) {
                lblJenis.setText("Jenis Plastik (mis. PET / PP)");
                chkKondisi.setText("Plastik bersih (tidak dipotong 20%)");
                chkKondisi.setVisible(true);
                lblKemurnian.setVisible(false);
                txtKemurnian.setVisible(false);
            } else if ("Kertas".equals(kat)) {
                lblJenis.setText("Jenis Kertas (mis. Kardus / HVS)");
                chkKondisi.setText("Kertas kering (tidak dipotong 30%)");
                chkKondisi.setVisible(true);
                lblKemurnian.setVisible(false);
                txtKemurnian.setVisible(false);
            } else {
                lblJenis.setText("Jenis Logam (mis. Tembaga / Besi)");
                chkKondisi.setVisible(false);
                lblKemurnian.setVisible(true);
                txtKemurnian.setVisible(true);
            }
        });

        gc.gridy = 0; gc.gridx = 0; panel.add(new JLabel("Subkelas Sampah"), gc);
        gc.gridx = 1; panel.add(cmbKategori, gc);
        gc.gridy = 1; gc.gridx = 0; panel.add(new JLabel("Nama Sampah"), gc);
        gc.gridx = 1; panel.add(txtNama, gc);
        gc.gridy = 2; gc.gridx = 0; panel.add(new JLabel("Harga per Kg (Rp)"), gc);
        gc.gridx = 1; panel.add(txtHarga, gc);
        gc.gridy = 3; gc.gridx = 0; panel.add(lblJenis, gc);
        gc.gridx = 1; panel.add(txtJenis, gc);
        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 2; panel.add(chkKondisi, gc);
        gc.gridwidth = 1;
        gc.gridy = 5; gc.gridx = 0; panel.add(lblKemurnian, gc);
        gc.gridx = 1; panel.add(txtKemurnian, gc);

        JButton btnSimpan = Theme.createPrimaryButton("Simpan ke Neon DB");
        btnSimpan.addActionListener(e -> {
            try {
                String id = context.nextSampahId();
                String nama = txtNama.getText().trim();
                String jenis = txtJenis.getText().trim();
                double harga = Double.parseDouble(txtHarga.getText().trim());
                if (nama.isEmpty() || jenis.isEmpty()) {
                    Theme.showError(dialog, "Nama dan jenis spesifik wajib diisi.");
                    return;
                }
                String kat = (String) cmbKategori.getSelectedItem();
                Sampah baru;
                if ("Plastik".equals(kat)) {
                    baru = new Plastik(id, nama, harga, jenis, chkKondisi.isSelected());
                } else if ("Kertas".equals(kat)) {
                    baru = new Kertas(id, nama, harga, jenis, chkKondisi.isSelected());
                } else {
                    double kemurnian = Double.parseDouble(txtKemurnian.getText().trim().replace(',', '.'));
                    baru = new Logam(id, nama, harga, jenis, kemurnian);
                }
                context.tambahSampah(baru);
                dialog.dispose();
                refreshData();
                Theme.showInfo(this, "Berhasil menambah jenis sampah: " + baru);
            } catch (NumberFormatException ex) {
                Theme.showError(dialog, "Harga atau kadar kemurnian harus berupa angka valid.");
            } catch (Exception ex) {
                Theme.showError(dialog, "Gagal menambah sampah: " + ex.getMessage());
            }
        });
        gc.gridy = 6; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(12, 4, 4, 4);
        panel.add(btnSimpan, gc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openTambahHadiahDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tambah Hadiah Baru", true);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1.0;

        JTextField txtNama = new JTextField();
        JTextField txtPoin = new JTextField();
        JTextField txtStok = new JTextField();

        gc.gridy = 0; gc.gridx = 0; panel.add(new JLabel("Nama Hadiah"), gc);
        gc.gridx = 1; panel.add(txtNama, gc);
        gc.gridy = 1; gc.gridx = 0; panel.add(new JLabel("Poin Dibutuhkan"), gc);
        gc.gridx = 1; panel.add(txtPoin, gc);
        gc.gridy = 2; gc.gridx = 0; panel.add(new JLabel("Stok Awal"), gc);
        gc.gridx = 1; panel.add(txtStok, gc);

        JButton btnSimpan = Theme.createPrimaryButton("Simpan Hadiah");
        btnSimpan.addActionListener(e -> {
            try {
                String id = context.nextHadiahId();
                String nama = txtNama.getText().trim();
                int poin = Integer.parseInt(txtPoin.getText().trim());
                int stok = Integer.parseInt(txtStok.getText().trim());
                if (nama.isEmpty()) {
                    Theme.showError(dialog, "Nama hadiah tidak boleh kosong.");
                    return;
                }
                Hadiah h = new Hadiah(id, nama, poin, stok);
                context.tambahHadiah(h);
                dialog.dispose();
                refreshData();
                Theme.showInfo(this, "Hadiah berhasil ditambahkan: " + h);
            } catch (NumberFormatException ex) {
                Theme.showError(dialog, "Poin dan stok harus berupa angka bulat.");
            } catch (Exception ex) {
                Theme.showError(dialog, "Gagal menambah hadiah: " + ex.getMessage());
            }
        });
        gc.gridy = 3; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(12, 4, 4, 4);
        panel.add(btnSimpan, gc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleTambahStokHadiah() {
        int row = tblHadiah.getSelectedRow();
        if (row < 0 || row >= context.getDaftarHadiah().size()) {
            Theme.showError(this, "Pilih hadiah dari tabel yang ingin ditambah stoknya.");
            return;
        }
        Hadiah h = context.getDaftarHadiah().get(row);
        String input = JOptionPane.showInputDialog(this, "Tambahkan jumlah stok untuk " + h.getNamaHadiah() + ":", "5");
        if (input == null) {
            return;
        }
        try {
            int tambahan = Integer.parseInt(input.trim());
            context.tambahStokHadiah(h, tambahan);
            refreshData();
            Theme.showInfo(this, "Stok " + h.getNamaHadiah() + " sekarang: " + h.getStok());
        } catch (NumberFormatException ex) {
            Theme.showError(this, "Masukkan jumlah stok angka bulat.");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal memperbarui stok: " + ex.getMessage());
        }
    }
}
