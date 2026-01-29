(function () {
  function el(id) { return document.getElementById(id); }
  function normTags(str) {
    if (!str) return [];
    return str.split(/\s+/).map(function (s) { return s.trim().toLowerCase(); }).filter(Boolean);
  }

  var qcBtn = el("qcBtn");
  var qcResetBtn = el("qcResetBtn");

  function setAlert(type) {
    var alert = el("qcAlert");
    alert.classList.remove("alert-success", "alert-warning", "alert-info", "alert-danger");
    alert.classList.add(type);
  }

  function showResult(type, title, text) {
    el("qcResult").style.display = "block";
    setAlert(type);
    el("qcTitle").textContent = title;
    el("qcText").textContent = text;

    el("qcLegal").textContent =
      "Références : notes communes 02/2026 (Ministère des Finances / D.G.E.L.F) et textes de loi en vigueur. " 
  }

  function getProfileTags() {
    var activity = el("qcActivity").value;
    var clients  = el("qcClients").value;
    var status   = el("qcStatus").value;
    var sector   = el("qcSector").value;
    var mode     = el("qcMode").value;

    var tags = ["general", "legal", "ttn"];

    if (activity === "services") tags.push("services");
    if (activity === "goods") tags.push("goods");
    if (activity === "mixed") tags.push("services", "goods");

    if (clients === "public") tags.push("public");
    if (clients === "b2b") tags.push("b2b");
    if (clients === "b2c") tags.push("b2c");
    if (clients === "mixed") tags.push("b2b", "b2c");

    if (status === "ready") tags.push("ready");
    if (status === "missing") tags.push("pieces", "dossier");
    if (status === "already") tags.push("process");

    if (sector === "meds") tags.push("meds");
    if (sector === "hydro") tags.push("hydro");
    if (sector === "both") tags.push("meds", "hydro");
    if (sector === "retail") tags.push("retail");

    if (mode === "web") tags.push("web");
    if (mode === "edi") tags.push("edi");
    if (mode === "unknown") tags.push("mode");

    return tags;
  }

  function highlightFaq(profileTags) {
    var faqSection = document.querySelector(".ef-faq");
    if (!faqSection) return;

    var items = faqSection.querySelectorAll(".ef-faq-item");
    if (!items || !items.length) return;

    var profileSet = {};
    profileTags.forEach(function (t) { profileSet[t] = true; });

    var matchedCount = 0;

    items.forEach(function (item) {
      var itemTags = normTags(item.getAttribute("data-faq-tags"));
      var match = false;

      // Match si au moins 1 tag commun
      for (var i = 0; i < itemTags.length; i++) {
        if (profileSet[itemTags[i]]) {
          match = true;
          break;
        }
      }

      item.classList.remove("ef-faq-match", "ef-faq-dim");
      if (match) {
        item.classList.add("ef-faq-match");
        matchedCount++;
      } else {
        item.classList.add("ef-faq-dim");
      }
    });

    el("qcFaqHint").style.display = matchedCount > 0 ? "block" : "none";

    // Option : scroller vers la FAQ après calcul
    // faqSection.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  function compute() {
    var required = ["qcActivity", "qcClients", "qcStatus", "qcSector", "qcMode"];
    for (var i = 0; i < required.length; i++) {
      if (!el(required[i]).value) {
        showResult("alert-warning",
          "Merci de compléter toutes les réponses",
          "Choisissez une option pour chaque question pour obtenir un résultat plus précis.");
        return;
      }
    }

    var activity = el("qcActivity").value;
    var clients  = el("qcClients").value;
    var status   = el("qcStatus").value;
    var sector   = el("qcSector").value;

    var profileTags = getProfileTags();

    // Logique “pratique” (orientation) :
    // - Services + (public ou B2B ou mixte) => fortement concerné
    // - Public => fortement concerné
    // - Médicaments/hydro B2B => concerné (selon périmètre)
    // - B2C pur => souvent à vérifier (cas particuliers)

    if (clients === "public") {
      if (status === "ready") {
        showResult("alert-success",
          "Vous êtes très probablement concerné : inscrivez-vous maintenant",
          "Si vous facturez le secteur public, l’inscription et l’activation vous éviteront les blocages lors de l’émission.");
      } else if (status === "missing") {
        showResult("alert-info",
          "Vous êtes très probablement concerné : commencez la demande",
          "Vous pouvez démarrer l’inscription et compléter les pièces manquantes ensuite.");
      } else {
        showResult("alert-success",
          "Continuez votre demande",
          "Finalisez votre dossier pour activer votre accès.");
      }
      highlightFaq(profileTags);
      return;
    }

    if (sector === "meds" || sector === "hydro" || sector === "both") {
      showResult("alert-success",
        "Vous êtes probablement concerné pour ces opérations",
        "Les secteurs médicaments/hydrocarbures (entre professionnels) sont traités spécifiquement. Inscrivez-vous pour sécuriser la conformité de vos émissions.");
      highlightFaq(profileTags);
      return;
    }

    if (sector === "retail") {
      showResult("alert-warning",
        "Cas à vérifier : activité de détail",
        "Certaines obligations diffèrent selon que vous vendez au détail ou entre professionnels. Vérifiez votre cas dans les textes et les notes communes.");
      highlightFaq(profileTags);
      return;
    }

    if (activity === "services" || activity === "mixed") {
      if (clients === "b2b" || clients === "mixed") {
        if (status === "ready") {
          showResult("alert-success",
            "Vous êtes très probablement concerné : inscrivez-vous",
            "L’extension aux prestations de services implique de préparer votre adhésion et votre mode d’émission.");
        } else if (status === "missing") {
          showResult("alert-info",
            "Vous êtes très probablement concerné : commencez la demande",
            "Vous pouvez commencer aujourd’hui et compléter les pièces plus tard.");
        } else {
          showResult("alert-success",
            "Continuez votre demande",
            "Finalisez votre dossier pour activer votre accès.");
        }
        highlightFaq(profileTags);
        return;
      }
    }

    if (clients === "b2c") {
      showResult("alert-warning",
        "Vous êtes peut-être concerné selon vos opérations",
        "Si vous facturez surtout des particuliers, certaines opérations peuvent ne pas être concernées. Vérifiez votre cas au regard des textes et notes communes.");
      highlightFaq(profileTags);
      return;
    }

    showResult("alert-warning",
      "Cas particulier : vérification recommandée",
      "Votre situation nécessite une vérification (nature des opérations, type de clients, obligations applicables). Vous pouvez démarrer la demande et demander de l’assistance.");
    highlightFaq(profileTags);
  }

  
//  reset du formulaire quand on ferme le modal 
//Aide rapide : suis-je concerne ?

  function reset() {
    el("qcActivity").value = "";
    el("qcClients").value = "";
    el("qcStatus").value = "";
    el("qcSector").value = "";
    el("qcMode").value = "";
    el("qcResult").style.display = "none";
    el("qcFaqHint").style.display = "none";

    var faqSection = document.querySelector(".ef-faq");
    if (!faqSection) return;
    var items = faqSection.querySelectorAll(".ef-faq-item");
    items.forEach(function (item) {
      item.classList.remove("ef-faq-match", "ef-faq-dim");
    });
  }

  qcBtn.addEventListener("click", compute);
  qcResetBtn.addEventListener("click", reset);
})();


(function () {
  var modalEl = document.getElementById('efQuickCheckModal');
  if (!modalEl) return;

  modalEl.addEventListener('hidden.bs.modal', function () {
    var resetBtn = document.getElementById('qcResetBtn');
    if (resetBtn) resetBtn.click();
  });
})();


// Quick Check Modal

(function () {
  var modalEl = document.getElementById('efQuickCheckModal');
  var opener = document.getElementById('qcStartBtn');
  if (!modalEl || !opener) return;

  // Quand le modal est complètement fermé, on remet le focus sur le bouton qui l’a ouvert
  modalEl.addEventListener('hidden.bs.modal', function () {
    opener.focus();
  });

  // Optionnel : reset du formulaire à la fermeture (comme tu voulais)
  modalEl.addEventListener('hidden.bs.modal', function () {
    var resetBtn = document.getElementById('qcResetBtn');
    if (resetBtn) resetBtn.click();
  });
})();
 

