import {
  state,
  formatRp,
  escapeHtml,
  apiCall,
  showToast,
  showModal,
  syncUrlWithState,
  triggerRender
} from '../state.js';
import { renderProfilSection, bindProfilEvents } from './profilSection.js';

export function renderNasabahPage() {
  const u = state.currentUser;
  const dompet = u.dompet || { idDompet: '-', saldoRupiah: 0, totalPoin: 0 };
  const mySetorans = (state.appData.daftarSetoran || []).filter((s) => s.idNasabah === u.idPengguna);

  return `
    <main class="nasabah-container">
      <!-- Hero Dompet & Identitas Nasabah -->
      <section class="nasabah-wallet-hero">
        <div class="wallet-profile-info">
          <h2>Halo, ${escapeHtml(u.nama)}!</h2>
          <p style="color:var(--brown-100);font-size:0.88rem;">
            Setor sampah terpilah, pantau proses penimbangan &amp; verifikasi petugas, tarik saldo rupiah, atau tukar poin reward.
          </p>
          <div class="wallet-meta-list">
            <span><strong>Email:</strong> ${escapeHtml(u.email)}</span>
            <span><strong>No. HP:</strong> ${escapeHtml(u.noHp)}</span>
            <span><strong>Alamat:</strong> ${escapeHtml(u.alamat || '-')}</span>
            <span><strong>Terdaftar:</strong> ${escapeHtml(u.tanggalDaftar || '-')}</span>
          </div>
        </div>

        <div class="wallet-stat-box">
          <div class="wallet-stat-label">
            <span>SALDO DOMPET</span>
          </div>
          <div class="wallet-stat-value">${formatRp(dompet.saldoRupiah)}</div>
          <button class="btn btn-primary btn-sm btn-block" data-nasabah-tab="tarik">
            Tarik Saldo (Transfer Bank / E-Wallet)
          </button>
        </div>

        <div class="wallet-stat-box">
          <div class="wallet-stat-label">
            <span>POIN REWARD TERKUMPUL</span>
            <span class="badge badge-caramel">1 Poin / Rp100</span>
          </div>
          <div class="wallet-stat-value">${dompet.totalPoin} <span style="font-size:1rem;font-weight:600;">poin</span></div>
          <button class="btn btn-secondary btn-sm btn-block" data-nasabah-tab="hadiah">
            Tukar Poin dengan Hadiah (${(state.appData.daftarHadiah || []).length} Katalog)
          </button>
        </div>
      </section>

      <!-- Navigasi Menu Nasabah -->
      <nav class="nasabah-nav-pills">
        <button class="nasabah-pill-btn ${state.nasabahTab === 'setor' ? 'active' : ''}" data-nasabah-tab="setor">
          1. Setor Sampah Baru
        </button>
        <button class="nasabah-pill-btn ${state.nasabahTab === 'riwayat' ? 'active' : ''}" data-nasabah-tab="riwayat">
          2. Riwayat Setoran Saya (${mySetorans.length})
        </button>
        <button class="nasabah-pill-btn ${state.nasabahTab === 'tarik' ? 'active' : ''}" data-nasabah-tab="tarik">
          3. Tarik Saldo
        </button>
        <button class="nasabah-pill-btn ${state.nasabahTab === 'hadiah' ? 'active' : ''}" data-nasabah-tab="hadiah">
          4. Tukar Poin &amp; Hadiah
        </button>
        <button class="nasabah-pill-btn ${state.nasabahTab === 'profil' ? 'active' : ''}" data-nasabah-tab="profil">
          5. Profil &amp; Ubah Password
        </button>
      </nav>

      ${renderNasabahTabContent(mySetorans)}
    </main>
  `;
}

function renderNasabahTabContent(mySetorans) {
  switch (state.nasabahTab) {
    case 'setor':
      return renderNasabahSetorTab();
    case 'riwayat':
      return renderNasabahRiwayatTab(mySetorans);
    case 'tarik':
      return renderNasabahTarikTab();
    case 'hadiah':
      return renderNasabahHadiahTab();
    case 'profil':
      return renderProfilSection();
    default:
      return renderNasabahSetorTab();
  }
}

function renderNasabahSetorTab() {
  const sampahs = state.appData.daftarSampah || [];
  const totalCart = state.cart.reduce((acc, item) => acc + item.subtotal, 0);
  const estimasiPoin = Math.floor(totalCart / 100);

  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Katalog Jenis Sampah</h3>
          </div>
        </div>

        <div class="sampah-catalog-grid">
          ${sampahs
            .map(
              (s) => `
            <div class="sampah-item-card">
              <div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem;">
                  <span class="mono" style="font-size:0.75rem;color:var(--text-muted);">${escapeHtml(s.idSampah)}</span>
                  <span class="badge badge-brown">${escapeHtml(s.subkelas)}</span>
                </div>
                <h4 style="font-size:1.05rem;color:var(--brown-900);margin-bottom:0.2rem;">${escapeHtml(s.namaSampah)}</h4>
                <div style="font-size:0.8rem;color:var(--text-secondary);margin-bottom:0.5rem;">
                  Jenis Spesifik: <strong>${escapeHtml(s.jenisSpesifik)}</strong>
                </div>
                <div class="badge badge-caramel" style="margin-bottom:0.65rem;display:inline-block;">
                  ${escapeHtml(s.keteranganKondisi)}
                </div>
                <div style="background:var(--surface-cream);padding:0.55rem 0.75rem;border-radius:8px;font-size:0.8rem;">
                  <div>Harga Dasar: <strong class="mono">${formatRp(s.hargaPerKg)}/kg</strong></div>
                  <div>Nilai Efektif: <strong class="mono" style="color:var(--brown-900);">${formatRp(s.hargaEfektifPerKg)}/kg</strong></div>
                </div>
              </div>

              <div style="display:flex;gap:0.5rem;align-items:center;margin-top:0.3rem;">
                <input
                  type="number"
                  step="0.1"
                  min="0.1"
                  value="1.0"
                  class="form-input mono"
                  style="padding:0.48rem 0.6rem;width:95px;"
                  id="berat-input-${escapeHtml(s.idSampah)}"
                  title="Berat dalam kg (> 0)"
                />
                <span style="font-size:0.8rem;color:var(--text-muted);">kg</span>
                <button class="btn btn-primary btn-sm" style="flex:1;" data-add-cart="${escapeHtml(s.idSampah)}">
                  + Tambah
                </button>
              </div>
            </div>
          `
            )
            .join('')}
        </div>
      </div>

      <div class="card" style="position:sticky;top:88px;">
        <div class="card-header">
          <div>
            <h3 class="card-title">Keranjang</h3>
            <p class="card-subtitle">Harga sampah dikunci pada saat setoran diajukan</p>
          </div>
          ${
            state.cart.length > 0
              ? `<button class="btn btn-danger btn-sm" id="btn-clear-cart">Kosongkan</button>`
              : ''
          }
        </div>

        ${
          state.cart.length === 0
            ? `
          <div style="padding:2.2rem 1rem;text-align:center;background:var(--surface-ivory);border-radius:10px;border:1px dashed var(--border-strong);color:var(--text-muted);">
            Keranjang masih kosong.<br/>Pilih jenis sampah di sebelah kiri dan masukkan berat (kg).
          </div>
        `
            : `
          <div class="table-wrap" style="margin-bottom:1rem;">
            <table>
              <thead>
                <tr>
                  <th>Sampah</th>
                  <th>Berat</th>
                  <th>Subtotal</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                ${state.cart
                  .map(
                    (c, idx) => `
                  <tr>
                    <td>
                      <strong>${escapeHtml(c.sampah.namaSampah)}</strong>
                      <div style="font-size:0.72rem;color:var(--text-muted);">${escapeHtml(c.sampah.subkelas)} &bull; ${escapeHtml(c.sampah.jenisSpesifik)}</div>
                    </td>
                    <td class="mono">${c.berat} kg</td>
                    <td class="mono"><strong>${formatRp(c.subtotal)}</strong></td>
                    <td>
                      <button class="btn btn-danger btn-sm" data-remove-cart="${idx}" style="padding:0.2rem 0.5rem;">✕</button>
                    </td>
                  </tr>
                `
                  )
                  .join('')}
              </tbody>
            </table>
          </div>

          <div style="background:var(--surface-cream);padding:1rem;border-radius:10px;margin-bottom:1rem;">
            <div style="display:flex;justify-content:space-between;margin-bottom:0.35rem;">
              <span style="font-size:0.85rem;color:var(--text-secondary);">Estimasi Total Nilai:</span>
              <strong class="mono" style="font-size:1.1rem;color:var(--brown-900);">${formatRp(totalCart)}</strong>
            </div>
            <div style="display:flex;justify-content:space-between;">
              <span style="font-size:0.85rem;color:var(--text-secondary);">Estimasi Poin (1 poin/Rp100):</span>
              <strong class="mono" style="color:var(--caramel-600);">+${estimasiPoin} poin</strong>
            </div>
          </div>

          <button class="btn btn-primary btn-block" id="btn-submit-setoran" style="padding:0.8rem;">
            Ajukan Setoran
          </button>
        `
        }
      </div>
    </div>
  `;
}

function renderNasabahRiwayatTab(mySetorans) {
  return `
    <div class="card">
      <div class="card-header">
        <div>
          <h3 class="card-title">Riwayat Setoran Saya</h3>
          <p class="card-subtitle">Pantau perubahan status setoran atau batalkan setoran yang belum selesai</p>
        </div>
      </div>

      <div class="status-flow-bar">
        <strong>Alur Status Setoran:</strong>
        <span class="badge badge-DIAJUKAN">1. DIAJUKAN</span>
        <span class="flow-arrow">&rarr;</span>
        <span class="badge badge-DITIMBANG">2. DITIMBANG</span>
        <span class="flow-arrow">&rarr;</span>
        <span class="badge badge-DIVERIFIKASI">3. DIVERIFIKASI</span>
        <span class="flow-arrow">&rarr;</span>
        <span class="badge badge-SELESAI">4. SELESAI</span>
        <span style="margin-left:auto;color:var(--text-muted);">atau</span>
        <span class="badge badge-DITOLAK">DITOLAK</span>
      </div>

      ${
        mySetorans.length === 0
          ? `<div style="padding:2rem;text-align:center;color:var(--text-muted);">Belum ada riwayat setoran sampah.</div>`
          : `
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID Setoran</th>
                <th>Tanggal</th>
                <th>Rincian Item</th>
                <th>Total Nilai</th>
                <th>Poin</th>
                <th>Petugas Verifikator</th>
                <th>Status</th>
                <th>Aksi</th>
              </tr>
            </thead>
            <tbody>
              ${mySetorans
                .map(
                  (s) => `
                <tr>
                  <td class="mono"><strong>${escapeHtml(s.idSetoran)}</strong></td>
                  <td>${escapeHtml(s.tanggalSetor)}</td>
                  <td>
                    ${s.daftarDetail
                      .map(
                        (d) =>
                          `<div style="font-size:0.8rem;">&bull; ${escapeHtml(d.namaSampah)} (${d.berat} kg) = <span class="mono">${formatRp(d.subtotal)}</span></div>`
                      )
                      .join('')}
                  </td>
                  <td class="mono">
                    <strong>${formatRp(s.status === 'DIAJUKAN' ? s.estimasiNilai : s.totalNilai)}</strong>
                    ${s.status === 'DIAJUKAN' ? '<div style="font-size:0.7rem;color:var(--text-muted);">(Estimasi sebelum ditimbang)</div>' : ''}
                  </td>
                  <td class="mono">${s.status === 'DIAJUKAN' ? s.estimasiPoin : s.totalPoin} poin</td>
                  <td>${escapeHtml(s.namaPetugas)}</td>
                  <td><span class="badge badge-${escapeHtml(s.status)}">${escapeHtml(s.status)}</span></td>
                  <td>
                    <div style="display:flex;gap:0.4rem;">
                      <button class="btn btn-secondary btn-sm" data-detail-setoran="${escapeHtml(s.idSetoran)}">Detail</button>
                      ${
                        s.status === 'DIAJUKAN' || s.status === 'DITIMBANG'
                          ? `<button class="btn btn-danger btn-sm" data-cancel-setoran="${escapeHtml(s.idSetoran)}">Batalkan</button>`
                          : ''
                      }
                    </div>
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

function renderNasabahTarikTab() {
  const dompet = state.currentUser.dompet || { saldoRupiah: 0 };
  const isBank = state.tarikForm.tipeMetode === 'BANK';
  const jml = Number(state.tarikForm.jumlah) || 0;
  const biayaAdmin = isBank ? 2500 : Math.round(jml * 0.01);
  const totalPotong = jml + biayaAdmin;
  const cukup = jml > 0 && dompet.saldoRupiah >= totalPotong;

  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Penarikan Saldo</h3>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:0.85rem;margin-bottom:1.25rem;">
          <button
            type="button"
            class="pipeline-stage-card ${isBank ? 'active' : ''}"
            data-metode-type="BANK"
          >
            <div class="stage-title">Transfer Bank</div>
            <div style="margin-top:0.35rem;font-weight:700;font-size:0.82rem;">Biaya Admin Tetap: Rp2.500</div>
          </button>

          <button
            type="button"
            class="pipeline-stage-card ${!isBank ? 'active' : ''}"
            data-metode-type="EWALLET"
          >
            <div class="stage-title">E-Wallet</div>
            <div style="margin-top:0.35rem;font-weight:700;font-size:0.82rem;">Biaya Admin: 1% dari Jumlah</div>
          </button>
        </div>

        <form id="form-tarik-saldo">
          <div class="form-group">
            <label class="form-label">Jumlah Penarikan (Rp)</label>
            <input
              type="number"
              min="1000"
              step="500"
              class="form-input mono"
              id="tarik-jumlah"
              value="${jml}"
              required
            />
          </div>

          ${
            isBank
              ? `
            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Nama Bank</label>
                <input type="text" class="form-input" id="tarik-f1" value="${escapeHtml(state.tarikForm.field1)}" placeholder="BCA / Mandiri / BRI" required />
              </div>
              <div class="form-group">
                <label class="form-label">Nomor Rekening</label>
                <input type="text" class="form-input mono" id="tarik-f2" value="${escapeHtml(state.tarikForm.field2)}" placeholder="1234567890" required />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Nama Pemilik Rekening</label>
              <input type="text" class="form-input" id="tarik-f3" value="${escapeHtml(state.tarikForm.field3 || state.currentUser.nama)}" required />
            </div>
          `
              : `
            <div class="form-row-2">
              <div class="form-group">
                <label class="form-label">Penyedia E-Wallet</label>
                <input type="text" class="form-input" id="tarik-f1" value="${escapeHtml(state.tarikForm.field1 === 'BCA' ? 'GoPay' : state.tarikForm.field1)}" placeholder="GoPay / OVO / DANA / ShopeePay" required />
              </div>
              <div class="form-group">
                <label class="form-label">No. HP Terdaftar</label>
                <input type="text" class="form-input mono" id="tarik-f2" value="${escapeHtml(state.tarikForm.field2 === '1234567890' ? state.currentUser.noHp : state.tarikForm.field2)}" placeholder="0812xxxx" required />
              </div>
            </div>
          `
          }

          <button type="submit" class="btn btn-primary btn-block" style="padding:0.8rem;" ${!cukup ? 'disabled' : ''}>
            Proses Penarikan
          </button>
        </form>
      </div>

      <div class="card">
        <h3 class="card-title" style="margin-bottom:0.85rem;">Simulasi Potongan</h3>
        <div style="background:var(--surface-cream);padding:1.2rem;border-radius:12px;display:flex;flex-direction:column;gap:0.7rem;">
          <div style="display:flex;justify-content:space-between;">
            <span>Saldo Tersedia:</span>
            <strong class="mono">${formatRp(dompet.saldoRupiah)}</strong>
          </div>
          <div style="display:flex;justify-content:space-between;">
            <span>Nominal Diterima Nasabah:</span>
            <strong class="mono">${formatRp(jml)}</strong>
          </div>
          <div style="display:flex;justify-content:space-between;">
            <span>Biaya Admin:</span>
            <strong class="mono">${formatRp(biayaAdmin)} (${isBank ? 'Tetap Rp2.500' : '1%'})</strong>
          </div>
          <div style="border-top:1px solid var(--border-strong);padding-top:0.65rem;display:flex;justify-content:space-between;font-size:1.05rem;">
            <strong>Total Dipotong:</strong>
            <strong class="mono" style="color:var(--brown-900);">${formatRp(totalPotong)}</strong>
          </div>
          <div style="display:flex;justify-content:space-between;font-size:0.88rem;">
            <span>Estimasi Sisa Saldo:</span>
            <strong class="mono" style="color:${cukup ? '#166534' : '#991B1B'};">
              ${cukup ? formatRp(dompet.saldoRupiah - totalPotong) : 'Saldo Tidak Cukup!'}
            </strong>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderNasabahHadiahTab() {
  const hadiahs = state.appData.daftarHadiah || [];
  const myPoin = state.currentUser.dompet?.totalPoin || 0;

  return `
    <div class="card">
      <div class="card-header">
        <div>
          <h3 class="card-title">Katalog Penukaran Hadiah</h3>
        </div>
      </div>

      <div class="sampah-catalog-grid">
        ${hadiahs
          .map((h) => {
            const bisaTukar = h.tersedia && myPoin >= h.poinDibutuhkan;
            return `
            <div class="sampah-item-card">
              <div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.45rem;">
                  <span class="mono" style="font-size:0.75rem;color:var(--text-muted);">${escapeHtml(h.idHadiah)}</span>
                  <span class="badge ${h.tersedia ? 'badge-SELESAI' : 'badge-DITOLAK'}">
                    ${h.tersedia ? `Stok: ${h.stok}` : 'Stok Habis'}
                  </span>
                </div>
                <h4 style="font-size:1.1rem;color:var(--brown-900);margin-bottom:0.35rem;">${escapeHtml(h.namaHadiah)}</h4>
                <div class="mono" style="font-size:1.2rem;font-weight:700;color:var(--caramel-600);margin-bottom:0.65rem;">
                  ${h.poinDibutuhkan} Poin
                </div>
              </div>

              <button
                class="btn ${bisaTukar ? 'btn-primary' : 'btn-secondary'} btn-block"
                data-tukar-hadiah="${escapeHtml(h.idHadiah)}"
                ${!bisaTukar ? 'disabled' : ''}
              >
                ${
                  !h.tersedia
                    ? 'Stok Habis'
                    : myPoin < h.poinDibutuhkan
                    ? `Kurang ${h.poinDibutuhkan - myPoin} Poin`
                    : 'Tukar Poin Sekarang'
                }
              </button>
            </div>
          `;
          })
          .join('')}
      </div>
    </div>
  `;
}

export function bindNasabahEvents() {
  document.querySelectorAll('[data-nasabah-tab]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.nasabahTab = btn.getAttribute('data-nasabah-tab');
      syncUrlWithState(false);
      triggerRender();
    });
  });

  document.querySelectorAll('[data-add-cart]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idSampah = btn.getAttribute('data-add-cart');
      const input = document.getElementById(`berat-input-${idSampah}`);
      const berat = parseFloat(input?.value || '0');
      if (isNaN(berat) || berat <= 0) {
        showToast('Berat sampah harus lebih dari 0 kg.', true);
        return;
      }
      const sampah = (state.appData.daftarSampah || []).find((s) => s.idSampah === idSampah);
      if (!sampah) return;
      const subtotal = Math.round(sampah.hargaEfektifPerKg * berat);
      state.cart.push({ sampah, berat, subtotal });
      showToast(`Ditambahkan ke keranjang: ${sampah.namaSampah} (${berat} kg) = ${formatRp(subtotal)}`);
      triggerRender();
    });
  });

  document.querySelectorAll('[data-remove-cart]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idx = parseInt(btn.getAttribute('data-remove-cart'), 10);
      state.cart.splice(idx, 1);
      triggerRender();
    });
  });

  document.getElementById('btn-clear-cart')?.addEventListener('click', () => {
    state.cart = [];
    triggerRender();
  });

  document.getElementById('btn-submit-setoran')?.addEventListener('click', async () => {
    try {
      const items = state.cart.map((c) => ({
        idSampah: c.sampah.idSampah,
        berat: String(c.berat)
      }));
      const res = await apiCall('/api/setoran/ajukan', 'POST', {
        idNasabah: state.currentUser.idPengguna,
        items
      });
      state.cart = [];
      state.nasabahTab = 'riwayat';
      showToast(res.message);
      syncUrlWithState(false);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  document.querySelectorAll('[data-metode-type]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const t = btn.getAttribute('data-metode-type');
      state.tarikForm.tipeMetode = t;
      if (t === 'BANK') {
        state.tarikForm.field1 = 'BCA';
        state.tarikForm.field2 = '1234567890';
        state.tarikForm.field3 = state.currentUser?.nama || '';
      } else {
        state.tarikForm.field1 = 'GoPay';
        state.tarikForm.field2 = state.currentUser?.noHp || '0812';
      }
      triggerRender();
    });
  });

  document.getElementById('tarik-jumlah')?.addEventListener('input', (e) => {
    state.tarikForm.jumlah = parseFloat(e.target.value || '0');
    state.tarikForm.field1 = document.getElementById('tarik-f1')?.value || state.tarikForm.field1;
    state.tarikForm.field2 = document.getElementById('tarik-f2')?.value || state.tarikForm.field2;
    state.tarikForm.field3 = document.getElementById('tarik-f3')?.value || state.tarikForm.field3;
    triggerRender();
    const input = document.getElementById('tarik-jumlah');
    if (input) {
      input.focus();
      if (input.type !== 'number' && input.setSelectionRange) {
        input.setSelectionRange(input.value.length, input.value.length);
      }
    }
  });

  document.getElementById('form-tarik-saldo')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/dompet/tarik', 'POST', {
        idNasabah: state.currentUser.idPengguna,
        jumlah: String(document.getElementById('tarik-jumlah').value),
        tipeMetode: state.tarikForm.tipeMetode,
        field1: document.getElementById('tarik-f1')?.value || '',
        field2: document.getElementById('tarik-f2')?.value || '',
        field3: document.getElementById('tarik-f3')?.value || ''
      });
      showToast(res.message);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  document.querySelectorAll('[data-tukar-hadiah]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const idHadiah = btn.getAttribute('data-tukar-hadiah');
      try {
        const res = await apiCall('/api/hadiah/tukar', 'POST', {
          idNasabah: state.currentUser.idPengguna,
          idHadiah
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
