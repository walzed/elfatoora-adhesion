(function () {
  function byId(id) { return document.getElementById(id); }

  document.addEventListener("DOMContentLoaded", function () {
    var modalEl = byId("efVideoModal");
    if (!modalEl) return;

    var bsModal = window.bootstrap ? bootstrap.Modal.getOrCreateInstance(modalEl) : null;

    var stagePick = byId("efVideoPick");
    var stagePlay = byId("efVideoPlay");

    var btnPickFr = byId("efPickFr");
    var btnPickAr = byId("efPickAr");
    var btnBack = byId("efVideoBack");

    var video = byId("efVideoPlayer");
    var source = byId("efVideoSource");
    var title = byId("efVideoTitle");

    function showPick() {
      stagePick.style.display = "block";
      stagePlay.style.display = "none";
      title.textContent = "Regarder la vidéo";
      stopVideo();
    }

    function showPlay(lang) {
      stagePick.style.display = "none";
      stagePlay.style.display = "block";

      if (lang === "fr") {
        title.textContent = "El Fatoora - Facture électronique";
        source.src = "/videos/fatoora-video-fr.mp4";
        stagePlay.classList.remove("ef-rtl");
      } else {
        title.textContent = "الفاتورة الإلكترونية";
        source.src = "/videos/fatoora-video-ar.mp4";
        stagePlay.classList.add("ef-rtl");
      }

      video.load();
      video.play().catch(function () {
        // si autoplay bloqué, l'utilisateur clique Play
      });
    }

    function stopVideo() {
      if (!video) return;
      try {
        video.pause();
        video.currentTime = 0;
      } catch (e) {}
      if (source) source.removeAttribute("src");
      if (video) video.load();
    }

    // Ouverture modal : on revient à l'écran de choix
    modalEl.addEventListener("show.bs.modal", function () {
      showPick();
    });

    // Fermeture modal : stop + reset
    modalEl.addEventListener("hidden.bs.modal", function () {
      showPick();
    });

    if (btnPickFr) btnPickFr.addEventListener("click", function () { showPlay("fr"); });
    if (btnPickAr) btnPickAr.addEventListener("click", function () { showPlay("ar"); });
    if (btnBack) btnBack.addEventListener("click", function () { showPick(); });

    // Expose une fonction optionnelle si tu veux ouvrir via JS
    window.efOpenVideoModal = function () {
      if (bsModal) bsModal.show();
    };
  });
})();
