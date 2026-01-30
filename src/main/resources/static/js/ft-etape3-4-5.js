 // Ajoute/retire la classe is-checked sur le conteneur, pour refléter l'état (radio visible)
  (function () {
    function refresh() {
      document.querySelectorAll('[data-radio-tile]').forEach(function(tile){
        var input = tile.querySelector('input[type="radio"]');
        if (input && input.checked) tile.classList.add('is-checked');
        else tile.classList.remove('is-checked');
      });
    }
    document.addEventListener('change', function(e){
      if (e.target && e.target.matches('input[type="radio"]')) refresh();
    });
    refresh();
  })();

(function () {
  var info = document.getElementById("signManuInfo");
  if (!info) return;

  function getSelectedType() {
    var checked = document.querySelector('input[name="typeSignatureElfAdh"]:checked');
    return checked ? checked.value : null;
  }

  function refreshInfo() {
    var t = getSelectedType();
    info.style.display = (t === "MANUSCRITE") ? "" : "none";
  }

  var radios = document.querySelectorAll('input[name="typeSignatureElfAdh"]');
  for (var i = 0; i < radios.length; i++) {
    radios[i].addEventListener("change", refreshInfo);
  }

  refreshInfo();
})();

////**** */
  (function () {
    var ediInfo = document.getElementById('ediInfo');
    var ediFields = document.getElementById('ediFields');
    var canalEdiSelect = document.getElementById('canalEdiSelect');

    function toggleEdi() {
      var ediSelected = document.getElementById('mc2') && document.getElementById('mc2').checked;
      if (!ediInfo || !ediFields) return;

      if (ediSelected) {
        ediInfo.classList.remove('d-none');
        ediFields.classList.remove('d-none');
        if (canalEdiSelect) canalEdiSelect.setAttribute('required', 'required');
      } else {
        ediInfo.classList.add('d-none');
        ediFields.classList.add('d-none');
        if (canalEdiSelect) {
          canalEdiSelect.removeAttribute('required');
          canalEdiSelect.value = "";
        }
      }
    }

    document.addEventListener('change', function (e) {
      if (e.target && e.target.matches('input[type="radio"][name$="modeConnexion"]')) {
        toggleEdi();
      }
    });

    toggleEdi();
  })();
 
  ///****
  //  Copier Admin principal -> Responsable technique EDI (checkbox)
  (function () {
    var cb = document.getElementById('sameAsAdminEdi');
    if (!cb) return;

    var adminNom = document.getElementById('adminNomHidden');
    var adminPrenom = document.getElementById('adminPrenomHidden');
    var adminCin = document.getElementById('adminCinHidden');
    var adminEmail = document.getElementById('adminEmailHidden');
    var adminTel = document.getElementById('adminTelHidden');

    var techNom = document.getElementById('nomRespTechniqueEdiInput');
    var techPrenom = document.getElementById('prenomRespTechniqueEdiInput');
    var techCin = document.getElementById('cinRespTechniqueEdiInput');
    var techEmail = document.getElementById('emailRespTechniqueEdiInput');
    var techTel = document.getElementById('telRespTechniqueEdiInput');

    var prev = { nom:"", prenom:"", cin:"", email:"", tel:"" };

    function copyFromAdmin() {
      if (techNom) techNom.value = (adminNom && adminNom.value) ? adminNom.value : "";
      if (techPrenom) techPrenom.value = (adminPrenom && adminPrenom.value) ? adminPrenom.value : "";
      if (techCin) techCin.value = (adminCin && adminCin.value) ? adminCin.value : "";
      if (techEmail) techEmail.value = (adminEmail && adminEmail.value) ? adminEmail.value : "";
      if (techTel) techTel.value = (adminTel && adminTel.value) ? adminTel.value : "";

      // Prénom admin n’existe pas dans le wizard : on laisse tel quel
      if (techPrenom && !techPrenom.value) techPrenom.value = "";
    }

    function savePrev() {
      prev.nom = techNom ? (techNom.value || "") : "";
      prev.prenom = techPrenom ? (techPrenom.value || "") : "";
      prev.cin = techCin ? (techCin.value || "") : "";
      prev.email = techEmail ? (techEmail.value || "") : "";
      prev.tel = techTel ? (techTel.value || "") : "";
    }

    function restorePrev() {
      if (techNom) techNom.value = prev.nom;
      if (techPrenom) techPrenom.value = prev.prenom;
      if (techCin) techCin.value = prev.cin;
      if (techEmail) techEmail.value = prev.email;
      if (techTel) techTel.value = prev.tel;
    }

    cb.addEventListener('change', function () {
      if (cb.checked) {
        savePrev();
        copyFromAdmin();
      } else {
        restorePrev();
      }
    });
  })();
  
  //// etape 4
  function updatePreview(input) {
  			const card = input.closest('.doc-card');
  			if (!card)
  				return;
  			const preview = card.querySelector('.preview-info');
  			if (!preview)
  				return;

  			if (input.files && input.files[0]) {
  				const f = input.files[0];
  				const fileName = f.name;
  				const fileSize = (f.size / 1024).toFixed(2);

  				preview.innerHTML = '<div class="d-flex justify-content-between align-items-center">'
  						+ '<span><i class="fa-solid fa-file-circle-plus me-2 text-primary"></i>'
  						+ '<strong>Prêt à envoyer :</strong> '
  						+ fileName
  						+ '</span>'
  						+ '<span class="badge bg-secondary">'
  						+ fileSize + ' Ko</span>' + '</div>';

  				preview.style.display = 'block';
  				preview.classList.remove('animate__fadeOut');
  				preview.classList.add('animate__fadeIn');
  			}
  		}

  		function toggleUploadSection() {
  			var sec = document.getElementById('uploadSection');
  			var btn = document.getElementById('btnToggleUpload');
  			if (!sec)
  				return;

  			var currentlyHidden = (sec.style.display === 'none');
  			if (currentlyHidden) {
  				sec.style.display = 'block';
  				if (btn)
  					btn.textContent = 'Masquer';
  				sec.scrollIntoView({
  					behavior : 'smooth',
  					block : 'start'
  				});
  			} else {
  				sec.style.display = 'none';
  				if (btn)
  					btn.textContent = 'Remplacer Document(s)';
  			}
  		}
		
/// etape 5
(function () {
  var cb1 = document.getElementById("accepteContrat");
  var cb2 = document.getElementById("conserveOriginaux");
  var btn = document.getElementById("btnNext");

  if (!cb1 || !cb2 || !btn) return;

  var tile1 = document.getElementById("tileContrat");
  var tile2 = document.getElementById("tileOriginaux");

  function refresh() {
    var ok = cb1.checked && cb2.checked;
    btn.disabled = !ok;
    if (tile1) tile1.classList.remove("accept-error");
    if (tile2) tile2.classList.remove("accept-error");
  }

  cb1.addEventListener("change", refresh);
  cb2.addEventListener("change", refresh);
  refresh();

  var err = document.getElementById("pageError");
  if (err) {
    setTimeout(function () {
      var z = document.getElementById("acceptZone");
      if (z && z.scrollIntoView) {
        z.scrollIntoView({ behavior: "smooth", block: "center" });
      }
      if (!cb1.checked && tile1) tile1.classList.add("accept-error");
      if (!cb2.checked && tile2) tile2.classList.add("accept-error");
    }, 50);
  }
})();		

// --- Validation taille fichier (Etape 4) ---
// Note: MAX_FILE_SIZE_STR et MAX_SIZE_BYTES doivent être définis globalement dans la page HTML
// car ils dépendent de la configuration serveur injectée par Thymeleaf.
function validateAndPreview(input) {
    // On vérifie si les constantes sont définies, sinon on utilise une valeur par défaut (1MB)
    const maxSize = (typeof MAX_SIZE_BYTES !== 'undefined') ? MAX_SIZE_BYTES : 1048576;
    const maxStr = (typeof MAX_FILE_SIZE_STR !== 'undefined') ? MAX_FILE_SIZE_STR : '1MB';

    if (input.files && input.files[0]) {
        const file = input.files[0];

        // Check size
        if (file.size > maxSize) {
            alert("Le fichier est trop volumineux (" + (file.size / 1024 / 1024).toFixed(2) + " MB). La taille maximum autorisée est de " + maxStr);
            input.value = ""; // Reset input

            // Masquer la preview si elle était affichée
            const card = input.closest('.doc-card');
            if (card) {
                const preview = card.querySelector('.preview-info');
                if (preview) {
                    preview.style.display = 'none';
                }
            }
            return;
        }

        // Call original updatePreview
        updatePreview(input);
    }
}
