# Sistem Bank Sampah (Backend Pure Java OOP + Frontend Web Vite)

Implementasi **Class Diagram & ERD Sistem Bank Sampah** (15 kelas domain PBO, versi dengan `Admin` terpisah).
Repositori ini dibagi menjadi **2 bagian terpisah**:

1. **Backend (`src/banksampah/`) — 100% Pure Java**:
   - Berisi 15 kelas domain PBO sesuai Class Diagram & ERD (`pengguna`, `sampah`, `setoran`, `keuangan`, `hadiah`).
   - Dilengkapi konektor JDBC PostgreSQL (`banksampah.db`) dan HTTP REST Server bawaan JDK (`banksampah.server.WebServerMain` dengan `com.sun.net.httpserver.HttpServer`).
   - Tersedia pula antarmuka CLI murni (`banksampah.Main`).
2. **Frontend (`frontend/`) — Web Modern (HTML, CSS, JS + Vite)**:
   - Tampilan web bertema **Coklat & Putih** (*Warm Espresso Brown & Crisp White*).
   - Memiliki **3 akses dan tampilan berbeda** sesuai peran pengguna pada ERD: **Nasabah**, **Petugas**, dan **Admin**.

---

## Cara Menjalankan Tampilan Web (Vite + Pure Java Backend)

Cukup jalankan satu perintah di root proyek:

```bash
npm install
npm run dev
```

Saat `npm run dev` dijalankan:
1. Plugin Vite (`vite.config.js`) otomatis mengompilasi seluruh kelas Java di `src/banksampah/` ke folder `out/` dan menyalakan **Java HTTP Server** (`banksampah.server.WebServerMain`) di `http://127.0.0.1:8080`.
2. Server pengembangan **Vite** aktif di `http://localhost:5173` dan meneruskan request `/api/*` ke backend Java.

### Akun Contoh (3 Akses Berbeda)

| Peran (Role) | Email | Password | Tampilan & Fungsi Utama |
| :--- | :--- | :--- | :--- |
| **1. Nasabah** | `budi@mail.com` | `rahasia1` | Portal Warga & `Dompet` (1:1 Composition), Setor Sampah (`DetailSetoran` + `Setoran`), Riwayat & Batalkan Setoran, Tarik Saldo (`TransferBank` / `EWallet`), Tukar Poin (`Hadiah`). |
| **2. Petugas** | `sari@mail.com` | `petugas1` | Stasiun Kerja Operasional (`idPetugas`, `wilayahTugas`), Antrean Penimbangan (`DIAJUKAN` &rarr; `DITIMBANG`), Verifikasi & Pencairan ke Dompet (`DITIMBANG` &rarr; `DIVERIFIKASI` &rarr; `SELESAI`), Riwayat Verifikasi Petugas. |
| **3. Admin** | `dewi@mail.com` | `admin123` | Konsol Manajemen Eksekutif (`levelAkses`), Laporan & Statistik Status Setoran (`buatLaporan`), Kelola Jenis Sampah (`Plastik`, `Kertas`, `Logam`), Kelola Hadiah, Kelola Pengguna, dan Peta 15 Kelas ERD. |

---

## Cara Menjalankan Versi CLI (Terminal Murni)

Dibutuhkan JDK 11 atau lebih baru:

```powershell
javac -Xlint:all -d out src/banksampah/*.java src/banksampah/pengguna/*.java src/banksampah/sampah/*.java src/banksampah/setoran/*.java src/banksampah/keuangan/*.java src/banksampah/hadiah/*.java
java -cp out banksampah.Main
```

---

## Daftar 15 Kelas Diagram / ERD (`src/banksampah/`)

| Kelas di Diagram | Jenis | File (di dalam `src/banksampah/`) |
| --- | --- | --- |
| `Pengguna` | `abstract class` | `pengguna/Pengguna.java` |
| `Nasabah`, `Petugas`, `Admin` | `extends Pengguna` | `pengguna/Nasabah.java`, `pengguna/Petugas.java`, `pengguna/Admin.java` |
| `Sampah` | `abstract class` | `sampah/Sampah.java` |
| `Plastik`, `Kertas`, `Logam` | `extends Sampah` | `sampah/Plastik.java`, `sampah/Kertas.java`, `sampah/Logam.java` |
| `Setoran` | `class` | `setoran/Setoran.java` |
| `DetailSetoran` | `class` | `setoran/DetailSetoran.java` |
| `Dompet` | `class` | `keuangan/Dompet.java` |
| `MetodePenarikan` | `interface` | `keuangan/MetodePenarikan.java` |
| `TransferBank`, `EWallet` | `implements MetodePenarikan` | `keuangan/TransferBank.java`, `keuangan/EWallet.java` |
| `Hadiah` | `class` | `hadiah/Hadiah.java` |

---

## Kondisi & Aturan Bisnis pada ERD yang Dimuat di Tampilan Web

| Entitas / Konsep | Kondisi pada Kode & Tampilan Web |
| --- | --- |
| **Poin Setoran** | `1 poin` untuk setiap `Rp100` nilai setoran (dibulatkan ke bawah). |
| **Plastik (`Plastik extends Sampah`)** | Atribut `jenisPlastik` & `bersih`. Nilai dikurangi **20%** bila plastik tidak bersih (`kotor`). |
| **Kertas (`Kertas extends Sampah`)** | Atribut `jenisKertas` & `kering`. Nilai dikurangi **30%** bila kertas tidak kering (`basah`). |
| **Logam (`Logam extends Sampah`)** | Atribut `jenisLogam` & `kadarKemurnian`. Nilai dikalikan kadar kemurnian (`0.0` s.d. `1.0`). |
| **Alur Status `Setoran`** | `DIAJUKAN` &rarr; `hitungTotal()` saat ditimbang petugas menjadi `DITIMBANG` &rarr; `verifikasi()` melewati `DIVERIFIKASI` lalu menjadi `SELESAI` (saldo & poin masuk `Dompet`). Bila total nilai 0 atau dibatalkan (`batalkan()`), status menjadi `DITOLAK`. |
| **Harga Terkunci (`DetailSetoran`)** | `subtotal` dikunci saat `DetailSetoran` dibuat. Perubahan `hargaPerKg` oleh Admin hanya berlaku untuk setoran berikutnya. |
| **Penarikan Saldo (`MetodePenarikan`)** | `TransferBank`: biaya admin tetap **Rp2.500**. `EWallet`: biaya admin **1%** dari jumlah penarikan. Total potongan `Dompet` = jumlah penarikan + biaya admin. |
| **Penukaran Hadiah (`Hadiah`)** | Hanya berhasil bila `cekStok() == true` (`stok > 0`) dan poin di `Dompet` mencukupi (`kurangiPoin` & `kurangiStok`). |
| **Keamanan Password (`Pengguna`)** | `ubahPassword(lama, baru)` memvalidasi password lama cocok dan password baru minimal **6 karakter**. |
