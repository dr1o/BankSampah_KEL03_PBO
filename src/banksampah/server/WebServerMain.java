package banksampah.server;

import banksampah.hadiah.Hadiah;
import banksampah.pengguna.Nasabah;
import banksampah.pengguna.Pengguna;
import banksampah.sampah.Sampah;
import banksampah.setoran.Setoran;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * HTTP REST API Server Pure Java (menggunakan com.sun.net.httpserver.HttpServer bawaan JDK).
 * Menyediakan endpoint bagi Frontend Web (Vite HTML/CSS/JS) untuk mengakses 15 kelas domain OOP.
 */
public class WebServerMain {

    private static final BankSampahService SERVICE = new BankSampahService();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
                // gunakan default 8080
            }
        }

        System.out.println("======================================================");
        System.out.println("   BACKEND PURE JAVA OOP - SISTEM BANK SAMPAH");
        System.out.println("======================================================");
        SERVICE.initialize();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", new ApiHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("[READY] Java OOP REST API berjalan di http://127.0.0.1:" + port + "/api/state");
    }

    private static class ApiHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String method = ex.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            String path = ex.getRequestURI().getPath();
            try {
                if ("GET".equalsIgnoreCase(method) && "/api/state".equals(path)) {
                    sendJson(ex, 200, "{\"ok\":true,\"state\":" + SERVICE.buildFullStateJson() + "}");
                    return;
                }

                String body = readBody(ex);
                Map<String, String> payload = JsonUtil.parseObject(body);

                switch (path) {
                    case "/api/reload": {
                        SERVICE.initialize();
                        sendSuccess(ex, "Data berhasil disinkronkan ulang.", null);
                        break;
                    }
                    case "/api/login": {
                        Pengguna user = SERVICE.login(payload.get("email"), payload.get("password"));
                        sendSuccess(ex, "Selamat datang kembali, " + user.getNama() + "!", SERVICE.toJsonPengguna(user));
                        break;
                    }
                    case "/api/logout": {
                        SERVICE.logout(payload.get("idPengguna"));
                        sendSuccess(ex, "Anda telah keluar dari sesi.", null);
                        break;
                    }
                    case "/api/register": {
                        Nasabah baru = SERVICE.registerNasabah(
                                payload.get("nama"),
                                payload.get("email"),
                                payload.get("password"),
                                payload.get("noHp"),
                                payload.get("alamat")
                        );
                        sendSuccess(ex, "Pendaftaran Nasabah " + baru.getIdPengguna() + " berhasil! Dompet "
                                + baru.getDompet().getIdDompet() + " telah dibuat.", SERVICE.toJsonPengguna(baru));
                        break;
                    }
                    case "/api/pengguna/tambah": {
                        Pengguna baru = SERVICE.tambahPenggunaOlehAdmin(
                                payload.get("role"),
                                payload.get("nama"),
                                payload.get("email"),
                                payload.get("password"),
                                payload.get("noHp"),
                                payload.get("extra1"),
                                payload.get("extra2")
                        );
                        sendSuccess(ex, "Pengguna baru (" + baru.getClass().getSimpleName() + " - "
                                + baru.getIdPengguna() + ") berhasil ditambahkan.", SERVICE.toJsonPengguna(baru));
                        break;
                    }
                    case "/api/pengguna/password": {
                        SERVICE.ubahPassword(
                                payload.get("idPengguna"),
                                payload.get("passwordLama"),
                                payload.get("passwordBaru")
                        );
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idPengguna"));
                        sendSuccess(ex, "Password berhasil diperbarui.", updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/pengguna/nama": {
                        SERVICE.ubahNama(payload.get("idPengguna"), payload.get("namaBaru"));
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idPengguna"));
                        sendSuccess(ex, "Nama profil berhasil diperbarui.", updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/setoran/ajukan": {
                        List<Map<String, String>> items = JsonUtil.parseObjectArray(payload.get("items"));
                        Setoran s = SERVICE.ajukanSetoran(payload.get("idNasabah"), items);
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idNasabah"));
                        sendSuccess(ex, "Setoran " + s.getIdSetoran()
                                + " berhasil diajukan dengan status DIAJUKAN (menunggu penimbangan Petugas).",
                                updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/setoran/timbang": {
                        Setoran s = SERVICE.timbangSetoran(payload.get("idPetugas"), payload.get("idSetoran"));
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idPetugas"));
                        sendSuccess(ex, "Setoran " + s.getIdSetoran() + " selesai ditimbang! Total nilai dan poin telah dihitung (status: DITIMBANG).",
                                updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/setoran/verifikasi": {
                        Setoran s = SERVICE.verifikasiSetoran(payload.get("idPetugas"), payload.get("idSetoran"));
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idPetugas"));
                        sendSuccess(ex, "Setoran " + s.getIdSetoran()
                                + " berhasil diverifikasi (SELESAI)! Saldo dan poin telah masuk ke Dompet Nasabah.",
                                updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/setoran/batalkan": {
                        Setoran s = SERVICE.batalkanSetoran(payload.get("idSetoran"), payload.get("idPengguna"));
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idPengguna"));
                        sendSuccess(ex, "Setoran " + s.getIdSetoran() + " telah dibatalkan/ditolak (status: DITOLAK).",
                                updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/dompet/tarik": {
                        double jumlah = Double.parseDouble(payload.getOrDefault("jumlah", "0"));
                        SERVICE.tarikSaldo(
                                payload.get("idNasabah"),
                                jumlah,
                                payload.get("tipeMetode"),
                                payload.get("field1"),
                                payload.get("field2"),
                                payload.get("field3")
                        );
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idNasabah"));
                        sendSuccess(ex, "Penarikan saldo berhasil diproses!", updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/hadiah/tukar": {
                        SERVICE.tukarPoin(payload.get("idNasabah"), payload.get("idHadiah"));
                        Pengguna updated = SERVICE.findPenggunaById(payload.get("idNasabah"));
                        sendSuccess(ex, "Penukaran poin hadiah berhasil!", updated != null ? SERVICE.toJsonPengguna(updated) : null);
                        break;
                    }
                    case "/api/sampah/tambah": {
                        double harga = Double.parseDouble(payload.getOrDefault("hargaPerKg", "0"));
                        boolean kondisi = Boolean.parseBoolean(payload.getOrDefault("kondisi", "true"));
                        double kemurnian = Double.parseDouble(payload.getOrDefault("kadarKemurnian", "1.0"));
                        Sampah baru = SERVICE.tambahSampah(
                                payload.get("kategori"),
                                payload.get("namaSampah"),
                                harga,
                                payload.get("jenisSpesifik"),
                                kondisi,
                                kemurnian
                        );
                        sendSuccess(ex, "Jenis sampah baru (" + baru.getIdSampah() + " - " + baru.getNamaSampah() + ") berhasil ditambahkan.", null);
                        break;
                    }
                    case "/api/sampah/harga": {
                        double hargaBaru = Double.parseDouble(payload.getOrDefault("hargaBaru", "0"));
                        Sampah s = SERVICE.ubahHargaSampah(payload.get("idSampah"), hargaBaru);
                        sendSuccess(ex, "Harga " + s.getNamaSampah() + " berhasil diperbarui.", null);
                        break;
                    }
                    case "/api/hadiah/tambah": {
                        int poin = Integer.parseInt(payload.getOrDefault("poinDibutuhkan", "0"));
                        int stok = Integer.parseInt(payload.getOrDefault("stok", "0"));
                        Hadiah h = SERVICE.tambahHadiah(payload.get("namaHadiah"), poin, stok);
                        sendSuccess(ex, "Hadiah baru (" + h.getIdHadiah() + " - " + h.getNamaHadiah() + ") berhasil ditambahkan.", null);
                        break;
                    }
                    case "/api/hadiah/stok": {
                        int tambahan = Integer.parseInt(payload.getOrDefault("tambahan", "0"));
                        Hadiah h = SERVICE.tambahStokHadiah(payload.get("idHadiah"), tambahan);
                        sendSuccess(ex, "Stok hadiah " + h.getNamaHadiah() + " berhasil ditambah menjadi " + h.getStok() + ".", null);
                        break;
                    }
                    default:
                        sendError(ex, 404, "Endpoint tidak ditemukan: " + path);
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendError(ex, 400, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendError(ex, 500, "Kesalahan sistem: " + e.getMessage());
            }
        }

        private void addCorsHeaders(HttpExchange ex) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        }

        private String readBody(HttpExchange ex) throws IOException {
            try (InputStream is = ex.getRequestBody()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        private void sendSuccess(HttpExchange ex, String message, String userJson) throws IOException {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"ok\":true,");
            sb.append("\"message\":").append(JsonUtil.quote(message)).append(",");
            sb.append("\"user\":").append(userJson != null ? userJson : "null").append(",");
            sb.append("\"state\":").append(SERVICE.buildFullStateJson());
            sb.append("}");
            sendJson(ex, 200, sb.toString());
        }

        private void sendError(HttpExchange ex, int status, String errorMsg) throws IOException {
            String json = "{\"ok\":false,\"error\":" + JsonUtil.quote(errorMsg) + "}";
            sendJson(ex, status, json);
        }

        private void sendJson(HttpExchange ex, int status, String json) throws IOException {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
