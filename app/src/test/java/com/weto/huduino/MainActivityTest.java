package com.weto.huduino;

import org.junit.Test;

import static org.junit.Assert.*;

import android.app.AlertDialog;
import android.content.Context;

public class MainActivityTest {
    @Test
    public void changeInterceptedNotificationImageTest() throws Exception {
        assertEquals(1, NotificationService.InterceptedNotificationCode.FACEBOOK_CODE);
        assertEquals(5, NotificationService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
    }
}