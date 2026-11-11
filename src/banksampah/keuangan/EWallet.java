package banksampah.keuangan;

import java.util.Locale;
import java.util.Objects;

/** Penarikan saldo lewat dompet digital. Biaya admin 1% dari jumlah penarikan. */
public class EWallet implements MetodePenarikan {

    // ---- Atribut (sesuai diagram) ----
    private final String penyedia;
    private final String noHpTerdaftar;

    public EWallet(String penyedia, String noHpTerdaftar) {
        this.penyedia = Objects.requireNonNull(penyedia, "penyedia tidak boleh null");
        this.noHpTerdaftar = Objects.requireNonNull(noHpTerdaftar, "noHpTerdaftar tidak boleh null");
    }

    // ---- Method (sesuai diagram) ----
    @Override
    public boolean proses(double jumlah) {
        if (jumlah <= 0) {
            return false;
        }
        System.out.println(String.format(Locale.forLanguageTag("id-ID"),
                "Pengiriman Rp%,.0f ke %s %s berhasil diproses.", jumlah, penyedia, noHpTerdaftar));
        return true;
    }

    @Override
    public double hitungBiayaAdmin(double jumlah) {
        return jumlah * 0.01;
    }

    @Override
    public String getNamaMetode() {
        return "E-Wallet " + penyedia;
    }
}
