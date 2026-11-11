package banksampah.pengguna;

import banksampah.setoran.Setoran;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Petugas bank sampah yang menimbang sampah dan memverifikasi setoran. */
public class Petugas extends Pengguna {

    // ---- Atribut (sesuai diagram) ----
    private final String idPetugas;
    private final String wilayahTugas;

    // ---- Relasi (diagram): Petugas 1 --> 0..* Setoran (setoran yang berhasil diverifikasi petugas ini) ----
    private final List<Setoran> daftarSetoran = new ArrayList<>();

    public Petugas(String idPengguna, String nama, String email, String password, String noHp,
                   String idPetugas, String wilayahTugas) {
        super(idPengguna, nama, email, password, noHp);
        this.idPetugas = Objects.requireNonNull(idPetugas, "idPetugas tidak boleh null");
        this.wilayahTugas = Objects.requireNonNull(wilayahTugas, "wilayahTugas tidak boleh null");
    }

    // ---- Method (sesuai diagram) ----

    /** Menimbang setoran: menghitung total nilai dan poin, lalu status menjadi DITIMBANG. */
    public void timbangSampah(Setoran setoran) {
        Objects.requireNonNull(setoran, "setoran tidak boleh null");
        if (!"DIAJUKAN".equals(setoran.getStatus())) {
            System.out.println("Setoran tidak dapat ditimbang. Status sekarang: " + setoran.getStatus());
            return;
        }
        double total = setoran.hitungTotal();
        if ("DIAJUKAN".equals(setoran.getStatus())) {
            System.out.println("Setoran tidak berisi sampah, penimbangan dibatalkan.");
            return;
        }
        System.out.println(String.format(Locale.forLanguageTag("id-ID"),
                "%s menimbang setoran. Total nilai Rp%,.0f. Status: %s", getNama(), total, setoran.getStatus()));
    }

    /** Memverifikasi setoran yang sudah ditimbang. Mengembalikan true bila berhasil dan saldo masuk ke dompet. */
    public boolean verifikasiSetoran(Setoran setoran) {
        Objects.requireNonNull(setoran, "setoran tidak boleh null");
        boolean berhasil = setoran.verifikasi(this);
        if (berhasil) {
            daftarSetoran.add(setoran);
            System.out.println(getNama() + " memverifikasi setoran: " + setoran);
        } else {
            System.out.println("Verifikasi tidak berhasil. Status setoran: " + setoran.getStatus());
        }
        return berhasil;
    }

    @Override
    public void tampilkanMenu() {
        System.out.println("=== Menu Petugas: " + getNama() + " (" + wilayahTugas + ") ===");
        System.out.println("1. Timbang sampah");
        System.out.println("2. Verifikasi setoran");
        System.out.println("3. Ubah password");
        System.out.println("0. Logout");
    }

    @Override
    public String toString() {
        return super.toString() + " | " + idPetugas + " | wilayah " + wilayahTugas
                + " | " + daftarSetoran.size() + " setoran diverifikasi";
    }
}
