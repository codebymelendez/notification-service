package dev.codebymelendez.notifications.domain.repository;

import dev.codebymelendez.notifications.domain.model.DeliveryAttempt;
import dev.codebymelendez.notifications.domain.model.DeliveryAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
    
    List<DeliveryAttempt> findByNotificationIdOrderByAttemptNumberAsc(UUID notificationId);
    
    long countByNotificationId(UUID notificationId);
    
    List<DeliveryAttempt> findByStatusAndAttemptedAtBetween(
            DeliveryAttemptStatus status, 
            Instant start, 
            Instant end
    );
    
    @Query("""
        SELECT da.status, COUNT(da)
        FROM DeliveryAttempt da
        GROUP BY da.status
        """)
    List<Object[]> countByStatusGrouped();
    
    @Query(value = """
        SELECT error_message, COUNT(*) as count 
        FROM delivery_attempts 
        WHERE status = 'FAILED' AND error_message IS NOT NULL 
        GROUP BY error_message 
        ORDER BY count DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMostCommonErrors(@Param("limit") int limit);
}
