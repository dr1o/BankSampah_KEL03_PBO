// Demo Sistem Bank Sampah (skrip JShell, bukan kelas Java).
// Jalankan dari folder proyek:  jshell -s --class-path out demo.jsh
import banksampah.hadiah.*;
import banksampah.keuangan.*;
import banksampah.pengguna.*;
import banksampah.sampah.*;
import banksampah.setoran.*;
import java.util.*;

// --- Data awal ---
Plastik plastik = new Plastik("SMP-01", "Botol PET", 3000, "PET", true);
Kertas kertas = new Kertas("SMP-02", "Kardus", 1500, "Kardus", true);
Logam logam = new Logam("SMP-03", "Aluminium", 8000, "Aluminium", 0.9);
List<Sampah> daftarSampah = new ArrayList<>(Arrays.asList(plastik, kertas, logam));
List<Hadiah> daftarHadiah = new ArrayList<>(Arrays.asList(new Hadiah("H-01", "Payung", 100, 2)));
List<Pengguna> daftarPengguna = new ArrayList<>();
List<Setoran> daftarSetoran = new ArrayList<>();

Nasabah budi = new Nasabah("N-001", "Budi", "budi@mail.com", "rahasia1", "0812", "Jl. Mawar 1");
Petugas sari = new Petugas("P-001", "Sari", "sari@mail.com", "petugas1", "0813", "PTG-01", "Wilayah A");
Admin dewi = new Admin("A-001", "Dewi", "dewi@mail.com", "admin123", "0814", "PENUH",
        daftarSampah, daftarHadiah, daftarPengguna, daftarSetoran);
daftarPengguna.add(budi);
daftarPengguna.add(sari);
daftarPengguna.add(dewi);

System.out.println("\n##### 1. Login dan menu (polymorphism tampilkanMenu) #####");
System.out.println("Login Budi: " + budi.login("budi@mail.com", "rahasia1"));
for (Pengguna p : daftarPengguna) { p.tampilkanMenu(); }

System.out.println("\n##### 2. Setor sampah, timbang, verifikasi #####");
Setoran s1 = new Setoran("S-001", budi.getDompet());
s1.tambahDetail(new DetailSetoran(plastik, 2));
s1.tambahDetail(new DetailSetoran(kertas, 3));
s1.tambahDetail(new DetailSetoran(logam, 1));
budi.setorSampah(s1);
daftarSetoran.add(s1);
sari.timbangSampah(s1);
sari.verifikasiSetoran(s1);
System.out.println(budi.getDompet());

System.out.println("\n##### 3. Tukar poin dengan hadiah #####");
budi.tukarPoin(daftarHadiah.get(0));
budi.tukarPoin(daftarHadiah.get(0));

System.out.println("\n##### 4. Tarik saldo (polymorphism MetodePenarikan) #####");
budi.tarikSaldo(10000, new TransferBank("BRI", "1234567890", "Budi"));
budi.tarikSaldo(5000, new EWallet("GoPay", "0812"));
budi.tarikSaldo(1000, new EWallet("GoPay", "0812"));
System.out.println(budi.getDompet());

System.out.println("\n##### 5. Admin #####");
dewi.kelolaPengguna();
dewi.buatLaporan();

/exit
