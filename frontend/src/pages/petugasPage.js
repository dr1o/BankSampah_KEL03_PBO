import {
  state,
  formatRp,
  escapeHtml,
  apiCall,
  showToast,
  syncUrlWithState,
  triggerRender
} from '../state.js';
import { renderProfilSection, bindProfilEvents } from './profilSection.js';

export function renderPetugasPage() {
  const u = state.currentUser;
  const allSetorans = state.appData.daftarSetoran || [];
  const listDiajukan = allSetorans.filter((s) => s.status === 'DIAJUKAN');
  const listDitimbang = allSetorans.filter((s) => s.status === 'DITIMBANG');
  const listVerifikasiSaya = allSetorans.filter(
    (s) => s.status === 'SELESAI' && s.idPetugas === u.idPengguna
  );

  return `
    <main class="petugas-container">
      <!-- Stasiun Operasional Petugas Header -->
      <section class="petugas-station-banner">
        <div class="station-badge-group">
          <div class="station-icon">PTG</div>
          <div>
            <div style="display:flex;align-items:center;gap:0.5rem;">
              <span class="badge badge-brown">STASIUN KERJA PETUGAS</span>
              <span class="mono" style="font-size:0.8rem;font-weight:700;color:var(--brown-800);">
                ID Petugas: ${escapeHtml(u.idPetugas || '-')}
              </span>
              <span class="badge badge-caramel">Wilayah Tugas: ${escapeHtml(u.wilayahTugas || '-')}</span>
            </div>
            <h2 style="font-family:var(--font-display);font-size:1.5rem;color:var(--brown-900);margin-top:0.25rem;">
              Petugas ${escapeHtml(u.nama)} (${escapeHtml(u.idPengguna)})
            </h2>
          </div>
        </div>

        <div style="text-align:right;">
          <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;font-weight:700;">
            Total Verifikasi Saya
          </div>
          <div class="mono" style="font-size:1.55rem;font-weight:700;color:var(--brown-900);">
            ${listVerifikasiSaya.length} Setoran Terverifikasi
          </div>
        </div>
      </section>

      <!-- Pipeline Stage Switcher -->
      <section class="pipeline-stepper">
        <button class="pipeline-stage-card ${state.petugasTab === 'timbang' ? 'active' : ''}" data-petugas-tab="timbang">
          <div class="stage-count">${listDiajukan.length}</div>
          <div class="stage-title">1. Antrean Timbang (DIAJUKAN)</div>
          <div class="stage-desc">Timbang &amp; hitung total setoran masuk</div>
        </button>

        <button class="pipeline-stage-card ${state.petugasTab === 'verifikasi' ? 'active' : ''}" data-petugas-tab="verifikasi">
          <div class="stage-count">${listDitimbang.length}</div>
          <div class="stage-title">2. Siap Verifikasi (DITIMBANG)</div>
          <div class="stage-desc">Verifikasi &amp; cairkan saldo ke dompet</div>
        </button>

        <button class="pipeline-stage-card ${state.petugasTab === 'riwayat' ? 'active' : ''}" data-petugas-tab="riwayat">
          <div class="stage-count">${listVerifikasiSaya.length}</div>
          <div class="stage-title">3. Riwayat Verifikasi Saya</div>
          <div class="stage-desc">Daftar setoran yang telah diverifikasi</div>
        </button>

        <button class="pipeline-stage-card ${state.petugasTab === 'profil' ? 'active' : ''}" data-petugas-tab="profil">
          <div class="stage-count">⚙</div>
          <div class="stage-title">4. Profil &amp; Password</div>
          <div class="stage-desc">Kelola akun Petugas</div>
        </button>
      </section>

      ${renderPetugasStageContent(listDiajukan, listDitimbang, listVerifikasiSaya)}
    </main>
  `;
}

function renderPetugasStageContent(listDiajukan, listDitimbang, listVerifikasiSaya) {
  if (state.petugasTab === 'profil') {
    return renderProfilSection();
  }

  if (state.petugasTab === 'timbang') {
    return `
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Tahap 1: Antrean Penimbangan Sampah</h3>
            <p class="card-subtitle">
              Menghitung total nilai &amp; poin (1 poin/Rp100) lalu mengubah status setoran menjadi DITIMBANG.
            </p>
          </div>
        </div>

        ${
          listDiajukan.length === 0
            ? `<div style="padding:2.5rem;text-align:center;color:var(--text-muted);background:var(--surface-ivory);border-radius:10px;">Tidak ada antrean setoran berstatus DIAJUKAN saat ini.</div>`
            : `
          <div class="queue-cards-grid">
            ${listDiajukan
              .map(
                (s) => `
              <div class="queue-setoran-card">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <div>
                    <span class="mono" style="font-weight:700;font-size:1rem;color:var(--brown-900);">${escapeHtml(s.idSetoran)}</span>
                    <div style="font-size:0.8rem;color:var(--text-muted);">${escapeHtml(s.tanggalSetor)}</div>
                  </div>
                  <span class="badge badge-DIAJUKAN">DIAJUKAN</span>
                </div>

                <div style="background:var(--surface-cream);padding:0.75rem;border-radius:8px;font-size:0.83rem;">
                  <div><strong>Penyetor:</strong> ${escapeHtml(s.namaNasabah)} (${escapeHtml(s.idNasabah)})</div>
                </div>

                <div>
                  <div style="font-size:0.76rem;font-weight:700;text-transform:uppercase;color:var(--text-muted);margin-bottom:0.35rem;">
                    Item Sampah (${s.daftarDetail.length} Item):
                  </div>
                  ${s.daftarDetail
                    .map(
                      (d) => `
                    <div style="display:flex;justify-content:space-between;font-size:0.82rem;padding:0.25rem 0;border-bottom:1px dashed var(--border-warm);">
                      <span>${escapeHtml(d.namaSampah)} <span class="badge badge-brown">${escapeHtml(d.subkelas)}</span> &times; ${d.berat} kg</span>
                      <strong class="mono">${formatRp(d.subtotal)}</strong>
                    </div>
                  `
                    )
                    .join('')}
                </div>

                <div style="display:flex;justify-content:space-between;align-items:center;font-size:0.86rem;">
                  <span>Hasil Timbang:</span>
                  <strong class="mono" style="color:var(--brown-900);">${formatRp(s.estimasiNilai)} (${s.estimasiPoin} poin)</strong>
                </div>

                <div style="display:flex;gap:0.6rem;margin-top:auto;">
                  <button class="btn btn-primary" style="flex:1;" data-petugas-timbang="${escapeHtml(s.idSetoran)}">
                    ⚖ Timbang &amp; Hitung Total
                  </button>
                  <button class="btn btn-danger btn-sm" data-cancel-setoran="${escapeHtml(s.idSetoran)}">
                    Tolak
                  </button>
                </div>
              </div>
            `
              )
              .join('')}
          </div>
        `
        }
      </div>
    `;
  }

  if (state.petugasTab === 'verifikasi') {
    return `
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Tahap 2: Verifikasi Setoran &amp; Pencairan Saldo</h3>
            <p class="card-subtitle">
              Memverifikasi setoran yang telah ditimbang untuk mencairkan saldo &amp; poin ke Nasabah.
            </p>
          </div>
        </div>

        ${
          listDitimbang.length === 0
            ? `<div style="padding:2.5rem;text-align:center;color:var(--text-muted);background:var(--surface-ivory);border-radius:10px;">Belum ada setoran berstatus DITIMBANG yang menunggu verifikasi.</div>`
            : `
          <div class="queue-cards-grid">
            ${listDitimbang
              .map(
                (s) => `
              <div class="queue-setoran-card">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <div>
                    <span class="mono" style="font-weight:700;font-size:1rem;color:var(--brown-900);">${escapeHtml(s.idSetoran)}</span>
                    <div style="font-size:0.8rem;color:var(--text-muted);">${escapeHtml(s.tanggalSetor)}</div>
                  </div>
                  <span class="badge badge-DITIMBANG">DITIMBANG</span>
                </div>

                <div style="background:var(--surface-cream);padding:0.8rem;border-radius:8px;font-size:0.84rem;">
                  <div><strong>Nasabah:</strong> ${escapeHtml(s.namaNasabah)} (${escapeHtml(s.idNasabah)})</div>
                  <div style="margin-top:0.4rem;padding-top:0.4rem;border-top:1px dashed var(--border-strong);display:flex;justify-content:space-between;">
                    <span>Pencairan Saldo &amp; Poin:</span>
                    <strong class="mono" style="color:#166534;">+${formatRp(s.totalNilai)} | +${s.totalPoin} poin</strong>
                  </div>
                </div>

                <div>
                  ${s.daftarDetail
                    .map(
                      (d) => `
                    <div style="display:flex;justify-content:space-between;font-size:0.82rem;padding:0.22rem 0;">
                      <span>&bull; ${escapeHtml(d.namaSampah)} (${d.berat} kg)</span>
                      <span class="mono">${formatRp(d.subtotal)}</span>
                    </div>
                  `
                    )
                    .join('')}
                </div>

                <div style="display:flex;gap:0.6rem;margin-top:auto;">
                  <button class="btn btn-primary" style="flex:1;" data-petugas-verifikasi="${escapeHtml(s.idSetoran)}">
                    ✓ Verifikasi &amp; Cairkan Saldo
                  </button>
                  <button class="btn btn-danger btn-sm" data-cancel-setoran="${escapeHtml(s.idSetoran)}">
                    Tolak
                  </button>
                </div>
              </div>
            `
              )
              .join('')}
          </div>
        `
        }
      </div>
    `;
  }

  return `
    <div class="card">
      <div class="card-header">
        <div>
          <h3 class="card-title">Riwayat Setoran yang Diverifikasi oleh ${escapeHtml(state.currentUser.nama)}</h3>
        </div>
      </div>

      ${
        listVerifikasiSaya.length === 0
          ? `<div style="padding:2rem;text-align:center;color:var(--text-muted);">Belum ada setoran yang Anda verifikasi.</div>`
          : `
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID Setoran</th>
                <th>Tanggal</th>
                <th>Nasabah</th>
                <th>Total Nilai</th>
                <th>Poin</th>
                <th>Status</th>
                <th>Rincian</th>
              </tr>
            </thead>
            <tbody>
              ${listVerifikasiSaya
                .map(
                  (s) => `
                <tr>
                  <td class="mono"><strong>${escapeHtml(s.idSetoran)}</strong></td>
                  <td>${escapeHtml(s.tanggalSetor)}</td>
                  <td>${escapeHtml(s.namaNasabah)}</td>
                  <td class="mono"><strong>${formatRp(s.totalNilai)}</strong></td>
                  <td class="mono">+${s.totalPoin} poin</td>
                  <td><span class="badge badge-${escapeHtml(s.status)}">${escapeHtml(s.status)}</span></td>
                  <td>
                    <button class="btn btn-secondary btn-sm" data-detail-setoran="${escapeHtml(s.idSetoran)}">Lihat Detail</button>
                  </td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>
        </div>
      `
      }
    </div>
  `;
}

export function bindPetugasEvents() {
  document.querySelectorAll('[data-petugas-tab]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.petugasTab = btn.getAttribute('data-petugas-tab');
      syncUrlWithState(false);
      triggerRender();
    });
  });

  document.querySelectorAll('[data-petugas-timbang]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const idSetoran = btn.getAttribute('data-petugas-timbang');
      try {
        const res = await apiCall('/api/setoran/timbang', 'POST', {
          idPetugas: state.currentUser.idPengguna,
          idSetoran
        });
        showToast(res.message);
        state.petugasTab = 'verifikasi';
        syncUrlWithState(false);
        triggerRender();
      } catch (err) {
        showToast(err.message, true);
      }
    });
  });

  document.querySelectorAll('[data-petugas-verifikasi]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const idSetoran = btn.getAttribute('data-petugas-verifikasi');
      try {
        const res = await apiCall('/api/setoran/verifikasi', 'POST', {
          idPetugas: state.currentUser.idPengguna,
          idSetoran
        });
        showToast(res.message);
        triggerRender();
      } catch (err) {
        showToast(err.message, true);
      }
    });
  });

  bindProfilEvents();
}
