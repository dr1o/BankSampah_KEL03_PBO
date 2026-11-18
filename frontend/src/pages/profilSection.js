import { state, escapeHtml, apiCall, showToast, triggerRender } from '../state.js';

export function renderProfilSection() {
  const u = state.currentUser;
  return `
    <div class="setor-split-grid">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Informasi Akun</h3>
          </div>
        </div>

        <form id="form-ubah-nama">
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label">ID Pengguna</label>
              <input type="text" class="form-input mono" value="${escapeHtml(u.idPengguna)}" disabled />
            </div>
            <div class="form-group">
              <label class="form-label">Peran</label>
              <input type="text" class="form-input" value="${escapeHtml(u.className)}" disabled />
            </div>
          </div>
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label">Email</label>
              <input type="text" class="form-input" value="${escapeHtml(u.email)}" disabled />
            </div>
            <div class="form-group">
              <label class="form-label">Nomor HP</label>
              <input type="text" class="form-input" value="${escapeHtml(u.noHp)}" disabled />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Nama Lengkap</label>
            <input type="text" class="form-input" id="prof-nama" value="${escapeHtml(u.nama)}" required />
          </div>
          <button type="submit" class="btn btn-primary">Simpan Perubahan Nama</button>
        </form>
      </div>

      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Ubah Password</h3>
          </div>
        </div>

        <form id="form-ubah-password">
          <div class="form-group">
            <label class="form-label">Password Lama</label>
            <input type="password" class="form-input" id="pass-lama" placeholder="Masukkan password saat ini" required />
          </div>
          <div class="form-group">
            <label class="form-label">Password Baru</label>
            <input type="password" class="form-input" id="pass-baru" minlength="6" placeholder="Minimal 6 karakter" required />
          </div>
          <button type="submit" class="btn btn-primary">Perbarui Password</button>
        </form>
      </div>
    </div>
  `;
}

export function bindProfilEvents() {
  document.getElementById('form-ubah-nama')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/pengguna/nama', 'POST', {
        idPengguna: state.currentUser.idPengguna,
        namaBaru: document.getElementById('prof-nama').value
      });
      showToast(res.message);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });

  document.getElementById('form-ubah-password')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/pengguna/password', 'POST', {
        idPengguna: state.currentUser.idPengguna,
        passwordLama: document.getElementById('pass-lama').value,
        passwordBaru: document.getElementById('pass-baru').value
      });
      showToast(res.message);
      document.getElementById('pass-lama').value = '';
      document.getElementById('pass-baru').value = '';
    } catch (err) {
      showToast(err.message, true);
    }
  });
}
