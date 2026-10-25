/* RaktSetu frontend - plain JavaScript, koi framework nahi.
   Flow: page load -> stats + donors + requests fetch -> HTML cards render -> forms se POST/PUT/DELETE */
(() => {
  'use strict';

  // ---------- Config & constants ----------
  const API_BASE = ((window.APP_CONFIG && window.APP_CONFIG.API_BASE) || '').replace(/\/$/, '');
  const GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  const STATES = ['Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi', 'Goa', 'Gujarat', 'Haryana',
    'Himachal Pradesh', 'Jharkhand', 'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya', 'Mizoram',
    'Nagaland', 'Odisha', 'Punjab', 'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura', 'Uttar Pradesh', 'Uttarakhand',
    'West Bengal', 'Jammu and Kashmir', 'Ladakh', 'Chandigarh', 'Puducherry'];

  const state = {
    page: 0,
    size: 9,
    filters: { q: '', bloodGroup: '', city: '', available: '' },
    adminKey: safeGet('adminKey') || ''
  };

  // ---------- Small helpers ----------
  const $ = (sel, root = document) => root.querySelector(sel);
  const esc = (s) => String(s ?? '').replace(/[&<>"']/g,
    (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const debounce = (fn, ms = 350) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };
  const fmtDate = (iso) => new Date(iso + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  const todayISO = () => { const d = new Date(); d.setMinutes(d.getMinutes() - d.getTimezoneOffset()); return d.toISOString().slice(0, 10); };

  function safeGet(k) { try { return sessionStorage.getItem(k); } catch { return null; } }
  function safeSet(k, v) { try { sessionStorage.setItem(k, v); } catch { /* ignore */ } }
  function safeDel(k) { try { sessionStorage.removeItem(k); } catch { /* ignore */ } }

  function toast(msg, type = 'error') {
    const el = document.createElement('div');
    el.className = 'toast ' + type;
    el.textContent = msg;
    $('#toasts').appendChild(el);
    setTimeout(() => el.remove(), 4500);
  }

  class ApiError extends Error {
    constructor(status, data) { super(data && data.message || 'Error'); this.status = status; this.data = data || {}; }
  }

  async function api(path, { method = 'GET', body, admin = false, key } = {}) {
    const headers = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (admin) headers['X-Admin-Key'] = key ?? state.adminKey;
    const res = await fetch(API_BASE + path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
    if (!res.ok) {
      let data = {};
      try { data = await res.json(); } catch { /* body nahi tha */ }
      throw new ApiError(res.status, data);
    }
    return res.status === 204 ? null : res.json();
  }

  function showError(err) {
    if (err instanceof ApiError) {
      if (err.data.errors) {
        toast(Object.values(err.data.errors).join(' • '));
      } else {
        toast(err.message);
      }
    } else {
      toast('Could not connect to the server. Please check your internet and that the backend is running.');
    }
  }

  // Admin action 401 de to auto logout
  async function adminAction(fn) {
    try { return await fn(); } catch (err) {
      if (err instanceof ApiError && err.status === 401) { logoutAdmin(); toast('Admin session expired. Please log in again.'); }
      else showError(err);
      return undefined;
    }
  }

  // ---------- Form templates ----------
  const groupOptions = (placeholder) =>
    (placeholder ? `<option value="">${placeholder}</option>` : '') + GROUPS.map((g) => `<option value="${g}">${g}</option>`).join('');

  function donorFormHTML({ edit = false } = {}) {
    return `
      ${edit ? '<h3>Edit donor details</h3>' : ''}
      <div class="grid-2">
        <label>Full name *<input name="fullName" required maxlength="100"></label>
        <label>Blood group *<select name="bloodGroup" required>${groupOptions('Select')}</select></label>
      </div>
      <div class="grid-2">
        <label>Age (18-65) *<input name="age" type="number" min="18" max="65" required></label>
        <label>Gender *
          <select name="gender" required>
            <option value="">Select</option><option>Male</option><option>Female</option><option>Other</option>
          </select>
        </label>
      </div>
      <div class="grid-2">
        <label>Mobile number *<input name="phone" inputmode="numeric" placeholder="10 digits" required></label>
        <label>Email (optional)<input name="email" type="email" maxlength="120"></label>
      </div>
      <div class="grid-2">
        <label>City *<input name="city" required maxlength="80"></label>
        <label>State *<input name="state" list="stateList" required maxlength="80"></label>
      </div>
      <label>Last blood donation date (if any)<input name="lastDonationDate" type="date" max="${todayISO()}"></label>
      <label>Anything else to add (optional)<textarea name="notes" maxlength="300" placeholder="e.g. also available at night"></textarea></label>
      <label class="check"><input type="checkbox" name="available" checked> I am currently available to donate blood</label>
      ${edit ? '' : '<label class="check"><input type="checkbox" name="consent" required> I agree that my details (name, phone number, city) may be shown publicly in this directory.</label>'}
      <div class="actions">
        ${edit ? '<button type="button" class="btn btn-outline" data-close>Cancel</button>' : ''}
        <button class="btn btn-primary" type="submit">${edit ? 'Save Changes' : 'Register'}</button>
      </div>`;
  }

  function readDonorForm(form) {
    const f = new FormData(form);
    const val = (n) => String(f.get(n) ?? '').trim();
    return {
      fullName: val('fullName'),
      bloodGroup: val('bloodGroup'),
      age: Number(val('age')),
      gender: val('gender'),
      phone: val('phone').replace(/\D/g, '').slice(-10), // +91 / spaces hata do
      email: val('email') || null,
      city: val('city'),
      state: val('state'),
      lastDonationDate: val('lastDonationDate') || null,
      available: form.elements.available.checked,
      notes: val('notes') || null
    };
  }

  function fillForm(form, data) {
    Object.entries(data).forEach(([k, v]) => {
      const el = form.elements[k];
      if (!el || el instanceof RadioNodeList) return;
      if (el.type === 'checkbox') el.checked = !!v; else el.value = v ?? '';
    });
  }

  // ---------- Stats & chips ----------
  async function loadStats() {
    try {
      const s = await api('/api/donors/stats');
      $('#statTotal').textContent = s.totalDonors;
      $('#statAvailable').textContent = s.availableDonors;
      $('#statOpen').textContent = s.openRequests;
      $('#groupChips').innerHTML = GROUPS.map((g) => `
        <button type="button" class="chip ${state.filters.bloodGroup === g ? 'active' : ''}" data-group="${g}">
          <b>${g}</b><small>${s.byBloodGroup[g] ?? 0} donors</small>
        </button>`).join('');
    } catch (err) { showError(err); }
  }

  // ---------- Donors list ----------
  function donorCard(d) {
    let badge = '<span class="badge ok">Available</span>';
    if (!d.available) badge = '<span class="badge off">Currently unavailable</span>';
    else if (!d.eligible) badge = '<span class="badge rest">Rest period</span>';

    const last = d.lastDonationDate ? 'Last donation: ' + fmtDate(d.lastDonationDate) : 'First-time donor';
    return `
      <article class="card">
        <div class="card-top">
          <div class="avatar">${esc(d.bloodGroup)}</div>
          <div>
            <h3>${esc(d.fullName)}</h3>
            <p class="meta">📍 ${esc(d.city)}, ${esc(d.state)}</p>
          </div>
        </div>
        <div>${badge}</div>
        <p class="meta">${esc(d.age)} yrs • ${esc(d.gender)}<br>${esc(last)}</p>
        ${d.notes ? `<p class="notes">“${esc(d.notes)}”</p>` : ''}
        <div class="card-actions">
          <a class="btn btn-primary btn-sm" href="tel:+91${esc(d.phone)}">📞 Call</a>
          <a class="btn btn-outline btn-sm" href="https://wa.me/91${esc(d.phone)}" target="_blank" rel="noopener">WhatsApp</a>
          <button class="btn btn-outline btn-sm admin-only" data-edit="${d.id}" type="button">Edit</button>
          <button class="btn btn-danger btn-sm admin-only" data-del="${d.id}" type="button">Delete</button>
        </div>
      </article>`;
  }

  async function loadDonors() {
    const grid = $('#donorGrid');
    grid.innerHTML = '<div class="empty">Loading…</div>';
    const params = new URLSearchParams({ page: state.page, size: state.size });
    Object.entries(state.filters).forEach(([k, v]) => { if (v) params.set(k, v); }); // URLSearchParams '+' ko %2B bana deta hai
    try {
      const res = await api('/api/donors?' + params);
      grid.innerHTML = res.content.length
        ? res.content.map(donorCard).join('')
        : '<div class="empty">No donors found. Try changing the filters, or register yourself.</div>';
      renderPager(res);
    } catch (err) {
      grid.innerHTML = '<div class="empty">Could not load donors. Is the backend running?</div>';
      showError(err);
    }
  }

  function renderPager(p) {
    $('#pager').innerHTML = p.totalPages > 1 ? `
      <button type="button" data-page="${p.page - 1}" ${p.page === 0 ? 'disabled' : ''}>← Previous</button>
      <span>Page ${p.page + 1} / ${p.totalPages}</span>
      <button type="button" data-page="${p.page + 1}" ${p.page + 1 >= p.totalPages ? 'disabled' : ''}>Next →</button>` : '';
  }

  // ---------- Requests ----------
  function requestCard(r) {
    const open = r.status === 'Open';
    return `
      <article class="req ${open ? '' : 'fulfilled'}">
        <div class="req-top">
          <h4><span class="bg-tag">${esc(r.bloodGroup)}</span>${esc(r.patientName)}</h4>
          <span class="badge ${esc(r.urgency.toLowerCase())}">${esc(r.urgency)}</span>
        </div>
        <p class="meta">🏥 ${esc(r.hospital)}, ${esc(r.city)}<br>
          ${esc(r.units)} unit(s) • needed by ${esc(fmtDate(r.requiredDate))} • Status: <b>${esc(r.status)}</b></p>
        <div class="card-actions">
          ${open ? `<a class="btn btn-primary btn-sm" href="tel:+91${esc(r.contactPhone)}">📞 ${esc(r.contactPhone)}</a>` : ''}
          ${open ? `<button class="btn btn-outline btn-sm admin-only" data-fulfil="${r.id}" type="button">Mark fulfilled</button>` : ''}
          <button class="btn btn-danger btn-sm admin-only" data-delreq="${r.id}" type="button">Delete</button>
        </div>
      </article>`;
  }

  async function loadRequests() {
    const box = $('#requestGrid');
    try {
      const list = await api('/api/requests?status=' + encodeURIComponent($('#reqStatus').value));
      box.innerHTML = list.length ? list.map(requestCard).join('') : '<div class="empty">No requests right now.</div>';
    } catch (err) {
      box.innerHTML = '<div class="empty">Could not load requests.</div>';
      showError(err);
    }
  }

  // ---------- Admin ----------
  function setAdminUI(on) {
    document.body.classList.toggle('is-admin', on);
    $('#adminBtn').textContent = on ? 'Admin Logout' : 'Admin Login';
  }

  function logoutAdmin() {
    state.adminKey = '';
    safeDel('adminKey');
    setAdminUI(false);
  }

  // ---------- Wire up events ----------
  function init() {
    // State suggestions + forms
    document.body.insertAdjacentHTML('beforeend',
      `<datalist id="stateList">${STATES.map((s) => `<option value="${s}">`).join('')}</datalist>`);

    $('#registerForm').innerHTML = donorFormHTML();
    $('#editForm').innerHTML = donorFormHTML({ edit: true });
    $('#filterForm').elements.bloodGroup.innerHTML = groupOptions('All blood groups');
    $('#requestForm').elements.bloodGroup.innerHTML = groupOptions('Select');
    $('#requestForm').elements.requiredDate.min = todayISO();

    // Mobile menu
    $('#menuBtn').addEventListener('click', () => {
      const nav = $('#navLinks');
      const open = nav.classList.toggle('open');
      $('#menuBtn').setAttribute('aria-expanded', String(open));
    });
    $('#navLinks').addEventListener('click', (e) => { if (e.target.tagName === 'A') $('#navLinks').classList.remove('open'); });

    // Filters
    const filterForm = $('#filterForm');
    const applyFilters = () => {
      state.filters = {
        q: filterForm.elements.q.value.trim(),
        bloodGroup: filterForm.elements.bloodGroup.value,
        city: filterForm.elements.city.value.trim(),
        available: filterForm.elements.available.value
      };
      state.page = 0;
      loadDonors();
      loadStats(); // chip ka active highlight update
    };
    const applyDebounced = debounce(applyFilters);
    filterForm.addEventListener('submit', (e) => { e.preventDefault(); applyFilters(); });
    filterForm.elements.q.addEventListener('input', applyDebounced);
    filterForm.elements.city.addEventListener('input', applyDebounced);
    filterForm.elements.bloodGroup.addEventListener('change', applyFilters);
    filterForm.elements.available.addEventListener('change', applyFilters);
    $('#resetFilters').addEventListener('click', () => { filterForm.reset(); applyFilters(); });

    // Blood group chips
    $('#groupChips').addEventListener('click', (e) => {
      const chip = e.target.closest('[data-group]');
      if (!chip) return;
      filterForm.elements.bloodGroup.value = state.filters.bloodGroup === chip.dataset.group ? '' : chip.dataset.group;
      applyFilters();
      $('#donors').scrollIntoView();
    });

    // Pagination
    $('#pager').addEventListener('click', (e) => {
      const btn = e.target.closest('[data-page]');
      if (!btn || btn.disabled) return;
      state.page = Number(btn.dataset.page);
      loadDonors();
      $('#donors').scrollIntoView();
    });

    // Donor card actions (edit / delete) - event delegation
    $('#donorGrid').addEventListener('click', async (e) => {
      const editBtn = e.target.closest('[data-edit]');
      const delBtn = e.target.closest('[data-del]');
      if (editBtn) {
        try {
          const d = await api('/api/donors/' + editBtn.dataset.edit);
          const form = $('#editForm');
          fillForm(form, d);
          form.dataset.id = d.id;
          $('#editDialog').showModal();
        } catch (err) { showError(err); }
      }
      if (delBtn && confirm('Are you sure you want to delete this donor?')) {
        const done = await adminAction(() => api('/api/donors/' + delBtn.dataset.del, { method: 'DELETE', admin: true }));
        if (done !== undefined) { toast('Donor deleted.', 'success'); loadDonors(); loadStats(); }
      }
    });

    // Register
    const registerForm = $('#registerForm');
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!registerForm.reportValidity()) return;
      const btn = registerForm.querySelector('[type=submit]');
      btn.disabled = true;
      try {
        await api('/api/donors', { method: 'POST', body: readDonorForm(registerForm) });
        toast('Thank you! You are now registered as a donor.', 'success');
        registerForm.reset();
        await Promise.all([loadStats(), loadDonors()]);
        $('#donors').scrollIntoView();
      } catch (err) { showError(err); } finally { btn.disabled = false; }
    });

    // Edit (admin)
    const editForm = $('#editForm');
    editForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!editForm.reportValidity()) return;
      const done = await adminAction(() =>
        api('/api/donors/' + editForm.dataset.id, { method: 'PUT', body: readDonorForm(editForm), admin: true }));
      if (done !== undefined) {
        $('#editDialog').close();
        toast('Donor updated.', 'success');
        loadDonors(); loadStats();
      }
    });

    // Requests
    $('#reqStatus').addEventListener('change', loadRequests);
    const requestForm = $('#requestForm');
    requestForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!requestForm.reportValidity()) return;
      const f = new FormData(requestForm);
      const val = (n) => String(f.get(n) ?? '').trim();
      const body = {
        patientName: val('patientName'),
        bloodGroup: val('bloodGroup'),
        units: Number(val('units')),
        hospital: val('hospital'),
        city: val('city'),
        contactPhone: val('contactPhone').replace(/\D/g, '').slice(-10),
        urgency: val('urgency'),
        requiredDate: val('requiredDate')
      };
      const btn = requestForm.querySelector('[type=submit]');
      btn.disabled = true;
      try {
        await api('/api/requests', { method: 'POST', body });
        toast('Request posted.', 'success');
        requestForm.reset();
        $('#reqStatus').value = 'Open';
        await Promise.all([loadRequests(), loadStats()]);
      } catch (err) { showError(err); } finally { btn.disabled = false; }
    });

    $('#requestGrid').addEventListener('click', async (e) => {
      const fulfil = e.target.closest('[data-fulfil]');
      const del = e.target.closest('[data-delreq]');
      if (fulfil) {
        const done = await adminAction(() => api(`/api/requests/${fulfil.dataset.fulfil}/fulfil`, { method: 'PATCH', admin: true }));
        if (done !== undefined) { toast('Request marked as fulfilled.', 'success'); loadRequests(); loadStats(); }
      }
      if (del && confirm('Delete this request?')) {
        const done = await adminAction(() => api('/api/requests/' + del.dataset.delreq, { method: 'DELETE', admin: true }));
        if (done !== undefined) { toast('Request deleted.', 'success'); loadRequests(); loadStats(); }
      }
    });

    // Dialog close buttons
    document.addEventListener('click', (e) => {
      const close = e.target.closest('[data-close]');
      if (close) close.closest('dialog').close();
    });

    // Admin login/logout
    $('#adminBtn').addEventListener('click', () => {
      if (state.adminKey) { logoutAdmin(); toast('Admin logged out.', 'success'); }
      else { $('#adminForm').reset(); $('#adminDialog').showModal(); }
    });
    $('#adminForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const key = $('#adminForm').elements.key.value;
      try {
        await api('/api/admin/verify', { admin: true, key });
        state.adminKey = key;
        safeSet('adminKey', key);
        setAdminUI(true);
        $('#adminDialog').close();
        toast('Admin logged in.', 'success');
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) toast('Incorrect admin key.'); else showError(err);
      }
    });

    // Pehli baar data lao
    setAdminUI(!!state.adminKey);
    loadStats();
    loadDonors();
    loadRequests();
  }

  document.addEventListener('DOMContentLoaded', init);
})();
