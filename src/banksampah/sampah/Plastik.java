package banksampah.sampah;

import java.util.Objects;

/** Sampah plastik. Nilai dikurangi 20% bila plastik kotor. */
public class Plastik extends Sampah {

    // ---- Atribut (sesuai diagram) ----
    private final String jenisPlastik;
    private final boolean bersih;

    public Plastik(String idSampah, String namaSampah, double hargaPerKg,
                   String jenisPlastik, boolean bersih) {
        super(idSampah, namaSampah, hargaPerKg);
        this.jenisPlastik = Objects.requireNonNull(jenisPlastik, "jenisPlastik tidak boleh null");
        this.bersih = bersih;
    }

    // ---- Method (sesuai diagram) ----
    @Override
    public double hitungNilai(double berat) {
        if (berat < 0) {
            throw new IllegalArgumentException("Berat tidak boleh negatif.");
        }
        double nilai = hargaPerKg * berat;
        return bersih ? nilai : nilai * 0.8; // plastik kotor dipotong 20%
    }

    @Override
    public String toString() {
        return super.toString() + " [" + jenisPlastik + ", " + (bersih ? "bersih" : "kotor") + "]";
    }
}
