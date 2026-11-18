package banksampah.db;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Mengelola koneksi JDBC murni ke database cloud Neon PostgreSQL.
 * Membaca DATABASE_URL dari environment variable atau file .env.
 */
public final class DatabaseConnection {

    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static String cachedJdbcUrl;
    private static String cachedUser;
    private static String cachedPassword;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        ensureConfigured();
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL JDBC tidak ditemukan (pastikan lib/postgresql.jar ada di classpath).", e);
        }
        return DriverManager.getConnection(cachedJdbcUrl, cachedUser, cachedPassword);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static synchronized void ensureConfigured() throws SQLException {
        if (cachedJdbcUrl != null) {
            return;
        }
        String rawUrl = loadDatabaseUrl();
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new SQLException("DATABASE_URL tidak ditemukan di environment maupun file .env.");
        }
        parseDatabaseUrl(rawUrl.trim());
    }

    private static String loadDatabaseUrl() {
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        Path[] candidates = {
            Paths.get(".env"),
            Paths.get("..", ".env")
        };
        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                try {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                            continue;
                        }
                        if (trimmed.startsWith("DATABASE_URL=")) {
                            String val = trimmed.substring("DATABASE_URL=".length()).trim();
                            if ((val.startsWith("\"") && val.endsWith("\""))
                                    || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            return val;
                        }
                    }
                } catch (IOException ignored) {
                    // Coba kandidat path berikutnya
                }
            }
        }
        return null;
    }

    private static void parseDatabaseUrl(String rawUrl) throws SQLException {
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            cachedJdbcUrl = rawUrl;
            cachedUser = System.getenv("DB_USER");
            cachedPassword = System.getenv("DB_PASSWORD");
            return;
        }
        if (!rawUrl.startsWith("postgresql://") && !rawUrl.startsWith("postgres://")) {
            throw new SQLException("Format DATABASE_URL tidak valid. Harus diawali postgresql://");
        }
        try {
            URI uri = new URI(rawUrl);
            String userInfo = uri.getUserInfo();
            if (userInfo == null || !userInfo.contains(":")) {
                throw new SQLException("DATABASE_URL harus menyertakan username dan password.");
            }
            int sep = userInfo.indexOf(':');
            cachedUser = userInfo.substring(0, sep);
            cachedPassword = userInfo.substring(sep + 1);

            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() != null ? uri.getPath() : "/neondb";
            String query = uri.getQuery();

            StringBuilder jdbc = new StringBuilder();
            jdbc.append("jdbc:postgresql://").append(host).append(":").append(port).append(path);
            if (query != null && !query.isBlank()) {
                jdbc.append("?").append(query);
            } else {
                jdbc.append("?sslmode=require");
            }
            cachedJdbcUrl = jdbc.toString();
        } catch (URISyntaxException e) {
            throw new SQLException("Gagal mem-parsing DATABASE_URL: " + e.getMessage(), e);
        }
    }
}
