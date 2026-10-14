/* LinguaVerse prototype shell — 纯静态原型公共脚本（无依赖） */
(function () {
  const NAV = [
    { key: 'home', label: '今日', icon: '✦', href: 'home.html' },
    { key: 'courses', label: '课程', icon: '⛰', href: 'courses.html' },
    { key: 'review', label: '复习', icon: '◍', href: 'review.html' },
    { key: 'community', label: '社区', icon: '✉', href: 'community.html' },
    { key: 'achievements', label: '成就', icon: '★', href: 'achievements.html' },
    { key: 'dashboard', label: '我的', icon: '☻', href: 'dashboard.html' },
  ];

  const PHASE = { m: ['MVP', 'phase-m'], 1: ['P1', 'phase-1'], 2: ['P2', 'phase-2'] };

  function topbar(active) {
    const links = NAV.map(n =>
      `<a href="${n.href}" class="${n.key === active ? 'active' : ''}"><span>${n.icon}</span>${n.label}</a>`).join('');
    return `
    <header class="topbar">
      <div class="wrap topbar-inner">
        <a class="brand" href="home.html"><span class="brand-mark">L</span>LinguaVerse</a>
        <nav class="nav">
          ${links}
        </nav>
        <a class="row gap-sm" href="profile.html" style="margin-left:auto">
          <span class="avatar">林</span>
        </a>
      </div>
    </header>
    <nav class="mobile-tabbar">
      ${NAV.map(n => `<a href="${n.href}" class="${n.key === active ? 'active' : ''}"><span>${n.icon}</span>${n.label}</a>`).join('')}
    </nav>`;
  }

  function adminSide(active) {
    const items = [
      { key: 'dash', label: '数据看板', href: 'admin-dashboard.html' },
      { key: 'users', label: '用户管理', href: 'admin-users.html' },
      { key: 'courses', label: '课程内容', href: 'admin-courses.html' },
      { key: 'mod', label: '内容审核', href: 'admin-moderation.html' },
    ];
    return `
    <aside class="admin-side">
      <div class="brand" style="color:#fff;margin:6px 10px 22px;font-size:18px"><span class="brand-mark">L</span>LV 后台</div>
      ${items.map(i => `<a href="${i.href}" class="${i.key === active ? 'active' : ''}">${i.label}</a>`).join('')}
      <div style="position:absolute;bottom:18px;left:14px;right:14px"><a href="../index.html">← 返回原型索引</a></div>
    </aside>`;
  }

  document.addEventListener('DOMContentLoaded', function () {
    const body = document.body;
    if (body.dataset.shell === 'app') {
      const active = body.dataset.nav;
      const header = $toHTML(`<div>${topbar(active)}</div>`);
      const tabbar = $toHTML(`<div>${topbar(active)}</div>`).querySelector('.mobile-tabbar');
      body.prepend(header.querySelector('.topbar'));
      body.append(tabbar);
    }
    if (body.dataset.shell === 'admin') {
      const mount = document.createElement('div');
      mount.innerHTML = adminSide(body.dataset.nav);
      body.prepend(mount.firstElementChild);
      body.classList.add('admin-body');
    }
    const phase = body.dataset.phase;
    if (phase && PHASE[phase]) {
      const el = document.createElement('div');
      el.className = 'phase ' + PHASE[phase][1];
      el.textContent = PHASE[phase][0] + ' · 原型';
      body.append(el);
    }
    bindInteractions();
  });

  function $toHTML(str) { const t = document.createElement('template'); t.innerHTML = str.trim(); return t.content.firstChild; }

  function toast(msg) {
    let t = document.querySelector('.toast');
    if (!t) { t = document.createElement('div'); t.className = 'toast'; document.body.append(t); }
    t.textContent = msg; t.style.display = 'block';
    clearTimeout(t._h); t._h = setTimeout(() => (t.style.display = 'none'), 1800);
  }
  window.lvToast = toast;

  function bindInteractions() {
    document.addEventListener('click', function (e) {
      const flip = e.target.closest('[data-flip]');
      if (flip) { flip.closest('.flip').classList.toggle('flipped'); return; }

      const mark = e.target.closest('[data-mark]');
      if (mark) { toast(mark.dataset.mark); return; }

      const openModal = e.target.closest('[data-modal-open]');
      if (openModal) {
        const m = document.getElementById(openModal.dataset.modalOpen);
        if (m) m.classList.add('open');
        return;
      }
      const closeModal = e.target.closest('[data-modal-close], .modal-mask');
      if (closeModal) {
        const mask = closeModal.classList.contains('modal-mask') && closeModal === e.target ? closeModal : closeModal.closest('.modal-mask');
        if (mask) mask.classList.remove('open');
        return;
      }

      const play = e.target.closest('[data-play]');
      if (play) {
        const on = play.dataset.play === 'on';
        play.dataset.play = on ? '' : 'on';
        play.innerHTML = on ? '▶ 播放' : '❚❚ 播放中…';
        if (!on) setTimeout(() => { play.dataset.play = ''; play.innerHTML = '▶ 播放'; }, 2200);
        return;
      }

      const tab = e.target.closest('[data-tab]');
      if (tab) {
        const group = tab.closest('[data-tabs]');
        if (group) {
          group.querySelectorAll('[data-tab]').forEach(t => t.classList.remove('active'));
          tab.classList.add('active');
          const panelWrap = document.querySelector(group.dataset.tabs || '');
          if (panelWrap) {
            panelWrap.children && [...panelWrap.children].forEach((p, i) =>
              p.classList.toggle('hide', i !== +tab.dataset.tab));
          }
        }
      }
    });

    /* 闪卡标记后自动下一张（演示） */
    document.addEventListener('click', function (e) {
      const navBtn = e.target.closest('[data-card-nav]');
      if (!navBtn) return;
      const stack = document.querySelector('[data-card-stack]');
      if (!stack) return;
      const dir = navBtn.dataset.cardNav;
      const cur = stack.querySelector('.card-face:not(.hide)');
      const faces = [...stack.querySelectorAll('.card-face')];
      const idx = faces.indexOf(cur);
      const next = faces[Math.max(0, Math.min(faces.length - 1, idx + (dir === 'next' ? 1 : -1)))];
      faces.forEach(f => f.classList.add('hide'));
      next.classList.remove('hide');
      const count = document.querySelector('[data-card-count]');
      if (count) count.textContent = `${faces.indexOf(next) + 1} / ${faces.length}`;
    });
  }
})();
