package tn.tn.elfatoora.entity;

import tn.tn.elfatoora.model.AdhesionStatut;
import tn.tn.elfatoora.model.CanalEdi;

import java.io.Serializable;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="adhesion_dossier", indexes = {
        @Index(name="idx_dossier_ref", columnList="dossier_ref", unique = true),
        @Index(name="idx_dossier_statut", columnList="statut")
})
public class AdhesionDossier implements  Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="draft_id", nullable=false, unique = true)
    private UUID draftId;

    @Column(name="dossier_ref", length=64, unique = true)
    private String dossierRef; // visible client, générée à soumission

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private AdhesionStatut statut = AdhesionStatut.DRAFT;

    

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;
    // =========================
    // Entreprise (nullable en DRAFT)
    // =========================
    @Column(name="forme_juridique") // MORALE/PHYSIQUE
    private String formeJuridique;
    
    @Column(name="secteur_activite") // 
    public String secteurActivite;
    
    @Column(name="raison_sociale")
    private String raisonSociale;
    
    @Column(name="nom_personne_physique")
    private String nomPersonnePhysique;
    
    @Column(name="prenom_personne_physique")
    private String prenomPersonnePhysique;

    @Column(name="registre_commerce")
    private String registreCommerce;

    @Column(name="matricule_fiscal")
    private String matriculeFiscal;

    @Column(name="codetva")
    private String codeTVA;

    @Column(name="code_categorie")
    private String codeCategorie;

    @Column(name="etab_secondaire")
    private String etabSecondaire;

    @Column(columnDefinition="text")
    private String adresse;

    @Column(name="code_postal")
    private String codePostal;

    private String gouvernorat;
    private String ville;
    private String telephone;

    @Column(name="email_general")
    private String emailGeneral;

    @Column(name="email_facturation")
    private String emailFacturation;

    // =========================
    // Responsables (nullable en DRAFT)
    // =========================
    @Column(name="nom_resp_legal")
    private String nomRespLegal;
    
    @Column(name="prenom_resp_legal")
    private String prenomRespLegal;

    @Column(name="fonction_resp_legal")
    private String fonctionRespLegal;

    @Column(name="tel_resp_legal")
    private String telRespLegal;

    @Column(name="email_resp_legal")
    private String emailRespLegal;

    @Column(name="cin_resp_legal")
    private String cinRespLegal;

    @Column(name="nom_admin_principal")
    private String nomAdminPrincipal;
    
    @Column(name="prenom_admin_principal")
    private String prenomAdminPrincipal;
    

    @Column(name="cin_admin_principal")
    private String cinAdminPrincipal;

    @Column(name="tel_admin_principal")
    private String telAdminPrincipal;

    @Column(name="email_admin_principal")
    private String emailAdminPrincipal;

    // =========================
    // Connexion (nullable en DRAFT)
    // =========================
    @Column(name="type_signature_elf_adh")
    private String typeSignatureElfAdh;   // ELECTRONIQUE/MANUSCRITE

    @Column(name="mode_connexion", length = 50)
    private String modeConnexion;   // WEB/EDI

    @Column(name="nombre_comptes", nullable=false)
    private Integer nombreComptes = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_edi", length = 50)
    private CanalEdi canalEdi;
    
    @Column(name="ip_fixe")
    private String ipFixe;

    // =========================
    // Responsable Technique EDI
    // =========================
    @Column(name="nom_resp_technique_edi")
    private String nomRespTechniqueEdi;

    @Column(name="prenom_resp_technique_edi")
    private String prenomRespTechniqueEdi;
    
    @Column(name="cin_resp_technique_edi")
    private String cinRespTechniqueEdi;

    @Column(name="email_resp_technique_edi")
    private String emailRespTechniqueEdi;
    
    @Column(name="tel_resp_technique_edi")
    private String telRespTechniqueEdi;

    // =========================
    // Consentements (NOT NULL OK)
    // =========================
    @Column(name="accepte_contrat", nullable=false)
    private boolean accepteContrat = false;

    @Column(name="conserve_originaux", nullable=false)
    private boolean conserveOriginaux = false;

    // =========================
    // Signature meta (nullable sauf bool)
    // =========================
    @Column(name="signature_ok", nullable=false)
    private boolean signatureOk = false;

    @Column(name="cert_serial")
    private String certSerial;

    @Column(name="cert_authority")
    private String certAuthority;

    @Column(name="cert_expiry")
    private String certExpiry;

    @Column(name="signature_hash", length=64)
    private String signatureHash;

    @Column(name="horodatage")
    private String horodatage;
    
    
	@Column(name = "to_erp", nullable = false)
	private Integer toErp = 0;

    // =========================
    // Backoffice
    // =========================
    @Column(name="motif_decision", columnDefinition="text")
    private String motifDecision;

    @Column(name="decided_by")
    private String decidedBy;

    @Column(name="decided_at")
    private LocalDateTime decidedAt;

    // =========================
    // Dates (NOT NULL)
    // =========================
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================
    // Getters/Setters
    // =========================

    public Long getId() { return id; }

    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }
    
    

    public String getFormeJuridique() {
		return formeJuridique;
	}

	public void setFormeJuridique(String formeJuridique) {
		this.formeJuridique = formeJuridique;
	}

	
	public String getSecteurActivite() {
		return secteurActivite;
	}

	public void setSecteurActivite(String secteurActivite) {
		this.secteurActivite = secteurActivite;
	}

	public String getNomPersonnePhysique() {
		return nomPersonnePhysique;
	}

	public void setNomPersonnePhysique(String nomPersonnePhysique) {
		this.nomPersonnePhysique = nomPersonnePhysique;
	}

	public String getPrenomPersonnePhysique() {
		return prenomPersonnePhysique;
	}

	public void setPrenomPersonnePhysique(String prenomPersonnePhysique) {
		this.prenomPersonnePhysique = prenomPersonnePhysique;
	}

	public String getDossierRef() { return dossierRef; }
    public void setDossierRef(String dossierRef) { this.dossierRef = dossierRef; }

    public AdhesionStatut getStatut() { return statut; }
    public void setStatut(AdhesionStatut statut) { this.statut = statut; }

    

    public AppUser getAppUser() { return appUser; }
    public void setAppUser(AppUser appUser) { this.appUser = appUser; }
    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }

    public String getRegistreCommerce() { return registreCommerce; }
    public void setRegistreCommerce(String registreCommerce) { this.registreCommerce = registreCommerce; }

    public String getMatriculeFiscal() { return matriculeFiscal; }
    public void setMatriculeFiscal(String matriculeFiscal) { this.matriculeFiscal = matriculeFiscal; }

    public String getCodeTVA() { return codeTVA; }
    public void setCodeTVA(String codeTVA) { this.codeTVA = codeTVA; }

    public String getCodeCategorie() { return codeCategorie; }
    public void setCodeCategorie(String codeCategorie) { this.codeCategorie = codeCategorie; }

    public String getEtabSecondaire() { return etabSecondaire; }
    public void setEtabSecondaire(String etabSecondaire) { this.etabSecondaire = etabSecondaire; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public String getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(String gouvernorat) { this.gouvernorat = gouvernorat; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmailGeneral() { return emailGeneral; }
    public void setEmailGeneral(String emailGeneral) { this.emailGeneral = emailGeneral; }

    public String getEmailFacturation() { return emailFacturation; }
    public void setEmailFacturation(String emailFacturation) { this.emailFacturation = emailFacturation; }

    public String getNomRespLegal() { return nomRespLegal; }
    public void setNomRespLegal(String nomRespLegal) { this.nomRespLegal = nomRespLegal; }

    public String getFonctionRespLegal() { return fonctionRespLegal; }
    public void setFonctionRespLegal(String fonctionRespLegal) { this.fonctionRespLegal = fonctionRespLegal; }

    public String getTelRespLegal() { return telRespLegal; }
    public void setTelRespLegal(String telRespLegal) { this.telRespLegal = telRespLegal; }

    public String getEmailRespLegal() { return emailRespLegal; }
    public void setEmailRespLegal(String emailRespLegal) { this.emailRespLegal = emailRespLegal; }

    public String getCinRespLegal() { return cinRespLegal; }
    public void setCinRespLegal(String cinRespLegal) { this.cinRespLegal = cinRespLegal; }

    public String getNomAdminPrincipal() { return nomAdminPrincipal; }
    public void setNomAdminPrincipal(String nomAdminPrincipal) { this.nomAdminPrincipal = nomAdminPrincipal; }

    public String getCinAdminPrincipal() { return cinAdminPrincipal; }
    public void setCinAdminPrincipal(String cinAdminPrincipal) { this.cinAdminPrincipal = cinAdminPrincipal; }

    public String getTelAdminPrincipal() { return telAdminPrincipal; }
    public void setTelAdminPrincipal(String telAdminPrincipal) { this.telAdminPrincipal = telAdminPrincipal; }

    public String getEmailAdminPrincipal() { return emailAdminPrincipal; }
    public void setEmailAdminPrincipal(String emailAdminPrincipal) { this.emailAdminPrincipal = emailAdminPrincipal; }



    public String getTypeSignatureElfAdh() {
		return typeSignatureElfAdh;
	}

	public void setTypeSignatureElfAdh(String typeSignatureElfAdh) {
		this.typeSignatureElfAdh = typeSignatureElfAdh;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getModeConnexion() { return modeConnexion; }
    public void setModeConnexion(String modeConnexion) { this.modeConnexion = modeConnexion; }

    public Integer getNombreComptes() { return nombreComptes; }
    public void setNombreComptes(Integer nombreComptes) { this.nombreComptes = nombreComptes; }

    public CanalEdi getCanalEdi() { 
        return canalEdi; 
    }

    public void setCanalEdi(CanalEdi canalEdi) { 
        this.canalEdi = canalEdi; 
    }




    public String getIpFixe() {
		return ipFixe;
	}

	public void setIpFixe(String ipFixe) {
		this.ipFixe = ipFixe;
	}

	public String getNomRespTechniqueEdi() {
		return nomRespTechniqueEdi;
	}

	public void setNomRespTechniqueEdi(String nomRespTechniqueEdi) {
		this.nomRespTechniqueEdi = nomRespTechniqueEdi;
	}

	public String getPrenomRespTechniqueEdi() {
		return prenomRespTechniqueEdi;
	}

	public void setPrenomRespTechniqueEdi(String prenomRespTechniqueEdi) {
		this.prenomRespTechniqueEdi = prenomRespTechniqueEdi;
	}

	public String getEmailRespTechniqueEdi() {
		return emailRespTechniqueEdi;
	}

	public void setEmailRespTechniqueEdi(String emailRespTechniqueEdi) {
		this.emailRespTechniqueEdi = emailRespTechniqueEdi;
	}

	public String getTelRespTechniqueEdi() {
		return telRespTechniqueEdi;
	}

	public void setTelRespTechniqueEdi(String telRespTechniqueEdi) {
		this.telRespTechniqueEdi = telRespTechniqueEdi;
	}
	
	

	public String getCinRespTechniqueEdi() {
		return cinRespTechniqueEdi;
	}

	public void setCinRespTechniqueEdi(String cinRespTechniqueEdi) {
		this.cinRespTechniqueEdi = cinRespTechniqueEdi;
	}

	public boolean isAccepteContrat() { return accepteContrat; }
    public void setAccepteContrat(boolean accepteContrat) { this.accepteContrat = accepteContrat; }

    public boolean isConserveOriginaux() { return conserveOriginaux; }
    public void setConserveOriginaux(boolean conserveOriginaux) { this.conserveOriginaux = conserveOriginaux; }

    public boolean isSignatureOk() { return signatureOk; }
    public void setSignatureOk(boolean signatureOk) { this.signatureOk = signatureOk; }

    public String getCertSerial() { return certSerial; }
    public void setCertSerial(String certSerial) { this.certSerial = certSerial; }

    public String getCertAuthority() { return certAuthority; }
    public void setCertAuthority(String certAuthority) { this.certAuthority = certAuthority; }

    public String getCertExpiry() { return certExpiry; }
    public void setCertExpiry(String certExpiry) { this.certExpiry = certExpiry; }

    public String getSignatureHash() { return signatureHash; }
    public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }

    public String getHorodatage() { return horodatage; }
    public void setHorodatage(String horodatage) { this.horodatage = horodatage; }

    public String getMotifDecision() { return motifDecision; }
    public void setMotifDecision(String motifDecision) { this.motifDecision = motifDecision; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

	public String getPrenomRespLegal() {
		return prenomRespLegal;
	}

	public void setPrenomRespLegal(String prenomRespLegal) {
		this.prenomRespLegal = prenomRespLegal;
	}

	public String getPrenomAdminPrincipal() {
		return prenomAdminPrincipal;
	}

	public void setPrenomAdminPrincipal(String prenomAdminPrincipal) {
		this.prenomAdminPrincipal = prenomAdminPrincipal;
	}

	public Integer getToErp() {
		return toErp;
	}

	public void setToErp(Integer toErp) {
		this.toErp = toErp;
	}

 
}
