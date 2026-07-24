package org.example.backendweride.platform.notifications.domain.model.commands;

/**
 * Comando para marcar una notificación como leída.
 * Incluye el id del usuario autenticado para comprobar que la notificación le pertenece.
 */
public record MarkNotificationAsReadCommand(String notificationId, String userId) {
}