
package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "adhesion_signataire", indexes = { @Index(name = "idx_sig_draft_id", columnList = "draft_id"),
		@Index(name = "idx_sig_mf", columnList = "matricule_fiscale"), @Index(name = "idx_sig_cin", columnList = "cin"),
		@Index(name = "idx_sig_desactivated", columnList = "desactivated"),
		@Index(name = "idx_sig_draft_mf_cin", columnList = "draft_id, matricule_fiscale, cin") })
public class AdhesionSignataire {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Draft id (colonne simple utilisée pour les filtres métier)
	 */
	@Column(name = "draft_id", nullable = true)
	private UUID draftId;

	/**
	 * Relation lecture seule vers le dossier Basée sur la même colonne draft_id
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(
	    name = "draft_id",
	    referencedColumnName = "draft_id",
	    insertable = false,
	    updatable = false
	)
	private AdhesionDossier dossier;

	@Column(name = "matricule_fiscale", length = 32)
	private String matriculeFiscale;

	@Column(name = "cin", length = 32)
	private String cin;

	@Column(name = "rc_rne", length = 64)
	private String rcRne;

	@Column(name = "email", length = 190)
	private String email;

	@Column(name = "nom", length = 120)
	private String nom;

	@Column(name = "prenom", length = 120)
	private String prenom;

	@Column(name = "cert_num_serie", columnDefinition = "text")
	private String certNumSerie;

	@Column(name = "cert_debut_validite")
	private LocalDate certDebutValidite;

	@Column(name = "cert_fin_validite")
	private LocalDate certFinValidite;

	@Column(name = "desactivated", nullable = false)
	private Integer desactivated = 0;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt = OffsetDateTime.now();

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	/*
	 * ========================== Getters / Setters ==========================
	 */

	public Long getId() {
		return id;
	}

	public UUID getDraftId() {
		return draftId;
	}

	public void setDraftId(UUID draftId) {
		this.draftId = draftId;
	}

	public AdhesionDossier getDossier() {
		return dossier;
	}

	public void setDossier(AdhesionDossier dossier) {
		this.dossier = dossier;
	}

	public String getMatriculeFiscale() {
		return matriculeFiscale;
	}

	public void setMatriculeFiscale(String matriculeFiscale) {
		this.matriculeFiscale = matriculeFiscale;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getRcRne() {
		return rcRne;
	}

	public void setRcRne(String rcRne) {
		this.rcRne = rcRne;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getCertNumSerie() {
		return certNumSerie;
	}

	public void setCertNumSerie(String certNumSerie) {
		this.certNumSerie = certNumSerie;
	}

	public LocalDate getCertDebutValidite() {
		return certDebutValidite;
	}

	public void setCertDebutValidite(LocalDate certDebutValidite) {
		this.certDebutValidite = certDebutValidite;
	}

	public LocalDate getCertFinValidite() {
		return certFinValidite;
	}

	public void setCertFinValidite(LocalDate certFinValidite) {
		this.certFinValidite = certFinValidite;
	}

	public Integer getDesactivated() {
		return desactivated;
	}

	public void setDesactivated(Integer desactivated) {
		this.desactivated = desactivated;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
