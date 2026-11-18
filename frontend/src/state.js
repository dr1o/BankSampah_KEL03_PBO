export const state = {
  appData: null,
  currentUser: null,
  authTab: 'login', // 'login' | 'register'
  nasabahTab: 'setor', // 'setor' | 'riwayat' | 'tarik' | 'hadiah' | 'profil'
  petugasTab: 'timbang', // 'timbang' | 'verifikasi' | 'riwayat' | 'profil'
  adminTab: 'laporan', // 'laporan' | 'sampah' | 'hadiah' | 'pengguna' | 'erd' | 'profil'
  cart: [], // [{ sampah, berat, subtotal }]
  tarikForm: {
    tipeMetode: 'BANK',
    jumlah: 10000,
    field1: 'BCA',
    field2: '1234567890',
    field3: ''
  },
  sampahFormKategori: 'PLASTIK',
  userFormRole: 'PETUGAS',
  adminRoleFilter: 'ALL',
  adminStatusFilter: 'ALL'
};

let renderCallback = () => {};

export function setRenderCallback(fn) {
  renderCallback = fn;
}

export function triggerRender() {
  renderCallback();
}

export const formatRp = (num) => {
  const n = Number(num) || 0;
  return 'Rp' + Math.round(n).toLocaleString('id-ID');
};

export const escapeHtml = (str) => {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
};

export function showToast(message, isError = false) {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const el = document.createElement('div');
  el.className = `toast ${isError ? 'error' : ''}`;
  el.textContent = message;
  container.appendChild(el);
  setTimeout(() => {
    el.remove();
  }, 4000);
}

export function showModal(title, contentHtml) {
  const root = document.getElementById('modal-root');
  if (!root) return;
  root.innerHTML = `
    <div class="modal-backdrop" id="modal-backdrop">
      <div class="modal-box">
        <div class="card-header" style="margin-bottom:1rem; border-bottom:1px solid var(--border-warm); padding-bottom:0.75rem;">
          <h3 class="card-title">${escapeHtml(title)}</h3>
          <button class="btn btn-secondary btn-sm" id="modal-close-btn">Tutup ✕</button>
        </div>
        <div>${contentHtml}</div>
      </div>
    </div>
  `;
  document.getElementById('modal-close-btn')?.addEventListener('click', closeModal);
  document.getElementById('modal-backdrop')?.addEventListener('click', (e) => {
    if (e.target.id === 'modal-backdrop') closeModal();
  });
}

export function closeModal() {
  const root = document.getElementById('modal-root');
  if (root) root.innerHTML = '';
}

export async function apiCall(path, method = 'GET', bodyObj = null) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (bodyObj) {
    opts.body = JSON.stringify(bodyObj);
  }
  const res = await fetch(path, opts);
  const data = await res.json();
  if (!res.ok || !data.ok) {
    throw new Error(data.error || 'Terjadi kesalahan pada server.');
  }
  if (data.state) {
    state.appData = data.state;
    if (state.currentUser) {
      const updated = state.appData.daftarPengguna.find(
        (u) => u.idPengguna === state.currentUser.idPengguna
      );
      if (updated) {
        state.currentUser = updated;
      }
    }
  }
  if (data.user) {
    state.currentUser = data.user;
    localStorage.setItem('bs_current_user_id', data.user.idPengguna);
  }
  return data;
}

/* ========================================================
   URL ROUTER (History API)
   ======================================================== */
export function getExpectedPath() {
  if (!state.currentUser) {
    return state.authTab === 'register' ? '/register' : '/login';
  }
  const role = state.currentUser.role;
  if (role === 'NASABAH') {
    return `/nasabah/${state.nasabahTab}`;
  }
  if (role === 'PETUGAS') {
    return `/petugas/${state.petugasTab}`;
  }
  if (role === 'ADMIN') {
    return `/admin/${state.adminTab}`;
  }
  return '/login';
}

export function navigateTo(path, replace = false) {
  if (window.location.pathname !== path) {
    if (replace) {
      window.history.replaceState({}, '', path);
    } else {
      window.history.pushState({}, '', path);
    }
  }
  applyRouteFromLocation();
}

export function syncUrlWithState(replace = false) {
  const target = getExpectedPath();
  if (window.location.pathname !== target) {
    if (replace) {
      window.history.replaceState({}, '', target);
    } else {
      window.history.pushState({}, '', target);
    }
  }
}

export function applyRouteFromLocation() {
  const rawPath = window.location.pathname.toLowerCase().replace(/\/+$/, '') || '/login';
  const parts = rawPath.split('/').filter(Boolean); // e.g. ['nasabah', 'setor']
  const rootSegment = parts[0] || 'login';
  const subSegment = parts[1] || '';

  const users = state.appData?.daftarPengguna || [];

  if (rootSegment === 'login' || rootSegment === 'register') {
    state.currentUser = null;
    localStorage.removeItem('bs_current_user_id');
    state.authTab = rootSegment === 'register' ? 'register' : 'login';
    triggerRender();
    return;
  }

  if (rootSegment === 'nasabah') {
    if (!state.currentUser || state.currentUser.role !== 'NASABAH') {
      const nasabahUser = users.find((u) => u.role === 'NASABAH');
      if (nasabahUser) {
        state.currentUser = nasabahUser;
        state.tarikForm.field3 = nasabahUser.nama;
        localStorage.setItem('bs_current_user_id', nasabahUser.idPengguna);
      }
    }
    const validTabs = ['setor', 'riwayat', 'tarik', 'hadiah', 'profil'];
    state.nasabahTab = validTabs.includes(subSegment) ? subSegment : 'setor';
    syncUrlWithState(true);
    triggerRender();
    return;
  }

  if (rootSegment === 'petugas') {
    if (!state.currentUser || state.currentUser.role !== 'PETUGAS') {
      const petugasUser = users.find((u) => u.role === 'PETUGAS');
      if (petugasUser) {
        state.currentUser = petugasUser;
        localStorage.setItem('bs_current_user_id', petugasUser.idPengguna);
      }
    }
    const validTabs = ['timbang', 'verifikasi', 'riwayat', 'profil'];
    state.petugasTab = validTabs.includes(subSegment) ? subSegment : 'timbang';
    syncUrlWithState(true);
    triggerRender();
    return;
  }

  if (rootSegment === 'admin') {
    if (!state.currentUser || state.currentUser.role !== 'ADMIN') {
      const adminUser = users.find((u) => u.role === 'ADMIN');
      if (adminUser) {
        state.currentUser = adminUser;
        localStorage.setItem('bs_current_user_id', adminUser.idPengguna);
      }
    }
    const validTabs = ['laporan', 'sampah', 'hadiah', 'pengguna', 'erd', 'profil'];
    state.adminTab = validTabs.includes(subSegment) ? subSegment : 'laporan';
    syncUrlWithState(true);
    triggerRender();
    return;
  }

  // Fallback: cek sesi tersimpan atau arahkan ke /login
  const savedId = localStorage.getItem('bs_current_user_id');
  if (savedId) {
    const savedUser = users.find((u) => u.idPengguna === savedId);
    if (savedUser) {
      state.currentUser = savedUser;
      syncUrlWithState(true);
      triggerRender();
      return;
    }
  }

  state.currentUser = null;
  state.authTab = 'login';
  syncUrlWithState(true);
  triggerRender();
}

export async function quickLogin(email, password) {
  try {
    const res = await apiCall('/api/login', 'POST', { email, password });
    state.cart = [];
    if (state.currentUser?.role === 'NASABAH') {
      state.tarikForm.field3 = state.currentUser.nama;
      state.nasabahTab = 'setor';
    } else if (state.currentUser?.role === 'PETUGAS') {
      state.petugasTab = 'timbang';
    } else if (state.currentUser?.role === 'ADMIN') {
      state.adminTab = 'laporan';
    }
    showToast(res.message);
    syncUrlWithState(false);
    triggerRender();
  } catch (err) {
    showToast(err.message, true);
  }
}
