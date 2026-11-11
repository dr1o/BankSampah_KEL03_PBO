package banksampah;

import banksampah.hadiah.Hadiah;
import banksampah.keuangan.EWallet;
import banksampah.keuangan.MetodePenarikan;
import banksampah.keuangan.TransferBank;
import banksampah.pengguna.Admin;
import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;
import banksampah.pengguna.Petugas;
import banksampah.sampah.Kertas;
import banksampah.sampah.Logam;
import banksampah.sampah.Plastik;
import banksampah.sampah.Sampah;
import banksampah.setoran.DetailSetoran;
import banksampah.setoran.Setoran;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Titik masuk aplikasi CLI Bank Sampah (bukan bagian dari class diagram).
 * Hanya mengurus input dan output; aturan bisnis tetap berada di kelas-kelas diagram.
 * Data disimpan di memori, sehingga kembali ke data awal setiap program dijalankan.
 */
public class Main {

    private static final Scanner INPUT = new Scanner(System.in);

    private static final List<Pengguna> daftarPengguna = new ArrayList<>();
    private static final List<Sampah> daftarSampah = new ArrayList<>();
    private static final List<Hadiah> daftarHadiah = new ArrayList<>();
    private static final List<Setoran> daftarSetoran = new ArrayList<>();
    // Pengguna tidak punya getEmail(), jadi keunikan email dicatat di sini.
    private static final Set<String> daftarEmail = new HashSet<>();

    public static void main(String[] args) {
        siapkanDataAwal();
        System.out.println("=======================================");
        System.out.println("        SISTEM BANK SAMPAH");
        System.out.println("=======================================");
        System.out.println("Akun contoh (email / password):");
        System.out.println("  Nasabah : budi@mail.com / rahasia1");
        System.out.println("  Petugas : sari@mail.com / petugas1");
        System.out.println("  Admin   : dewi@mail.com / admin123");

        while (true) {
            System.out.println();
            System.out.println("=== Menu Utama ===");
            System.out.println("1. Login");
            System.out.println("2. Daftar sebagai nasabah");
            System.out.println("0. Keluar");
            switch (baca("Pilih menu: ")) {
                case "1":
                    login();
                    break;
                case "2":
                    daftarNasabah();
                    break;
                case "0":
                    System.out.println("Terima kasih telah menggunakan Sistem Bank Sampah.");
                    return;
                default:
                    System.out.println("Pilihan tidak tersedia.");
            }
        }
    }

    private static void siapkanDataAwal() {
        daftarSampah.add(new Plastik("SMP-01", "Botol PET", 3000, "PET", true));
        daftarSampah.add(new Kertas("SMP-02", "Kardus", 1500, "Kardus", true));
        daftarSampah.add(new Logam("SMP-03", "Aluminium", 8000, "Aluminium", 0.9));

        daftarHadiah.add(new Hadiah("H-01", "Payung", 100, 2));
        daftarHadiah.add(new Hadiah("H-02", "Tas Belanja", 50, 5));

        tambahPengguna(new Nasabah("N-001", "Budi", "budi@mail.com", "rahasia1", "0812", "Jl. Mawar 1"),
                "budi@mail.com");
        tambahPengguna(new Petugas("P-001", "Sari", "sari@mail.com", "petugas1", "0813", "PTG-01", "Wilayah A"),
                "sari@mail.com");
        tambahPengguna(new Admin("A-001", "Dewi", "dewi@mail.com", "admin123", "0814", "PENUH",
                daftarSampah, daftarHadiah, daftarPengguna, daftarSetoran), "dewi@mail.com");
    }

    private static void tambahPengguna(Pengguna pengguna, String email) {
        daftarPengguna.add(pengguna);
        daftarEmail.add(email);
    }

    // ---- Login dan pendaftaran ----

    private static void login() {
        String email = baca("Email: ");
        String password = baca("Password: ");
        for (Pengguna pengguna : daftarPengguna) {
            if (pengguna.login(email, password)) {
                System.out.println("Login berhasil. Selamat datang, " + pengguna.getNama() + ".");
                jalankanSesi(pengguna);
                return;
            }
        }
        System.out.println("Email atau password salah.");
    }

    private static void daftarNasabah() {
        System.out.println("\n=== Pendaftaran Nasabah ===");
        String nama = bacaTeks("Nama: ");
        String email = bacaTeks("Email: ");
        if (!email.contains("@")) {
            System.out.println("Pendaftaran gagal: format email tidak valid.");
            return;
        }
        if (daftarEmail.contains(email)) {
            System.out.println("Pendaftaran gagal: email sudah terdaftar.");
            return;
        }
        String password = baca("Password (minimal 6 karakter): ");
        if (password.length() < 6) {
            System.out.println("Pendaftaran gagal: password minimal 6 karakter.");
            return;
        }
        String noHp = bacaTeks("No. HP: ");
        String alamat = bacaTeks("Alamat: ");
        String id = String.format("N-%03d", daftarPengguna.size() + 1);
        tambahPengguna(new Nasabah(id, nama, email, password, noHp, alamat), email);
        System.out.println("Pendaftaran berhasil dengan ID " + id + ". Silakan login.");
    }

    /** Mengulang menu pengguna sampai logout. Menu dicetak oleh tampilkanMenu() (polymorphism). */
    private static void jalankanSesi(Pengguna pengguna) {
        boolean aktif = true;
        while (aktif) {
            System.out.println();
            pengguna.tampilkanMenu();
            String pilihan = baca("Pilih menu: ");
            if (pengguna instanceof Nasabah) {
                aktif = menuNasabah((Nasabah) pengguna, pilihan);
            } else if (pengguna instanceof Petugas) {
                aktif = menuPetugas((Petugas) pengguna, pilihan);
            } else if (pengguna instanceof Admin) {
                aktif = menuAdmin((Admin) pengguna, pilihan);
            }
        }
        pengguna.logout();
    }

    private static void ubahPassword(Pengguna pengguna) {
        String lama = baca("Password lama: ");
        String baru = baca("Password baru (minimal 6 karakter): ");
        if (pengguna.ubahPassword(lama, baru)) {
            System.out.println("Password berhasil diubah.");
        } else {
            System.out.println("Gagal: password lama salah atau password baru kurang dari 6 karakter.");
        }
    }

    // ---- Menu Nasabah ----

    /** Mengembalikan false bila pengguna memilih logout. */
    private static boolean menuNasabah(Nasabah nasabah, String pilihan) {
        switch (pilihan) {
            case "1":
                setorSampah(nasabah);
                break;
            case "2":
                lihatRiwayat(nasabah);
                break;
            case "3":
                System.out.println(nasabah.getDompet());
                break;
            case "4":
                tukarPoin(nasabah);
                break;
            case "5":
                tarikSaldo(nasabah);
                break;
            case "6":
                ubahPassword(nasabah);
                break;
            case "0":
                return false;
            default:
                System.out.println("Pilihan tidak tersedia.");
        }
        return true;
    }

    private static void setorSampah(Nasabah nasabah) {
        String id = String.format("S-%03d", daftarSetoran.size() + 1);
        Setoran setoran = new Setoran(id, nasabah.getDompet());
        int jumlahItem = 0;
        System.out.println("\n=== Setor Sampah ===");
        while (true) {
            tampilkanDaftar(daftarSampah);
            int nomor = bacaNomor("Nomor sampah (0 = selesai): ", daftarSampah.size());
            if (nomor == 0) {
                break;
            }
            double berat = bacaDesimal("Berat dalam kg, misalnya 2,5 (0 = batal): ");
            if (berat == 0) {
                continue;
            }
            DetailSetoran detail = new DetailSetoran(daftarSampah.get(nomor - 1), berat);
            setoran.tambahDetail(detail);
            jumlahItem++;
            System.out.println("Ditambahkan: " + detail);
        }
        if (jumlahItem == 0) {
            System.out.println("Setoran dibatalkan karena tidak ada sampah yang dipilih.");
            return;
        }
        nasabah.setorSampah(setoran);
        daftarSetoran.add(setoran);
        System.out.println("Setoran menunggu ditimbang oleh petugas.");
    }

    private static void lihatRiwayat(Nasabah nasabah) {
        List<Setoran> riwayat = nasabah.lihatRiwayat();
        System.out.println("\n=== Riwayat Setoran ===");
        if (riwayat.isEmpty()) {
            System.out.println("Belum ada setoran.");
            return;
        }
        tampilkanDaftar(riwayat);
        int nomor = bacaNomor("Nomor setoran yang dibatalkan (0 = kembali): ", riwayat.size());
        if (nomor == 0) {
            return;
        }
        Setoran setoran = riwayat.get(nomor - 1);
        String status = setoran.getStatus();
        if (status.equals("DIAJUKAN") || status.equals("DITIMBANG")) {
            setoran.batalkan();
            System.out.println("Setoran dibatalkan. Status: " + setoran.getStatus());
        } else {
            System.out.println("Setoran berstatus " + status + " tidak dapat dibatalkan.");
        }
    }

    private static void tukarPoin(Nasabah nasabah) {
        System.out.println("\n=== Tukar Poin ===");
        if (daftarHadiah.isEmpty()) {
            System.out.println("Belum ada hadiah.");
            return;
        }
        System.out.println("Poin kamu: " + nasabah.getDompet().getTotalPoin());
        tampilkanDaftar(daftarHadiah);
        int nomor = bacaNomor("Nomor hadiah (0 = kembali): ", daftarHadiah.size());
        if (nomor != 0) {
            nasabah.tukarPoin(daftarHadiah.get(nomor - 1));
        }
    }

    private static void tarikSaldo(Nasabah nasabah) {
        System.out.println("\n=== Tarik Saldo ===");
        System.out.println(nasabah.getDompet());
        System.out.println("1. Transfer Bank (biaya admin Rp2.500)");
        System.out.println("2. E-Wallet (biaya admin 1%)");
        int nomor = bacaNomor("Metode penarikan (0 = kembali): ", 2);
        if (nomor == 0) {
            return;
        }
        MetodePenarikan metode;
        if (nomor == 1) {
            metode = new TransferBank(bacaTeks("Nama bank: "), bacaTeks("No. rekening: "), bacaTeks("Nama pemilik: "));
        } else {
            metode = new EWallet(bacaTeks("Penyedia e-wallet: "), bacaTeks("No. HP terdaftar: "));
        }
        double jumlah = bacaDesimal("Jumlah penarikan dalam rupiah, tanpa titik, misalnya 10000 (0 = batal): ");
        if (jumlah > 0) {
            nasabah.tarikSaldo(jumlah, metode);
        }
    }

    // ---- Menu Petugas ----

    /** Mengembalikan false bila pengguna memilih logout. */
    private static boolean menuPetugas(Petugas petugas, String pilihan) {
        switch (pilihan) {
            case "1":
                Setoran untukDitimbang = pilihSetoran("DIAJUKAN", "ditimbang");
                if (untukDitimbang != null) {
                    petugas.timbangSampah(untukDitimbang);
                }
                break;
            case "2":
                Setoran untukDiverifikasi = pilihSetoran("DITIMBANG", "diverifikasi");
                if (untukDiverifikasi != null) {
                    petugas.verifikasiSetoran(untukDiverifikasi);
                }
                break;
            case "3":
                ubahPassword(petugas);
                break;
            case "0":
                return false;
            default:
                System.out.println("Pilihan tidak tersedia.");
        }
        return true;
    }

    /** Menampilkan setoran berstatus tertentu dan mengembalikan pilihan, atau null bila tidak ada atau kembali. */
    private static Setoran pilihSetoran(String status, String tindakan) {
        List<Setoran> pilihan = new ArrayList<>();
        for (Setoran setoran : daftarSetoran) {
            if (status.equals(setoran.getStatus())) {
                pilihan.add(setoran);
            }
        }
        if (pilihan.isEmpty()) {
            System.out.println("Tidak ada setoran yang perlu " + tindakan + ".");
            return null;
        }
        for (int i = 0; i < pilihan.size(); i++) {
            System.out.println((i + 1) + ". " + pilihan.get(i) + " | nasabah " + namaPenyetor(pilihan.get(i)));
        }
        int nomor = bacaNomor("Nomor setoran yang " + tindakan + " (0 = kembali): ", pilihan.size());
        return nomor == 0 ? null : pilihan.get(nomor - 1);
    }

    private static String namaPenyetor(Setoran setoran) {
        for (Pengguna pengguna : daftarPengguna) {
            if (pengguna instanceof Nasabah && ((Nasabah) pengguna).lihatRiwayat().contains(setoran)) {
                return pengguna.getNama();
            }
        }
        return "-";
    }

    // ---- Menu Admin ----

    /** Mengembalikan false bila pengguna memilih logout. */
    private static boolean menuAdmin(Admin admin, String pilihan) {
        switch (pilihan) {
            case "1":
                admin.kelolaJenisSampah();
                break;
            case "2":
                admin.kelolaHadiah();
                break;
            case "3":
                admin.kelolaPengguna();
                break;
            case "4":
                admin.buatLaporan();
                break;
            case "5":
                ubahPassword(admin);
                break;
            case "0":
                return false;
            default:
                System.out.println("Pilihan tidak tersedia.");
        }
        return true;
    }

    // ---- Pembantu input dan output ----

    private static void tampilkanDaftar(List<?> daftar) {
        for (int i = 0; i < daftar.size(); i++) {
            System.out.println((i + 1) + ". " + daftar.get(i));
        }
    }

    /** Membaca satu baris. Program berakhir dengan rapi bila input ditutup (Ctrl+Z / Ctrl+D). */
    private static String baca(String prompt) {
        System.out.print(prompt);
        if (!INPUT.hasNextLine()) {
            System.out.println("\nInput berakhir. Program ditutup.");
            System.exit(0);
        }
        return INPUT.nextLine().trim();
    }

    private static String bacaTeks(String prompt) {
        while (true) {
            String teks = baca(prompt);
            if (!teks.isEmpty()) {
                return teks;
            }
            System.out.println("Masukan tidak boleh kosong.");
        }
    }

    /** Membaca angka dari 0 sampai maksimum. */
    private static int bacaNomor(String prompt, int maksimum) {
        while (true) {
            try {
                int nomor = Integer.parseInt(baca(prompt));
                if (nomor >= 0 && nomor <= maksimum) {
                    return nomor;
                }
            } catch (NumberFormatException e) {
                // Pesan yang sama dengan nomor di luar rentang.
            }
            System.out.println("Masukkan angka dari 0 sampai " + maksimum + ".");
        }
    }

    /** Membaca angka desimal yang tidak negatif. Koma diterima sebagai pemisah desimal. */
    private static double bacaDesimal(String prompt) {
        while (true) {
            try {
                double nilai = Double.parseDouble(baca(prompt).replace(',', '.'));
                // NaN dan Infinity lolos dari parseDouble, tetapi akan merusak saldo.
                if (nilai >= 0 && !Double.isInfinite(nilai)) {
                    return nilai;
                }
            } catch (NumberFormatException e) {
                // Pesan yang sama dengan angka di luar rentang.
            }
            System.out.println("Masukkan angka yang valid dan tidak negatif.");
        }
    }
}
