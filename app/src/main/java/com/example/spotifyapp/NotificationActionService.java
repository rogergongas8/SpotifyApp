package com.example.spotifyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Aquí recibiremos los clics de la notificación
        // Enviaremos un broadcast que MainActivity o PlayerActivity escucharán
        context.sendBroadcast(new Intent("TRACKS_TRACKS")
                .putExtra("actionname", intent.getAction()));
    }
}