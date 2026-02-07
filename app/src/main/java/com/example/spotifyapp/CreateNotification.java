package com.example.spotifyapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CreateNotification {

    public static final String CHANNEL_ID = "channel_spotify_app";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_NEXT = "action_next";

    public static Notification notification;

    public static void createNotification(Context context, Cancion cancion, int playButton, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap icon;
            if (cancion.getCaratula() != null) {
                icon = cancion.getCaratula();
            } else {
                icon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_gallery);
            }

            // Intent para abrir la App al tocar la notificación (¡Esto la hace útil!)
            Intent intentOpenApp = new Intent(context, MainActivity.class);
            intentOpenApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentOpenApp, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // PendingIntents para los botones
            Intent intentPrevious = new Intent(context, NotificationActionService.class).setAction(ACTION_PREVIOUS);
            PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent intentNext = new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT);
            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Crear la notificación con estilo MediaStyle profesional
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentTitle(cancion.getTitulo())
                    .setContentText(cancion.getArtista())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setContentIntent(contentIntent) // Click para abrir app
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(android.R.drawable.ic_media_previous, "Anterior", pendingIntentPrevious)
                    .addAction(playButton, "Play", pendingIntentPlay)
                    .addAction(android.R.drawable.ic_media_next, "Siguiente", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            try {
                notificationManagerCompat.notify(1, notification);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL_ID,
                    "Spotify App Music",
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controles de música");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}