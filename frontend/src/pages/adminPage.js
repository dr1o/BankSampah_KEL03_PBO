import {
  state,
  formatRp,
  escapeHtml,
  apiCall,
  showToast,
  showModal,
  closeModal,
  syncUrlWithState,
  triggerRender
} from '../state.js';
import { renderProfilSection, bindProfilEvents } from './profilSection.js';

export function renderAdminPage() {
  const u = state.currentUser;
  const lap = state.appData.laporan || {};

  return `
    <div class="admin-shell">
      <aside class="admin-sidebar">
        <div>
          <div class="admin-profile-box">
            <span class="badge badge-caramel" style="margin-bottom:0.4rem;">KONSOL ADMIN &bull; ${escapeHtml(u.idPengguna)}</span>
            <h3 style="font-family:var(--font-display);font-size:1.2rem;color:var(--surface-white);">${escapeHtml(u.nama)}</h3>
            <div style="font-size:0.78rem;color:var(--brown-200);margin-top:0.2rem;">${escapeHtml(u.email)}</div>
            <div style="margin-top:0.55rem;font-size:0.75rem;color:var(--caramel-100);">
              Level Akses: <strong>${escapeHtml(u.levelAkses || 'PENUH')}</strong>
            </div>
          </div>

          <div class="admin-nav-list">
            <button class="admin-nav-btn ${state.adminTab === 'laporan' ? 'active' : ''}" data-admin-tab="laporan">
              <span>1. Buat Laporan Sistem</span>
              <span class="mono" style="font-size:0.75rem;">${lap.jumlahSetoran || 0}</span>
            </button>
            <button class="admin-nav-btn ${state.adminTab === 'sampah' ? 'active' : ''}" data-admin-tab="sampah">
              <span>2. Kelola Jenis Sampah</span>
              <span class="mono" style="font-size:0.75rem;">${lap.jumlahJenisSampah || 0}</span>
            </button>
            <button class="admin-nav-btn ${state.adminTab === 'hadiah' ? 'active' : ''}" data-admin-tab="hadiah">
              <span>3. Kelola Hadiah</span>
              <span class="mono" style="font-size:0.75rem;">${lap.jumlahHadiah || 0}</span>
            </button>
            <button class="admin-nav-btn ${state.adminTab === 'pengguna' ? 'active' : ''}" data-admin-tab="pengguna">
              <span>4. Kelola Pengguna</span>
              <span class="mono" style="font-size:0.75rem;">${lap.totalPengguna || 0}</span>
            </button>
            <button class="admin-nav-btn ${state.adminTab === 'erd' ? 'active' : ''}" data-admin-tab="erd">
              <span>5. Peta 15 Kelas &amp; ERD</span>
              <span class="mono" style="font-size:0.75rem;">15</span>
            </button>
            <button class="admin-nav-btn ${state.adminTab === 'profil' ? 'active' : ''}" data-admin-tab="profil">
              <span>6. Ubah Password Admin</span>
              <span>⚙</span>
            </button>
          </div>
        </div>
      </aside>

      <main class="admin-main">
        ${renderAdminMainContent()}
      </main>
    </div>
  `;
}

function renderAdminMainContent() {
  switch (state.adminTab) {
    case 'laporan':
      return renderAdminLaporanTab();
    case 'sampah':
      return renderAdminSampahTab();
    case 'hadiah':
      return renderAdminHadiahTab();
    case 'pengguna':
      return renderAdminPenggunaTab();
    case 'erd':
      return renderAdminErdTab();
    case 'profil':
      return renderProfilSection();
    default:
      return renderAdminLaporanTab();
  }
}

function renderAdminLaporanTab() {
  const lap = state.appData.laporan || {};
  const allSetorans = state.appData.daftarSetoran || [];
  const filtered =
    state.adminStatusFilter === 'ALL'
      ? allSetorans
      : allSetorans.filter((s) => s.status === state.adminStatusFilter);

  return `
    <div>
      <div style="margin-bottom:1.25rem;">
        <h2 style="font-family:var(--font-display);font-size:1.6rem;color:var(--brown-900);">
          Laporan Eksekutif Bank Sampah
        </h2>
        <p style="color:var(--text-secondary);font-size:0.88rem;">
          Rekapitulasi real-time seluruh pengguna, katalog sampah, hadiah, dan transaksi setoran.
        </p>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">Total Pengguna Terdaftar</div>
          <div class="kpi-value">${lap.totalPengguna || 0}</div>
          <div class="kpi-sub">${lap.jumlahNasabah || 0} Nasabah &bull; ${lap.jumlahPetugas || 0} Petugas &bull; ${lap.jumlahAdmin || 0} Admin</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Katalog Sampah &amp; Hadiah</div>
          <div class="kpi-value">${lap.jumlahJenisSampah || 0} / ${lap.jumlahHadiah || 0}</div>
          <div class="kpi-sub">${lap.jumlahJenisSampah || 0} Jenis Sampah &bull; ${lap.jumlahHadiah || 0} Hadiah</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Nilai Setoran Selesai</div>
          <div class="kpi-value">${formatRp(lap.totalNilaiSelesai || 0)}</div>
          <div class="kpi-sub">Total ${lap.totalPoinSelesai || 0} poin dihasilkan</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">Saldo Beredar</div>
          <div class="kpi-value">${formatRp(lap.totalSaldoBeredar || 0)}</div>
          <div class="kpi-sub">Poin aktif: ${lap.totalPoinBeredar || 0} poin</div>
        </div>
      </div>

      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-header">
          <div>
            <h3 class="card-title">Distribusi Status Transaksi Setoran</h3>
            <p class="card-subtitle">Klik status untuk memfilter tabel transaksi di bawah</p>
          </div>
          <div style="display:flex;gap:0.45rem;flex-wrap:wrap;">
            <button class="btn ${state.adminStatusFilter === 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="ALL">
              Semua (${lap.jumlahSetoran || 0})
            </button>
            <button class="btn ${state.adminStatusFilter === 'DIAJUKAN' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="DIAJUKAN">
              DIAJUKAN (${lap.statusDiajukan || 0})
            </button>
            <button class="btn ${state.adminStatusFilter === 'DITIMBANG' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="DITIMBANG">
              DITIMBANG (${lap.statusDitimbang || 0})
            </button>
            <button class="btn ${state.adminStatusFilter === 'DIVERIFIKASI' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="DIVERIFIKASI">
              DIVERIFIKASI (${lap.statusDiverifikasi || 0})
            </button>
            <button class="btn ${state.adminStatusFilter === 'SELESAI' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="SELESAI">
              SELESAI (${lap.statusSelesai || 0})
            </button>
            <button class="btn ${state.adminStatusFilter === 'DITOLAK' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-status="DITOLAK">
              DITOLAK (${lap.statusDitolak || 0})
            </button>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID Setoran</th>
                <th>Tanggal</th>
                <th>Nasabah Penyetor</th>
                <th>Petugas Verifikator</th>
                <th>Jumlah Item</th>
                <th>Total Nilai</th>
                <th>Poin</th>
                <th>Status</th>
                <th>Aksi</th>
              </tr>
            </thead>
            <tbody>
              ${
                filtered.length === 0
                  ? `<tr><td colspan="9" style="text-align:center;padding:1.5rem;color:var(--text-muted);">Belum ada data setoran untuk filter ini.</td></tr>`
                  : filtered
                      .map(
                        (s) => `
                  <tr>
                    <td class="mono"><strong>${escapeHtml(s.idSetoran)}</strong></td>
                    <td>${escapeHtml(s.tanggalSetor)}</td>
                    <td>${escapeHtml(s.namaNasabah)} <span class="mono" style="font-size:0.75rem;color:var(--text-muted);">(${escapeHtml(s.idNasabah)})</span></td>
                    <td>${escapeHtml(s.namaPetugas)}</td>
                    <td>${s.daftarDetail.length} item</td>
                    <td class="mono"><strong>${formatRp(s.status === 'DIAJUKAN' ? s.estimasiNilai : s.totalNilai)}</strong></td>
                    <td class="mono">${s.status === 'DIAJUKAN' ? s.estimasiPoin : s.totalPoin} poin</td>
                    <td><span class="badge badge-${escapeHtml(s.status)}">${escapeHtml(s.status)}</span></td>
                    <td>
                      <button class="btn btn-secondary btn-sm" data-detail-setoran="${escapeHtml(s.idSetoran)}">Rincian</button>
                    </td>
                  </tr>
                `
                      )
                      .join('')
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;
}

function renderAdminSampahTab() {
  const sampahs = state.appData.daftarSampah || [];
  const kat = state.sampahFormKategori;

  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Daftar Jenis Sampah</h3>
            <p class="card-subtitle">
              Perubahan harga hanya berlaku untuk setoran berikutnya.
            </p>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nama Sampah</th>
                <th>Kategori</th>
                <th>Kondisi</th>
                <th>Harga Dasar/kg</th>
                <th>Nilai Efektif/kg</th>
                <th>Ubah Harga</th>
              </tr>
            </thead>
            <tbody>
              ${sampahs
                .map(
                  (s) => `
                <tr>
                  <td class="mono"><strong>${escapeHtml(s.idSampah)}</strong></td>
                  <td><strong>${escapeHtml(s.namaSampah)}</strong></td>
                  <td><span class="badge badge-brown">${escapeHtml(s.subkelas)}</span></td>
                  <td>
                    <div><strong>${escapeHtml(s.jenisSpesifik)}</strong></div>
                    <div style="font-size:0.75rem;color:var(--text-secondary);">${escapeHtml(s.keteranganKondisi)}</div>
                  </td>
                  <td class="mono">${formatRp(s.hargaPerKg)}</td>
                  <td class="mono"><strong>${formatRp(s.hargaEfektifPerKg)}</strong></td>
                  <td>
                    <button class="btn btn-secondary btn-sm" data-edit-harga="${escapeHtml(s.idSampah)}">
                      Ubah Harga
                    </button>
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">+ Tambah Jenis Sampah Baru</h3>
          </div>
        </div>

        <form id="form-tambah-sampah">
          <div class="form-group">
            <label class="form-label">Kategori Sampah</label>
            <select class="form-select" id="smp-kategori">
              <option value="PLASTIK" ${kat === 'PLASTIK' ? 'selected' : ''}>Plastik</option>
              <option value="KERTAS" ${kat === 'KERTAS' ? 'selected' : ''}>Kertas</option>
              <option value="LOGAM" ${kat === 'LOGAM' ? 'selected' : ''}>Logam</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Nama Sampah</label>
            <input type="text" class="form-input" id="smp-nama" placeholder="Contoh: Botol HDPE / Kertas HVS / Tembaga" required />
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label">Harga Dasar per Kg (Rp)</label>
              <input type="number" min="0" step="100" class="form-input mono" id="smp-harga" placeholder="3500" required />
            </div>
            <div class="form-group">
              <label class="form-label">
                ${kat === 'PLASTIK' ? 'Jenis Plastik' : kat === 'KERTAS' ? 'Jenis Kertas' : 'Jenis Logam'}
              </label>
              <input type="text" class="form-input" id="smp-jenis" placeholder="${kat === 'PLASTIK' ? 'PET / HDPE / PP' : kat === 'KERTAS' ? 'HVS / Kardus / Koran' : 'Tembaga / Besi / Aluminium'}" required />
            </div>
          </div>

          ${
            kat === 'PLASTIK'
              ? `
            <div class="form-group">
              <label class="form-label">Kondisi Kebersihan Plastik</label>
              <select class="form-select" id="smp-kondisi">
                <option value="true">Bersih &rarr; Nilai 100%</option>
                <option value="false">Kotor &rarr; Nilai dikurangi 20% (80%)</option>
              </select>
            </div>
          `
              : kat === 'KERTAS'
              ? `
            <div class="form-group">
              <label class="form-label">Kondisi Kekeringan Kertas</label>
              <select class="form-select" id="smp-kondisi">
                <option value="true">Kering &rarr; Nilai 100%</option>
                <option value="false">Basah / Tidak Kering &rarr; Nilai dikurangi 30% (70%)</option>
              </select>
            </div>
          `
              : `
            <div class="form-group">
              <label class="form-label">Kadar Kemurnian Logam (0.0 - 1.0)</label>
              <input type="number" min="0.05" max="1.0" step="0.05" value="0.90" class="form-input mono" id="smp-kemurnian" required />
            </div>
          `
          }

          <button type="submit" class="btn btn-primary btn-block" style="margin-top:0.5rem;">
            Simpan Sampah Baru
          </button>
        </form>
      </div>
    </div>
  `;
}

function renderAdminHadiahTab() {
  const hadiahs = state.appData.daftarHadiah || [];

  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Katalog Hadiah</h3>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID Hadiah</th>
                <th>Nama Hadiah</th>
                <th>Poin Dibutuhkan</th>
                <th>Stok</th>
                <th>Status</th>
                <th>Aksi</th>
              </tr>
            </thead>
            <tbody>
              ${hadiahs
                .map(
                  (h) => `
                <tr>
                  <td class="mono"><strong>${escapeHtml(h.idHadiah)}</strong></td>
                  <td><strong>${escapeHtml(h.namaHadiah)}</strong></td>
                  <td class="mono">${h.poinDibutuhkan} poin</td>
                  <td class="mono"><strong>${h.stok}</strong></td>
                  <td>
                    <span class="badge ${h.tersedia ? 'badge-SELESAI' : 'badge-DITOLAK'}">
                      ${h.tersedia ? 'Tersedia' : 'Habis'}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-secondary btn-sm" data-add-stok="${escapeHtml(h.idHadiah)}">
                      + Tambah Stok
                    </button>
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">+ Tambah Hadiah Baru</h3>
          </div>
        </div>

        <form id="form-tambah-hadiah">
          <div class="form-group">
            <label class="form-label">Nama Hadiah</label>
            <input type="text" class="form-input" id="had-nama" placeholder="Contoh: Tumbler Stainless / Voucher Sembako" required />
          </div>
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label">Poin Dibutuhkan</label>
              <input type="number" min="1" step="5" class="form-input mono" id="had-poin" placeholder="75" required />
            </div>
            <div class="form-group">
              <label class="form-label">Stok Awal</label>
              <input type="number" min="0" step="1" class="form-input mono" id="had-stok" placeholder="10" required />
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-block" style="margin-top:0.5rem;">
            Simpan Hadiah Baru
          </button>
        </form>
      </div>
    </div>
  `;
}

function renderAdminPenggunaTab() {
  const users = state.appData.daftarPengguna || [];
  const filtered =
    state.adminRoleFilter === 'ALL'
      ? users
      : users.filter((u) => u.role === state.adminRoleFilter);
  const roleForm = state.userFormRole;

  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Daftar Pengguna Sistem</h3>
          </div>
          <div style="display:flex;gap:0.4rem;">
            <button class="btn ${state.adminRoleFilter === 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-role="ALL">Semua</button>
            <button class="btn ${state.adminRoleFilter === 'NASABAH' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-role="NASABAH">Nasabah</button>
            <button class="btn ${state.adminRoleFilter === 'PETUGAS' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-role="PETUGAS">Petugas</button>
            <button class="btn ${state.adminRoleFilter === 'ADMIN' ? 'btn-primary' : 'btn-secondary'} btn-sm" data-filter-role="ADMIN">Admin</button>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Peran</th>
                <th>Nama &amp; Kontak</th>
                <th>Detail Peran</th>
              </tr>
            </thead>
            <tbody>
              ${filtered
                .map((u) => {
                  let detailHtml = '';
                  if (u.role === 'NASABAH') {
                    detailHtml = `
                      <div><strong>Alamat:</strong> ${escapeHtml(u.alamat)} | <strong>Daftar:</strong> ${escapeHtml(u.tanggalDaftar)}</div>
                      <div class="mono" style="font-size:0.78rem;color:var(--brown-800);margin-top:0.2rem;">
                        Saldo: <strong>${formatRp(u.dompet?.saldoRupiah)}</strong> &bull; <strong>${u.dompet?.totalPoin || 0} poin</strong> &bull; ${u.jumlahSetoran || 0} setoran
                      </div>
                    `;
                  } else if (u.role === 'PETUGAS') {
                    detailHtml = `
                      <div><strong>ID Petugas:</strong> <span class="mono">${escapeHtml(u.idPetugas)}</span> | <strong>Wilayah Tugas:</strong> ${escapeHtml(u.wilayahTugas)}</div>
                      <div style="font-size:0.78rem;color:var(--text-secondary);margin-top:0.2rem;">
                        Setoran Terverifikasi: <strong>${u.jumlahVerifikasi || 0} setoran</strong>
                      </div>
                    `;
                  } else {
                    detailHtml = `<div><strong>Level Akses:</strong> <span class="badge badge-caramel">${escapeHtml(u.levelAkses)}</span></div>`;
                  }
                  return `
                    <tr>
                      <td class="mono"><strong>${escapeHtml(u.idPengguna)}</strong></td>
                      <td><span class="badge badge-brown">${escapeHtml(u.className)}</span></td>
                      <td>
                        <strong>${escapeHtml(u.nama)}</strong>
                        <div style="font-size:0.76rem;color:var(--text-muted);">${escapeHtml(u.email)} &bull; ${escapeHtml(u.noHp)}</div>
                      </td>
                      <td>${detailHtml}</td>
                    </tr>
                  `;
                })
                .join('')}
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">+ Tambah Pengguna Baru</h3>
          </div>
        </div>

        <form id="form-admin-add-user">
          <div class="form-group">
            <label class="form-label">Peran Pengguna</label>
            <select class="form-select" id="usr-role">
              <option value="PETUGAS" ${roleForm === 'PETUGAS' ? 'selected' : ''}>Petugas</option>
              <option value="NASABAH" ${roleForm === 'NASABAH' ? 'selected' : ''}>Nasabah</option>
              <option value="ADMIN" ${roleForm === 'ADMIN' ? 'selected' : ''}>Admin</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Nama Lengkap</label>
            <input type="text" class="form-input" id="usr-nama" placeholder="Nama lengkap" required />
          </div>
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label">Email</label>
              <input type="email" class="form-input" id="usr-email" placeholder="user@mail.com" required />
            </div>
            <div class="form-group">
              <label class="form-label">No. HP</label>
              <input type="text" class="form-input" id="usr-hp" placeholder="0813xxxx" required />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Password (minimal 6 karakter)</label>
            <input type="password" class="form-input" id="usr-pass" minlength="6" placeholder="Minimal 6 karakter" required />
          </div>

          ${
            roleForm === 'NASABAH'
              ? `
            <div class="form-group">
              <label class="form-label">Alamat Nasabah</label>
              <input type="text" class="form-input" id="usr-extra1" placeholder="Jl. Kenanga No. 5" required />
            </div>
          `
              : roleForm === 'PETUGAS'
              ? `
            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Kode Petugas</label>
                <input type="text" class="form-input mono" id="usr-extra1" placeholder="PTG-02" required />
              </div>
              <div class="form-group">
                <label class="form-label">Wilayah Tugas</label>
                <input type="text" class="form-input" id="usr-extra2" placeholder="Wilayah B" required />
              </div>
            </div>
          `
              : `
            <div class="form-group">
              <label class="form-label">Level Akses Admin</label>
              <input type="text" class="form-input" id="usr-extra1" value="PENUH" required />
            </div>
          `
          }

          <button type="submit" class="btn btn-primary btn-block">
            Tambah Pengguna Baru
          </button>
        </form>
      </div>
    </div>
  `;
}

function renderAdminErdTab() {
  const classes = [
    { name: 'Pengguna', type: 'abstract class', attrs: ['- idPengguna: String', '- nama: String', '- email: String', '- password: String', '- noHp: String'], methods: ['+ login(email, password): boolean', '+ logout(): void', '+ ubahPassword(lama, baru): boolean', '+ tampilkanMenu(): void {abstract}'] },
    { name: 'Nasabah', type: 'extends Pengguna', attrs: ['- alamat: String', '- tanggalDaftar: Date', '- dompet: Dompet (1 *-- 1)', '- riwayatSetoran: List<Setoran>'], methods: ['+ setorSampah(Setoran): void', '+ lihatRiwayat(): List<Setoran>', '+ tukarPoin(Hadiah): boolean', '+ tarikSaldo(double, MetodePenarikan): boolean'] },
    { name: 'Petugas', type: 'extends Pengguna', attrs: ['- idPetugas: String', '- wilayahTugas: String', '- daftarSetoran: List<Setoran>'], methods: ['+ timbangSampah(Setoran): void', '+ verifikasiSetoran(Setoran): boolean'] },
    { name: 'Admin', type: 'extends Pengguna', attrs: ['- levelAkses: String', '- daftarSampah: List<Sampah>', '- daftarHadiah: List<Hadiah>', '- daftarPengguna: List<Pengguna>', '- daftarSetoran: List<Setoran>'], methods: ['+ kelolaJenisSampah(): void', '+ kelolaHadiah(): void', '+ kelolaPengguna(): void', '+ buatLaporan(): void'] },
    { name: 'Sampah', type: 'abstract class', attrs: ['- idSampah: String', '- namaSampah: String', '# hargaPerKg: double'], methods: ['+ hitungNilai(berat): double {abstract}', '+ hitungPoin(nilai): int', '+ setHargaPerKg(harga): void'] },
    { name: 'Plastik', type: 'extends Sampah', attrs: ['- jenisPlastik: String', '- bersih: boolean'], methods: ['+ hitungNilai(berat): double (kotor -20%)'] },
    { name: 'Kertas', type: 'extends Sampah', attrs: ['- jenisKertas: String', '- kering: boolean'], methods: ['+ hitungNilai(berat): double (basah -30%)'] },
    { name: 'Logam', type: 'extends Sampah', attrs: ['- jenisLogam: String', '- kadarKemurnian: double'], methods: ['+ hitungNilai(berat): double (* kemurnian)'] },
    { name: 'Setoran', type: 'class', attrs: ['- idSetoran: String', '- tanggalSetor: Date', '- status: String', '- totalNilai: double', '- totalPoin: int', '- daftarDetail: List<DetailSetoran>', '- dompet: Dompet'], methods: ['+ tambahDetail(DetailSetoran): void', '+ hitungTotal(): double', '+ verifikasi(Petugas): boolean', '+ batalkan(): void'] },
    { name: 'DetailSetoran', type: 'class', attrs: ['- berat: double', '- subtotal: double', '- sampah: Sampah'], methods: ['+ hitungSubtotal(): double'] },
    { name: 'Dompet', type: 'class', attrs: ['- idDompet: String', '- saldoRupiah: double', '- totalPoin: int'], methods: ['+ tambahSaldo(double): void', '+ kurangiSaldo(double): boolean', '+ tambahPoin(int): void', '+ kurangiPoin(int): boolean'] },
    { name: 'MetodePenarikan', type: 'interface', attrs: [], methods: ['+ proses(jumlah): boolean', '+ hitungBiayaAdmin(jumlah): double', '+ getNamaMetode(): String'] },
    { name: 'TransferBank', type: 'implements MetodePenarikan', attrs: ['- namaBank: String', '- noRekening: String', '- namaPemilik: String'], methods: ['+ proses(jumlah): boolean', '+ hitungBiayaAdmin(jumlah): Rp2.500'] },
    { name: 'EWallet', type: 'implements MetodePenarikan', attrs: ['- penyedia: String', '- noHpTerdaftar: String'], methods: ['+ proses(jumlah): boolean', '+ hitungBiayaAdmin(jumlah): 1%'] },
    { name: 'Hadiah', type: 'class', attrs: ['- idHadiah: String', '- namaHadiah: String', '- poinDibutuhkan: int', '- stok: int'], methods: ['+ cekStok(): boolean', '+ kurangiStok(): void', '+ tambahStok(int): void'] }
  ];

  return `
    <div class="card">
      <div class="card-header">
        <div>
          <h3 class="card-title">Kesesuaian 15 Kelas Diagram &amp; ERD</h3>
          <p class="card-subtitle">Seluruh atribut, visibilitas, method, dan relasi diimplementasikan di folder <code>src/banksampah/</code></p>
        </div>
      </div>

      <div class="erd-class-grid">
        ${classes
          .map(
            (c) => `
          <div class="erd-class-box">
            <div class="erd-class-head">
              <span>${escapeHtml(c.name)}</span>
              <span style="font-size:0.68rem;font-weight:400;color:var(--caramel-100);">${escapeHtml(c.type)}</span>
            </div>
            <div class="erd-class-body">
              ${c.attrs.map((a) => `<div>${escapeHtml(a)}</div>`).join('')}
              <div class="erd-divider"></div>
              ${c.methods.map((m) => `<div style="color:var(--brown-800);font-weight:500;">${escapeHtml(m)}</div>`).join('')}
            </div>
          </div>
        `
          )
          .join('')}
      </div>
    </div>
  `;
}

export function bindAdminEvents() {
  document.querySelectorAll('[data-admin-tab]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.adminTab = btn.getAttribute('data-admin-tab');
      syncUrlWithState(false);
      triggerRender();
    });
  });

  document.querySelectorAll('[data-filter-status]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.adminStatusFilter = btn.getAttribute('data-filter-status');
      triggerRender();
    });
  });

  document.querySelectorAll('[data-filter-role]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.adminRoleFilter = btn.getAttribute('data-filter-role');
      triggerRender();
    });
  });

  document.getElementById('smp-kategori')?.addEventListener('change', (e) => {
    state.sampahFormKategori = e.target.value;
    triggerRender();
  });

  document.getElementById('form-tambah-sampah')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const kat = state.sampahFormKategori;
      const res = await apiCall('/api/sampah/tambah', 'POST', {
        kategori: kat,
        namaSampah: document.getElementById('smp-nama').value,
        hargaPerKg: String(document.getElementById('smp-harga').value),
        jenisSpesifik: document.getElementById('smp-jenis').value,
        kondisi: document.getElementById('smp-kondisi')?.value || 'true',
        kadarKemurnian: document.getElementById('smp-kemurnian')?.value || '1.0'
      });
      showToast(res.message);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  document.querySelectorAll('[data-edit-harga]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idSampah = btn.getAttribute('data-edit-harga');
      const s = (state.appData.daftarSampah || []).find((x) => x.idSampah === idSampah);
      if (!s) return;
      showModal(
        `Ubah Harga ${s.namaSampah} (${s.idSampah})`,
        `
        <form id="form-modal-harga">
          <div class="form-group">
            <label class="form-label">Harga Baru per Kg (Rp)</label>
            <input type="number" min="0" step="100" class="form-input mono" id="modal-input-harga" value="${s.hargaPerKg}" required />
          </div>
          <button type="submit" class="btn btn-primary btn-block">Simpan Harga Baru</button>
        </form>
      `
      );
      document.getElementById('form-modal-harga')?.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        try {
          const hargaBaru = document.getElementById('modal-input-harga').value;
          const res = await apiCall('/api/sampah/harga', 'POST', {
            idSampah: s.idSampah,
            hargaBaru: String(hargaBaru)
          });
          closeModal();
          showToast(res.message);
          triggerRender();
        } catch (err) {
          showToast(err.message, true);
        }
      });
    });
  });

  document.getElementById('form-tambah-hadiah')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/hadiah/tambah', 'POST', {
        namaHadiah: document.getElementById('had-nama').value,
        poinDibutuhkan: String(document.getElementById('had-poin').value),
        stok: String(document.getElementById('had-stok').value)
      });
      showToast(res.message);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  document.querySelectorAll('[data-add-stok]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idHadiah = btn.getAttribute('data-add-stok');
      const h = (state.appData.daftarHadiah || []).find((x) => x.idHadiah === idHadiah);
      if (!h) return;
      showModal(
        `Tambah Stok Hadiah: ${h.namaHadiah}`,
        `
        <form id="form-modal-stok">
          <p style="font-size:0.84rem;color:var(--text-secondary);margin-bottom:1rem;">
            Stok saat ini: <strong>${h.stok}</strong>.
          </p>
          <div class="form-group">
            <label class="form-label">Jumlah Tambahan Stok</label>
            <input type="number" min="1" step="1" class="form-input mono" id="modal-input-stok" value="5" required />
          </div>
          <button type="submit" class="btn btn-primary btn-block">Tambahkan Stok</button>
        </form>
      `
      );
      document.getElementById('form-modal-stok')?.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        try {
          const tambahan = document.getElementById('modal-input-stok').value;
          const res = await apiCall('/api/hadiah/stok', 'POST', {
            idHadiah: h.idHadiah,
            tambahan: String(tambahan)
          });
          closeModal();
          showToast(res.message);
          triggerRender();
        } catch (err) {
          showToast(err.message, true);
        }
      });
    });
  });

  document.getElementById('usr-role')?.addEventListener('change', (e) => {
    state.userFormRole = e.target.value;
    triggerRender();
  });

  document.getElementById('form-admin-add-user')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/pengguna/tambah', 'POST', {
        role: state.userFormRole,
        nama: document.getElementById('usr-nama').value,
        email: document.getElementById('usr-email').value,
        noHp: document.getElementById('usr-hp').value,
        password: document.getElementById('usr-pass').value,
        extra1: document.getElementById('usr-extra1')?.value || '',
        extra2: document.getElementById('usr-extra2')?.value || ''
      });
      showToast(res.message);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  bindProfilEvents();
}
