package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="adhesion_piece_version", indexes = {
        @Index(name="idx_pv_draft", columnList="draft_id"),
        @Index(name="idx_pv_type", columnList="draft_id,piece_type")
})
public class AdhesionPieceVersion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="draft_id", nullable=false)
    private UUID draftId;

    @Column(name="piece_type", nullable=false, length=32)
    private String pieceType; // RC/CIF/CIN_RESP/CIN_ADMIN...

    @Column(name="version_no", nullable=false)
    private int versionNo;

    @Column(nullable=false, length=20)
    private String status = "ACTIVE"; // ACTIVE/SUPERSEDED...

    private String originalFilename;
    private String contentType;

    @Column(name="size_bytes", nullable=false)
    private long sizeBytes;

    @Column(nullable=false, length=64)
    private String sha256;

    @Column(name="object_key", nullable=false, length=512)
    private String objectKey;

    private String uploadedBy;

    @Column(name="uploaded_at", nullable=false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Getters/Setters
    public Long getId() { return id; }
    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }
    public String getPieceType() { return pieceType; }
    public void setPieceType(String pieceType) { this.pieceType = pieceType; }
    public int getVersionNo() { return versionNo; }
    public void setVersionNo(int versionNo) { this.versionNo = versionNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
