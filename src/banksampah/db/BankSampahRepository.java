package banksampah.db;

import banksampah.hadiah.Hadiah;
import banksampah.keuangan.Dompet;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository (DAO) untuk menghubungkan objek-objek OOP Bank Sampah dengan database Neon PostgreSQL.
 * Seluruh baris data dari SQL diubah menjadi instance kelas domain (Polymorphism).
 */
public class BankSampahRepository {

    public void loadAll(List<Pengguna> daftarPengguna,
                        List<Sampah> daftarSampah,
                        List<Hadiah> daftarHadiah,
                        List<Setoran> daftarSetoran) throws SQLException {
        daftarPengguna.clear();
        daftarSampah.clear();
        daftarHadiah.clear();
        daftarSetoran.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Muat Katalog Sampah -> instantiate Plastik / Kertas / Logam
            Map<String, Sampah> mapSampah = new HashMap<>();
            String sqlSampah = "SELECT id_sampah, nama_sampah, harga_per_kg, kategori, jenis_spesifik, "
                    + "kondisi_bersih, kondisi_kering, kadar_kemurnian FROM sampah ORDER BY id_sampah";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlSampah)) {
                while (rs.next()) {
                    String id = rs.getString("id_sampah");
                    String nama = rs.getString("nama_sampah");
                    double harga = rs.getDouble("harga_per_kg");
                    String kategori = rs.getString("kategori");
                    String jenis = rs.getString("jenis_spesifik");

                    Sampah sampah;
                    switch (kategori) {
                        case "PLASTIK":
                            sampah = new Plastik(id, nama, harga, jenis, rs.getBoolean("kondisi_bersih"));
                            break;
                        case "KERTAS":
                            sampah = new Kertas(id, nama, harga, jenis, rs.getBoolean("kondisi_kering"));
                            break;
                        case "LOGAM":
                            sampah = new Logam(id, nama, harga, jenis, rs.getDouble("kadar_kemurnian"));
                            break;
                        default:
                            throw new SQLException("Kategori sampah tidak dikenal: " + kategori);
                    }
                    daftarSampah.add(sampah);
                    mapSampah.put(id, sampah);
                }
            }

            // 2. Muat Katalog Hadiah
            String sqlHadiah = "SELECT id_hadiah, nama_hadiah, poin_dibutuhkan, stok FROM hadiah ORDER BY id_hadiah";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlHadiah)) {
                while (rs.next()) {
                    daftarHadiah.add(new Hadiah(
                            rs.getString("id_hadiah"),
                            rs.getString("nama_hadiah"),
                            rs.getInt("poin_dibutuhkan"),
                            rs.getInt("stok")
                    ));
                }
            }

            // 3. Muat Dompet per Nasabah
            Map<String, Dompet> mapDompetByPengguna = new HashMap<>();
            String sqlDompet = "SELECT id_dompet, id_pengguna, saldo_rupiah, total_poin FROM dompet";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlDompet)) {
                while (rs.next()) {
                    Dompet d = new Dompet(
                            rs.getString("id_dompet"),
                            rs.getDouble("saldo_rupiah"),
                            rs.getInt("total_poin")
                    );
                    mapDompetByPengguna.put(rs.getString("id_pengguna"), d);
                }
            }

            // 4. Muat Pengguna -> instantiate Nasabah / Petugas / Admin
            Map<String, Nasabah> mapNasabah = new HashMap<>();
            Map<String, Petugas> mapPetugas = new HashMap<>();
            String sqlPengguna = "SELECT id_pengguna, nama, email, password, no_hp, role, alamat, "
                    + "tanggal_daftar, id_petugas, wilayah_tugas, tingkat_akses FROM pengguna ORDER BY id_pengguna";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlPengguna)) {
                while (rs.next()) {
                    String id = rs.getString("id_pengguna");
                    String nama = rs.getString("nama");
                    String email = rs.getString("email");
                    String pass = rs.getString("password");
                    String noHp = rs.getString("no_hp") != null ? rs.getString("no_hp") : "-";
                    String role = rs.getString("role");

                    switch (role) {
                        case "NASABAH": {
                            String alamat = rs.getString("alamat") != null ? rs.getString("alamat") : "-";
                            Timestamp ts = rs.getTimestamp("tanggal_daftar");
                            Date tglDaftar = ts != null ? new Date(ts.getTime()) : new Date();
                            Dompet dompet = mapDompetByPengguna.getOrDefault(id, new Dompet("DMP-" + id));
                            Nasabah nasabah = new Nasabah(id, nama, email, pass, noHp, alamat, tglDaftar, dompet);
                            daftarPengguna.add(nasabah);
                            mapNasabah.put(id, nasabah);
                            break;
                        }
                        case "PETUGAS": {
                            String idPetugas = rs.getString("id_petugas") != null ? rs.getString("id_petugas") : "PTG-" + id;
                            String wilayah = rs.getString("wilayah_tugas") != null ? rs.getString("wilayah_tugas") : "-";
                            Petugas petugas = new Petugas(id, nama, email, pass, noHp, idPetugas, wilayah);
                            daftarPengguna.add(petugas);
                            mapPetugas.put(id, petugas);
                            break;
                        }
                        case "ADMIN": {
                            String akses = rs.getString("tingkat_akses") != null ? rs.getString("tingkat_akses") : "PENUH";
                            Admin admin = new Admin(id, nama, email, pass, noHp, akses,
                                    daftarSampah, daftarHadiah, daftarPengguna, daftarSetoran);
                            daftarPengguna.add(admin);
                            break;
                        }
                        default:
                            throw new SQLException("Role pengguna tidak dikenal: " + role);
                    }
                }
            }

            // 5. Muat DetailSetoran dikelompokkan per id_setoran
            Map<String, List<DetailSetoran>> mapDetails = new HashMap<>();
            String sqlDetail = "SELECT id_setoran, id_sampah, berat, subtotal FROM detail_setoran ORDER BY id";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlDetail)) {
                while (rs.next()) {
                    String idSetoran = rs.getString("id_setoran");
                    Sampah sampah = mapSampah.get(rs.getString("id_sampah"));
                    if (sampah != null) {
                        DetailSetoran detail = new DetailSetoran(
                                sampah,
                                rs.getDouble("berat"),
                                rs.getDouble("subtotal")
                        );
                        mapDetails.computeIfAbsent(idSetoran, k -> new ArrayList<>()).add(detail);
                    }
                }
            }

            // 6. Muat Setoran
            String sqlSetoran = "SELECT id_setoran, id_nasabah, id_petugas, tanggal_setor, status, "
                    + "total_nilai, total_poin FROM setoran ORDER BY tanggal_setor DESC, id_setoran DESC";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlSetoran)) {
                while (rs.next()) {
                    String idSetoran = rs.getString("id_setoran");
                    String idNasabah = rs.getString("id_nasabah");
                    String idPetugas = rs.getString("id_petugas");
                    Timestamp ts = rs.getTimestamp("tanggal_setor");
                    Date tglSetor = ts != null ? new Date(ts.getTime()) : new Date();
                    String status = rs.getString("status");
                    double totalNilai = rs.getDouble("total_nilai");
                    int totalPoin = rs.getInt("total_poin");

                    Nasabah nasabah = mapNasabah.get(idNasabah);
                    if (nasabah != null) {
                        List<DetailSetoran> details = mapDetails.getOrDefault(idSetoran, new ArrayList<>());
                        Setoran setoran = new Setoran(idSetoran, nasabah.getDompet(), tglSetor, status,
                                totalNilai, totalPoin, details);
                        daftarSetoran.add(setoran);
                        nasabah.muatRiwayatSetoran(setoran);
                        if (idPetugas != null && "SELESAI".equals(status)) {
                            Petugas petugas = mapPetugas.get(idPetugas);
                            if (petugas != null) {
                                petugas.muatSetoranTerverifikasi(setoran);
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveNasabah(Nasabah nasabah) throws SQLException {
        String sqlUser = "INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, alamat, tanggal_daftar) "
                + "VALUES (?, ?, ?, ?, ?, 'NASABAH', ?, ?)";
        String sqlDompet = "INSERT INTO dompet (id_dompet, id_pengguna, saldo_rupiah, total_poin) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser);
                 PreparedStatement psDompet = conn.prepareStatement(sqlDompet)) {

                psUser.setString(1, nasabah.getIdPengguna());
                psUser.setString(2, nasabah.getNama());
                psUser.setString(3, nasabah.getEmail());
                psUser.setString(4, nasabah.getPassword());
                psUser.setString(5, nasabah.getNoHp());
                psUser.setString(6, nasabah.getAlamat());
                psUser.setTimestamp(7, new Timestamp(nasabah.getTanggalDaftar().getTime()));
                psUser.executeUpdate();

                Dompet d = nasabah.getDompet();
                psDompet.setString(1, d.getIdDompet());
                psDompet.setString(2, nasabah.getIdPengguna());
                psDompet.setDouble(3, d.getSaldoRupiah());
                psDompet.setInt(4, d.getTotalPoin());
                psDompet.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void savePetugas(Petugas petugas) throws SQLException {
        String sql = "INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, id_petugas, wilayah_tugas) "
                + "VALUES (?, ?, ?, ?, ?, 'PETUGAS', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, petugas.getIdPengguna());
            ps.setString(2, petugas.getNama());
            ps.setString(3, petugas.getEmail());
            ps.setString(4, petugas.getPassword());
            ps.setString(5, petugas.getNoHp());
            ps.setString(6, petugas.getIdPetugas());
            ps.setString(7, petugas.getWilayahTugas());
            ps.executeUpdate();
        }
    }

    public void saveAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, tingkat_akses) "
                + "VALUES (?, ?, ?, ?, ?, 'ADMIN', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getIdPengguna());
            ps.setString(2, admin.getNama());
            ps.setString(3, admin.getEmail());
            ps.setString(4, admin.getPassword());
            ps.setString(5, admin.getNoHp());
            ps.setString(6, admin.getLevelAkses());
            ps.executeUpdate();
        }
    }

    public void updateNamaPengguna(Pengguna pengguna) throws SQLException {
        String sql = "UPDATE pengguna SET nama = ? WHERE id_pengguna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pengguna.getNama());
            ps.setString(2, pengguna.getIdPengguna());
            ps.executeUpdate();
        }
    }

    public void updatePassword(Pengguna pengguna) throws SQLException {
        String sql = "UPDATE pengguna SET password = ? WHERE id_pengguna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pengguna.getPassword());
            ps.setString(2, pengguna.getIdPengguna());
            ps.executeUpdate();
        }
    }

    public void saveSampah(Sampah sampah) throws SQLException {
        String sql = "INSERT INTO sampah (id_sampah, nama_sampah, harga_per_kg, kategori, jenis_spesifik, "
                + "kondisi_bersih, kondisi_kering, kadar_kemurnian) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sampah.getIdSampah());
            ps.setString(2, sampah.getNamaSampah());
            ps.setDouble(3, sampah.getHargaPerKg());

            if (sampah instanceof Plastik) {
                Plastik p = (Plastik) sampah;
                ps.setString(4, "PLASTIK");
                ps.setString(5, p.getJenisPlastik());
                ps.setBoolean(6, p.isBersih());
                ps.setBoolean(7, true);
                ps.setDouble(8, 1.0);
            } else if (sampah instanceof Kertas) {
                Kertas k = (Kertas) sampah;
                ps.setString(4, "KERTAS");
                ps.setString(5, k.getJenisKertas());
                ps.setBoolean(6, true);
                ps.setBoolean(7, k.isKering());
                ps.setDouble(8, 1.0);
            } else if (sampah instanceof Logam) {
                Logam l = (Logam) sampah;
                ps.setString(4, "LOGAM");
                ps.setString(5, l.getJenisLogam());
                ps.setBoolean(6, true);
                ps.setBoolean(7, true);
                ps.setDouble(8, l.getKadarKemurnian());
            } else {
                throw new SQLException("Subkelas Sampah tidak dikenal: " + sampah.getClass().getName());
            }
            ps.executeUpdate();
        }
    }

    public void updateHargaSampah(Sampah sampah) throws SQLException {
        String sql = "UPDATE sampah SET harga_per_kg = ? WHERE id_sampah = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, sampah.getHargaPerKg());
            ps.setString(2, sampah.getIdSampah());
            ps.executeUpdate();
        }
    }

    public void saveHadiah(Hadiah hadiah) throws SQLException {
        String sql = "INSERT INTO hadiah (id_hadiah, nama_hadiah, poin_dibutuhkan, stok) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hadiah.getIdHadiah());
            ps.setString(2, hadiah.getNamaHadiah());
            ps.setInt(3, hadiah.getPoinDibutuhkan());
            ps.setInt(4, hadiah.getStok());
            ps.executeUpdate();
        }
    }

    public void updateHadiahStok(Hadiah hadiah) throws SQLException {
        String sql = "UPDATE hadiah SET stok = ? WHERE id_hadiah = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hadiah.getStok());
            ps.setString(2, hadiah.getIdHadiah());
            ps.executeUpdate();
        }
    }

    public void saveSetoran(Nasabah nasabah, Setoran setoran) throws SQLException {
        String sqlSetoran = "INSERT INTO setoran (id_setoran, id_nasabah, tanggal_setor, status, total_nilai, total_poin) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detail_setoran (id_setoran, id_sampah, berat, subtotal) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psSet = conn.prepareStatement(sqlSetoran);
                 PreparedStatement psDet = conn.prepareStatement(sqlDetail)) {

                psSet.setString(1, setoran.getIdSetoran());
                psSet.setString(2, nasabah.getIdPengguna());
                psSet.setTimestamp(3, new Timestamp(setoran.getTanggalSetor().getTime()));
                psSet.setString(4, setoran.getStatus());
                psSet.setDouble(5, setoran.getTotalNilai());
                psSet.setInt(6, setoran.getTotalPoin());
                psSet.executeUpdate();

                for (DetailSetoran detail : setoran.getDaftarDetail()) {
                    psDet.setString(1, setoran.getIdSetoran());
                    psDet.setString(2, detail.getSampah().getIdSampah());
                    psDet.setDouble(3, detail.getBerat());
                    psDet.setDouble(4, detail.getSubtotal());
                    psDet.addBatch();
                }
                psDet.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateSetoranTimbang(Setoran setoran) throws SQLException {
        String sql = "UPDATE setoran SET status = ?, total_nilai = ?, total_poin = ? WHERE id_setoran = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, setoran.getStatus());
            ps.setDouble(2, setoran.getTotalNilai());
            ps.setInt(3, setoran.getTotalPoin());
            ps.setString(4, setoran.getIdSetoran());
            ps.executeUpdate();
        }
    }

    public void saveVerifikasiSetoran(Petugas petugas, Setoran setoran) throws SQLException {
        String sqlSetoran = "UPDATE setoran SET status = ?, id_petugas = ?, total_nilai = ?, total_poin = ? WHERE id_setoran = ?";
        String sqlDompet = "UPDATE dompet SET saldo_rupiah = ?, total_poin = ? WHERE id_dompet = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psSet = conn.prepareStatement(sqlSetoran);
                 PreparedStatement psDom = conn.prepareStatement(sqlDompet)) {

                psSet.setString(1, setoran.getStatus());
                psSet.setString(2, petugas.getIdPengguna());
                psSet.setDouble(3, setoran.getTotalNilai());
                psSet.setInt(4, setoran.getTotalPoin());
                psSet.setString(5, setoran.getIdSetoran());
                psSet.executeUpdate();

                Dompet d = setoran.getDompet();
                psDom.setDouble(1, d.getSaldoRupiah());
                psDom.setInt(2, d.getTotalPoin());
                psDom.setString(3, d.getIdDompet());
                psDom.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateSetoranStatus(Setoran setoran) throws SQLException {
        String sql = "UPDATE setoran SET status = ? WHERE id_setoran = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, setoran.getStatus());
            ps.setString(2, setoran.getIdSetoran());
            ps.executeUpdate();
        }
    }

    public void saveTukarPoin(Dompet dompet, Hadiah hadiah) throws SQLException {
        String sqlDompet = "UPDATE dompet SET total_poin = ? WHERE id_dompet = ?";
        String sqlHadiah = "UPDATE hadiah SET stok = ? WHERE id_hadiah = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDom = conn.prepareStatement(sqlDompet);
                 PreparedStatement psHad = conn.prepareStatement(sqlHadiah)) {

                psDom.setInt(1, dompet.getTotalPoin());
                psDom.setString(2, dompet.getIdDompet());
                psDom.executeUpdate();

                psHad.setInt(1, hadiah.getStok());
                psHad.setString(2, hadiah.getIdHadiah());
                psHad.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void syncDompet(Dompet dompet) throws SQLException {
        String sql = "UPDATE dompet SET saldo_rupiah = ?, total_poin = ? WHERE id_dompet = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, dompet.getSaldoRupiah());
            ps.setInt(2, dompet.getTotalPoin());
            ps.setString(3, dompet.getIdDompet());
            ps.executeUpdate();
        }
    }

    public static String generateNextId(String prefix, List<String> existingIds, int digits) {
        int max = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith(prefix)) {
                String suffix = id.substring(prefix.length());
                try {
                    int num = Integer.parseInt(suffix);
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Lewati format ID non-standar
                }
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }
}
