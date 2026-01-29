/*
 * Adhesion - Etape 2
 * Copie rapide: Représentant légal -> Administrateur principal
 */

(function () {
  'use strict';

  function byName(name) {
    return document.querySelector('[name="' + name + '"]');
  }

  function getValue(el) {
    return el ? (el.value || '') : '';
  }

  function setValue(el, val) {
    if (!el) return;
    el.value = (val == null) ? '' : String(val);
    try {
      el.dispatchEvent(new Event('input', { bubbles: true }));
      el.dispatchEvent(new Event('blur', { bubbles: true }));
    } catch (e) {
      // ignore
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    var chk = document.getElementById('copyRespToAdmin');

    if (!chk) return;

    var adminBackup = null;

    function snapshotAdmin() {
      adminBackup = {
        nom: getValue(byName('nomAdminPrincipal')),
        cin: getValue(byName('cinAdminPrincipal')),
        tel: getValue(byName('telAdminPrincipal')),
        email: getValue(byName('emailAdminPrincipal'))
      };
    }

    function restoreAdmin() {
      if (!adminBackup) return;
      setValue(byName('nomAdminPrincipal'), adminBackup.nom);
      setValue(byName('cinAdminPrincipal'), adminBackup.cin);
      setValue(byName('telAdminPrincipal'), adminBackup.tel);
      setValue(byName('emailAdminPrincipal'), adminBackup.email);
    }

    function copyRespToAdmin() {
      var nomResp = getValue(byName('nomRespLegal'));
      var telResp = getValue(byName('telRespLegal'));
      var emailResp = getValue(byName('emailRespLegal'));
      var cinResp = getValue(byName('cinRespLegal'));

      setValue(byName('nomAdminPrincipal'), nomResp);
      setValue(byName('telAdminPrincipal'), telResp);
	  setValue(byName('emailAdminPrincipal'), emailResp);
	  setValue(byName('cinAdminPrincipal'), cinResp);
  
    }

    function handleCopy() {
      if (!adminBackup) snapshotAdmin();
      copyRespToAdmin();
    }


    if (chk) {
      chk.addEventListener('change', function () {
        if (chk.checked) {
          handleCopy();
        } else {
          restoreAdmin();
        }
      });
    }
  });
})();
