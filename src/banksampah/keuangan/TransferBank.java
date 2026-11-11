package banksampah.keuangan;

import java.util.Locale;
import java.util.Objects;

/** Penarikan saldo lewat rekening bank. Biaya admin tetap Rp2.500. */
public class TransferBank implements MetodePenarikan {

    // ---- Atribut (sesuai diagram) ----
    private final String namaBank;
    private final String noRekening;
    private final String namaPemilik;

    public TransferBank(String namaBank, String noRekening, String namaPemilik) {
        this.namaBank = Objects.requireNonNull(namaBank, "namaBank tidak boleh null");
        this.noRekening = Objects.requireNonNull(noRekening, "noRekening tidak boleh null");
        this.namaPemilik = Objects.requireNonNull(namaPemilik, "namaPemilik tidak boleh null");
    }

    // ---- Method (sesuai diagram) ----
    @Override
    public boolean proses(double jumlah) {
        if (jumlah <= 0) {
            return false;
        }
        System.out.println(String.format(Locale.forLanguageTag("id-ID"),
                "Transfer Rp%,.0f ke %s %s a.n. %s berhasil diproses.", jumlah, namaBank, noRekening, namaPemilik));
        return true;
    }

    @Override
    public double hitungBiayaAdmin(double jumlah) {
        return 2500;
    }

    @Override
    public String getNamaMetode() {
        return "Transfer Bank " + namaBank;
    }
}
