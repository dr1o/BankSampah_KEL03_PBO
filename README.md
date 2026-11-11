# Sistem Bank Sampah (Java)

Implementasi Class Diagram Sistem Bank Sampah (15 kelas, versi dengan `Admin` terpisah).
Satu file Java per kelas di diagram, dikelompokkan ke dalam folder (package) sesuai kegunaannya.
Satu-satunya kelas di luar diagram adalah `banksampah.Main`, yaitu aplikasi CLI untuk menjalankan sistem.

## Cara kompilasi dan menjalankan

Dibutuhkan JDK 11 atau lebih baru (diuji dengan JDK 21).

```
javac -Xlint:all -d out src/banksampah/*.java src/banksampah/pengguna/*.java src/banksampah/sampah/*.java src/banksampah/setoran/*.java src/banksampah/keuangan/*.java src/banksampah/hadiah/*.java
java -cp out banksampah.Main
```

Akun contoh ditampilkan saat program dimulai. Data hanya disimpan di memori, jadi kembali ke data awal
setiap program dijalankan. Program dirancang untuk diketik langsung di terminal; bila input dikirim sekaligus
lewat pipe, input setelah menu Kelola Jenis Sampah atau Kelola Hadiah akan hilang karena menu itu memakai
`Scanner` sendiri di `Admin`.

Untuk menjalankan demo otomatis tanpa input:

```
jshell -s --class-path out demo.jsh
```

`demo.jsh` adalah skrip JShell (bukan kelas Java), jadi tidak menambah kelas di luar diagram.
Di IDE (IntelliJ, NetBeans, Eclipse), cukup impor folder `src` sebagai source folder.

## Daftar kelas dan file

| Kelas di diagram | Jenis | File (di dalam `src/banksampah/`) |
| --- | --- | --- |
| Pengguna | abstract class | `pengguna/Pengguna.java` |
| Nasabah, Petugas, Admin | extends Pengguna | `pengguna/Nasabah.java`, `pengguna/Petugas.java`, `pengguna/Admin.java` |
| Sampah | abstract class | `sampah/Sampah.java` |
| Plastik, Kertas, Logam | extends Sampah | `sampah/Plastik.java`, `sampah/Kertas.java`, `sampah/Logam.java` |
| Setoran | class | `setoran/Setoran.java` |
| DetailSetoran | class | `setoran/DetailSetoran.java` |
| Dompet | class | `keuangan/Dompet.java` |
| MetodePenarikan | interface | `keuangan/MetodePenarikan.java` |
| TransferBank, EWallet | implements MetodePenarikan | `keuangan/TransferBank.java`, `keuangan/EWallet.java` |
| Hadiah | class | `hadiah/Hadiah.java` |

## Aturan bisnis yang dipilih

| Hal | Aturan |
| --- | --- |
| Poin | 1 poin untuk setiap Rp100 nilai setoran (dibulatkan ke bawah) |
| Plastik | nilai dikurangi 20% bila tidak bersih |
| Kertas | nilai dikurangi 30% bila tidak kering |
| Logam | nilai dikalikan kadar kemurnian (0.0 sampai 1.0) |
| Biaya admin | Transfer Bank tetap Rp2.500, E-Wallet 1% dari jumlah |
| Penarikan | saldo dipotong sejumlah penarikan ditambah biaya admin |
| Password | minimal 6 karakter saat diubah |
| Harga sampah | dikunci saat DetailSetoran dibuat, perubahan harga hanya berlaku untuk setoran berikutnya |

## Alur status Setoran

DIAJUKAN, lalu `hitungTotal()` (dipanggil saat penimbangan) menjadi DITIMBANG, lalu `verifikasi()` menjadi
DIVERIFIKASI lalu SELESAI. `verifikasi()` menghasilkan DITOLAK bila total nilai 0. `batalkan()` mengubah
status menjadi DITOLAK untuk setoran yang belum selesai.

## Yang ada di kode tetapi tidak tertulis di kotak kelas pada diagram

Semua atribut dan method di kotak kelas diagram ada di kode dengan nama, tipe, dan visibilitas yang sama.
Tambahan yang tidak tertulis di diagram hanyalah:

1. **Constructor** di setiap kelas konkret (tidak digambar di diagram).
2. **`toString()`** di sebagian kelas (override `Object`, untuk mencetak data).
3. **Konstanta status** `private static final String` di `Setoran`.
4. **Field relasi**, yaitu satu field per garis relasi di diagram:

| Relasi di diagram | Field di kode |
| --- | --- |
| Nasabah 1 *-- 1 Dompet | `Nasabah.dompet` |
| Nasabah 1 --> 0..* Setoran | `Nasabah.riwayatSetoran` |
| Petugas 1 --> 0..* Setoran | `Petugas.daftarSetoran` |
| Setoran 1 *-- 1..* DetailSetoran | `Setoran.daftarDetail` |
| DetailSetoran 0..* --> 1 Sampah | `DetailSetoran.sampah` |
| Setoran ..> Dompet | `Setoran.dompet` |
| Admin ..> Sampah | `Admin.daftarSampah` |
| Admin ..> Hadiah | `Admin.daftarHadiah` |

Dua field `Admin` berikut dibutuhkan oleh method di diagram, tetapi relasinya belum digambar:
`Admin.daftarPengguna` (untuk `kelolaPengguna()`) dan `Admin.daftarSetoran` (untuk `buatLaporan()`).

## Temuan untuk perbaikan diagram (Tahap 2)

1. **Poin tidak bisa memakai `Sampah.hitungPoin()`.** `Setoran` tidak punya jalur ke `Sampah` (hanya lewat
   `DetailSetoran`, yang tidak punya akses ke poin), sehingga aturan poin ditulis ulang di `Setoran.hitungTotal()`
   dan `Sampah.hitungPoin()` tidak terpakai. Usulan: tambahkan `+getPoin() int` pada `DetailSetoran`.
2. **`Setoran` tidak punya `setStatus()`.** Perubahan status terjadi di dalam `hitungTotal()`, `verifikasi()`,
   dan `batalkan()`. Usulan: tambah method penimbangan khusus atau enumeration status.
3. **Petugas tidak dapat mengubah berat.** `DetailSetoran` tidak punya `setBerat()`, jadi `timbangSampah()` hanya
   menghitung total, bukan memperbarui berat hasil timbangan.
4. **Relasi Admin ke Pengguna dan Setoran belum digambar** (lihat bagian di atas).
5. **`lihatRiwayat()` hanya mencakup setoran**, karena penarikan dan penukaran poin tidak punya kelas sendiri.
