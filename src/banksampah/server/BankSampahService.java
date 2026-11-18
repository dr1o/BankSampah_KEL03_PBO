package banksampah.server;

import banksampah.db.BankSampahRepository;
import banksampah.hadiah.Hadiah;
import banksampah.keuangan.Dompet;
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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service Backend Pure Java yang mengoordinasikan 15 kelas domain PBO (sesuai Class Diagram &amp; ERD)
 * serta menyinkronkan perubahan ke database Neon PostgreSQL.
 */
public class BankSampahService {

    private final BankSampahRepository repository = new BankSampahRepository();
    private final List<Pengguna> daftarPengguna = new ArrayList<>();
    private final List<Sampah> daftarSampah = new ArrayList<>();
    private final List<Hadiah> daftarHadiah = new ArrayList<>();
    private final List<Setoran> daftarSetoran = new ArrayList<>();
    private final List<String> oopLogs = new ArrayList<>();

    private boolean dbConnected = false;
    private String dbStatusMessage = "Memuat koneksi...";

    public synchronized void initialize() {
        try {
            repository.loadAll(daftarPengguna, daftarSampah, daftarHadiah, daftarSetoran);
            if (daftarPengguna.isEmpty() && daftarSampah.isEmpty()) {
                seedMemoryData();
            }
            dbConnected = true;
            dbStatusMessage = "Terhubung ke Neon PostgreSQL";
            logOop("Factory & Polymorphism", "Memuat " + daftarSampah.size()
                    + " objek Sampah (Plastik/Kertas/Logam), " + daftarPengguna.size()
                    + " Pengguna (Nasabah/Petugas/Admin), " + daftarHadiah.size()
                    + " Hadiah, dan " + daftarSetoran.size() + " Setoran dari Neon DB.");
        } catch (Exception ex) {
            dbConnected = false;
            dbStatusMessage = "Mode Memori Lokal (Fallback: " + ex.getMessage() + ")";
            seedMemoryData();
            logOop("In-Memory Fallback", "Menggunakan data awal di memori untuk 15 kelas domain OOP.");
        }
    }

    private void seedMemoryData() {
        daftarPengguna.clear();
        daftarSampah.clear();
        daftarHadiah.clear();
        daftarSetoran.clear();

        daftarSampah.add(new Plastik("SMP-01", "Botol PET", 3000, "PET", true));
        daftarSampah.add(new Plastik("SMP-04", "Plastik Kresek/HDPE Kotor", 2500, "HDPE", false));
        daftarSampah.add(new Kertas("SMP-02", "Kardus", 1500, "Kardus", true));
        daftarSampah.add(new Kertas("SMP-05", "Kertas Koran Lembab", 1200, "Koran", false));
        daftarSampah.add(new Logam("SMP-03", "Aluminium", 8000, "Aluminium", 0.9));

        daftarHadiah.add(new Hadiah("H-01", "Payung", 100, 2));
        daftarHadiah.add(new Hadiah("H-02", "Tas Belanja", 50, 5));

        daftarPengguna.add(new Nasabah("N-001", "Budi", "budi@mail.com", "rahasia1", "0812", "Jl. Mawar 1"));
        daftarPengguna.add(new Petugas("P-001", "Sari", "sari@mail.com", "petugas1", "0813", "PTG-01", "Wilayah A"));
        daftarPengguna.add(new Admin("A-001", "Dewi", "dewi@mail.com", "admin123", "0814", "PENUH",
                daftarSampah, daftarHadiah, daftarPengguna, daftarSetoran));
    }

    public synchronized void logOop(String concept, String message) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String entry = String.format("[%s] [%s] %s", time, concept, message);
        oopLogs.add(0, entry);
        if (oopLogs.size() > 120) {
            oopLogs.remove(oopLogs.size() - 1);
        }
        System.out.println(entry);
    }

    public synchronized Pengguna login(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email dan password wajib diisi.");
        }
        for (Pengguna p : daftarPengguna) {
            if (p.login(email.trim(), password)) {
                p.tampilkanMenu(); // memanggil abstract method tampilkanMenu() sesuai subkelas
                logOop("Inheritance & Polymorphism", p.getClass().getSimpleName()
                        + ".login() & tampilkanMenu() berhasil untuk " + p.getNama() + " (" + p.getIdPengguna() + ")");
                return p;
            }
        }
        throw new IllegalArgumentException("Email atau password salah.");
    }

    public synchronized void logout(String idPengguna) {
        Pengguna p = findPenggunaById(idPengguna);
        if (p != null) {
            p.logout();
            logOop("Pengguna.logout()", p.getNama() + " (" + p.getClass().getSimpleName() + ") telah logout.");
        }
    }

    public synchronized Nasabah registerNasabah(String nama, String email, String password, String noHp, String alamat)
            throws SQLException {
        validateCommonUserFields(nama, email, password, noHp);
        if (alamat == null || alamat.isBlank()) {
            throw new IllegalArgumentException("Alamat nasabah wajib diisi.");
        }
        List<String> existingIds = new ArrayList<>();
        for (Pengguna p : daftarPengguna) {
            existingIds.add(p.getIdPengguna());
        }
        String newId = BankSampahRepository.generateNextId("N-", existingIds, 3);
        Nasabah nasabah = new Nasabah(newId, nama.trim(), email.trim(), password, noHp.trim(), alamat.trim());
        if (dbConnected) {
            repository.saveNasabah(nasabah);
        }
        daftarPengguna.add(nasabah);
        logOop("Composition (Nasabah *-- Dompet)", "Instansiasi Nasabah " + newId
                + " otomatis membentuk objek Dompet " + nasabah.getDompet().getIdDompet() + ".");
        return nasabah;
    }

    public synchronized Pengguna tambahPenggunaOlehAdmin(String role, String nama, String email, String password,
                                                         String noHp, String extra1, String extra2) throws SQLException {
        validateCommonUserFields(nama, email, password, noHp);
        String cleanRole = role != null ? role.trim().toUpperCase(Locale.ROOT) : "NASABAH";
        List<String> existingIds = new ArrayList<>();
        for (Pengguna p : daftarPengguna) {
            existingIds.add(p.getIdPengguna());
        }

        switch (cleanRole) {
            case "NASABAH": {
                String alamat = (extra1 != null && !extra1.isBlank()) ? extra1.trim() : "-";
                return registerNasabah(nama, email, password, noHp, alamat);
            }
            case "PETUGAS": {
                String newId = BankSampahRepository.generateNextId("P-", existingIds, 3);
                String idPetugas = (extra1 != null && !extra1.isBlank()) ? extra1.trim() : ("PTG-" + newId.substring(2));
                String wilayah = (extra2 != null && !extra2.isBlank()) ? extra2.trim() : "Wilayah A";
                Petugas petugas = new Petugas(newId, nama.trim(), email.trim(), password, noHp.trim(), idPetugas, wilayah);
                if (dbConnected) {
                    repository.savePetugas(petugas);
                }
                daftarPengguna.add(petugas);
                logOop("Inheritance (Petugas extends Pengguna)", "Admin menambahkan Petugas baru "
                        + newId + " (" + idPetugas + " - " + wilayah + ").");
                return petugas;
            }
            case "ADMIN": {
                String newId = BankSampahRepository.generateNextId("A-", existingIds, 3);
                String levelAkses = (extra1 != null && !extra1.isBlank()) ? extra1.trim() : "PENUH";
                Admin admin = new Admin(newId, nama.trim(), email.trim(), password, noHp.trim(), levelAkses,
                        daftarSampah, daftarHadiah, daftarPengguna, daftarSetoran);
                if (dbConnected) {
                    repository.saveAdmin(admin);
                }
                daftarPengguna.add(admin);
                logOop("Inheritance (Admin extends Pengguna)", "Admin menambahkan Admin baru "
                        + newId + " dengan levelAkses=" + levelAkses + ".");
                return admin;
            }
            default:
                throw new IllegalArgumentException("Role pengguna tidak valid: " + role);
        }
    }

    private void validateCommonUserFields(String nama, String email, String password, String noHp) {
        if (nama == null || nama.isBlank() || email == null || email.isBlank()
                || password == null || noHp == null || noHp.isBlank()) {
            throw new IllegalArgumentException("Nama, email, password, dan nomor HP wajib diisi.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter (aturan Pengguna.ubahPassword).");
        }
        for (Pengguna p : daftarPengguna) {
            if (email.trim().equalsIgnoreCase(p.getEmail())) {
                throw new IllegalArgumentException("Email sudah terdaftar di sistem.");
            }
        }
    }

    public synchronized void ubahPassword(String idPengguna, String passwordLama, String passwordBaru)
            throws SQLException {
        Pengguna p = findPenggunaById(idPengguna);
        if (p == null) {
            throw new IllegalArgumentException("Pengguna tidak ditemukan.");
        }
        boolean ok = p.ubahPassword(passwordLama, passwordBaru);
        if (!ok) {
            throw new IllegalArgumentException("Gagal mengubah password: password lama salah atau password baru kurang dari 6 karakter.");
        }
        if (dbConnected) {
            repository.updatePassword(p);
        }
        logOop("Encapsulation (Pengguna.ubahPassword)", "Password milik " + p.getNama()
                + " (" + p.getIdPengguna() + ") berhasil diperbarui.");
    }

    public synchronized void ubahNama(String idPengguna, String namaBaru) throws SQLException {
        Pengguna p = findPenggunaById(idPengguna);
        if (p == null) {
            throw new IllegalArgumentException("Pengguna tidak ditemukan.");
        }
        p.setNama(namaBaru != null ? namaBaru.trim() : "");
        if (dbConnected) {
            repository.updateNamaPengguna(p);
        }
        logOop("Encapsulation (Pengguna.setNama)", "Nama pengguna " + p.getIdPengguna()
                + " diperbarui menjadi " + p.getNama() + ".");
    }

    public synchronized Setoran ajukanSetoran(String idNasabah, List<Map<String, String>> rawItems)
            throws SQLException {
        Pengguna p = findPenggunaById(idNasabah);
        if (!(p instanceof Nasabah)) {
            throw new IllegalArgumentException("Hanya Nasabah yang dapat mengajukan setoran sampah.");
        }
        Nasabah nasabah = (Nasabah) p;
        if (rawItems == null || rawItems.isEmpty()) {
            throw new IllegalArgumentException("Setoran harus memiliki minimal 1 DetailSetoran.");
        }

        List<String> existingIds = new ArrayList<>();
        for (Setoran s : daftarSetoran) {
            existingIds.add(s.getIdSetoran());
        }
        String newId = BankSampahRepository.generateNextId("S-", existingIds, 3);
        Setoran setoran = new Setoran(newId, nasabah.getDompet());

        for (Map<String, String> item : rawItems) {
            String idSampah = item.get("idSampah");
            double berat;
            try {
                berat = Double.parseDouble(item.getOrDefault("berat", "0"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Berat sampah harus berupa angka yang valid.");
            }
            Sampah sampah = findSampahById(idSampah);
            if (sampah == null) {
                throw new IllegalArgumentException("Jenis sampah tidak ditemukan: " + idSampah);
            }
            DetailSetoran detail = new DetailSetoran(sampah, berat);
            setoran.tambahDetail(detail);
            logOop("Polymorphism (" + sampah.getClass().getSimpleName() + ".hitungNilai)",
                    "DetailSetoran mengunci subtotal " + sampah.getNamaSampah() + " (" + berat + " kg) = "
                            + formatRupiah(detail.getSubtotal()));
        }

        if (dbConnected) {
            repository.saveSetoran(nasabah, setoran);
        }
        nasabah.setorSampah(setoran);
        daftarSetoran.add(0, setoran);
        logOop("Composition (Setoran *-- DetailSetoran)", "Nasabah " + nasabah.getNama()
                + " mengajukan Setoran " + newId + " (" + rawItems.size() + " item) dengan status [DIAJUKAN].");
        return setoran;
    }

    public synchronized Setoran timbangSetoran(String idPetugas, String idSetoran) throws SQLException {
        Pengguna p = findPenggunaById(idPetugas);
        if (!(p instanceof Petugas)) {
            throw new IllegalArgumentException("Hanya Petugas yang dapat menimbang setoran.");
        }
        Petugas petugas = (Petugas) p;
        Setoran setoran = findSetoranById(idSetoran);
        if (setoran == null) {
            throw new IllegalArgumentException("Setoran tidak ditemukan: " + idSetoran);
        }
        if (!"DIAJUKAN".equals(setoran.getStatus())) {
            throw new IllegalArgumentException("Hanya setoran berstatus DIAJUKAN yang dapat ditimbang (status saat ini: "
                    + setoran.getStatus() + ").");
        }
        petugas.timbangSampah(setoran);
        if (dbConnected) {
            repository.updateSetoranTimbang(setoran);
        }
        logOop("Petugas.timbangSampah -> Setoran.hitungTotal()",
                "Petugas " + petugas.getNama() + " menimbang " + setoran.getIdSetoran()
                        + " -> Total: " + formatRupiah(setoran.getTotalNilai())
                        + ", Poin: " + setoran.getTotalPoin() + " (1 poin/Rp100) -> Status [DITIMBANG].");
        return setoran;
    }

    public synchronized Setoran verifikasiSetoran(String idPetugas, String idSetoran) throws SQLException {
        Pengguna p = findPenggunaById(idPetugas);
        if (!(p instanceof Petugas)) {
            throw new IllegalArgumentException("Hanya Petugas yang dapat memverifikasi setoran.");
        }
        Petugas petugas = (Petugas) p;
        Setoran setoran = findSetoranById(idSetoran);
        if (setoran == null) {
            throw new IllegalArgumentException("Setoran tidak ditemukan: " + idSetoran);
        }
        if (!"DITIMBANG".equals(setoran.getStatus())) {
            throw new IllegalArgumentException("Hanya setoran berstatus DITIMBANG yang dapat diverifikasi (status saat ini: "
                    + setoran.getStatus() + ").");
        }
        boolean ok = petugas.verifikasiSetoran(setoran);
        if (ok) {
            if (dbConnected) {
                repository.saveVerifikasiSetoran(petugas, setoran);
            }
            logOop("Petugas.verifikasiSetoran -> Dompet",
                    "Setoran " + setoran.getIdSetoran() + " melewati DIVERIFIKASI -> SELESAI. Dompet "
                            + setoran.getDompet().getIdDompet() + " bertambah +"
                            + formatRupiah(setoran.getTotalNilai()) + " & +" + setoran.getTotalPoin() + " poin.");
        } else {
            if (dbConnected) {
                repository.updateSetoranStatus(setoran);
            }
            logOop("Setoran.verifikasi()", "Verifikasi menghasilkan status [" + setoran.getStatus() + "].");
            if ("DITOLAK".equals(setoran.getStatus())) {
                throw new IllegalArgumentException("Setoran ditolak otomatis karena total nilai = 0.");
            }
            throw new IllegalArgumentException("Verifikasi gagal. Status setoran: " + setoran.getStatus());
        }
        return setoran;
    }

    public synchronized Setoran batalkanSetoran(String idSetoran, String idPengguna) throws SQLException {
        Setoran setoran = findSetoranById(idSetoran);
        if (setoran == null) {
            throw new IllegalArgumentException("Setoran tidak ditemukan: " + idSetoran);
        }
        Pengguna actor = findPenggunaById(idPengguna);
        String actorName = actor != null ? (actor.getClass().getSimpleName() + " " + actor.getNama()) : "Sistem";
        setoran.batalkan();
        if (dbConnected) {
            repository.updateSetoranStatus(setoran);
        }
        logOop("Setoran.batalkan()", actorName + " membatalkan/menolak setoran "
                + setoran.getIdSetoran() + " -> Status berubah menjadi [DITOLAK].");
        return setoran;
    }

    public synchronized void tarikSaldo(String idNasabah, double jumlah, String tipeMetode,
                                        String field1, String field2, String field3) throws SQLException {
        Pengguna p = findPenggunaById(idNasabah);
        if (!(p instanceof Nasabah)) {
            throw new IllegalArgumentException("Hanya Nasabah yang dapat menarik saldo dompet.");
        }
        Nasabah nasabah = (Nasabah) p;
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah penarikan harus lebih dari Rp0.");
        }

        MetodePenarikan metode;
        if ("EWALLET".equalsIgnoreCase(tipeMetode)) {
            if (field1 == null || field1.isBlank() || field2 == null || field2.isBlank()) {
                throw new IllegalArgumentException("Penyedia E-Wallet dan Nomor HP Terdaftar wajib diisi.");
            }
            metode = new EWallet(field1.trim(), field2.trim());
        } else {
            if (field1 == null || field1.isBlank() || field2 == null || field2.isBlank()
                    || field3 == null || field3.isBlank()) {
                throw new IllegalArgumentException("Nama Bank, Nomor Rekening, dan Nama Pemilik wajib diisi.");
            }
            metode = new TransferBank(field1.trim(), field2.trim(), field3.trim());
        }

        double biayaAdmin = metode.hitungBiayaAdmin(jumlah);
        double totalPotong = jumlah + biayaAdmin;
        double saldoSebelum = nasabah.getDompet().getSaldoRupiah();
        if (saldoSebelum < totalPotong) {
            throw new IllegalArgumentException("Saldo tidak mencukupi! Butuh " + formatRupiah(totalPotong)
                    + " (Penarikan " + formatRupiah(jumlah) + " + Biaya Admin " + formatRupiah(biayaAdmin)
                    + "), sedangkan saldo Dompet Anda " + formatRupiah(saldoSebelum) + ".");
        }

        boolean ok = nasabah.tarikSaldo(jumlah, metode);
        if (!ok) {
            throw new IllegalArgumentException("Penarikan gagal diproses oleh " + metode.getNamaMetode() + ".");
        }
        if (dbConnected) {
            repository.syncDompet(nasabah.getDompet());
        }
        logOop("Interface Polymorphism (MetodePenarikan -> " + metode.getClass().getSimpleName() + ")",
                nasabah.getNama() + " menarik " + formatRupiah(jumlah) + " via " + metode.getNamaMetode()
                        + " (Biaya admin: " + formatRupiah(biayaAdmin) + "). Sisa saldo: "
                        + formatRupiah(nasabah.getDompet().getSaldoRupiah()) + ".");
    }

    public synchronized void tukarPoin(String idNasabah, String idHadiah) throws SQLException {
        Pengguna p = findPenggunaById(idNasabah);
        if (!(p instanceof Nasabah)) {
            throw new IllegalArgumentException("Hanya Nasabah yang dapat menukar poin.");
        }
        Nasabah nasabah = (Nasabah) p;
        Hadiah hadiah = findHadiahById(idHadiah);
        if (hadiah == null) {
            throw new IllegalArgumentException("Hadiah tidak ditemukan: " + idHadiah);
        }
        if (!hadiah.cekStok()) {
            throw new IllegalArgumentException("Stok hadiah " + hadiah.getNamaHadiah() + " sudah habis.");
        }
        if (nasabah.getDompet().getTotalPoin() < hadiah.getPoinDibutuhkan()) {
            throw new IllegalArgumentException("Poin tidak mencukupi! Butuh " + hadiah.getPoinDibutuhkan()
                    + " poin, sedangkan poin Anda " + nasabah.getDompet().getTotalPoin() + " poin.");
        }
        boolean ok = nasabah.tukarPoin(hadiah);
        if (!ok) {
            throw new IllegalArgumentException("Penukaran poin gagal.");
        }
        if (dbConnected) {
            repository.saveTukarPoin(nasabah.getDompet(), hadiah);
        }
        logOop("Nasabah.tukarPoin -> Dompet & Hadiah",
                nasabah.getNama() + " menukar " + hadiah.getPoinDibutuhkan() + " poin dengan "
                        + hadiah.getNamaHadiah() + ". Sisa poin: " + nasabah.getDompet().getTotalPoin()
                        + ", sisa stok: " + hadiah.getStok() + ".");
    }

    public synchronized Sampah tambahSampah(String kategori, String namaSampah, double hargaPerKg,
                                            String jenisSpesifik, boolean kondisiBool, double kadarKemurnian)
            throws SQLException {
        if (namaSampah == null || namaSampah.isBlank() || jenisSpesifik == null || jenisSpesifik.isBlank()) {
            throw new IllegalArgumentException("Nama sampah dan jenis spesifik wajib diisi.");
        }
        if (hargaPerKg < 0) {
            throw new IllegalArgumentException("Harga per kg tidak boleh negatif.");
        }
        List<String> ids = new ArrayList<>();
        for (Sampah s : daftarSampah) {
            ids.add(s.getIdSampah());
        }
        String newId = BankSampahRepository.generateNextId("SMP-", ids, 2);
        String kat = kategori != null ? kategori.trim().toUpperCase(Locale.ROOT) : "PLASTIK";

        Sampah baru;
        switch (kat) {
            case "PLASTIK":
                baru = new Plastik(newId, namaSampah.trim(), hargaPerKg, jenisSpesifik.trim(), kondisiBool);
                break;
            case "KERTAS":
                baru = new Kertas(newId, namaSampah.trim(), hargaPerKg, jenisSpesifik.trim(), kondisiBool);
                break;
            case "LOGAM":
                baru = new Logam(newId, namaSampah.trim(), hargaPerKg, jenisSpesifik.trim(), kadarKemurnian);
                break;
            default:
                throw new IllegalArgumentException("Kategori sampah tidak valid: " + kategori);
        }

        if (dbConnected) {
            repository.saveSampah(baru);
        }
        daftarSampah.add(baru);
        logOop("Inheritance (" + baru.getClass().getSimpleName() + " extends Sampah)",
                "Admin menambahkan jenis sampah baru: " + baru);
        return baru;
    }

    public synchronized Sampah ubahHargaSampah(String idSampah, double hargaBaru) throws SQLException {
        Sampah s = findSampahById(idSampah);
        if (s == null) {
            throw new IllegalArgumentException("Sampah tidak ditemukan: " + idSampah);
        }
        double hargaLama = s.getHargaPerKg();
        s.setHargaPerKg(hargaBaru);
        if (dbConnected) {
            repository.updateHargaSampah(s);
        }
        logOop("Encapsulation (Sampah.setHargaPerKg)",
                "Admin mengubah harga " + s.getNamaSampah() + " dari " + formatRupiah(hargaLama)
                        + " menjadi " + formatRupiah(hargaBaru)
                        + "/kg (setoran lama tetap terkunci pada subtotal DetailSetoran).");
        return s;
    }

    public synchronized Hadiah tambahHadiah(String namaHadiah, int poinDibutuhkan, int stok) throws SQLException {
        if (namaHadiah == null || namaHadiah.isBlank()) {
            throw new IllegalArgumentException("Nama hadiah wajib diisi.");
        }
        List<String> ids = new ArrayList<>();
        for (Hadiah h : daftarHadiah) {
            ids.add(h.getIdHadiah());
        }
        String newId = BankSampahRepository.generateNextId("H-", ids, 2);
        Hadiah baru = new Hadiah(newId, namaHadiah.trim(), poinDibutuhkan, stok);
        if (dbConnected) {
            repository.saveHadiah(baru);
        }
        daftarHadiah.add(baru);
        logOop("Admin.kelolaHadiah -> new Hadiah()",
                "Admin menambahkan katalog hadiah baru: " + baru);
        return baru;
    }

    public synchronized Hadiah tambahStokHadiah(String idHadiah, int tambahan) throws SQLException {
        Hadiah h = findHadiahById(idHadiah);
        if (h == null) {
            throw new IllegalArgumentException("Hadiah tidak ditemukan: " + idHadiah);
        }
        h.tambahStok(tambahan);
        if (dbConnected) {
            repository.updateHadiahStok(h);
        }
        logOop("Encapsulation (Hadiah.tambahStok)",
                "Admin menambah stok " + h.getNamaHadiah() + " sebanyak +" + tambahan
                        + " -> Total stok: " + h.getStok() + ".");
        return h;
    }

    public synchronized Pengguna findPenggunaById(String id) {
        if (id == null) {
            return null;
        }
        for (Pengguna p : daftarPengguna) {
            if (id.equals(p.getIdPengguna())) {
                return p;
            }
        }
        return null;
    }

    public synchronized Sampah findSampahById(String id) {
        if (id == null) {
            return null;
        }
        for (Sampah s : daftarSampah) {
            if (id.equals(s.getIdSampah())) {
                return s;
            }
        }
        return null;
    }

    public synchronized Hadiah findHadiahById(String id) {
        if (id == null) {
            return null;
        }
        for (Hadiah h : daftarHadiah) {
            if (id.equals(h.getIdHadiah())) {
                return h;
            }
        }
        return null;
    }

    public synchronized Setoran findSetoranById(String id) {
        if (id == null) {
            return null;
        }
        for (Setoran s : daftarSetoran) {
            if (id.equals(s.getIdSetoran())) {
                return s;
            }
        }
        return null;
    }

    public synchronized Nasabah findPenyetor(Setoran setoran) {
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

    public synchronized Petugas findVerifikator(Setoran setoran) {
        for (Pengguna p : daftarPengguna) {
            if (p instanceof Petugas) {
                Petugas pt = (Petugas) p;
                if (pt.getDaftarSetoran().contains(setoran)) {
                    return pt;
                }
            }
        }
        return null;
    }

    private static String formatRupiah(double amount) {
        return String.format(Locale.forLanguageTag("id-ID"), "Rp%,.0f", amount);
    }

    private static String formatIsoDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    public synchronized String toJsonPengguna(Pengguna p) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idPengguna\":").append(JsonUtil.quote(p.getIdPengguna())).append(",");
        sb.append("\"nama\":").append(JsonUtil.quote(p.getNama())).append(",");
        sb.append("\"email\":").append(JsonUtil.quote(p.getEmail())).append(",");
        sb.append("\"noHp\":").append(JsonUtil.quote(p.getNoHp())).append(",");
        String role = p.getClass().getSimpleName().toUpperCase(Locale.ROOT);
        sb.append("\"role\":").append(JsonUtil.quote(role)).append(",");
        sb.append("\"className\":").append(JsonUtil.quote(p.getClass().getSimpleName()));

        if (p instanceof Nasabah) {
            Nasabah n = (Nasabah) p;
            Dompet d = n.getDompet();
            sb.append(",\"alamat\":").append(JsonUtil.quote(n.getAlamat()));
            sb.append(",\"tanggalDaftar\":").append(JsonUtil.quote(formatIsoDate(n.getTanggalDaftar())));
            sb.append(",\"jumlahSetoran\":").append(n.lihatRiwayat().size());
            sb.append(",\"dompet\":{");
            sb.append("\"idDompet\":").append(JsonUtil.quote(d.getIdDompet())).append(",");
            sb.append("\"saldoRupiah\":").append(d.getSaldoRupiah()).append(",");
            sb.append("\"totalPoin\":").append(d.getTotalPoin());
            sb.append("}");
        } else if (p instanceof Petugas) {
            Petugas pt = (Petugas) p;
            sb.append(",\"idPetugas\":").append(JsonUtil.quote(pt.getIdPetugas()));
            sb.append(",\"wilayahTugas\":").append(JsonUtil.quote(pt.getWilayahTugas()));
            sb.append(",\"jumlahVerifikasi\":").append(pt.getDaftarSetoran().size());
        } else if (p instanceof Admin) {
            Admin a = (Admin) p;
            sb.append(",\"levelAkses\":").append(JsonUtil.quote(a.getLevelAkses()));
        }
        sb.append("}");
        return sb.toString();
    }

    public synchronized String toJsonSampah(Sampah s) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idSampah\":").append(JsonUtil.quote(s.getIdSampah())).append(",");
        sb.append("\"namaSampah\":").append(JsonUtil.quote(s.getNamaSampah())).append(",");
        sb.append("\"hargaPerKg\":").append(s.getHargaPerKg()).append(",");
        sb.append("\"hargaEfektifPerKg\":").append(s.hitungNilai(1.0)).append(",");
        sb.append("\"poinPerKg\":").append(s.hitungPoin(s.hitungNilai(1.0))).append(",");
        sb.append("\"subkelas\":").append(JsonUtil.quote(s.getClass().getSimpleName())).append(",");

        if (s instanceof Plastik) {
            Plastik p = (Plastik) s;
            sb.append("\"kategori\":\"PLASTIK\",");
            sb.append("\"jenisSpesifik\":").append(JsonUtil.quote(p.getJenisPlastik())).append(",");
            sb.append("\"bersih\":").append(p.isBersih()).append(",");
            sb.append("\"faktorNilai\":").append(p.isBersih() ? 1.0 : 0.8).append(",");
            sb.append("\"keteranganKondisi\":").append(JsonUtil.quote(
                    p.isBersih() ? "Bersih (100% harga penuh)" : "Kotor (Potongan 20% -> 80% harga)"));
        } else if (s instanceof Kertas) {
            Kertas k = (Kertas) s;
            sb.append("\"kategori\":\"KERTAS\",");
            sb.append("\"jenisSpesifik\":").append(JsonUtil.quote(k.getJenisKertas())).append(",");
            sb.append("\"kering\":").append(k.isKering()).append(",");
            sb.append("\"faktorNilai\":").append(k.isKering() ? 1.0 : 0.7).append(",");
            sb.append("\"keteranganKondisi\":").append(JsonUtil.quote(
                    k.isKering() ? "Kering (100% harga penuh)" : "Basah/Lembab (Potongan 30% -> 70% harga)"));
        } else if (s instanceof Logam) {
            Logam l = (Logam) s;
            sb.append("\"kategori\":\"LOGAM\",");
            sb.append("\"jenisSpesifik\":").append(JsonUtil.quote(l.getJenisLogam())).append(",");
            sb.append("\"kadarKemurnian\":").append(l.getKadarKemurnian()).append(",");
            sb.append("\"faktorNilai\":").append(l.getKadarKemurnian()).append(",");
            sb.append("\"keteranganKondisi\":").append(JsonUtil.quote(
                    "Kemurnian " + (int) Math.round(l.getKadarKemurnian() * 100) + "% (Nilai x " + l.getKadarKemurnian() + ")"));
        }
        sb.append("}");
        return sb.toString();
    }

    public synchronized String toJsonHadiah(Hadiah h) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idHadiah\":").append(JsonUtil.quote(h.getIdHadiah())).append(",");
        sb.append("\"namaHadiah\":").append(JsonUtil.quote(h.getNamaHadiah())).append(",");
        sb.append("\"poinDibutuhkan\":").append(h.getPoinDibutuhkan()).append(",");
        sb.append("\"stok\":").append(h.getStok()).append(",");
        sb.append("\"tersedia\":").append(h.cekStok());
        sb.append("}");
        return sb.toString();
    }

    public synchronized String toJsonSetoran(Setoran s) {
        Nasabah penyetor = findPenyetor(s);
        Petugas verifikator = findVerifikator(s);
        double estimasiSubtotal = 0;
        for (DetailSetoran d : s.getDaftarDetail()) {
            estimasiSubtotal += d.getSubtotal();
        }
        int estimasiPoin = (int) (estimasiSubtotal / 100);

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idSetoran\":").append(JsonUtil.quote(s.getIdSetoran())).append(",");
        sb.append("\"tanggalSetor\":").append(JsonUtil.quote(formatIsoDate(s.getTanggalSetor()))).append(",");
        sb.append("\"status\":").append(JsonUtil.quote(s.getStatus())).append(",");
        sb.append("\"totalNilai\":").append(s.getTotalNilai()).append(",");
        sb.append("\"totalPoin\":").append(s.getTotalPoin()).append(",");
        sb.append("\"estimasiNilai\":").append(estimasiSubtotal).append(",");
        sb.append("\"estimasiPoin\":").append(estimasiPoin).append(",");
        sb.append("\"idDompet\":").append(JsonUtil.quote(s.getDompet().getIdDompet())).append(",");
        sb.append("\"idNasabah\":").append(JsonUtil.quote(penyetor != null ? penyetor.getIdPengguna() : "")).append(",");
        sb.append("\"namaNasabah\":").append(JsonUtil.quote(penyetor != null ? penyetor.getNama() : "-")).append(",");
        sb.append("\"idPetugas\":").append(JsonUtil.quote(verifikator != null ? verifikator.getIdPengguna() : "")).append(",");
        sb.append("\"kodePetugas\":").append(JsonUtil.quote(verifikator != null ? verifikator.getIdPetugas() : "")).append(",");
        sb.append("\"namaPetugas\":").append(JsonUtil.quote(verifikator != null ? verifikator.getNama() : "-")).append(",");

        sb.append("\"daftarDetail\":[");
        List<DetailSetoran> details = s.getDaftarDetail();
        for (int i = 0; i < details.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            DetailSetoran d = details.get(i);
            Sampah smp = d.getSampah();
            sb.append("{");
            sb.append("\"idSampah\":").append(JsonUtil.quote(smp.getIdSampah())).append(",");
            sb.append("\"namaSampah\":").append(JsonUtil.quote(smp.getNamaSampah())).append(",");
            sb.append("\"subkelas\":").append(JsonUtil.quote(smp.getClass().getSimpleName())).append(",");
            sb.append("\"berat\":").append(d.getBerat()).append(",");
            sb.append("\"subtotal\":").append(d.getSubtotal()).append(",");
            sb.append("\"hargaSaatIni\":").append(smp.getHargaPerKg());
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public synchronized String buildFullStateJson() {
        int cNasabah = 0;
        int cPetugas = 0;
        int cAdmin = 0;
        double totalSaldoDompet = 0;
        int totalPoinDompet = 0;

        for (Pengguna p : daftarPengguna) {
            if (p instanceof Nasabah) {
                cNasabah++;
                Nasabah n = (Nasabah) p;
                totalSaldoDompet += n.getDompet().getSaldoRupiah();
                totalPoinDompet += n.getDompet().getTotalPoin();
            } else if (p instanceof Petugas) {
                cPetugas++;
            } else if (p instanceof Admin) {
                cAdmin++;
            }
        }

        int sDiajukan = 0;
        int sDitimbang = 0;
        int sDiverifikasi = 0;
        int sSelesai = 0;
        int sDitolak = 0;
        double totalNilaiSelesai = 0;
        int totalPoinSelesai = 0;

        for (Setoran s : daftarSetoran) {
            switch (s.getStatus()) {
                case "DIAJUKAN":
                    sDiajukan++;
                    break;
                case "DITIMBANG":
                    sDitimbang++;
                    break;
                case "DIVERIFIKASI":
                    sDiverifikasi++;
                    break;
                case "SELESAI":
                    sSelesai++;
                    totalNilaiSelesai += s.getTotalNilai();
                    totalPoinSelesai += s.getTotalPoin();
                    break;
                case "DITOLAK":
                    sDitolak++;
                    break;
                default:
                    break;
            }
        }

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"dbConnected\":").append(dbConnected).append(",");
        sb.append("\"dbStatusMessage\":").append(JsonUtil.quote(dbStatusMessage)).append(",");

        // Pengguna
        sb.append("\"daftarPengguna\":[");
        for (int i = 0; i < daftarPengguna.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJsonPengguna(daftarPengguna.get(i)));
        }
        sb.append("],");

        // Sampah
        sb.append("\"daftarSampah\":[");
        for (int i = 0; i < daftarSampah.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJsonSampah(daftarSampah.get(i)));
        }
        sb.append("],");

        // Hadiah
        sb.append("\"daftarHadiah\":[");
        for (int i = 0; i < daftarHadiah.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJsonHadiah(daftarHadiah.get(i)));
        }
        sb.append("],");

        // Setoran
        sb.append("\"daftarSetoran\":[");
        for (int i = 0; i < daftarSetoran.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJsonSetoran(daftarSetoran.get(i)));
        }
        sb.append("],");

        // Laporan (Admin.buatLaporan summary)
        sb.append("\"laporan\":{");
        sb.append("\"totalPengguna\":").append(daftarPengguna.size()).append(",");
        sb.append("\"jumlahNasabah\":").append(cNasabah).append(",");
        sb.append("\"jumlahPetugas\":").append(cPetugas).append(",");
        sb.append("\"jumlahAdmin\":").append(cAdmin).append(",");
        sb.append("\"jumlahJenisSampah\":").append(daftarSampah.size()).append(",");
        sb.append("\"jumlahHadiah\":").append(daftarHadiah.size()).append(",");
        sb.append("\"jumlahSetoran\":").append(daftarSetoran.size()).append(",");
        sb.append("\"statusDiajukan\":").append(sDiajukan).append(",");
        sb.append("\"statusDitimbang\":").append(sDitimbang).append(",");
        sb.append("\"statusDiverifikasi\":").append(sDiverifikasi).append(",");
        sb.append("\"statusSelesai\":").append(sSelesai).append(",");
        sb.append("\"statusDitolak\":").append(sDitolak).append(",");
        sb.append("\"totalNilaiSelesai\":").append(totalNilaiSelesai).append(",");
        sb.append("\"totalPoinSelesai\":").append(totalPoinSelesai).append(",");
        sb.append("\"totalSaldoBeredar\":").append(totalSaldoDompet).append(",");
        sb.append("\"totalPoinBeredar\":").append(totalPoinDompet);
        sb.append("},");

        // OOP Logs
        sb.append("\"oopLogs\":[");
        for (int i = 0; i < oopLogs.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(JsonUtil.quote(oopLogs.get(i)));
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
