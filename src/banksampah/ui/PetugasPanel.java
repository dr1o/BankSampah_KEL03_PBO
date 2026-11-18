package banksampah.ui;

import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Petugas;
import banksampah.setoran.DetailSetoran;
import banksampah.setoran.Setoran;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard khusus peran Petugas:
 * Menimbang setoran (DIAJUKAN -> DITIMBANG) dan memverifikasi setoran
 * agar saldo dan poin otomatis masuk ke Dompet Nasabah (DITIMBANG -> SELESAI).
 */
@SuppressWarnings({"serial", "this-escape"})
public class PetugasPanel extends JPanel implements RefreshablePanel {

    private final BankSampahContext context;
    private final Petugas petugas;

    private JLabel lblCountDiajukan;
    private JLabel lblCountDitimbang;
    private JLabel lblCountSelesai;

    private DefaultTableModel modelDiajukan;
    private JTable tblDiajukan;
    private final List<Setoran> listDiajukan = new ArrayList<>();

    private DefaultTableModel modelDitimbang;
    private JTable tblDitimbang;
    private final List<Setoran> listDitimbang = new ArrayList<>();

    private DefaultTableModel modelSelesai;
    private JTable tblSelesai;
    private final List<Setoran> listSelesai = new ArrayList<>();

    public PetugasPanel(BankSampahContext context, Petugas petugas) {
        this.context = context;
        this.petugas = petugas;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.COOL_GREY);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel statsStrip = new JPanel(new GridLayout(1, 3, 14, 0));
        statsStrip.setOpaque(false);

        Theme.CardPanel c1 = new Theme.CardPanel(Theme.WARN_FG);
        c1.setLayout(new BorderLayout(0, 4));
        JLabel t1 = new JLabel("ANTREAN TIMBANG (DIAJUKAN)");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t1.setForeground(Theme.TEXT_MUTED);
        lblCountDiajukan = new JLabel("0 setoran");
        lblCountDiajukan.setFont(Theme.FONT_STAT);
        lblCountDiajukan.setForeground(Theme.WARN_FG);
        c1.add(t1, BorderLayout.NORTH);
        c1.add(lblCountDiajukan, BorderLayout.CENTER);

        Theme.CardPanel c2 = new Theme.CardPanel(Theme.TECH_BLUE);
        c2.setLayout(new BorderLayout(0, 4));
        JLabel t2 = new JLabel("SIAP DIVERIFIKASI (DITIMBANG)");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t2.setForeground(Theme.TEXT_MUTED);
        lblCountDitimbang = new JLabel("0 setoran");
        lblCountDitimbang.setFont(Theme.FONT_STAT);
        lblCountDitimbang.setForeground(Theme.TECH_BLUE);
        c2.add(t2, BorderLayout.NORTH);
        c2.add(lblCountDitimbang, BorderLayout.CENTER);

        Theme.CardPanel c3 = new Theme.CardPanel(Theme.SUCCESS_FG);
        c3.setLayout(new BorderLayout(0, 4));
        JLabel t3 = new JLabel("TERVERIFIKASI SAYA (" + petugas.getWilayahTugas().toUpperCase() + ")");
        t3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t3.setForeground(Theme.TEXT_MUTED);
        lblCountSelesai = new JLabel("0 setoran");
        lblCountSelesai.setFont(Theme.FONT_STAT);
        lblCountSelesai.setForeground(Theme.SUCCESS_FG);
        c3.add(t3, BorderLayout.NORTH);
        c3.add(lblCountSelesai, BorderLayout.CENTER);

        statsStrip.add(c1);
        statsStrip.add(c2);
        statsStrip.add(c3);
        add(statsStrip, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY_BOLD);
        tabs.addTab("1. Antrean Timbang (DIAJUKAN)", createTimbangTab());
        tabs.addTab("2. Siap Verifikasi (DITIMBANG)", createVerifikasiTab());
        tabs.addTab("3. Riwayat Terverifikasi", createRiwayatTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTimbangTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.WARN_FG);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Setoran Menunggu Penimbangan Petugas");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnDetail = Theme.createSecondaryButton("Lihat Rincian Item");
        btnDetail.addActionListener(e -> showDetails(tblDiajukan, listDiajukan));
        JButton btnTolak = Theme.createDangerButton("Tolak");
        btnTolak.addActionListener(e -> handleTolak(tblDiajukan, listDiajukan));
        JButton btnTimbang = Theme.createPrimaryButton("Timbang & Hitung Total");
        btnTimbang.addActionListener(e -> handleTimbang());
        btns.add(btnDetail);
        btns.add(btnTolak);
        btns.add(btnTimbang);

        top.add(title, BorderLayout.WEST);
        top.add(btns, BorderLayout.EAST);

        modelDiajukan = new DefaultTableModel(
                new String[]{"ID Setoran", "Nasabah", "Tanggal", "Jumlah Item", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblDiajukan = new JTable(modelDiajukan);
        Theme.styleTable(tblDiajukan);
        tblDiajukan.getColumnModel().getColumn(4).setCellRenderer(Theme.createStatusCellRenderer());

        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(tblDiajukan), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createVerifikasiTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.TECH_BLUE);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Setoran Sudah Ditimbang \u2014 Verifikasi untuk Cairkan Saldo & Poin");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnDetail = Theme.createSecondaryButton("Lihat Rincian Item");
        btnDetail.addActionListener(e -> showDetails(tblDitimbang, listDitimbang));
        JButton btnTolak = Theme.createDangerButton("Tolak");
        btnTolak.addActionListener(e -> handleTolak(tblDitimbang, listDitimbang));
        JButton btnVerif = Theme.createPrimaryButton("Verifikasi & Cairkan ke Dompet");
        btnVerif.addActionListener(e -> handleVerifikasi());
        btns.add(btnDetail);
        btns.add(btnTolak);
        btns.add(btnVerif);

        top.add(title, BorderLayout.WEST);
        top.add(btns, BorderLayout.EAST);

        modelDitimbang = new DefaultTableModel(
                new String[]{"ID Setoran", "Nasabah", "Tanggal", "Item", "Total Nilai", "Poin", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblDitimbang = new JTable(modelDitimbang);
        Theme.styleTable(tblDitimbang);
        tblDitimbang.getColumnModel().getColumn(6).setCellRenderer(Theme.createStatusCellRenderer());

        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(tblDitimbang), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createRiwayatTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.SUCCESS_FG);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Setoran yang Telah Diverifikasi oleh " + petugas.getNama());
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);
        JButton btnDetail = Theme.createSecondaryButton("Lihat Rincian Item");
        btnDetail.addActionListener(e -> showDetails(tblSelesai, listSelesai));
        top.add(title, BorderLayout.WEST);
        top.add(btnDetail, BorderLayout.EAST);

        modelSelesai = new DefaultTableModel(
                new String[]{"ID Setoran", "Nasabah", "Tanggal", "Item", "Total Nilai", "Poin", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblSelesai = new JTable(modelSelesai);
        Theme.styleTable(tblSelesai);
        tblSelesai.getColumnModel().getColumn(6).setCellRenderer(Theme.createStatusCellRenderer());

        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(tblSelesai), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    @Override
    public void refreshData() {
        listDiajukan.clear();
        listDitimbang.clear();
        listSelesai.clear();

        for (Setoran s : context.getDaftarSetoran()) {
            if ("DIAJUKAN".equals(s.getStatus())) {
                listDiajukan.add(s);
            } else if ("DITIMBANG".equals(s.getStatus())) {
                listDitimbang.add(s);
            }
        }
        listSelesai.addAll(petugas.getDaftarSetoran());

        lblCountDiajukan.setText(listDiajukan.size() + " setoran");
        lblCountDitimbang.setText(listDitimbang.size() + " setoran");
        lblCountSelesai.setText(listSelesai.size() + " setoran");

        modelDiajukan.setRowCount(0);
        for (Setoran s : listDiajukan) {
            Nasabah n = context.findPenyetor(s);
            modelDiajukan.addRow(new Object[]{
                    s.getIdSetoran(),
                    n != null ? n.getNama() : "-",
                    Theme.formatTanggal(s.getTanggalSetor()),
                    s.getDaftarDetail().size() + " item",
                    s.getStatus()
            });
        }

        modelDitimbang.setRowCount(0);
        for (Setoran s : listDitimbang) {
            Nasabah n = context.findPenyetor(s);
            modelDitimbang.addRow(new Object[]{
                    s.getIdSetoran(),
                    n != null ? n.getNama() : "-",
                    Theme.formatTanggal(s.getTanggalSetor()),
                    s.getDaftarDetail().size() + " item",
                    Theme.formatRupiah(s.getTotalNilai()),
                    s.getTotalPoin() + " poin",
                    s.getStatus()
            });
        }

        modelSelesai.setRowCount(0);
        for (Setoran s : listSelesai) {
            Nasabah n = context.findPenyetor(s);
            modelSelesai.addRow(new Object[]{
                    s.getIdSetoran(),
                    n != null ? n.getNama() : "-",
                    Theme.formatTanggal(s.getTanggalSetor()),
                    s.getDaftarDetail().size() + " item",
                    Theme.formatRupiah(s.getTotalNilai()),
                    s.getTotalPoin() + " poin",
                    s.getStatus()
            });
        }
    }

    private void handleTimbang() {
        int row = tblDiajukan.getSelectedRow();
        if (row < 0 || row >= listDiajukan.size()) {
            Theme.showError(this, "Pilih setoran yang ingin ditimbang terlebih dahulu.");
            return;
        }
        Setoran s = listDiajukan.get(row);
        try {
            context.timbangSetoran(petugas, s);
            refreshData();
            Theme.showInfo(this, "Penimbangan berhasil!\nID: " + s.getIdSetoran()
                    + "\nTotal Nilai: " + Theme.formatRupiah(s.getTotalNilai())
                    + "\nTotal Poin: " + s.getTotalPoin()
                    + "\nStatus berubah menjadi: " + s.getStatus());
        } catch (Exception ex) {
            Theme.showError(this, "Gagal menimbang setoran: " + ex.getMessage());
        }
    }

    private void handleVerifikasi() {
        int row = tblDitimbang.getSelectedRow();
        if (row < 0 || row >= listDitimbang.size()) {
            Theme.showError(this, "Pilih setoran yang ingin diverifikasi terlebih dahulu.");
            return;
        }
        Setoran s = listDitimbang.get(row);
        try {
            if (context.verifikasiSetoran(petugas, s)) {
                refreshData();
                Theme.showInfo(this, "Verifikasi berhasil!\nSaldo sebesar " + Theme.formatRupiah(s.getTotalNilai())
                        + " dan " + s.getTotalPoin() + " poin telah masuk ke Dompet Nasabah.");
            } else {
                Theme.showError(this, "Verifikasi tidak berhasil. Status sekarang: " + s.getStatus());
            }
        } catch (Exception ex) {
            Theme.showError(this, "Gagal memverifikasi ke Neon DB: " + ex.getMessage());
        }
    }

    private void handleTolak(JTable table, List<Setoran> source) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= source.size()) {
            Theme.showError(this, "Pilih setoran yang ingin ditolak terlebih dahulu.");
            return;
        }
        Setoran s = source.get(row);
        try {
            context.batalkanSetoran(s);
            refreshData();
            Theme.showInfo(this, "Setoran " + s.getIdSetoran() + " telah ditolak (DITOLAK).");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal menolak setoran: " + ex.getMessage());
        }
    }

    private void showDetails(JTable table, List<Setoran> source) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= source.size()) {
            Theme.showError(this, "Pilih setoran dari tabel untuk melihat detail item.");
            return;
        }
        Setoran s = source.get(row);
        Nasabah n = context.findPenyetor(s);
        StringBuilder sb = new StringBuilder();
        sb.append("ID Setoran : ").append(s.getIdSetoran()).append("\n");
        sb.append("Nasabah    : ").append(n != null ? n.getNama() : "-").append("\n");
        sb.append("Status     : ").append(s.getStatus()).append("\n\n");
        sb.append("Item Sampah:\n");
        for (DetailSetoran d : s.getDaftarDetail()) {
            sb.append(" - ").append(d).append("\n");
        }
        Theme.showInfo(this, sb.toString());
    }
}
