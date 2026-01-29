package tn.tn.elfatoora.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tn.elfatoora.entity.OidcAuthLog;

public interface OidcAuthLogRepository extends JpaRepository<OidcAuthLog, Long> {
    Optional<OidcAuthLog> findByState(String state);
}
