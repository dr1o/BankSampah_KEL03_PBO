package banksampah.setoran;

import banksampah.keuangan.Dompet;
import banksampah.pengguna.Petugas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Satu transaksi penyetoran sampah.
 * Status (String): DIAJUKAN, DITIMBANG, DIVERIFIKASI, SELESAI, atau DITOLAK.
 * Alur: DIAJUKAN -> (hitungTotal) DITIMBANG -> (verifikasi) DIVERIFIKASI -> SELESAI.
 */
public class Setoran {

    // Konstanta nilai status (bukan atribut instance)
    private static final String DIAJUKAN = "DIAJUKAN";
    private static final String DITIMBANG = "DITIMBANG";
    private static final String DIVERIFIKASI = "DIVERIFIKASI";
    private static final String SELESAI = "SELESAI";
    private static final String DITOLAK = "DITOLAK";

    // ---- Atribut (sesuai diagram) ----
    private final String idSetoran;
    private final Date tanggalSetor;
    private String status;
    private double totalNilai;
    private int totalPoin;

    // ---- Relasi (diagram): Setoran 1 *-- 1..* DetailSetoran ----
    private final List<DetailSetoran> daftarDetail = new ArrayList<>();

    // ---- Relasi (diagram): Setoran ..> Dompet (dompet milik nasabah penyetor) ----
    private final Dompet dompet;

    public Setoran(String idSetoran, Dompet dompet) {
        this.idSetoran = Objects.requireNonNull(idSetoran, "idSetoran tidak boleh null");
        this.dompet = Objects.requireNonNull(dompet, "dompet tidak boleh null");
        this.tanggalSetor = new Date();
        this.status = DIAJUKAN;
        this.totalNilai = 0;
        this.totalPoin = 0;
    }

    // ---- Method (sesuai diagram) ----

    /** Menambah item sampah. Hanya boleh saat status masih DIAJUKAN (sebelum ditimbang). */
    public void tambahDetail(DetailSetoran detail) {
        Objects.requireNonNull(detail, "detail tidak boleh null");
        if (!DIAJUKAN.equals(status)) {
            throw new IllegalStateException("Detail hanya bisa ditambah saat status DIAJUKAN (status sekarang: " + status + ").");
        }
        daftarDetail.add(detail);
    }

    /**
     * Menjumlahkan subtotal seluruh detail menjadi totalNilai dan menghitung totalPoin.
     * Dipanggil petugas saat penimbangan; bila status masih DIAJUKAN dan ada detail, status menjadi DITIMBANG.
     */
    public double hitungTotal() {
        double total = 0;
        for (DetailSetoran detail : daftarDetail) {
            total += detail.getSubtotal();
        }
        totalNilai = total;
        totalPoin = (int) (total / 100); // aturan sama dengan Sampah.hitungPoin(): 1 poin per Rp100
        if (DIAJUKAN.equals(status) && !daftarDetail.isEmpty()) {
            status = DITIMBANG;
        }
        return totalNilai;
    }

    /**
     * Verifikasi oleh petugas. Hanya setoran berstatus DITIMBANG yang dapat diverifikasi.
     * Berhasil: saldo dan poin ditambahkan ke dompet nasabah, status menjadi SELESAI, mengembalikan true.
     * Ditolak (total nilai 0): status menjadi DITOLAK, mengembalikan false.
     * Belum ditimbang atau sudah diproses: status tidak berubah, mengembalikan false.
     */
    public boolean verifikasi(Petugas petugas) {
        Objects.requireNonNull(petugas, "petugas tidak boleh null");
        if (!DITIMBANG.equals(status)) {
            return false;
        }
        if (totalNilai <= 0) {
            status = DITOLAK;
            return false;
        }
        status = DIVERIFIKASI;
        dompet.tambahSaldo(totalNilai);
        if (totalPoin > 0) {
            dompet.tambahPoin(totalPoin);
        }
        status = SELESAI;
        return true;
    }

    /** Membatalkan setoran yang belum selesai; statusnya menjadi DITOLAK. */
    public void batalkan() {
        if (SELESAI.equals(status)) {
            throw new IllegalStateException("Setoran yang sudah SELESAI tidak dapat dibatalkan.");
        }
        status = DITOLAK;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return idSetoran + " | " + status
                + String.format(Locale.forLanguageTag("id-ID"), " | Rp%,.0f | %d poin", totalNilai, totalPoin)
                + " | " + daftarDetail.size() + " item";
    }
}
