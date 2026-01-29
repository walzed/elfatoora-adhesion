package tn.tn.elfatoora.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.tn.elfatoora.entity.AdhesionPieceVersion;

public interface AdhesionPieceVersionRepository extends JpaRepository<AdhesionPieceVersion, Long> {

    List<AdhesionPieceVersion> findByDraftIdOrderByUploadedAtDesc(UUID draftId);

    List<AdhesionPieceVersion> findByDraftIdAndPieceTypeOrderByVersionNoDesc(UUID draftId, String pieceType);

    @Query("select coalesce(max(p.versionNo), 0) " +
           "from AdhesionPieceVersion p " +
           "where p.draftId = :draftId and p.pieceType = :pieceType")
    int maxVersion(@Param("draftId") UUID draftId, @Param("pieceType") String pieceType);

    @Query("select p from AdhesionPieceVersion p " +
           "where p.draftId = :draftId and upper(p.status) = 'ACTIVE'")
    List<AdhesionPieceVersion> findActiveByDraftId(@Param("draftId") UUID draftId);

    @Modifying
    @Query("update AdhesionPieceVersion p " +
           "set p.status = :newStatus " +
           "where p.draftId = :draftId and p.pieceType = :pieceType and upper(p.status) = 'ACTIVE'")
    int supersedeActiveByDraftIdAndType(@Param("draftId") UUID draftId,
                                        @Param("pieceType") String pieceType,
                                        @Param("newStatus") String newStatus);

	List<AdhesionPieceVersion> findByDraftIdOrderByPieceTypeAscVersionNoDesc(UUID draftId);

}
