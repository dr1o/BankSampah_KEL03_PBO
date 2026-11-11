package banksampah.setoran;

import banksampah.sampah.Sampah;

import java.util.Locale;
import java.util.Objects;

/**
 * Satu baris item dalam Setoran (relasi composition dengan Setoran).
 * Subtotal dihitung dengan memanggil hitungNilai() milik Sampah, sehingga rumus yang berjalan
 * mengikuti jenis sampah sebenarnya (polymorphism).
 */
public class DetailSetoran {

    // ---- Atribut (sesuai diagram) ----
    private final double berat;
    private double subtotal;

    // ---- Relasi (diagram): DetailSetoran 0..* --> 1 Sampah ----
    private final Sampah sampah;

    public DetailSetoran(Sampah sampah, double berat) {
        if (berat <= 0) {
            throw new IllegalArgumentException("Berat harus lebih dari 0 kg.");
        }
        this.sampah = Objects.requireNonNull(sampah, "sampah tidak boleh null");
        this.berat = berat;
        this.subtotal = sampah.hitungNilai(berat); // harga dikunci pada saat setoran diajukan
    }

    // ---- Method (sesuai diagram) ----

    public double hitungSubtotal() {
        subtotal = sampah.hitungNilai(berat);
        return subtotal;
    }

    public double getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return sampah.getNamaSampah() + " " + berat + " kg"
                + String.format(Locale.forLanguageTag("id-ID"), " = Rp%,.0f", subtotal);
    }
}
