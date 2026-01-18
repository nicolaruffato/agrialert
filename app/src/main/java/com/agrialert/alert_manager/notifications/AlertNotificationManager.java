package com.agrialert.alert_manager.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Alert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles alert notification channel creation and dispatching of alert notifications.
 */
public final class AlertNotificationManager {

    private static final String CHANNEL_ID = "agri_alerts";
    private static final String TAG = "AlertNotificationMgr";

    /**
     * Prevents instantiation; this is a static utility class.
     */
    private AlertNotificationManager() {
    }

    /**
     * Ensures the notification channel exists on Android O and above.
     *
     * @param context any context used to access system services
     */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alert meteo",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Notifiche per nuovi alert meteo dei campi");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Posts notifications for newly created alerts if notifications are permitted.
     *
     * @param context   any context used to build notifications
     * @param newAlerts list of alerts to notify
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public static void notifyNewAlerts(Context context, List<Alert> newAlerts) {
        if (newAlerts == null || newAlerts.isEmpty()) return;

        if (!canNotify(context)) {
            Log.w(TAG, "Permesso o stato notifiche non disponibile: skip invio notifiche alert");
            return;
        }

        ensureChannel(context);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        int notificationId = 2000;
        for (Alert alert : newAlerts) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            int smallIcon = alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert;

            String contentTitle = alert.getTitle() != null ? alert.getTitle() : "Nuovo alert meteo";
            String shortText = buildShortText(alert);
            String bigText = buildBigText(alert);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(smallIcon)
                    .setContentTitle(contentTitle)
                    .setContentText(shortText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            manager.notify(notificationId, builder.build());
            notificationId++;
        }
    }

    private static String buildShortText(Alert alert) {
        String description = alert != null && alert.getDescription() != null && !alert.getDescription().trim().isEmpty()
                ? alert.getDescription().trim()
                : "Condizione meteo rilevata";

        String forecastRange = formatForecastRange(alert != null ? alert.getForecastAt() : 0L,
                alert != null ? alert.getDurationMs() : 0L);
        if (forecastRange.isEmpty()) {
            return description;
        }
        return description + " \u2022 " + forecastRange;
    }

    private static String buildBigText(Alert alert) {
        if (alert == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (alert.getFieldAddress() != null && !alert.getFieldAddress().trim().isEmpty()) {
            sb.append("Campo: ").append(alert.getFieldAddress().trim()).append('\n');
        }
        if (alert.getDescription() != null && !alert.getDescription().trim().isEmpty()) {
            sb.append(alert.getDescription().trim().replace(" \u2022 ", "\n")).append('\n');
        }

        String forecastRange = formatForecastRange(alert.getForecastAt(), alert.getDurationMs());
        if (!forecastRange.isEmpty()) {
            sb.append("Previsto: ").append(forecastRange);
        }

        return sb.toString().trim();
    }

    private static String formatForecastRange(long startMs, long durationMs) {
        if (startMs <= 0L) {
            return "";
        }
        if (durationMs <= 0L) {
            return formatDateTime(startMs);
        }

        long endMs = startMs + durationMs;
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMs);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endMs);

        if (isSameDay(start, end)) {
            return formatDateTime(startMs) + "\u2013" + formatTime(endMs);
        }
        return formatDateTime(startMs) + "\u2013" + formatDateTime(endMs);
    }

    private static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private static String formatDateTime(long timestampMs) {
        if (timestampMs <= 0L) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        return df.format(new Date(timestampMs));
    }

    private static String formatTime(long timestampMs) {
        if (timestampMs <= 0L) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(new Date(timestampMs));
    }

    /**
     * Determines whether notifications can be posted for the current context and OS version.
     *
     * @param context context used to check permission and notification settings
     * @return {@code true} when notifications are allowed, {@code false} otherwise
     */
    private static boolean canNotify(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
