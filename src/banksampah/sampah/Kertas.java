package banksampah.sampah;

import java.util.Objects;

/** Sampah kertas. Nilai dikurangi 30% bila kertas basah (tidak kering). */
public class Kertas extends Sampah {

    // ---- Atribut (sesuai diagram) ----
    private final String jenisKertas;
    private final boolean kering;

    public Kertas(String idSampah, String namaSampah, double hargaPerKg,
                  String jenisKertas, boolean kering) {
        super(idSampah, namaSampah, hargaPerKg);
        this.jenisKertas = Objects.requireNonNull(jenisKertas, "jenisKertas tidak boleh null");
        this.kering = kering;
    }

    // ---- Method (sesuai diagram) ----
    @Override
    public double hitungNilai(double berat) {
        if (berat < 0) {
            throw new IllegalArgumentException("Berat tidak boleh negatif.");
        }
        double nilai = hargaPerKg * berat;
        return kering ? nilai : nilai * 0.7; // kertas basah dipotong 30%
    }

    @Override
    public String toString() {
        return super.toString() + " [" + jenisKertas + ", " + (kering ? "kering" : "basah") + "]";
    }
}
