package banksampah.hadiah;

import java.util.Objects;

/** Hadiah yang dapat ditukar dengan poin. */
public class Hadiah {

    // ---- Atribut (sesuai diagram) ----
    private final String idHadiah;
    private final String namaHadiah;
    private final int poinDibutuhkan;
    private int stok;

    public Hadiah(String idHadiah, String namaHadiah, int poinDibutuhkan, int stok) {
        if (poinDibutuhkan <= 0) {
            throw new IllegalArgumentException("Poin yang dibutuhkan harus lebih dari 0.");
        }
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
        this.idHadiah = Objects.requireNonNull(idHadiah, "idHadiah tidak boleh null");
        this.namaHadiah = Objects.requireNonNull(namaHadiah, "namaHadiah tidak boleh null");
        this.poinDibutuhkan = poinDibutuhkan;
        this.stok = stok;
    }

    // ---- Method (sesuai diagram) ----

    /** true bila stok hadiah masih tersedia. */
    public boolean cekStok() {
        return stok > 0;
    }

    public void kurangiStok() {
        if (stok <= 0) {
            throw new IllegalStateException("Stok hadiah " + namaHadiah + " sudah habis.");
        }
        stok--;
    }

    public int getPoinDibutuhkan() {
        return poinDibutuhkan;
    }

    @Override
    public String toString() {
        return idHadiah + " - " + namaHadiah + " (" + poinDibutuhkan + " poin, stok " + stok + ")";
    }
}
