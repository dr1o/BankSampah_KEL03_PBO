package banksampah.keuangan;

import java.util.Locale;
import java.util.Objects;

/**
 * Dompet milik satu Nasabah (relasi composition). Menyimpan saldo rupiah dan poin.
 * Encapsulation: tidak ada setSaldo()/setPoin(); nilai hanya berubah lewat method tambah dan kurangi,
 * sehingga saldo dan poin tidak pernah negatif.
 */
public class Dompet {

    // ---- Atribut (sesuai diagram) ----
    private final String idDompet;
    private double saldoRupiah;
    private int totalPoin;

    public Dompet(String idDompet) {
        this.idDompet = Objects.requireNonNull(idDompet, "idDompet tidak boleh null");
        this.saldoRupiah = 0;
        this.totalPoin = 0;
    }

    // ---- Method (sesuai diagram) ----

    public void tambahSaldo(double jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah saldo yang ditambahkan harus lebih dari 0.");
        }
        saldoRupiah += jumlah;
    }

    /** Mengembalikan false (saldo tidak berubah) bila jumlah tidak valid atau saldo tidak cukup. */
    public boolean kurangiSaldo(double jumlah) {
        if (jumlah <= 0 || jumlah > saldoRupiah) {
            return false;
        }
        saldoRupiah -= jumlah;
        return true;
    }

    public void tambahPoin(int poin) {
        if (poin <= 0) {
            throw new IllegalArgumentException("Poin yang ditambahkan harus lebih dari 0.");
        }
        totalPoin += poin;
    }

    /** Mengembalikan false (poin tidak berubah) bila poin tidak valid atau tidak cukup. */
    public boolean kurangiPoin(int poin) {
        if (poin <= 0 || poin > totalPoin) {
            return false;
        }
        totalPoin -= poin;
        return true;
    }

    public double getSaldoRupiah() {
        return saldoRupiah;
    }

    public int getTotalPoin() {
        return totalPoin;
    }

    @Override
    public String toString() {
        return "Dompet " + idDompet
                + String.format(Locale.forLanguageTag("id-ID"), " | saldo Rp%,.0f | poin %d", saldoRupiah, totalPoin);
    }
}
