(function () {
  function csrf() {
    var tokenMeta = document.querySelector('meta[name="_csrf"]');
    var headerMeta = document.querySelector('meta[name="_csrf_header"]');
    return {
      token: tokenMeta ? tokenMeta.getAttribute("content") : null,
      header: headerMeta ? headerMeta.getAttribute("content") : "X-CSRF-TOKEN"
    };
  }

  function apiFetch(url, options) {
    options = options || {};
    options.headers = options.headers || {};
    options.credentials = "same-origin"; // IMPORTANT: garde la session (wiz)

    // Ne force Content-Type que si on a un body (sinon GET inutile)
    if (options.body && !options.headers["Content-Type"]) {
      options.headers["Content-Type"] = "application/json";
    }

    var c = csrf();
    if (c.token) options.headers[c.header] = c.token;

    return fetch(url, options).then(function (r) {
      if (!r.ok) {
        return r.text().then(function (t) {
          throw new Error(t || ("HTTP " + r.status));
        });
      }

      // 204 No Content
      if (r.status === 204) return null;

      var ct = (r.headers.get("content-type") || "");
      if (ct.indexOf("application/json") === -1) {
        return r.text().then(function (t) {
          throw new Error("Réponse non JSON. Extrait: " + t.substring(0, 300));
        });
      }

      // FIX PRINCIPAL: retourner le JSON
      return r.json();
    });
  }

  function el(id) { return document.getElementById(id); }

  function escapeHtml(s) {
    if (s === null || s === undefined) return "";
    return String(s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function badge(desactivated) {
    if (desactivated === 1) {
      return '<span class="badge bg-secondary">Désactivé</span>';
    }
    return '<span class="badge bg-success">Actif</span>';
  }

  function rowClass(desactivated) {
    return desactivated === 1 ? ' class="table-secondary"' : "";
  }

  function render(list) {
    var tbody = el("efSignatairesTbody");
    if (!tbody) return;

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" class="text-muted">Aucun signataire pour le moment.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(function (s) {
      var toggleLabel = s.desactivated === 1 ? "Activer" : "Désactiver";
      var toggleTarget = s.desactivated === 1 ? 0 : 1;

      return (
        '<tr' + rowClass(s.desactivated) + ' data-id="' + s.id + '">' +
          '<td>' + escapeHtml(s.nom) + '</td>' +
          '<td>' + escapeHtml(s.prenom) + '</td>' +
          '<td>' + escapeHtml(s.cin) + '</td>' +
          '<td>' + escapeHtml(s.email) + '</td>' +
          '<td>' + escapeHtml(s.certNumSerie) + '</td>' +
          '<td>' + escapeHtml(s.certDebutValidite) + '</td>' +
          '<td>' + escapeHtml(s.certFinValidite) + '</td>' +
          '<td>' + badge(s.desactivated) + '</td>' +
          '<td class="text-end">' +
            '<button type="button" class="btn btn-sm btn-outline-primary me-2" data-action="edit">Modifier</button>' +
            '<button type="button" class="btn btn-sm btn-outline-danger" data-action="toggle" data-desactivated="' + toggleTarget + '">' + toggleLabel + '</button>' +
          '</td>' +
        '</tr>'
      );
    }).join("");
  }

  function openForm(mode, s) {
    el("efSignataireFormWrap").style.display = "block";
    el("efSignataireFormTitle").textContent = mode === "edit" ? "Modifier signataire" : "Nouveau signataire";

    el("efSigId").value = s && s.id ? s.id : "";
    el("efSigNom").value = s && s.nom ? s.nom : "";
    el("efSigPrenom").value = s && s.prenom ? s.prenom : "";
    el("efSigCin").value = s && s.cin ? s.cin : "";
    el("efSigEmail").value = s && s.email ? s.email : "";
    el("efSigCertSerie").value = s && s.certNumSerie ? s.certNumSerie : "";
    el("efSigCertDebut").value = s && s.certDebutValidite ? s.certDebutValidite : "";
    el("efSigCertFin").value = s && s.certFinValidite ? s.certFinValidite : "";
  }

  function closeForm() {
    el("efSignataireFormWrap").style.display = "none";
    el("efSigId").value = "";
  }

  function readForm() {
    return {
      nom: el("efSigNom").value,
      prenom: el("efSigPrenom").value,
      cin: el("efSigCin").value,
      email: el("efSigEmail").value,
      certNumSerie: el("efSigCertSerie").value,
      certDebutValidite: el("efSigCertDebut").value,
      certFinValidite: el("efSigCertFin").value
    };
  }

  function validate(p) {
    if (!p.nom || !p.nom.trim()) return "Le nom est obligatoire.";
    if (!p.prenom || !p.prenom.trim()) return "Le prénom est obligatoire.";
    if (!p.cin || !p.cin.trim()) return "Le CIN est obligatoire.";
    if (!p.email || !p.email.trim()) return "L'email est obligatoire.";

    // --- contrôle dates ---
    if (p.certDebutValidite && p.certFinValidite) {
      var d1 = new Date(p.certDebutValidite);
      var d2 = new Date(p.certFinValidite);

      if (d2 < d1) {
        return "La date de fin doit être supérieure ou égale à la date de début.";
      }
    }

    return null;
  }

  function load() {
    return apiFetch("/adhesion/signataires", { method: "GET" }).then(function (list) {
      render(list || []);
      return list;
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var modalEl = el("efSignatairesModal");
    if (!modalEl) return;

    modalEl.addEventListener("shown.bs.modal", function () {
      closeForm();
      load().catch(function () {
        el("efSignatairesTbody").innerHTML = '<tr><td colspan="9" class="text-danger">Erreur de chargement.</td></tr>';
      });
    });

    var addBtn = el("efBtnAddSignataire");
    if (addBtn) addBtn.addEventListener("click", function () {
      openForm("add", null);
    });

    var cancelBtn = el("efBtnCancelEdit");
    if (cancelBtn) cancelBtn.addEventListener("click", function () {
      closeForm();
    });

    var saveBtn = el("efBtnSaveSig");
    if (saveBtn) saveBtn.addEventListener("click", function () {
      var id = el("efSigId").value;
      var payload = readForm();
      var err = validate(payload);
      if (err) { alert(err); return; }

      var url = "/adhesion/signataires" + (id ? ("/" + id) : "");
      var method = id ? "PUT" : "POST";

      apiFetch(url, { method: method, body: JSON.stringify(payload) })
        .then(function () {
          closeForm();
          return load();
        })
        .catch(function (e) { alert("Erreur: " + e.message); });
    });

    var tbody = el("efSignatairesTbody");
    tbody.addEventListener("click", function (ev) {
      var btn = ev.target;
      if (!btn || !btn.getAttribute) return;

      var action = btn.getAttribute("data-action");
      if (!action) return;

      var tr = btn.closest("tr");
      var id = tr ? tr.getAttribute("data-id") : null;
      if (!id) return;

      if (action === "edit") {
        apiFetch("/adhesion/signataires", { method: "GET" })
          .then(function (list) {
            list = list || [];
            var s = list.find(function (x) { return String(x.id) === String(id); });
            if (!s) { alert("Signataire introuvable."); return; }
            openForm("edit", s);
          })
          .catch(function (e) { alert("Erreur: " + e.message); });
      }

      if (action === "toggle") {
        var d = btn.getAttribute("data-desactivated");
        apiFetch("/adhesion/signataires/" + id + "/toggle?desactivated=" + encodeURIComponent(d), { method: "POST" })
          .then(function () { return load(); })
          .catch(function (e) { alert("Erreur: " + e.message); });
      }
    });
  });
})();
