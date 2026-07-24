package org.example.backendweride.platform.notifications.application.commandservices;

import org.example.backendweride.platform.notifications.domain.model.aggregates.Notification;
import org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import org.example.backendweride.platform.notifications.infrastructure.persistence.jpa.NotificationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NotificationCommandServiceImplTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationCommandServiceImpl service = new NotificationCommandServiceImpl(notificationRepository);

    @Test
    void markAsRead_throws_whenNotificationBelongsToAnotherUser() {
        Notification foreign = mock(Notification.class);
        when(foreign.getUserId()).thenReturn("999");
        when(notificationRepository.findByPublicId("notif-001")).thenReturn(Optional.of(foreign));

        assertThrows(RuntimeException.class, () ->
                service.handle(new MarkNotificationAsReadCommand("notif-001", "42")));

        verify(foreign, never()).markAsRead();
    }

    @Test
    void markAsRead_marksAsRead_whenOwnedByCurrentUser() {
        Notification owned = mock(Notification.class);
        when(owned.getUserId()).thenReturn("42");
        when(notificationRepository.findByPublicId("notif-001")).thenReturn(Optional.of(owned));

        service.handle(new MarkNotificationAsReadCommand("notif-001", "42"));

        verify(owned).markAsRead();
        verify(notificationRepository).save(owned);
    }
}
