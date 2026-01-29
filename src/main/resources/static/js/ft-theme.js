// tooltip 
document.addEventListener("DOMContentLoaded", function () {
  if (window.bootstrap) {
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
      new bootstrap.Tooltip(el);
    });
  }
});

// ZOOm IMG  
function zoomImage(src) {
  console.log("Zoom demandé pour :", src);
  var modalElement = document.getElementById('imageZoomModal');
  var zoomedImg = document.getElementById('zoomedImage');

  if (zoomedImg && modalElement) {
    zoomedImg.src = src;
    var myModal = new bootstrap.Modal(modalElement);
    myModal.show();
  }
}

// 4. LOADER GLOBAL (upload / soumission) - fonctions globales
function ftShowLoader(message) {
  var loader = document.getElementById('ftLoader');
  if (!loader) return;

  var textEl = document.getElementById('ftLoaderText');
  if (textEl) {
    textEl.textContent = (message && String(message).trim().length > 0)
      ? String(message)
      : 'Traitement en cours...';
  }

  loader.hidden = false;
  loader.setAttribute('aria-hidden', 'false');
}

function ftHideLoader() {
  var loader = document.getElementById('ftLoader');
  if (!loader) return;
  loader.hidden = true;
  loader.setAttribute('aria-hidden', 'true');
}

window.ftShowLoader = ftShowLoader;
window.ftHideLoader = ftHideLoader;

document.addEventListener("DOMContentLoaded", function () {

  // 1. INITIALISATION DES TOOLTIPS (INFO-BULLES)
  if (window.bootstrap && bootstrap.Tooltip) {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (el) {
      return new bootstrap.Tooltip(el, { html: true, interactive: true, sanitize: false });
    });
  }


  // 2. GOUVERNORATS / VILLES
  var govInput = document.getElementById('govInput');
  var villeInput = document.getElementById('villeInput');
  var villeDatalist = document.getElementById('villeList');

  var tunisieData = {
    "Ariana": ["Ariana Ville", "Ettadhamen", "Kalaat El Andalous", "Mnihla", "Raoued", "Sidi Thabet"],
    "Béja": ["Béja Nord", "Béja Sud", "Amdoun", "Goubellat", "Medjez El Bab", "Nefza", "Téboursouk", "Testour", "Thibar"],
    "Ben Arous": ["Ben Arous", "Bou Mhel el-Bassatine", "El Mourouj", "Ezzahra", "Fouchana", "Hammam Chott", "Hammam Lif", "Mohamedia", "Medina Jedida", "Mégrine", "Mornag", "Radès"],
    "Bizerte": ["Bizerte Nord", "Bizerte Sud", "Ghar El Melh", "Mateur", "Menzel Bourguiba", "Menzel Jemil", "Ras Jebel", "Sejnane", "Tinja", "Utique", "Joumine", "Zarzouna"],
    "Gabès": ["Gabès Ville", "Gabès Médina", "Gabès Ouest", "Ghannouch", "Hamma", "Mareth", "Matmata", "Nouvelle Matmata", "Menzel Habib", "Métouia"],
    "Gafsa": ["Gafsa Nord", "Gafsa Sud", "Belkhir", "El Guettar", "El Ksar", "Mdhilla", "Métlaoui", "Moularès", "Redeyef", "Sened", "Sidi Aïch"],
    "Jendouba": ["Jendouba Ville", "Jendouba Nord", "Aïn Draham", "Balta-Bou Aouane", "Bou Salem", "Fernana", "Ghardimaou", "Oued Mliz", "Tabarka"],
    "Kairouan": ["Kairouan Nord", "Kairouan Sud", "Bou Hajla", "Chebika", "Echrarda", "Oueslatia", "Haffouz", "Hajeb El Ayoun", "Nasrallah", "Sbikha"],
    "Kasserine": ["Kasserine Nord", "Kasserine Sud", "Foussana", "Haïdra", "Jedelienne", "Feriana", "Magel Bel Abbès", "Sbeïtla", "Sbiba", "Thala", "Ezzouhour"],
    "Kébili": ["Kébili Nord", "Kébili Sud", "Douz Nord", "Douz Sud", "Faouar", "Souk Lahad"],
    "Le Kef": ["Le Kef Est", "Le Kef Ouest", "Dahmani", "Jérissa", "Kalâat Khasba", "Kalaat Senan", "Nebeur", "Sakiet Sidi Youssef", "Tajerouine", "Touiref"],
    "Mahdia": ["Mahdia", "Bou Merdes", "Chebba", "Chorbane", "El Jem", "Essouassi", "Hebira", "Ksour Essef", "Melloulèche", "Ouled Chamekh", "Sidi Alouane"],
    "La Manouba": ["La Manouba", "Borj El Amri", "Douar Hicher", "El Batan", "Mornaguia", "Oued Ellil", "Tebourba", "Jedeida"],
    "Médenine": ["Médenine Nord", "Médenine Sud", "Ben Guerdane", "Beni Khedache", "Houmt Souk (Djerba)", "Midoun (Djerba)", "Ajim (Djerba)", "Sidi Makhlouf", "Zarzis"],
    "Monastir": ["Monastir", "Bekalta", "Bembla", "Beni Hassen", "Jemmel", "Ksar Hellal", "Ksibet el-Médiouni", "Moknine", "Ouerdanine", "Sahline", "Sayada-Lamta-Bou Hajar", "Téboulba", "Zéramdine"],
    "Nabeul": ["Nabeul", "Béni Khiar", "Béni Khalled", "Bou Argoub", "Dar Chaâbane el-Fehri", "El Haouaria", "El Mida", "Grombalia", "Hammam Ghezèze", "Hammamet", "Kélibia", "Korba", "Menzel Bouzelfa", "Menzel Temime", "Soliman", "Takelsa"],
    "Sfax": ["Sfax Ville", "Sfax Ouest", "Sfax Sud", "Agareb", "Bir Ali Ben Khalifa", "El Amra", "El Hencha", "Graïba", "Jebiniana", "Kerkennah", "Mahres", "Menzel Chaker", "Sakiet Eddaïer", "Sakiet Ezzit", "Skhira"],
    "Sidi Bouzid": ["Sidi Bouzid Est", "Sidi Bouzid Ouest", "Bir El Hafey", "Cebbala Ouled Asker", "Jilma", "Menzel Bouzaiane", "Meknassy", "Mezzouna", "Ouled Haffouz", "Regueb", "Sidi Ali Ben Aoun"],
    "Siliana": ["Siliana Nord", "Siliana Sud", "Bargou", "Bou Arada", "Gaâfour", "Kesra", "Le Krib", "Makthar", "Rouhia", "Sidi Bou Rouis"],
    "Sousse": ["Sousse Ville", "Sousse Jawhara", "Sousse Riadh", "Sousse Sidi Abdelhamid", "Akouda", "Bouficha", "Enfidha", "Hammam Sousse", "Kalaâ Kebira", "Kalaâ Seghira", "Kondar", "M'saken", "Sidi Bou Ali", "Sidi El Hani", "Hergla"],
    "Tataouine": ["Tataouine Nord", "Tataouine Sud", "Bir Lahmar", "Dehiba", "Ghomrassen", "Remada", "Smâr"],
    "Tozeur": ["Tozeur", "Degache", "Hazoua", "Nefta", "Tamaghza"],
    "Tunis": ["Carthage", "La Medina", "Bab Bhar", "Bab Souika", "Omrane", "Omrane Supérieur", "Ettahrir", "El Menzah", "Cité El Khadra", "Le Bardo", "Sidi El Béchir", "La Goulette", "Le Kram", "La Marsa", "Sidi Hassine"],
    "Zaghouan": ["Zaghouan", "Bir Mcherga", "El Fahs", "Nadhour", "Saouaf", "Zriba"]
  };

  if (govInput) {
    govInput.addEventListener('change', function () {
      var selectedGov = this.value;
      if (villeDatalist) villeDatalist.innerHTML = '';
      if (villeInput) villeInput.value = '';

      if (tunisieData[selectedGov]) {
        tunisieData[selectedGov].forEach(function (ville) {
          var option = document.createElement('option');
          option.value = ville;
          villeDatalist.appendChild(option);
        });
      }
    });
  }

  // 3. VALIDATION UX
  document.querySelectorAll("form.ft-validate").forEach(function (form) {
    form.addEventListener("submit", function () {
      form.classList.add("ft-validated");
    }, false);

    form.querySelectorAll("input, textarea, select").forEach(function (el) {
      el.addEventListener("blur", function () {
        el.classList.add("ft-touched");
        el.classList.toggle("ft-invalid", !el.checkValidity());
      });
      el.addEventListener("input", function () {
        if (!el.classList.contains("ft-touched")) return;
        el.classList.toggle("ft-invalid", !el.checkValidity());
      });
    });
  });

  // 4. LOADER sur submit (un seul handler, pas de doublon)
  document.querySelectorAll('form[data-ft-loader="on-submit"]').forEach(function (form) {
    // reset au chargement
    delete form.dataset.ftSubmitting;

    form.addEventListener('submit', function (e) {
      if (form.dataset.ftSubmitting === '1') {
        e.preventDefault();
        return;
      }

      if (typeof form.checkValidity === 'function' && !form.checkValidity()) {
        return;
      }

      e.preventDefault();

      var msg = form.getAttribute('data-ft-loader-text');
      ftShowLoader(msg || 'Traitement en cours...');

      form.dataset.ftSubmitting = '1';

      window.requestAnimationFrame(function () {
        setTimeout(function () {
          HTMLFormElement.prototype.submit.call(form);
        }, 80);
      });

      // sécurité : reset si la navigation n'a pas lieu (rare)
      setTimeout(function () {
        delete form.dataset.ftSubmitting;
      }, 10000);
    });
  });

  // 5. Ajout automatique de l'astérisque rouge pour les champs required
  document.querySelectorAll('.ef-field').forEach(function (field) {
    var input = field.querySelector('input[required], select[required], textarea[required]');
    if (!input) return;

    var labelRow = field.querySelector('.ef-label-row');
    if (!labelRow) return;

    if (labelRow.querySelector('.ef-required-star')) return;

    var label = labelRow.querySelector('label.ef-mini-label');
    if (!label) return;

    var star = document.createElement('span');
    star.className = 'ef-required-star';
    star.setAttribute('aria-hidden', 'true');
    star.textContent = '*';

    var helpBtn = labelRow.querySelector('.ef-help-btn');
    if (helpBtn) {
      labelRow.insertBefore(star, helpBtn);
    } else {
      labelRow.appendChild(star);
    }
  });

});

// 6. Toggle password (œil) - robuste + anti-double-binding
(function () {
  // Si le fichier est chargé 2 fois, on ne bind qu'une seule fois
  if (window.__ftTogglePasswordBound) return;
  window.__ftTogglePasswordBound = true;

  function findClosestToggleButton(el) {
    while (el && el !== document) {
      if (el.matches && el.matches('[data-ft-toggle-password]')) return el;
      el = el.parentNode;
    }
    return null;
  }

  document.addEventListener('click', function (e) {
    var btn = e.target.closest
      ? e.target.closest('[data-ft-toggle-password]')
      : findClosestToggleButton(e.target);

    if (!btn) return;

    e.preventDefault();
    e.stopPropagation();

    var selector = btn.getAttribute('data-ft-toggle-password');
    if (!selector) return;

    var input = document.querySelector(selector);
    if (!input) return;

    var isPassword = (input.type === 'password');

    // Toggle type
    input.type = isPassword ? 'text' : 'password';

    input.focus({ preventScroll: true });

    var icon = btn.querySelector('i');
    if (icon) {
      icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
    }

    // Debug
    console.log("TOGGLE OK", selector, input.type, input.value);
  }, true);

})();

// Personne Morale ou Physique
(function () {
    function el(id){ return document.getElementById(id); }

    function applyFormeJuridique() {
      var morale = document.getElementById("fjMorale");
      var physique = document.getElementById("fjPhysique");

      var blocRS = el("blocRaisonSociale");
      var blocNP = el("blocNomPrenom");

      var rs = el("raisonSocialeInput");
      var nom = el("nomInput");
      var prenom = el("prenomInput");

      var isPhysique = physique && physique.checked;

      if (isPhysique) {
        blocRS.classList.add("d-none");
        blocNP.classList.remove("d-none");

        if (rs) rs.required = false;
        if (nom) nom.required = true;
        if (prenom) prenom.required = true;
      } else {
        blocRS.classList.remove("d-none");
        blocNP.classList.add("d-none");

        if (rs) rs.required = true;
        if (nom) nom.required = false;
        if (prenom) prenom.required = false;
      }

      // Marquage visuel des cartes sélectionnées
      document.querySelectorAll(".ef-fj-card").forEach(function(card){
        card.classList.remove("border-primary", "shadow-sm");
      });
      var checked = document.querySelector('input[name="formeJuridique"]:checked');
      if (checked) {
        var wrap = checked.closest("label") ? checked.closest("label").querySelector(".ef-fj-card") : null;
        if (wrap) wrap.classList.add("border-primary", "shadow-sm");
      }
    }

    document.addEventListener("DOMContentLoaded", function () {
      var morale = document.getElementById("fjMorale");
      var physique = document.getElementById("fjPhysique");

      if (morale) morale.addEventListener("change", applyFormeJuridique);
      if (physique) physique.addEventListener("change", applyFormeJuridique);

      // Premier rendu (si déjà sélectionné via données wizard)
      applyFormeJuridique();
    });
  })();
  
  // Copie automatique des champs Représentant légal -> Administrateur principal
    // Robuste : compatible Thymeleaf th:field (ID ou name)
    (function () {
      var cb = document.getElementById('copyRespToAdmin');
      if (!cb) return;

      function getField(suffix) {
        // th:field="*{nomRespLegal}" -> id/name possibles: nomRespLegal, wiz_nomRespLegal
        return document.getElementById(suffix)
            || document.getElementById('wiz_' + suffix)
            || document.querySelector('[name$=".' + suffix + '"]')
            || document.querySelector('[name="' + suffix + '"]');
      }

      var map = [
      	  { from: 'nomRespLegal', to: 'nomAdminPrincipal' },
      	  { from: 'prenomRespLegal', to: 'prenomAdminPrincipal' },
      	  { from: 'telRespLegal', to: 'telAdminPrincipal' },
      	  { from: 'emailRespLegal', to: 'emailAdminPrincipal' },
      	  { from: 'cinRespLegal', to: 'cinAdminPrincipal' }
      	];


      function copyNow() {
        map.forEach(function (m) {
          var f = getField(m.from);
          var t = getField(m.to);
          if (f && t) t.value = f.value || '';
        });
      }

      function bindLive() {
        map.forEach(function (m) {
          var f = getField(m.from);
          var t = getField(m.to);
          if (!f || !t) return;
          f.addEventListener('input', function () {
            if (cb.checked) t.value = f.value || '';
          });
        });
      }

      cb.addEventListener('change', function () {
        if (cb.checked) copyNow();
      });

      // binding live pour éviter copie “one shot”
      bindLive();
    })();
  // disparition du  message alert auto après 15secondes
  setTimeout(function () {
    document.querySelectorAll('.alert').forEach(function (el) {
      var alert = bootstrap.Alert.getOrCreateInstance(el);
      alert.close();
    });
  }, 15000);
  
  
  
