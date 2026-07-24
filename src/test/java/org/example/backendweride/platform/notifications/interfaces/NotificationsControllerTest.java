package org.example.backendweride.platform.notifications.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.notifications.domain.model.queries.GetAllNotificationsByUserIdQuery;
import org.example.backendweride.platform.notifications.domain.services.NotificationCommandService;
import org.example.backendweride.platform.notifications.domain.services.NotificationQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationsControllerTest {

    private final NotificationCommandService notificationCommandService = mock(NotificationCommandService.class);
    private final NotificationQueryService notificationQueryService = mock(NotificationQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final NotificationsController controller = new NotificationsController(
            notificationCommandService, notificationQueryService, authenticatedAccountProvider);

    @Test
    void getAllNotificationsByUserId_usesAuthenticatedUserId_notQueryParam() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(notificationQueryService.handle(any(GetAllNotificationsByUserIdQuery.class))).thenReturn(List.of());

        controller.getAllNotificationsByUserId("999");

        ArgumentCaptor<GetAllNotificationsByUserIdQuery> captor = ArgumentCaptor.forClass(GetAllNotificationsByUserIdQuery.class);
        verify(notificationQueryService).handle(captor.capture());
        assertEquals("42", captor.getValue().userId());
    }

    @Test
    void getNotificationById_returnsNotFound_whenNotificationBelongsToAnotherUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        var foreignNotification = mock(org.example.backendweride.platform.notifications.domain.model.aggregates.Notification.class);
        when(foreignNotification.getUserId()).thenReturn("999");
        when(notificationQueryService.handle(any(org.example.backendweride.platform.notifications.domain.model.queries.GetNotificationByIdQuery.class)))
                .thenReturn(java.util.Optional.of(foreignNotification));

        var response = controller.getNotificationById("notif-001");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void markAsRead_passesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);

        controller.markAsRead("notif-001");

        ArgumentCaptor<org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand> captor =
                ArgumentCaptor.forClass(org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand.class);
        verify(notificationCommandService).handle(captor.capture());
        assertEquals("42", captor.getValue().userId());
        assertEquals("notif-001", captor.getValue().notificationId());
    }
}
