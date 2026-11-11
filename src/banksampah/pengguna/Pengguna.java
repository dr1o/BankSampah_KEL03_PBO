package banksampah.pengguna;

import java.util.Objects;

/**
 * Abstract class: kelas induk semua pengguna sistem (Nasabah, Petugas, Admin).
 * Menyimpan data akun yang sama untuk semua peran.
 */
public abstract class Pengguna {

    // ---- Atribut (sesuai diagram) ----
    private final String idPengguna;
    private String nama;
    private final String email;
    private String password;
    private final String noHp;

    protected Pengguna(String idPengguna, String nama, String email, String password, String noHp) {
        this.idPengguna = Objects.requireNonNull(idPengguna, "idPengguna tidak boleh null");
        this.nama = Objects.requireNonNull(nama, "nama tidak boleh null");
        this.email = Objects.requireNonNull(email, "email tidak boleh null");
        this.password = Objects.requireNonNull(password, "password tidak boleh null");
        this.noHp = Objects.requireNonNull(noHp, "noHp tidak boleh null");
    }

    // ---- Method (sesuai diagram) ----

    /** Mengembalikan true jika email dan password cocok dengan data akun ini. */
    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public void logout() {
        System.out.println(nama + " telah logout.");
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tidak boleh kosong.");
        }
        this.nama = nama;
    }

    /** Mengubah password jika password lama benar dan password baru minimal 6 karakter. */
    public boolean ubahPassword(String lama, String baru) {
        if (!password.equals(lama) || baru == null || baru.length() < 6) {
            return false;
        }
        this.password = baru;
        return true;
    }

    /** Method abstrak: setiap peran menampilkan menu yang berbeda (polymorphism). */
    public abstract void tampilkanMenu();

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "] " + idPengguna + " | " + nama + " | " + email + " | " + noHp;
    }
}
