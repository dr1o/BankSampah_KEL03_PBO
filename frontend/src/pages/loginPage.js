import {
  state,
  apiCall,
  showToast,
  quickLogin,
  syncUrlWithState,
  triggerRender
} from '../state.js';

export function renderLoginPage() {
  return `
    <div class="auth-wrapper">
      <section class="auth-showcase">
        <div>
          <h1 class="auth-hero-title">Sistem Informasi Bank Sampah Berbasis OOP</h1>

          <div class="demo-accounts-box">
            <h3>Klik untuk Masuk Cepat Sesuai Peran Pengguna (3 Akses Berbeda):</h3>
            <div class="demo-role-cards">
              <button class="demo-role-card" data-quick-role="NASABAH">
                <span class="demo-role-tag">AKSES 1 &bull; NASABAH</span>
                <div class="demo-role-name">Budi (N-001)</div>
                <div class="demo-role-cred">budi@mail.com / rahasia1</div>
                <div style="font-size:0.73rem;color:var(--text-secondary);margin-top:0.35rem;">
                  Setor sampah, Dompet, Tarik Saldo, Tukar Hadiah
                </div>
              </button>

              <button class="demo-role-card" data-quick-role="PETUGAS">
                <span class="demo-role-tag" style="background:var(--caramel-600);">AKSES 2 &bull; PETUGAS</span>
                <div class="demo-role-name">Sari (P-001)</div>
                <div class="demo-role-cred">sari@mail.com / petugas1</div>
                <div style="font-size:0.73rem;color:var(--text-secondary);margin-top:0.35rem;">
                  Timbang Setoran &amp; Verifikasi Pencairan Dompet
                </div>
              </button>

              <button class="demo-role-card" data-quick-role="ADMIN">
                <span class="demo-role-tag" style="background:var(--brown-950);">AKSES 3 &bull; ADMIN</span>
                <div class="demo-role-name">Dewi (A-001)</div>
                <div class="demo-role-cred">dewi@mail.com / admin123</div>
                <div style="font-size:0.73rem;color:var(--text-secondary);margin-top:0.35rem;">
                  Kelola Sampah, Hadiah, Pengguna &amp; Laporan
                </div>
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="auth-form-side">
        <div class="auth-card">
          <div class="auth-tabs">
            <button class="auth-tab-btn ${state.authTab === 'login' ? 'active' : ''}" data-auth-tab="login">
              Masuk Akun (Login)
            </button>
            <button class="auth-tab-btn ${state.authTab === 'register' ? 'active' : ''}" data-auth-tab="register">
              Daftar Nasabah Baru
            </button>
          </div>

          ${
            state.authTab === 'login'
              ? `
            <form id="form-login">
              <div style="margin-bottom:1.25rem;">
                <h2 class="card-title">Selamat Datang</h2>
                <p class="card-subtitle">Masuk menggunakan email &amp; password akun Anda</p>
              </div>
              <div class="form-group">
                <label class="form-label">Alamat Email</label>
                <input type="email" class="form-input" id="login-email" placeholder="contoh: budi@mail.com" value="budi@mail.com" required />
              </div>
              <div class="form-group">
                <label class="form-label">Password</label>
                <input type="password" class="form-input" id="login-password" placeholder="Masukkan password" value="rahasia1" required />
              </div>
              <button type="submit" class="btn btn-primary btn-block" style="margin-top:0.5rem;padding:0.78rem;">
                Masuk ke Sistem Bank Sampah &rarr;
              </button>
            </form>
          `
              : `
            <form id="form-register">
              <div style="margin-bottom:1.1rem;">
                <h2 class="card-title">Registrasi Nasabah Baru</h2>
              </div>
              <div class="form-group">
                <label class="form-label">Nama Lengkap</label>
                <input type="text" class="form-input" id="reg-nama" placeholder="Nama lengkap warga" required />
              </div>
              <div class="form-row-2">
                <div class="form-group">
                  <label class="form-label">Email</label>
                  <input type="email" class="form-input" id="reg-email" placeholder="nama@mail.com" required />
                </div>
                <div class="form-group">
                  <label class="form-label">Nomor HP</label>
                  <input type="text" class="form-input" id="reg-hp" placeholder="0812xxxx" required />
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">Password (minimal 6 karakter)</label>
                <input type="password" class="form-input" id="reg-password" placeholder="Minimal 6 karakter" minlength="6" required />
              </div>
              <div class="form-group">
                <label class="form-label">Alamat Tempat Tinggal</label>
                <input type="text" class="form-input" id="reg-alamat" placeholder="Jl. Melati No. 10" required />
              </div>
              <button type="submit" class="btn btn-primary btn-block" style="margin-top:0.5rem;padding:0.78rem;">
                Daftar &amp; Buat Dompet Nasabah
              </button>
            </form>
          `
          }
        </div>
      </section>
    </div>
  `;
}

export function bindLoginEvents() {
  document.querySelectorAll('[data-auth-tab]').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.authTab = btn.getAttribute('data-auth-tab');
      syncUrlWithState(false);
      triggerRender();
    });
  });

  document.getElementById('form-login')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    await quickLogin(email, password);
  });

  document.getElementById('form-register')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const res = await apiCall('/api/register', 'POST', {
        nama: document.getElementById('reg-nama').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        noHp: document.getElementById('reg-hp').value,
        alamat: document.getElementById('reg-alamat').value
      });
      state.nasabahTab = 'setor';
      showToast(res.message);
      syncUrlWithState(false);
      triggerRender();
    } catch (err) {
      showToast(err.message, true);
    }
  });
}
