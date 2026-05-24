package com.example.ecommerce.ecommerce_backend.application.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Test
    void dispatch_sendsToAllSupportingChannels() {
        var channel1 = mock(NotificationChannel.class);
        var channel2 = mock(NotificationChannel.class);
        var payload = new NotificationPayload(1L, "ORDER_STATUS_CHANGED", "Title", "Msg", "ORDER", 10L);

        when(channel1.supports("ORDER_STATUS_CHANGED")).thenReturn(true);
        when(channel2.supports("ORDER_STATUS_CHANGED")).thenReturn(false);

        var dispatcher = new NotificationDispatcher(List.of(channel1, channel2));
        dispatcher.dispatch(payload);

        verify(channel1).send(payload);
        verify(channel2, never()).send(any());
    }

    @Test
    void dispatch_continuesWhenOneChannelFails() {
        var failingChannel = mock(NotificationChannel.class);
        var workingChannel = mock(NotificationChannel.class);
        var payload = new NotificationPayload(1L, "ORDER_STATUS_CHANGED", "Title", "Msg", "ORDER", 10L);

        when(failingChannel.supports(any())).thenReturn(true);
        when(workingChannel.supports(any())).thenReturn(true);
        doThrow(new RuntimeException("email server down")).when(failingChannel).send(any());

        var dispatcher = new NotificationDispatcher(List.of(failingChannel, workingChannel));

        assertDoesNotThrow(() -> dispatcher.dispatch(payload));
        verify(workingChannel).send(payload);
    }
}
