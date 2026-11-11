package banksampah.pengguna;

import banksampah.hadiah.Hadiah;
import banksampah.keuangan.Dompet;
import banksampah.keuangan.MetodePenarikan;
import banksampah.setoran.Setoran;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Warga penyetor sampah. Mewarisi data akun dari Pengguna. */
public class Nasabah extends Pengguna {

    // ---- Atribut (sesuai diagram) ----
    private final String alamat;
    private final Date tanggalDaftar;

    // ---- Relasi (diagram): Nasabah 1 *-- 1 Dompet (composition: dompet dibuat bersama nasabah) ----
    private final Dompet dompet;

    // ---- Relasi (diagram): Nasabah 1 --> 0..* Setoran ----
    private final List<Setoran> riwayatSetoran = new ArrayList<>();

    public Nasabah(String idPengguna, String nama, String email, String password, String noHp, String alamat) {
        super(idPengguna, nama, email, password, noHp);
        this.alamat = Objects.requireNonNull(alamat, "alamat tidak boleh null");
        this.tanggalDaftar = new Date();
        this.dompet = new Dompet("DMP-" + idPengguna);
    }

    // ---- Method (sesuai diagram) ----

    /**
     * Mengajukan setoran. Setoran sebaiknya dibuat dengan dompet milik nasabah ini:
     * new Setoran(id, nasabah.getDompet()).
     */
    public void setorSampah(Setoran setoran) {
        Objects.requireNonNull(setoran, "setoran tidak boleh null");
        riwayatSetoran.add(setoran);
        System.out.println(getNama() + " mengajukan setoran: " + setoran);
    }

    /** Menukar poin dengan hadiah. Berhasil bila stok tersedia dan poin cukup. */
    public boolean tukarPoin(Hadiah hadiah) {
        Objects.requireNonNull(hadiah, "hadiah tidak boleh null");
        if (!hadiah.cekStok()) {
            System.out.println("Penukaran gagal: stok hadiah habis.");
            return false;
        }
        if (!dompet.kurangiPoin(hadiah.getPoinDibutuhkan())) {
            System.out.println("Penukaran gagal: poin tidak cukup (butuh "
                    + hadiah.getPoinDibutuhkan() + ", punya " + dompet.getTotalPoin() + ").");
            return false;
        }
        hadiah.kurangiStok();
        System.out.println("Penukaran berhasil. Sisa poin: " + dompet.getTotalPoin());
        return true;
    }

    /**
     * Menarik saldo lewat metode yang dipilih (polymorphism lewat interface MetodePenarikan).
     * Saldo dipotong sebesar jumlah ditambah biaya admin metode tersebut.
     */
    public boolean tarikSaldo(double jumlah, MetodePenarikan metode) {
        Objects.requireNonNull(metode, "metode tidak boleh null");
        if (jumlah <= 0) {
            System.out.println("Penarikan gagal: jumlah harus lebih dari 0.");
            return false;
        }
        double biayaAdmin = metode.hitungBiayaAdmin(jumlah);
        double totalPotongan = jumlah + biayaAdmin;
        Locale id = Locale.forLanguageTag("id-ID");
        if (!dompet.kurangiSaldo(totalPotongan)) {
            System.out.println(String.format(id,
                    "Penarikan gagal: saldo tidak cukup (butuh Rp%,.0f termasuk biaya admin, saldo Rp%,.0f).",
                    totalPotongan, dompet.getSaldoRupiah()));
            return false;
        }
        if (!metode.proses(jumlah)) {
            dompet.tambahSaldo(totalPotongan); // pengiriman gagal: saldo dikembalikan
            System.out.println("Penarikan gagal diproses oleh " + metode.getNamaMetode() + ". Saldo dikembalikan.");
            return false;
        }
        System.out.println(String.format(id,
                "Penarikan via %s berhasil. Biaya admin Rp%,.0f. Sisa saldo Rp%,.0f.",
                metode.getNamaMetode(), biayaAdmin, dompet.getSaldoRupiah()));
        return true;
    }

    /** Daftar setoran yang pernah diajukan nasabah ini (hanya baca). */
    public List<Setoran> lihatRiwayat() {
        return Collections.unmodifiableList(new ArrayList<>(riwayatSetoran));
    }

    public Dompet getDompet() {
        return dompet;
    }

    @Override
    public void tampilkanMenu() {
        System.out.println("=== Menu Nasabah: " + getNama() + " ===");
        System.out.println("1. Setor sampah");
        System.out.println("2. Lihat riwayat setoran");
        System.out.println("3. Lihat saldo dan poin");
        System.out.println("4. Tukar poin dengan hadiah");
        System.out.println("5. Tarik saldo");
        System.out.println("6. Ubah password");
        System.out.println("0. Logout");
    }

    @Override
    public String toString() {
        return super.toString() + " | " + alamat + " | daftar " + tanggalDaftar;
    }
}
