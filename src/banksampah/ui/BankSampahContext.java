package banksampah.ui;

import banksampah.db.BankSampahRepository;
import banksampah.hadiah.Hadiah;
import banksampah.keuangan.MetodePenarikan;
import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;
import banksampah.pengguna.Petugas;
import banksampah.sampah.Sampah;
import banksampah.setoran.DetailSetoran;
import banksampah.setoran.Setoran;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Koordinator aplikasi GUI yang menjembatani UI Swing dengan 15 kelas domain PBO
 * serta menyinkronkan setiap transaksi ke database cloud Neon PostgreSQL.
 */
public class BankSampahContext {

    private final BankSampahRepository repository = new BankSampahRepository();
    private final List<Pengguna> daftarPengguna = new ArrayList<>();
    private final List<Sampah> daftarSampah = new ArrayList<>();
    private final List<Hadiah> daftarHadiah = new ArrayList<>();
    private final List<Setoran> daftarSetoran = new ArrayList<>();
    private final List<String> oopLogs = new ArrayList<>();
    private Consumer<String> logListener;

    private Pengguna currentUser;

    public void setLogListener(Consumer<String> listener) {
        this.logListener = listener;
    }

    public void logOop(String concept, String message) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String entry = String.format("[%s] [%s] %s", time, concept, message);
        oopLogs.add(0, entry);
        if (oopLogs.size() > 100) {
            oopLogs.remove(oopLogs.size() - 1);
        }
        System.out.println(entry);
        if (logListener != null) {
            logListener.accept(entry);
        }
    }

    public List<String> getOopLogs() {
        return java.util.Collections.unmodifiableList(oopLogs);
    }

    public void loadInitialData() throws SQLException {
        repository.loadAll(daftarPengguna, daftarSampah, daftarHadiah, daftarSetoran);
        logOop("Factory + Polymorphism", "Memuat " + daftarSampah.size() + " objek Sampah (Plastik/Kertas/Logam) dan "
                + daftarPengguna.size() + " Pengguna dari Neon DB.");
    }

    public Pengguna login(String email, String password) {
        for (Pengguna p : daftarPengguna) {
            if (p.login(email, password)) {
                this.currentUser = p;
                logOop("Inheritance & Polymorphism", p.getClass().getSimpleName() + ".login() berhasil untuk " + p.getNama());
                return p;
            }
        }
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            logOop("Pengguna", currentUser.getNama() + " memanggil logout()");
            currentUser.logout();
            currentUser = null;
        }
    }

    public Nasabah registerNasabah(String nama, String email, String password, String noHp, String alamat) throws SQLException {
        if (nama.isBlank() || email.isBlank() || noHp.isBlank() || alamat.isBlank()) {
            throw new IllegalArgumentException("Semua kolom pendaftaran wajib diisi.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }
        for (Pengguna p : daftarPengguna) {
            if (email.equalsIgnoreCase(p.getEmail())) {
                throw new IllegalArgumentException("Email sudah terdaftar.");
            }
        }
        List<String> existingIds = new ArrayList<>();
        for (Pengguna p : daftarPengguna) {
            existingIds.add(p.getIdPengguna());
        }
        String newId = BankSampahRepository.generateNextId("N-", existingIds, 3);
        Nasabah nasabah = new Nasabah(newId, nama, email, password, noHp, alamat);
        repository.saveNasabah(nasabah);
        daftarPengguna.add(nasabah);
        logOop("Composition", "Membuat Nasabah " + newId + " sekaligus Dompet " + nasabah.getDompet().getIdDompet() + " di Neon DB.");
        return nasabah;
    }

    public Setoran submitSetoran(Nasabah nasabah, List<DetailSetoran> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Keranjang setoran masih kosong.");
        }
        List<String> existingIds = new ArrayList<>();
        for (Setoran s : daftarSetoran) {
            existingIds.add(s.getIdSetoran());
        }
        String newId = BankSampahRepository.generateNextId("S-", existingIds, 3);
        Setoran setoran = new Setoran(newId, nasabah.getDompet());
        for (DetailSetoran item : items) {
            setoran.tambahDetail(item);
            logOop("Polymorphism", "DetailSetoran memanggil " + item.getSampah().getClass().getSimpleName()
                    + ".hitungNilai(" + item.getBerat() + " kg) = " + Theme.formatRupiah(item.getSubtotal()));
        }
        repository.saveSetoran(nasabah, setoran);
        nasabah.setorSampah(setoran);
        daftarSetoran.add(0, setoran);
        logOop("Composition", "Setoran " + newId + " (" + items.size() + " DetailSetoran) disimpan ke Neon DB [DIAJUKAN].");
        return setoran;
    }

    public void timbangSetoran(Petugas petugas, Setoran setoran) throws SQLException {
        petugas.timbangSampah(setoran);
        repository.updateSetoranTimbang(setoran);
        logOop("Setoran.hitungTotal()", "Petugas " + petugas.getNama() + " menimbang " + setoran.getIdSetoran()
                + " -> Total " + Theme.formatRupiah(setoran.getTotalNilai()) + " (" + setoran.getTotalPoin() + " poin) [DITIMBANG].");
    }

    public boolean verifikasiSetoran(Petugas petugas, Setoran setoran) throws SQLException {
        boolean ok = petugas.verifikasiSetoran(setoran);
        if (ok) {
            repository.saveVerifikasiSetoran(petugas, setoran);
            logOop("Encapsulation", "Petugas memverifikasi " + setoran.getIdSetoran()
                    + " -> Dompet.tambahSaldo() & tambahPoin() disimpan ke Neon DB [SELESAI].");
        }
        return ok;
    }

    public void batalkanSetoran(Setoran setoran) throws SQLException {
        setoran.batalkan();
        repository.updateSetoranStatus(setoran);
        logOop("Setoran.batalkan()", "Setoran " + setoran.getIdSetoran() + " dibatalkan -> status [DITOLAK].");
    }

    public boolean tukarPoin(Nasabah nasabah, Hadiah hadiah) throws SQLException {
        boolean ok = nasabah.tukarPoin(hadiah);
        if (ok) {
            repository.saveTukarPoin(nasabah.getDompet(), hadiah);
            logOop("Encapsulation", nasabah.getNama() + " menukar " + hadiah.getPoinDibutuhkan()
                    + " poin untuk " + hadiah.getNamaHadiah() + " (Dompet.kurangiPoin + Hadiah.kurangiStok).");
        }
        return ok;
    }

    public boolean tarikSaldo(Nasabah nasabah, double jumlah, MetodePenarikan metode) throws SQLException {
        double admin = metode.hitungBiayaAdmin(jumlah);
        boolean ok = nasabah.tarikSaldo(jumlah, metode);
        if (ok) {
            repository.syncDompet(nasabah.getDompet());
            logOop("Interface Polymorphism", metode.getClass().getSimpleName() + ".proses(" + Theme.formatRupiah(jumlah)
                    + ") | Biaya admin: " + Theme.formatRupiah(admin) + " -> Dompet disinkronkan ke Neon DB.");
        }
        return ok;
    }

    public void tambahSampah(Sampah sampah) throws SQLException {
        repository.saveSampah(sampah);
        daftarSampah.add(sampah);
        logOop("Inheritance", "Admin menambah objek " + sampah.getClass().getSimpleName() + " (" + sampah.getIdSampah() + ") ke Neon DB.");
    }

    public void ubahHargaSampah(Sampah sampah, double hargaBaru) throws SQLException {
        sampah.setHargaPerKg(hargaBaru);
        repository.updateHargaSampah(sampah);
        logOop("Encapsulation", "Admin memanggil " + sampah.getClass().getSimpleName() + ".setHargaPerKg("
                + Theme.formatRupiah(hargaBaru) + ") dan menyimpannya ke Neon DB.");
    }

    public void tambahHadiah(Hadiah hadiah) throws SQLException {
        repository.saveHadiah(hadiah);
        daftarHadiah.add(hadiah);
        logOop("Hadiah", "Admin menambah hadiah " + hadiah.getNamaHadiah() + " ke Neon DB.");
    }

    public void tambahStokHadiah(Hadiah hadiah, int tambahan) throws SQLException {
        hadiah.tambahStok(tambahan);
        repository.updateHadiahStok(hadiah);
        logOop("Encapsulation", "Admin menambah stok " + hadiah.getNamaHadiah() + " menjadi " + hadiah.getStok() + ".");
    }

    public boolean ubahPassword(Pengguna pengguna, String lama, String baru) throws SQLException {
        boolean ok = pengguna.ubahPassword(lama, baru);
        if (ok) {
            repository.updatePassword(pengguna);
            logOop("Pengguna.ubahPassword()", "Password " + pengguna.getNama() + " diperbarui di Neon DB.");
        }
        return ok;
    }

    public String nextSampahId() {
        List<String> ids = new ArrayList<>();
        for (Sampah s : daftarSampah) {
            ids.add(s.getIdSampah());
        }
        return BankSampahRepository.generateNextId("SMP-", ids, 2);
    }

    public String nextHadiahId() {
        List<String> ids = new ArrayList<>();
        for (Hadiah h : daftarHadiah) {
            ids.add(h.getIdHadiah());
        }
        return BankSampahRepository.generateNextId("H-", ids, 2);
    }

    public Nasabah findPenyetor(Setoran setoran) {
        for (Pengguna p : daftarPengguna) {
            if (p instanceof Nasabah) {
                Nasabah n = (Nasabah) p;
                if (n.lihatRiwayat().contains(setoran)) {
                    return n;
                }
            }
        }
        return null;
    }

    public Pengguna getCurrentUser() {
        return currentUser;
    }

    public List<Pengguna> getDaftarPengguna() {
        return daftarPengguna;
    }

    public List<Sampah> getDaftarSampah() {
        return daftarSampah;
    }

    public List<Hadiah> getDaftarHadiah() {
        return daftarHadiah;
    }

    public List<Setoran> getDaftarSetoran() {
        return daftarSetoran;
    }
}
