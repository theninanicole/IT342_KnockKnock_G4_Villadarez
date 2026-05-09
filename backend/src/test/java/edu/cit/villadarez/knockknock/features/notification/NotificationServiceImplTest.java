package edu.cit.villadarez.knockknock.features.notification;

import edu.cit.villadarez.knockknock.features.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getUserNotificationsUsesReadFilterWhenProvided() {
        User user = new User(UUID.randomUUID(), "Visitor", "visitor@example.com", "pw", "VISITOR");
        Notification notification = new Notification(user, "scheduled", "Confirmed", "Visit confirmed");

        when(notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(any(User.class), anyBoolean(), any(Pageable.class)))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getUserNotifications(user, false, 10);

        assertThat(result).containsExactly(notification);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByUserAndIsReadOrderByCreatedAtDesc(eq(user), eq(false), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void markAsReadSavesUnreadNotification() {
        User user = new User(UUID.randomUUID(), "Visitor", "visitor@example.com", "pw", "VISITOR");
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification(user, "scheduled", "Confirmed", "Visit confirmed");

        when(notificationRepository.findByNotifIdAndUser(notifId, user)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(user, notifId);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadDoesNotResaveAlreadyReadNotification() {
        User user = new User(UUID.randomUUID(), "Visitor", "visitor@example.com", "pw", "VISITOR");
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification(user, "scheduled", "Confirmed", "Visit confirmed");
        notification.setRead(true);

        when(notificationRepository.findByNotifIdAndUser(notifId, user)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(user, notifId);

        verify(notificationRepository, never()).save(notification);
    }

    @Test
    void markAsReadRejectsNotificationsThatDoNotBelongToUser() {
        User user = new User(UUID.randomUUID(), "Visitor", "visitor@example.com", "pw", "VISITOR");
        UUID notifId = UUID.randomUUID();

        when(notificationRepository.findByNotifIdAndUser(notifId, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(user, notifId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }
}
