package tn.tn.elfatoora.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import tn.tn.elfatoora.entity.AdhesionPieceVersion;

public interface PieceService {

    Map<String, AdhesionPieceVersion> latestByDraftId(UUID draftId);

    List<AdhesionPieceVersion> listByDraftId(UUID draftId);

    AdhesionPieceVersion uploadVersion(UUID draftId, String pieceType, MultipartFile file, String actor) throws Exception;

    InputStream download(String objectKey) throws Exception;
}
