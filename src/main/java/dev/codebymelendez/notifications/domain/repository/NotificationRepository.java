package dev.codebymelendez.notifications.domain.repository;

import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.Notification;
import dev.codebymelendez.notifications.domain.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByStatus(NotificationStatus status);
    
    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);
    
    long countByStatus(NotificationStatus status);
    
    List<Notification> findByChannel(Channel channel);
    
    List<Notification> findByChannelAndStatus(Channel channel, NotificationStatus status);
    
    Page<Notification> findByRecipientOrderByCreatedAtDesc(String recipient, Pageable pageable);
    
    List<Notification> findByCreatedAtBetween(Instant start, Instant end);
    
    @Query("""
        SELECT n FROM Notification n
        WHERE n.status = 'FAILED'
        AND n.updatedAt >= :since
        ORDER BY n.updatedAt DESC
        """)
    List<Notification> findRecentFailedNotifications(@Param("since") Instant since);

    @Query("""
        SELECT n FROM Notification n
        LEFT JOIN FETCH n.deliveryAttempts
        WHERE n.id = :id
        """)
    Optional<Notification> findByIdWithAttempts(@Param("id") UUID id);

    @Query("""
        SELECT n.status, COUNT(n)
        FROM Notification n
        GROUP BY n.status
        """)
    List<Object[]> countByStatusGrouped();
    
    @Query("""
        SELECT n.channel, n.status, COUNT(n)
        FROM Notification n
        GROUP BY n.channel, n.status
        """)
    List<Object[]> countByChannelAndStatusGrouped();
}
