package banksampah.sampah;

import java.util.Locale;
import java.util.Objects;

/**
 * Abstract class: kelas induk semua jenis sampah (Plastik, Kertas, Logam).
 * Cara menghitung nilai berbeda untuk tiap jenis (polymorphism lewat hitungNilai).
 */
public abstract class Sampah {

    // ---- Atribut (sesuai diagram) ----
    private final String idSampah;
    private final String namaSampah;
    protected double hargaPerKg; // protected: dipakai subkelas untuk menghitung nilai

    protected Sampah(String idSampah, String namaSampah, double hargaPerKg) {
        if (hargaPerKg < 0) {
            throw new IllegalArgumentException("Harga per kg tidak boleh negatif.");
        }
        this.idSampah = Objects.requireNonNull(idSampah, "idSampah tidak boleh null");
        this.namaSampah = Objects.requireNonNull(namaSampah, "namaSampah tidak boleh null");
        this.hargaPerKg = hargaPerKg;
    }

    // ---- Method (sesuai diagram) ----

    public String getNamaSampah() {
        return namaSampah;
    }

    public double getHargaPerKg() {
        return hargaPerKg;
    }

    public void setHargaPerKg(double harga) {
        if (harga < 0) {
            throw new IllegalArgumentException("Harga per kg tidak boleh negatif.");
        }
        this.hargaPerKg = harga;
    }

    /** Method abstrak: nilai rupiah untuk berat (kg) tertentu, diimplementasikan tiap subkelas. */
    public abstract double hitungNilai(double berat);

    /** Poin dari nilai rupiah: 1 poin untuk setiap Rp100 (pembulatan ke bawah). */
    public int hitungPoin(double nilai) {
        if (nilai <= 0) {
            return 0;
        }
        return (int) (nilai / 100);
    }

    @Override
    public String toString() {
        return idSampah + " - " + namaSampah
                + String.format(Locale.forLanguageTag("id-ID"), " (Rp%,.0f/kg)", hargaPerKg);
    }
}
