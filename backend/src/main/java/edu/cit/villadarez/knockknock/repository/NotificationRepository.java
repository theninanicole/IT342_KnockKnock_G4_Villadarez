package edu.cit.villadarez.knockknock.repository;

import edu.cit.villadarez.knockknock.entity.Notification;
import edu.cit.villadarez.knockknock.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead, Pageable pageable);

    Optional<Notification> findByNotifIdAndUser(UUID notifId, User user);
}
