package banksampah.sampah;

import java.util.Objects;

/** Sampah logam. Nilai dikalikan kadar kemurnian (0.0 sampai 1.0, misalnya 0.85 = 85%). */
public class Logam extends Sampah {

    // ---- Atribut (sesuai diagram) ----
    private final String jenisLogam;
    private final double kadarKemurnian;

    public Logam(String idSampah, String namaSampah, double hargaPerKg,
                 String jenisLogam, double kadarKemurnian) {
        super(idSampah, namaSampah, hargaPerKg);
        if (kadarKemurnian < 0 || kadarKemurnian > 1) {
            throw new IllegalArgumentException("Kadar kemurnian harus antara 0.0 dan 1.0.");
        }
        this.jenisLogam = Objects.requireNonNull(jenisLogam, "jenisLogam tidak boleh null");
        this.kadarKemurnian = kadarKemurnian;
    }

    // ---- Method (sesuai diagram) ----
    @Override
    public double hitungNilai(double berat) {
        if (berat < 0) {
            throw new IllegalArgumentException("Berat tidak boleh negatif.");
        }
        return hargaPerKg * berat * kadarKemurnian;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + jenisLogam + ", kemurnian " + (int) Math.round(kadarKemurnian * 100) + "%]";
    }
}
