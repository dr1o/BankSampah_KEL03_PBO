package banksampah.pengguna;

import banksampah.hadiah.Hadiah;
import banksampah.sampah.Sampah;
import banksampah.setoran.Setoran;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Pengelola sistem. Mengelola jenis sampah dan hadiah, melihat pengguna, dan membuat laporan.
 * Data yang dikelola diberikan lewat constructor sebagai daftar bersama (shared list),
 * sehingga perubahan yang dilakukan Admin langsung terlihat oleh bagian sistem lain.
 */
public class Admin extends Pengguna {

    // ---- Atribut (sesuai diagram) ----
    private final String levelAkses;

    // ---- Relasi (diagram): Admin ..> Sampah dan Admin ..> Hadiah ("mengelola") ----
    private final List<Sampah> daftarSampah;
    private final List<Hadiah> daftarHadiah;

    // ---- Data untuk kelolaPengguna() dan buatLaporan() ----
    private final List<Pengguna> daftarPengguna;
    private final List<Setoran> daftarSetoran;

    public Admin(String idPengguna, String nama, String email, String password, String noHp,
                 String levelAkses,
                 List<Sampah> daftarSampah, List<Hadiah> daftarHadiah,
                 List<Pengguna> daftarPengguna, List<Setoran> daftarSetoran) {
        super(idPengguna, nama, email, password, noHp);
        this.levelAkses = Objects.requireNonNull(levelAkses, "levelAkses tidak boleh null");
        this.daftarSampah = Objects.requireNonNull(daftarSampah, "daftarSampah tidak boleh null");
        this.daftarHadiah = Objects.requireNonNull(daftarHadiah, "daftarHadiah tidak boleh null");
        this.daftarPengguna = Objects.requireNonNull(daftarPengguna, "daftarPengguna tidak boleh null");
        this.daftarSetoran = Objects.requireNonNull(daftarSetoran, "daftarSetoran tidak boleh null");
    }

    // ---- Method (sesuai diagram) ----

    /** Menampilkan jenis sampah dan mengubah harga per kg lewat input konsol. */
    public void kelolaJenisSampah() {
        Scanner in = new Scanner(System.in); // tidak ditutup agar System.in tetap bisa dipakai
        while (true) {
            System.out.println("\n=== Kelola Jenis Sampah ===");
            if (daftarSampah.isEmpty()) {
                System.out.println("Belum ada jenis sampah.");
                return;
            }
            for (int i = 0; i < daftarSampah.size(); i++) {
                System.out.println((i + 1) + ". " + daftarSampah.get(i));
            }
            System.out.print("Nomor sampah yang diubah harganya (0 = kembali): ");
            if (!in.hasNextLine()) {
                return;
            }
            int nomor;
            try {
                nomor = Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Masukan harus berupa angka.");
                continue;
            }
            if (nomor == 0) {
                return;
            }
            if (nomor < 1 || nomor > daftarSampah.size()) {
                System.out.println("Nomor tidak valid.");
                continue;
            }
            System.out.print("Harga baru per kg (Rp): ");
            if (!in.hasNextLine()) {
                return;
            }
            try {
                double harga = Double.parseDouble(in.nextLine().trim());
                daftarSampah.get(nomor - 1).setHargaPerKg(harga);
                System.out.println("Harga berhasil diperbarui.");
            } catch (NumberFormatException e) {
                System.out.println("Harga harus berupa angka.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /** Menampilkan katalog hadiah dan menambah hadiah baru lewat input konsol. */
    public void kelolaHadiah() {
        Scanner in = new Scanner(System.in); // tidak ditutup agar System.in tetap bisa dipakai
        System.out.println("\n=== Kelola Hadiah ===");
        if (daftarHadiah.isEmpty()) {
            System.out.println("Belum ada hadiah.");
        }
        for (int i = 0; i < daftarHadiah.size(); i++) {
            System.out.println((i + 1) + ". " + daftarHadiah.get(i));
        }
        System.out.print("Tambah hadiah baru? (y/n): ");
        if (!in.hasNextLine() || !in.nextLine().trim().equalsIgnoreCase("y")) {
            return;
        }
        try {
            System.out.print("ID hadiah: ");
            String id = in.nextLine().trim();
            System.out.print("Nama hadiah: ");
            String nama = in.nextLine().trim();
            System.out.print("Poin dibutuhkan: ");
            int poin = Integer.parseInt(in.nextLine().trim());
            System.out.print("Stok: ");
            int stok = Integer.parseInt(in.nextLine().trim());
            daftarHadiah.add(new Hadiah(id, nama, poin, stok));
            System.out.println("Hadiah berhasil ditambahkan.");
        } catch (NumberFormatException e) {
            System.out.println("Poin dan stok harus berupa angka. Hadiah tidak ditambahkan.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + " Hadiah tidak ditambahkan.");
        } catch (NoSuchElementException e) {
            System.out.println("Input berhenti. Hadiah tidak ditambahkan.");
        }
    }

    /** Menampilkan seluruh pengguna terdaftar. */
    public void kelolaPengguna() {
        System.out.println("\n=== Kelola Pengguna ===");
        if (daftarPengguna.isEmpty()) {
            System.out.println("Belum ada pengguna.");
            return;
        }
        for (int i = 0; i < daftarPengguna.size(); i++) {
            System.out.println((i + 1) + ". " + daftarPengguna.get(i));
        }
    }

    /** Menampilkan ringkasan kegiatan bank sampah. */
    public void buatLaporan() {
        int jumlahNasabah = 0;
        int jumlahPetugas = 0;
        int jumlahAdmin = 0;
        for (Pengguna p : daftarPengguna) {
            if (p instanceof Nasabah) {
                jumlahNasabah++;
            } else if (p instanceof Petugas) {
                jumlahPetugas++;
            } else if (p instanceof Admin) {
                jumlahAdmin++;
            }
        }
        System.out.println("\n=== Laporan Bank Sampah ===");
        System.out.println("Pengguna        : " + daftarPengguna.size() + " (nasabah " + jumlahNasabah
                + ", petugas " + jumlahPetugas + ", admin " + jumlahAdmin + ")");
        System.out.println("Jenis sampah    : " + daftarSampah.size());
        System.out.println("Hadiah          : " + daftarHadiah.size());
        System.out.println("Jumlah setoran  : " + daftarSetoran.size());
        String[] semuaStatus = {"DIAJUKAN", "DITIMBANG", "DIVERIFIKASI", "SELESAI", "DITOLAK"};
        for (String status : semuaStatus) {
            int jumlah = 0;
            for (Setoran s : daftarSetoran) {
                if (status.equals(s.getStatus())) {
                    jumlah++;
                }
            }
            System.out.println("  - " + status + ": " + jumlah);
        }
        for (Setoran s : daftarSetoran) {
            System.out.println("  * " + s);
        }
    }

    @Override
    public void tampilkanMenu() {
        System.out.println("=== Menu Admin: " + getNama() + " (akses " + levelAkses + ") ===");
        System.out.println("1. Kelola jenis sampah");
        System.out.println("2. Kelola hadiah");
        System.out.println("3. Kelola pengguna");
        System.out.println("4. Buat laporan");
        System.out.println("5. Ubah password");
        System.out.println("0. Logout");
    }
}
