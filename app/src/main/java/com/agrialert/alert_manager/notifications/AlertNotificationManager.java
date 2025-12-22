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

import java.util.List;

public final class AlertNotificationManager {

    private static final String CHANNEL_ID = "agri_alerts";
    private static final String TAG = "AlertNotificationMgr";

    private AlertNotificationManager() {
    }

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

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(smallIcon)
                    .setContentTitle(alert.getTitle() != null ? alert.getTitle() : "Nuovo alert meteo")
                    .setContentText(alert.getDescription() != null ? alert.getDescription() : "Condizione meteo rilevata")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            manager.notify(notificationId, builder.build());
            notificationId++;
        }
    }

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
