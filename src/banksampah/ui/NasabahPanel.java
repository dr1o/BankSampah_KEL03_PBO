package banksampah.ui;

import banksampah.hadiah.Hadiah;
import banksampah.keuangan.Dompet;
import banksampah.keuangan.EWallet;
import banksampah.keuangan.MetodePenarikan;
import banksampah.keuangan.TransferBank;
import banksampah.pengguna.Nasabah;
import banksampah.sampah.Kertas;
import banksampah.sampah.Logam;
import banksampah.sampah.Plastik;
import banksampah.sampah.Sampah;
import banksampah.setoran.DetailSetoran;
import banksampah.setoran.Setoran;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard khusus peran Nasabah:
 * Setor sampah (membuat DetailSetoran & Setoran), kelola Dompet, tarik saldo (MetodePenarikan),
 * dan tukar poin dengan Hadiah.
 */
@SuppressWarnings({"serial", "this-escape"})
public class NasabahPanel extends JPanel implements RefreshablePanel {

    private final BankSampahContext context;
    private final Nasabah nasabah;

    private JLabel lblSaldo;
    private JLabel lblPoin;
    private JLabel lblTotalSetoran;

    private JPanel sampahGridPanel;
    private JPanel hadiahGridPanel;

    private final List<DetailSetoran> cart = new ArrayList<>();
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblCartTotal;

    private DefaultTableModel historyModel;
    private JTable historyTable;
    private List<Setoran> currentHistoryList = new ArrayList<>();

    public NasabahPanel(BankSampahContext context, Nasabah nasabah) {
        this.context = context;
        this.nasabah = nasabah;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.COOL_GREY);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Strip Kartu Statistik Dompet & Setoran
        JPanel statsStrip = new JPanel(new GridLayout(1, 3, 14, 0));
        statsStrip.setOpaque(false);

        Theme.CardPanel saldoCard = new Theme.CardPanel(Theme.TECH_BLUE);
        saldoCard.setLayout(new BorderLayout(0, 6));
        JLabel t1 = new JLabel("SALDO DOMPET (" + nasabah.getDompet().getIdDompet() + ")");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t1.setForeground(Theme.TEXT_MUTED);
        lblSaldo = new JLabel("Rp0");
        lblSaldo.setFont(Theme.FONT_STAT);
        lblSaldo.setForeground(Theme.NAVY);
        JButton btnTarik = Theme.createPrimaryButton("Tarik Saldo");
        btnTarik.addActionListener(e -> openTarikSaldoDialog());
        JPanel saldoBottom = new JPanel(new BorderLayout());
        saldoBottom.setOpaque(false);
        saldoBottom.add(lblSaldo, BorderLayout.WEST);
        saldoBottom.add(btnTarik, BorderLayout.EAST);
        saldoCard.add(t1, BorderLayout.NORTH);
        saldoCard.add(saldoBottom, BorderLayout.CENTER);

        Theme.CardPanel poinCard = new Theme.CardPanel(Theme.SUCCESS_FG);
        poinCard.setLayout(new BorderLayout(0, 6));
        JLabel t2 = new JLabel("TOTAL POIN REWARD (1 poin / Rp100)");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t2.setForeground(Theme.TEXT_MUTED);
        lblPoin = new JLabel("0 poin");
        lblPoin.setFont(Theme.FONT_STAT);
        lblPoin.setForeground(Theme.SUCCESS_FG);
        poinCard.add(t2, BorderLayout.NORTH);
        poinCard.add(lblPoin, BorderLayout.CENTER);

        Theme.CardPanel setorCard = new Theme.CardPanel(Theme.NAVY);
        setorCard.setLayout(new BorderLayout(0, 6));
        JLabel t3 = new JLabel("RIWAYAT PENYETORAN");
        t3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t3.setForeground(Theme.TEXT_MUTED);
        lblTotalSetoran = new JLabel("0 transaksi");
        lblTotalSetoran.setFont(Theme.FONT_STAT);
        lblTotalSetoran.setForeground(Theme.NAVY);
        setorCard.add(t3, BorderLayout.NORTH);
        setorCard.add(lblTotalSetoran, BorderLayout.CENTER);

        statsStrip.add(saldoCard);
        statsStrip.add(poinCard);
        statsStrip.add(setorCard);
        add(statsStrip, BorderLayout.NORTH);

        // 2. Workspace Tabbed
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY_BOLD);
        tabs.addTab("Setor Sampah", createSetorTab());
        tabs.addTab("Riwayat Setoran", createRiwayatTab());
        tabs.addTab("Tukar Poin & Hadiah", createHadiahTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createSetorTab() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Kiri: Grid Kartu Katalog Sampah
        sampahGridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        sampahGridPanel.setBackground(Theme.COOL_GREY);
        sampahGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 8));

        JPanel leftWrapper = new JPanel(new BorderLayout(0, 8));
        leftWrapper.setOpaque(false);
        JLabel lblKatalog = new JLabel("Pilih Jenis Sampah (Polymorphism hitungNilai per Subkelas)");
        lblKatalog.setFont(Theme.FONT_SUBTITLE);
        lblKatalog.setForeground(Theme.NAVY);
        leftWrapper.add(lblKatalog, BorderLayout.NORTH);

        JScrollPane scrollSampah = new JScrollPane(sampahGridPanel);
        scrollSampah.setBorder(null);
        scrollSampah.getVerticalScrollBar().setUnitIncrement(16);
        leftWrapper.add(scrollSampah, BorderLayout.CENTER);

        // Kanan: Keranjang Setoran
        Theme.CardPanel cartCard = new Theme.CardPanel(Theme.NAVY);
        cartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblCartTitle = new JLabel("Keranjang Setoran (DetailSetoran)");
        lblCartTitle.setFont(Theme.FONT_SUBTITLE);
        lblCartTitle.setForeground(Theme.NAVY);

        cartModel = new DefaultTableModel(new String[]{"Sampah", "Berat (kg)", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        Theme.styleTable(cartTable);
        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.setBorder(BorderFactory.createLineBorder(Theme.STEEL_GREY));

        JPanel cartFooter = new JPanel(new BorderLayout(0, 10));
        cartFooter.setOpaque(false);

        lblCartTotal = new JLabel("Estimasi Total: Rp0 (0 poin)");
        lblCartTotal.setFont(Theme.FONT_BODY_BOLD);
        lblCartTotal.setForeground(Theme.NAVY);

        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cartBtns.setOpaque(false);
        JButton btnHapus = Theme.createSecondaryButton("Hapus Item");
        btnHapus.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0 && row < cart.size()) {
                cart.remove(row);
                refreshCartTable();
            }
        });
        JButton btnKosong = Theme.createDangerButton("Kosongkan");
        btnKosong.addActionListener(e -> {
            cart.clear();
            refreshCartTable();
        });
        JButton btnAjukan = Theme.createPrimaryButton("Ajukan Setoran");
        btnAjukan.addActionListener(e -> handleAjukanSetoran());

        cartBtns.add(btnHapus);
        cartBtns.add(btnKosong);
        cartBtns.add(btnAjukan);

        cartFooter.add(lblCartTotal, BorderLayout.NORTH);
        cartFooter.add(cartBtns, BorderLayout.SOUTH);

        cartCard.add(lblCartTitle, BorderLayout.NORTH);
        cartCard.add(scrollCart, BorderLayout.CENTER);
        cartCard.add(cartFooter, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrapper, cartCard);
        split.setResizeWeight(0.58);
        split.setDividerSize(10);
        split.setBorder(null);
        split.setOpaque(false);

        container.add(split, BorderLayout.CENTER);
        return container;
    }

    private JPanel createRiwayatTab() {
        Theme.CardPanel card = new Theme.CardPanel(Theme.NAVY);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Riwayat Transaksi Setoran Saya");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnDetail = Theme.createSecondaryButton("Lihat Detail Item");
        btnDetail.addActionListener(e -> showSelectedSetoranDetails());
        JButton btnBatal = Theme.createDangerButton("Batalkan Setoran");
        btnBatal.addActionListener(e -> handleBatalkanSetoran());
        actions.add(btnDetail);
        actions.add(btnBatal);
        top.add(actions, BorderLayout.EAST);

        historyModel = new DefaultTableModel(
                new String[]{"ID Setoran", "Tanggal", "Item", "Total Nilai", "Poin", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyModel);
        Theme.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(Theme.createStatusCellRenderer());

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.STEEL_GREY));

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createHadiahTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel title = new JLabel("Katalog Hadiah (Tukar Poin Reward Anda)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.NAVY);

        hadiahGridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        hadiahGridPanel.setBackground(Theme.COOL_GREY);
        hadiahGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(hadiahGridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    @Override
    public void refreshData() {
        Dompet dompet = nasabah.getDompet();
        lblSaldo.setText(Theme.formatRupiah(dompet.getSaldoRupiah()));
        lblPoin.setText(dompet.getTotalPoin() + " poin");
        currentHistoryList = new ArrayList<>(nasabah.lihatRiwayat());
        lblTotalSetoran.setText(currentHistoryList.size() + " transaksi");

        refreshSampahCards();
        refreshCartTable();
        refreshHistoryTable();
        refreshHadiahCards();
    }

    private void refreshSampahCards() {
        sampahGridPanel.removeAll();
        for (Sampah s : context.getDaftarSampah()) {
            sampahGridPanel.add(buildSampahCard(s));
        }
        sampahGridPanel.revalidate();
        sampahGridPanel.repaint();
    }

    private JPanel buildSampahCard(Sampah s) {
        Color accent = Theme.TECH_BLUE;
        String categoryLabel = "SAMPAH";
        String conditionText = "";
        Color condBg = Theme.SUCCESS_BG;
        Color condFg = Theme.SUCCESS_FG;

        if (s instanceof Plastik) {
            Plastik p = (Plastik) s;
            categoryLabel = "PLASTIK";
            accent = Theme.TECH_BLUE;
            conditionText = p.getJenisPlastik() + " \u2022 " + (p.isBersih() ? "Bersih (100%)" : "Kotor (-20%)");
            if (!p.isBersih()) {
                condBg = Theme.WARN_BG;
                condFg = Theme.WARN_FG;
            }
        } else if (s instanceof Kertas) {
            Kertas k = (Kertas) s;
            categoryLabel = "KERTAS";
            accent = Theme.WARN_FG;
            conditionText = k.getJenisKertas() + " \u2022 " + (k.isKering() ? "Kering (100%)" : "Basah (-30%)");
            if (!k.isKering()) {
                condBg = Theme.WARN_BG;
                condFg = Theme.WARN_FG;
            }
        } else if (s instanceof Logam) {
            Logam l = (Logam) s;
            categoryLabel = "LOGAM";
            accent = Theme.NAVY;
            conditionText = l.getJenisLogam() + " \u2022 Kemurnian " + (int) Math.round(l.getKadarKemurnian() * 100) + "%";
            condBg = Theme.INFO_BG;
            condFg = Theme.INFO_FG;
        }

        Theme.CardPanel card = new Theme.CardPanel(accent);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblId = new JLabel(s.getIdSampah());
        lblId.setFont(Theme.FONT_MONO);
        lblId.setForeground(Theme.TEXT_MUTED);
        topRow.add(lblId, BorderLayout.WEST);
        topRow.add(Theme.createBadge(categoryLabel, Theme.ICE_BLUE, Theme.TECH_BLUE), BorderLayout.EAST);

        JLabel lblNama = new JLabel(s.getNamaSampah());
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNama.setForeground(Theme.NAVY);
        lblNama.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblHarga = new JLabel(Theme.formatRupiah(s.getHargaPerKg()) + " / kg");
        lblHarga.setFont(Theme.FONT_BODY_BOLD);
        lblHarga.setForeground(Theme.TEXT_DARK);
        lblHarga.setAlignmentX(LEFT_ALIGNMENT);

        JLabel badgeCond = Theme.createBadge(conditionText, condBg, condFg);
        badgeCond.setAlignmentX(LEFT_ALIGNMENT);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JTextField txtBerat = new JTextField("1.0");
        txtBerat.setFont(Theme.FONT_BODY);
        txtBerat.setToolTipText("Berat dalam kg");

        JButton btnAdd = Theme.createPrimaryButton("+ Keranjang");
        btnAdd.addActionListener(e -> {
            try {
                double berat = Double.parseDouble(txtBerat.getText().trim().replace(',', '.'));
                DetailSetoran detail = new DetailSetoran(s, berat);
                cart.add(detail);
                refreshCartTable();
                context.logOop("DetailSetoran", "Hitung estimasi " + s.getNamaSampah() + " (" + berat + " kg) = "
                        + Theme.formatRupiah(detail.getSubtotal()));
            } catch (NumberFormatException ex) {
                Theme.showError(this, "Masukkan berat angka yang valid (contoh: 2.5).");
            } catch (IllegalArgumentException ex) {
                Theme.showError(this, ex.getMessage());
            }
        });
        inputRow.add(txtBerat, BorderLayout.CENTER);
        inputRow.add(btnAdd, BorderLayout.EAST);

        card.add(topRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblNama);
        card.add(Box.createVerticalStrut(2));
        card.add(lblHarga);
        card.add(Box.createVerticalStrut(8));
        card.add(badgeCond);
        card.add(Box.createVerticalStrut(12));
        card.add(inputRow);

        return card;
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (DetailSetoran d : cart) {
            total += d.getSubtotal();
            cartModel.addRow(new Object[]{
                    d.getSampah().getNamaSampah(),
                    d.getBerat() + " kg",
                    Theme.formatRupiah(d.getSubtotal())
            });
        }
        int estimasiPoin = (int) (total / 100);
        lblCartTotal.setText("Estimasi Total: " + Theme.formatRupiah(total) + " (" + estimasiPoin + " poin)");
    }

    private void refreshHistoryTable() {
        historyModel.setRowCount(0);
        for (Setoran s : currentHistoryList) {
            historyModel.addRow(new Object[]{
                    s.getIdSetoran(),
                    Theme.formatTanggal(s.getTanggalSetor()),
                    s.getDaftarDetail().size() + " item",
                    Theme.formatRupiah(s.getTotalNilai()),
                    s.getTotalPoin() + " poin",
                    s.getStatus()
            });
        }
    }

    private void refreshHadiahCards() {
        hadiahGridPanel.removeAll();
        int userPoin = nasabah.getDompet().getTotalPoin();
        for (Hadiah h : context.getDaftarHadiah()) {
            Theme.CardPanel card = new Theme.CardPanel(h.cekStok() ? Theme.TECH_BLUE : Theme.TEXT_MUTED);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lblId = new JLabel(h.getIdHadiah());
            lblId.setFont(Theme.FONT_MONO);
            lblId.setForeground(Theme.TEXT_MUTED);
            JLabel stockBadge = h.cekStok()
                    ? Theme.createBadge("Stok: " + h.getStok(), Theme.SUCCESS_BG, Theme.SUCCESS_FG)
                    : Theme.createBadge("Habis", Theme.DANGER_BG, Theme.DANGER_FG);
            topRow.add(lblId, BorderLayout.WEST);
            topRow.add(stockBadge, BorderLayout.EAST);

            JLabel lblNama = new JLabel(h.getNamaHadiah());
            lblNama.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblNama.setForeground(Theme.NAVY);
            lblNama.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lblPoin = new JLabel(h.getPoinDibutuhkan() + " poin dibutuhkan");
            lblPoin.setFont(Theme.FONT_BODY_BOLD);
            lblPoin.setForeground(Theme.TECH_BLUE);
            lblPoin.setAlignmentX(LEFT_ALIGNMENT);

            JButton btnTukar = Theme.createPrimaryButton("Tukar Poin");
            btnTukar.setAlignmentX(LEFT_ALIGNMENT);
            btnTukar.setEnabled(h.cekStok() && userPoin >= h.getPoinDibutuhkan());
            btnTukar.addActionListener(e -> {
                try {
                    if (context.tukarPoin(nasabah, h)) {
                        Theme.showInfo(this, "Penukaran " + h.getNamaHadiah() + " berhasil! Sisa poin: "
                                + nasabah.getDompet().getTotalPoin());
                        refreshData();
                    } else {
                        Theme.showError(this, "Penukaran gagal. Periksa stok atau jumlah poin.");
                    }
                } catch (Exception ex) {
                    Theme.showError(this, "Gagal menyimpan ke Neon DB: " + ex.getMessage());
                }
            });

            card.add(topRow);
            card.add(Box.createVerticalStrut(8));
            card.add(lblNama);
            card.add(Box.createVerticalStrut(4));
            card.add(lblPoin);
            card.add(Box.createVerticalStrut(12));
            card.add(btnTukar);

            hadiahGridPanel.add(card);
        }
        hadiahGridPanel.revalidate();
        hadiahGridPanel.repaint();
    }

    private void handleAjukanSetoran() {
        if (cart.isEmpty()) {
            Theme.showError(this, "Keranjang setoran masih kosong. Tambahkan minimal 1 jenis sampah.");
            return;
        }
        try {
            Setoran created = context.submitSetoran(nasabah, new ArrayList<>(cart));
            cart.clear();
            refreshData();
            Theme.showInfo(this, "Setoran " + created.getIdSetoran() + " berhasil diajukan!\n"
                    + "Status saat ini: DIAJUKAN (menunggu penimbangan oleh Petugas).");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal mengajukan setoran: " + ex.getMessage());
        }
    }

    private void showSelectedSetoranDetails() {
        int row = historyTable.getSelectedRow();
        if (row < 0 || row >= currentHistoryList.size()) {
            Theme.showError(this, "Pilih salah satu transaksi setoran dari tabel terlebih dahulu.");
            return;
        }
        Setoran s = currentHistoryList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("ID Setoran : ").append(s.getIdSetoran()).append("\n");
        sb.append("Status     : ").append(s.getStatus()).append("\n");
        sb.append("Tanggal    : ").append(Theme.formatTanggal(s.getTanggalSetor())).append("\n\n");
        sb.append("Rincian Item:\n");
        for (DetailSetoran d : s.getDaftarDetail()) {
            sb.append(" - ").append(d).append("\n");
        }
        sb.append("\nTotal Nilai: ").append(Theme.formatRupiah(s.getTotalNilai()));
        sb.append(" (").append(s.getTotalPoin()).append(" poin)");
        Theme.showInfo(this, sb.toString());
    }

    private void handleBatalkanSetoran() {
        int row = historyTable.getSelectedRow();
        if (row < 0 || row >= currentHistoryList.size()) {
            Theme.showError(this, "Pilih transaksi yang ingin dibatalkan.");
            return;
        }
        Setoran s = currentHistoryList.get(row);
        if (!"DIAJUKAN".equals(s.getStatus()) && !"DITIMBANG".equals(s.getStatus())) {
            Theme.showError(this, "Setoran berstatus " + s.getStatus() + " tidak dapat dibatalkan.");
            return;
        }
        try {
            context.batalkanSetoran(s);
            refreshData();
            Theme.showInfo(this, "Setoran " + s.getIdSetoran() + " berhasil dibatalkan.");
        } catch (Exception ex) {
            Theme.showError(this, "Gagal membatalkan: " + ex.getMessage());
        }
    }

    private void openTarikSaldoDialog() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tarik Saldo (Polymorphism MetodePenarikan)", true);
        dialog.setSize(440, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1.0;

        JRadioButton rbBank = new JRadioButton("Transfer Bank (Admin Rp2.500)", true);
        JRadioButton rbEwallet = new JRadioButton("E-Wallet (Admin 1%)");
        rbBank.setOpaque(false);
        rbEwallet.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbBank);
        bg.add(rbEwallet);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbBank);
        radioRow.add(rbEwallet);

        JLabel lblField1 = new JLabel("Nama Bank");
        JTextField txtField1 = new JTextField("BCA");
        JLabel lblField2 = new JLabel("No. Rekening");
        JTextField txtField2 = new JTextField();
        JLabel lblField3 = new JLabel("Nama Pemilik");
        JTextField txtField3 = new JTextField(nasabah.getNama());

        JTextField txtJumlah = new JTextField("10000");
        JLabel lblPreview = new JLabel("Biaya admin: Rp2.500 | Total potong: Rp12.500");
        lblPreview.setFont(Theme.FONT_SMALL);
        lblPreview.setForeground(Theme.TECH_BLUE);

        Runnable updateLabelsAndPreview = () -> {
            boolean isBank = rbBank.isSelected();
            lblField1.setText(isBank ? "Nama Bank" : "Penyedia E-Wallet");
            if (isBank && txtField1.getText().equals("GoPay")) txtField1.setText("BCA");
            if (!isBank && txtField1.getText().equals("BCA")) txtField1.setText("GoPay");
            lblField2.setText(isBank ? "No. Rekening" : "No. HP Terdaftar");
            lblField3.setVisible(isBank);
            txtField3.setVisible(isBank);

            try {
                double jml = Double.parseDouble(txtJumlah.getText().trim());
                MetodePenarikan previewMetode = isBank
                        ? new TransferBank("X", "0", "X")
                        : new EWallet("X", "0");
                double admin = previewMetode.hitungBiayaAdmin(jml);
                lblPreview.setText("Biaya admin: " + Theme.formatRupiah(admin)
                        + " | Total potong: " + Theme.formatRupiah(jml + admin));
            } catch (NumberFormatException ex) {
                lblPreview.setText("Masukkan nominal angka yang valid.");
            }
        };

        rbBank.addActionListener(e -> updateLabelsAndPreview.run());
        rbEwallet.addActionListener(e -> updateLabelsAndPreview.run());
        txtJumlah.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLabelsAndPreview.run(); }
            public void removeUpdate(DocumentEvent e) { updateLabelsAndPreview.run(); }
            public void changedUpdate(DocumentEvent e) { updateLabelsAndPreview.run(); }
        });

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        panel.add(new JLabel("Saldo Tersedia: " + Theme.formatRupiah(nasabah.getDompet().getSaldoRupiah())), gc);
        gc.gridy = 1; panel.add(radioRow, gc);
        gc.gridwidth = 1;
        gc.gridy = 2; gc.gridx = 0; panel.add(lblField1, gc);
        gc.gridx = 1; panel.add(txtField1, gc);
        gc.gridy = 3; gc.gridx = 0; panel.add(lblField2, gc);
        gc.gridx = 1; panel.add(txtField2, gc);
        gc.gridy = 4; gc.gridx = 0; panel.add(lblField3, gc);
        gc.gridx = 1; panel.add(txtField3, gc);
        gc.gridy = 5; gc.gridx = 0; panel.add(new JLabel("Jumlah Tarik (Rp)"), gc);
        gc.gridx = 1; panel.add(txtJumlah, gc);
        gc.gridy = 6; gc.gridx = 0; gc.gridwidth = 2; panel.add(lblPreview, gc);

        JButton btnProses = Theme.createPrimaryButton("Proses Penarikan");
        btnProses.addActionListener(e -> {
            try {
                double jumlah = Double.parseDouble(txtJumlah.getText().trim());
                String f1 = txtField1.getText().trim();
                String f2 = txtField2.getText().trim();
                String f3 = txtField3.getText().trim();
                if (f1.isEmpty() || f2.isEmpty() || (rbBank.isSelected() && f3.isEmpty())) {
                    Theme.showError(dialog, "Lengkapi detail rekening/e-wallet terlebih dahulu.");
                    return;
                }
                MetodePenarikan metode = rbBank.isSelected()
                        ? new TransferBank(f1, f2, f3)
                        : new EWallet(f1, f2);
                if (context.tarikSaldo(nasabah, jumlah, metode)) {
                    dialog.dispose();
                    refreshData();
                    Theme.showInfo(this, "Penarikan via " + metode.getNamaMetode() + " berhasil diproses!\n"
                            + "Sisa saldo: " + Theme.formatRupiah(nasabah.getDompet().getSaldoRupiah()));
                } else {
                    Theme.showError(dialog, "Penarikan gagal: saldo tidak mencukupi (termasuk biaya admin) atau nominal tidak valid.");
                }
            } catch (NumberFormatException ex) {
                Theme.showError(dialog, "Masukkan nominal angka yang valid.");
            } catch (Exception ex) {
                Theme.showError(dialog, "Gagal memproses penarikan: " + ex.getMessage());
            }
        });
        gc.gridy = 7; gc.insets = new Insets(14, 4, 4, 4);
        panel.add(btnProses, gc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
