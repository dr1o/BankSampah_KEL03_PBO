-- ============================================================
-- Skema Database Sistem Bank Sampah (Neon PostgreSQL)
-- Disesuaikan dengan 15 Kelas Diagram Pemrograman Berorientasi Objek
-- ============================================================

-- 1. TABEL PENGGUNA (Mewakili hierarki Pengguna -> Nasabah, Petugas, Admin)
CREATE TABLE IF NOT EXISTS pengguna (
    id_pengguna     VARCHAR(20) PRIMARY KEY,
    nama            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password        VARCHAR(100) NOT NULL,
    no_hp           VARCHAR(20),
    role            VARCHAR(20) NOT NULL CHECK (role IN ('NASABAH', 'PETUGAS', 'ADMIN')),
    
    -- Khusus Nasabah
    alamat          TEXT,
    tanggal_daftar  TIMESTAMPTZ DEFAULT now(),
    
    -- Khusus Petugas
    id_petugas      VARCHAR(20),
    wilayah_tugas   VARCHAR(100),
    
    -- Khusus Admin
    tingkat_akses   VARCHAR(50)
);

-- 2. TABEL DOMPET (Mewakili kelas Dompet, komposisi 1-to-1 dengan Nasabah)
CREATE TABLE IF NOT EXISTS dompet (
    id_dompet       VARCHAR(30) PRIMARY KEY,
    id_pengguna     VARCHAR(20) UNIQUE NOT NULL REFERENCES pengguna(id_pengguna) ON DELETE CASCADE,
    saldo_rupiah    NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_poin      INTEGER NOT NULL DEFAULT 0
);

-- 3. TABEL SAMPAH (Mewakili hierarki Sampah -> Plastik, Kertas, Logam)
CREATE TABLE IF NOT EXISTS sampah (
    id_sampah        VARCHAR(20) PRIMARY KEY,
    nama_sampah      VARCHAR(100) NOT NULL,
    harga_per_kg     NUMERIC(12, 2) NOT NULL CHECK (harga_per_kg >= 0),
    kategori         VARCHAR(20) NOT NULL CHECK (kategori IN ('PLASTIK', 'KERTAS', 'LOGAM')),
    
    -- Kolom spesifik subkelas
    jenis_spesifik   VARCHAR(50) NOT NULL,            -- jenisPlastik / jenisKertas / jenisLogam
    kondisi_bersih   BOOLEAN DEFAULT TRUE,            -- khusus Plastik: TRUE (bersih), FALSE (kotor - potong 20%)
    kondisi_kering   BOOLEAN DEFAULT TRUE,            -- khusus Kertas: TRUE (kering), FALSE (basah - potong 30%)
    kadar_kemurnian  NUMERIC(4, 2) DEFAULT 1.00       -- khusus Logam: 0.00 sampai 1.00 (misal 0.90 = 90%)
);

-- 4. TABEL HADIAH (Mewakili kelas Hadiah)
CREATE TABLE IF NOT EXISTS hadiah (
    id_hadiah        VARCHAR(20) PRIMARY KEY,
    nama_hadiah      VARCHAR(100) NOT NULL,
    poin_dibutuhkan  INTEGER NOT NULL CHECK (poin_dibutuhkan > 0),
    stok             INTEGER NOT NULL DEFAULT 0 CHECK (stok >= 0)
);

-- 5. TABEL SETORAN (Mewakili transaksi Setoran)
CREATE TABLE IF NOT EXISTS setoran (
    id_setoran       VARCHAR(20) PRIMARY KEY,
    id_nasabah       VARCHAR(20) NOT NULL REFERENCES pengguna(id_pengguna),
    id_petugas       VARCHAR(20) REFERENCES pengguna(id_pengguna),
    tanggal_setor    TIMESTAMPTZ NOT NULL DEFAULT now(),
    status           VARCHAR(20) NOT NULL DEFAULT 'DIAJUKAN' 
                     CHECK (status IN ('DIAJUKAN', 'DITIMBANG', 'DIVERIFIKASI', 'SELESAI', 'DITOLAK')),
    total_nilai      NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_poin       INTEGER NOT NULL DEFAULT 0
);

-- 6. TABEL DETAIL SETORAN (Mewakili kelas DetailSetoran)
CREATE TABLE IF NOT EXISTS detail_setoran (
    id               SERIAL PRIMARY KEY,
    id_setoran       VARCHAR(20) NOT NULL REFERENCES setoran(id_setoran) ON DELETE CASCADE,
    id_sampah        VARCHAR(20) NOT NULL REFERENCES sampah(id_sampah),
    berat            NUMERIC(8, 2) NOT NULL CHECK (berat > 0),
    subtotal         NUMERIC(15, 2) NOT NULL DEFAULT 0.00
);

-- ============================================================
-- DATA AWAL (SEED DATA)
-- Sesuai dengan Main.siapkanDataAwal() pada Java
-- ============================================================

-- Data Pengguna
INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, alamat, tanggal_daftar)
VALUES ('N-001', 'Budi', 'budi@mail.com', 'rahasia1', '0812', 'NASABAH', 'Jl. Mawar 1', now())
ON CONFLICT (id_pengguna) DO NOTHING;

INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, id_petugas, wilayah_tugas)
VALUES ('P-001', 'Sari', 'sari@mail.com', 'petugas1', '0813', 'PETUGAS', 'PTG-01', 'Wilayah A')
ON CONFLICT (id_pengguna) DO NOTHING;

INSERT INTO pengguna (id_pengguna, nama, email, password, no_hp, role, tingkat_akses)
VALUES ('A-001', 'Dewi', 'dewi@mail.com', 'admin123', '0814', 'ADMIN', 'PENUH')
ON CONFLICT (id_pengguna) DO NOTHING;

-- Data Dompet Nasabah Budi
INSERT INTO dompet (id_dompet, id_pengguna, saldo_rupiah, total_poin)
VALUES ('DMP-N-001', 'N-001', 0.00, 0)
ON CONFLICT (id_dompet) DO NOTHING;

-- Data Sampah Awal (Plastik, Kertas, Logam)
INSERT INTO sampah (id_sampah, nama_sampah, harga_per_kg, kategori, jenis_spesifik, kondisi_bersih, kondisi_kering, kadar_kemurnian)
VALUES 
    ('SMP-01', 'Botol PET', 3000, 'PLASTIK', 'PET', TRUE, TRUE, 1.00),
    ('SMP-02', 'Kardus', 1500, 'KERTAS', 'Kardus', TRUE, TRUE, 1.00),
    ('SMP-03', 'Aluminium', 8000, 'LOGAM', 'Aluminium', TRUE, TRUE, 0.90)
ON CONFLICT (id_sampah) DO NOTHING;

-- Data Hadiah Awal
INSERT INTO hadiah (id_hadiah, nama_hadiah, poin_dibutuhkan, stok)
VALUES 
    ('H-01', 'Payung', 100, 2),
    ('H-02', 'Tas Belanja', 50, 5)
ON CONFLICT (id_hadiah) DO NOTHING;
