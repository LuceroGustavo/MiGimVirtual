/**
 * MiGymVirtual — confirmaciones de salida en móvil (≤991px, entorno 2) sin cambiar
 * lógica de servidor. Estilo: modal Bootstrap + modal-confirmar-header (Mattfuncional).
 */
(function () {
  'use strict';

  var MQ = '(max-width: 991px)';

  function isMobile() {
    return typeof window.matchMedia !== 'undefined' && window.matchMedia(MQ).matches;
  }

  function sameDestination(href) {
    try {
      var u = new URL(href, window.location.origin);
      return u.pathname === window.location.pathname && u.search === window.location.search;
    } catch (e) {
      return false;
    }
  }

  function showModal(title, message, onConfirm) {
    var m = document.getElementById('modalMgvConfirmSalida');
    var titleEl = document.getElementById('modalMgvConfirmSalidaTitulo');
    var bodyEl = document.getElementById('modalMgvConfirmSalidaBody');
    var btnOk = document.getElementById('modalMgvConfirmSalidaBtn');
    if (!m || !titleEl || !bodyEl || !btnOk) {
      if (window.confirm(message || title || '¿Continuar?')) onConfirm();
      return;
    }
    if (typeof bootstrap === 'undefined' || !bootstrap.Modal) {
      if (window.confirm(message || title)) onConfirm();
      return;
    }

    titleEl.innerHTML = '<i class="fas fa-question-circle me-2"></i>' + (title || 'Confirmar');
    bodyEl.textContent = message || '';

    try {
      var modalInst = bootstrap.Modal.getOrCreateInstance(m);
      var newBtn = btnOk.cloneNode(true);
      btnOk.parentNode.replaceChild(newBtn, btnOk);

      newBtn.addEventListener('click', function () {
        modalInst.hide();
        if (typeof onConfirm === 'function') {
          setTimeout(onConfirm, 0);
        }
      });

      modalInst.show();
    } catch (err) {
      if (window.confirm(message || title)) onConfirm();
    }
  }

  function init() {
    if (window.__mgvSalidaMovilInit) return;
    window.__mgvSalidaMovilInit = true;

    function bindLogout() {
      document.querySelectorAll('a.btn-logout-mobile[href*="logout"]').forEach(function (a) {
        a.addEventListener('click', function (e) {
          e.preventDefault();
          var href = a.getAttribute('href');
          showModal(
            'Cerrar sesión',
            '¿Está seguro que desea cerrar sesión?',
            function () {
              window.location.href = href;
            }
          );
        });
      });
    }

    function bindBottomNav() {
      document.querySelectorAll('#bottomNavMobile a.bottom-nav-item[href]').forEach(function (a) {
        a.addEventListener('click', function (e) {
          if (!isMobile()) return;
          var href = a.getAttribute('href');
          if (!href || href === '#') return;
          if (sameDestination(href)) return;
          var kind = a.getAttribute('data-mgv-salida');
          if (!kind) return;
          e.preventDefault();
          var map = {
            publico: [
              'Ir a página pública',
              '¿Ir a la página de inicio? Saldrá del panel del profesor.',
            ],
            manual: [
              'Manual de usuario',
              '¿Abrir el manual de usuario?',
            ],
            consultas: [
              'Consultas',
              '¿Ir a la sección de consultas y página pública? (Administración).',
            ],
            mas: [
              'Administración',
              '¿Ir a Administración del sistema?',
            ],
            dashboard: [
              'Panel del profesor',
              '¿Volver al panel del profesor?',
            ],
          };
          var pair = map[kind] || ['Confirmar', '¿Continuar?'];
          showModal(pair[0], pair[1], function () {
            window.location.href = href;
          });
        });
      });
    }

    function bindNavbarEnvelope() {
      document.querySelectorAll('a.navbar-envelope-mobile[data-mgv-salida="consultas-nav"][href]').forEach(function (a) {
        a.addEventListener('click', function (e) {
          var href = a.getAttribute('href');
          if (!href || sameDestination(href)) return;
          e.preventDefault();
          showModal(
            'Consultas recibidas',
            '¿Ir a la página pública y consultas?',
            function () {
              window.location.href = href;
            }
          );
        });
      });
    }

    /** Botón atrás del sistema (Android): pedir confirmación antes de salir del panel. Solo móvil. */
    function bindBackGuard() {
      if (!window.location.pathname.startsWith('/profesor')) return;
      if (!isMobile()) return;

      var popHandler = function () {
        if (!isMobile()) return;
        history.pushState({ mgvPanel: 1 }, '', location.href);
        showModal(
          'Salir del panel',
          '¿Seguro que desea salir del panel? Podrá volver a iniciar sesión después.',
          function () {
            window.removeEventListener('popstate', popHandler);
            try {
              if (history.length > 2) {
                history.go(-2);
              } else {
                window.location.href = '/login';
              }
            } catch (err) {
              window.location.href = '/login';
            }
          }
        );
      };

      history.pushState({ mgvPanel: 1 }, '', location.href);
      window.addEventListener('popstate', popHandler);
    }

    bindLogout();
    bindBottomNav();
    bindNavbarEnvelope();
    bindBackGuard();
  }

  function runWhenReady() {
    if (typeof bootstrap !== 'undefined') {
      init();
      return;
    }
    var n = 0;
    var t = setInterval(function () {
      n++;
      if (typeof bootstrap !== 'undefined') {
        clearInterval(t);
        init();
      } else if (n > 80) {
        clearInterval(t);
        init();
      }
    }, 50);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', runWhenReady);
  } else {
    runWhenReady();
  }
})();
