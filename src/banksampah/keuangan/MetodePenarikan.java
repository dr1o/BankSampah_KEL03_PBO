package banksampah.keuangan;

/**
 * Interface: kontrak yang wajib dipenuhi semua metode penarikan saldo
 * (TransferBank, EWallet). Metode baru dapat ditambahkan tanpa mengubah Nasabah.
 */
public interface MetodePenarikan {

    /** Memproses pengiriman dana sebesar jumlah. Mengembalikan true bila berhasil. */
    boolean proses(double jumlah);

    /** Biaya admin untuk penarikan sebesar jumlah (berbeda tiap metode). */
    double hitungBiayaAdmin(double jumlah);

    String getNamaMetode();
}
