package tn.tn.elfatoora.web;

import java.util.UUID;

import tn.tn.elfatoora.model.CanalEdi;

public class AdhesionWizard {
    public String draftId;     // UUID string stable
    public String dossierRef;  // référence métier générée à soumission
    public String statut;
    
    private UUID draftIdUUID;

   
 
	// Entreprise
    public String formeJuridique;
    public String raisonSociale;
    public String nomPersonnePhysique;
    public String prenomPersonnePhysique;
     
    public String registreCommerce;
    public String matriculeFiscal;
    public String codeTVA;
    public String codeCategorie;
    public String etabSecondaire;
    public String secteurActivite;
    public String adresse;
    public String codePostal;
    public String gouvernorat;
    public String ville;
    public String telephone;
    public String emailGeneral;
    public String emailFacturation;

    // Responsables
    public String nomRespLegal;
    public String prenomRespLegal;
    public String fonctionRespLegal;
    public String telRespLegal;
    public String emailRespLegal;
    public String cinRespLegal;

    public String nomAdminPrincipal;
    public String prenomAdminPrincipal;
    public String cinAdminPrincipal;
    public String telAdminPrincipal;
    public String emailAdminPrincipal;

    
    public String typeSignatureElfAdh;
    // Connexion
    public String modeConnexion;
    public Integer nombreComptes;
    public CanalEdi canalEdi;
    public String ipFixe;
    public String nomRespTechniqueEdi;
    public String prenomRespTechniqueEdi;
	public String cinRespTechniqueEdi;

    public String emailRespTechniqueEdi;
    public String telRespTechniqueEdi;

    // Consentements
    public boolean accepteContrat;
    public boolean conserveOriginaux;

    // Signature
    public boolean signatureOk;
    public String hash;
    public String horodatage;
    public String certSerial;
    public String certAuthority;
    public String certExpiry;
    
    public Integer toErp;
  
    
	public UUID getDraftIdUUID() {
		return draftIdUUID;
	}
	public void setDraftIdUUID(UUID draftIdUUID) {
		this.draftIdUUID = draftIdUUID;
	}
	public String getDraftId() {
		return draftId;
	}
	public void setDraftId(String draftId) {
		this.draftId = draftId;
	}
	
		
	public String getFormeJuridique() {
		return formeJuridique;
	}
	public void setFormeJuridique(String formeJuridique) {
		this.formeJuridique = formeJuridique;
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
	
	
	public String getSecteurActivite() { return secteurActivite; }
	public void setSecteurActivite(String secteurActivite) { this.secteurActivite = secteurActivite; }

	
	public String getDossierRef() {
		return dossierRef;
	}
	public void setDossierRef(String dossierRef) {
		this.dossierRef = dossierRef;
	}
	public String getStatut() {
		return statut;
	}
	public void setStatut(String statut) {
		this.statut = statut;
	}
	public String getRaisonSociale() {
		return raisonSociale;
	}
	public void setRaisonSociale(String raisonSociale) {
		this.raisonSociale = raisonSociale;
	}
	public String getRegistreCommerce() {
		return registreCommerce;
	}
	public void setRegistreCommerce(String registreCommerce) {
		this.registreCommerce = registreCommerce;
	}
	public String getMatriculeFiscal() {
		return matriculeFiscal;
	}
	public void setMatriculeFiscal(String matriculeFiscal) {
		this.matriculeFiscal = matriculeFiscal;
	}
	public String getCodeTVA() {
		return codeTVA;
	}
	public void setCodeTVA(String codeTVA) {
		this.codeTVA = codeTVA;
	}
	public String getCodeCategorie() {
		return codeCategorie;
	}
	public void setCodeCategorie(String codeCategorie) {
		this.codeCategorie = codeCategorie;
	}
	public String getEtabSecondaire() {
		return etabSecondaire;
	}
	public void setEtabSecondaire(String etabSecondaire) {
		this.etabSecondaire = etabSecondaire;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	public String getCodePostal() {
		return codePostal;
	}
	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}
	public String getGouvernorat() {
		return gouvernorat;
	}
	public void setGouvernorat(String gouvernorat) {
		this.gouvernorat = gouvernorat;
	}
	public String getVille() {
		return ville;
	}
	public void setVille(String ville) {
		this.ville = ville;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getEmailGeneral() {
		return emailGeneral;
	}
	public void setEmailGeneral(String emailGeneral) {
		this.emailGeneral = emailGeneral;
	}
	public String getEmailFacturation() {
		return emailFacturation;
	}
	public void setEmailFacturation(String emailFacturation) {
		this.emailFacturation = emailFacturation;
	}
	public String getNomRespLegal() {
		return nomRespLegal;
	}
	public void setNomRespLegal(String nomRespLegal) {
		this.nomRespLegal = nomRespLegal;
	}
	
	
	public String getPrenomRespLegal() {
		return prenomRespLegal;
	}
	public void setPrenomRespLegal(String prenomRespLegal) {
		this.prenomRespLegal = prenomRespLegal;
	}
	public String getFonctionRespLegal() {
		return fonctionRespLegal;
	}
	public void setFonctionRespLegal(String fonctionRespLegal) {
		this.fonctionRespLegal = fonctionRespLegal;
	}
	public String getTelRespLegal() {
		return telRespLegal;
	}
	public void setTelRespLegal(String telRespLegal) {
		this.telRespLegal = telRespLegal;
	}
	public String getEmailRespLegal() {
		return emailRespLegal;
	}
	public void setEmailRespLegal(String emailRespLegal) {
		this.emailRespLegal = emailRespLegal;
	}
	public String getCinRespLegal() {
		return cinRespLegal;
	}
	public void setCinRespLegal(String cinRespLegal) {
		this.cinRespLegal = cinRespLegal;
	}
	public String getNomAdminPrincipal() {
		return nomAdminPrincipal;
	}
	public void setNomAdminPrincipal(String nomAdminPrincipal) {
		this.nomAdminPrincipal = nomAdminPrincipal;
	}
	
	public String getPrenomAdminPrincipal() {
		return prenomAdminPrincipal;
	}
	public void setPrenomAdminPrincipal(String prenomAdminPrincipal) {
		this.prenomAdminPrincipal = prenomAdminPrincipal;
	}
	public String getCinAdminPrincipal() {
		return cinAdminPrincipal;
	}
	public void setCinAdminPrincipal(String cinAdminPrincipal) {
		this.cinAdminPrincipal = cinAdminPrincipal;
	}
	public String getTelAdminPrincipal() {
		return telAdminPrincipal;
	}
	public void setTelAdminPrincipal(String telAdminPrincipal) {
		this.telAdminPrincipal = telAdminPrincipal;
	}
	public String getEmailAdminPrincipal() {
		return emailAdminPrincipal;
	}
	public void setEmailAdminPrincipal(String emailAdminPrincipal) {
		this.emailAdminPrincipal = emailAdminPrincipal;
	}
	public String getTypeSignatureElfAdh() {
		return typeSignatureElfAdh;
	}
	public void setTypeSignatureElfAdh(String typeSignatureElfAdh) {
		this.typeSignatureElfAdh = typeSignatureElfAdh;
	}
	public String getModeConnexion() {
		return modeConnexion;
	}
	public void setModeConnexion(String modeConnexion) {
		this.modeConnexion = modeConnexion;
	}
	public Integer getNombreComptes() {
		return nombreComptes;
	}
	public void setNombreComptes(Integer nombreComptes) {
		this.nombreComptes = nombreComptes;
	}
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
	
	public String getCinRespTechniqueEdi() {
		return cinRespTechniqueEdi;
	}
	public void setCinRespTechniqueEdi(String cinRespTechniqueEdi) {
		this.cinRespTechniqueEdi = cinRespTechniqueEdi;
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
	public boolean isAccepteContrat() {
		return accepteContrat;
	}
	public void setAccepteContrat(boolean accepteContrat) {
		this.accepteContrat = accepteContrat;
	}
	public boolean isConserveOriginaux() {
		return conserveOriginaux;
	}
	public void setConserveOriginaux(boolean conserveOriginaux) {
		this.conserveOriginaux = conserveOriginaux;
	}
	public boolean isSignatureOk() {
		return signatureOk;
	}
	public void setSignatureOk(boolean signatureOk) {
		this.signatureOk = signatureOk;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getHorodatage() {
		return horodatage;
	}
	public void setHorodatage(String horodatage) {
		this.horodatage = horodatage;
	}
	public String getCertSerial() {
		return certSerial;
	}
	public void setCertSerial(String certSerial) {
		this.certSerial = certSerial;
	}
	public String getCertAuthority() {
		return certAuthority;
	}
	public void setCertAuthority(String certAuthority) {
		this.certAuthority = certAuthority;
	}
	public String getCertExpiry() {
		return certExpiry;
	}
	public void setCertExpiry(String certExpiry) {
		this.certExpiry = certExpiry;
	}
	public Integer getToErp() {
		return toErp;
	}
	public void setToErp(Integer toErp) {
		this.toErp = toErp;
	}
    
    
}
