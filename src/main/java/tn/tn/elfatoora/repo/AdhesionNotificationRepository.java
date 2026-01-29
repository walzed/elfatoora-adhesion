package tn.tn.elfatoora.repo;

import tn.tn.elfatoora.entity.AdhesionNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdhesionNotificationRepository extends JpaRepository<AdhesionNotification, Long> {
    List<AdhesionNotification> findByRecipientEmailOrderByCreatedAtDesc(String email);
}
