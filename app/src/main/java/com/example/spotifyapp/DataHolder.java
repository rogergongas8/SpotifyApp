package com.example.spotifyapp;

import android.media.MediaPlayer;
import java.util.ArrayList;

public class DataHolder {
    // Lista de canciones para que esté disponible en toda la app
    public static ArrayList<Cancion> listaCancionesGlobal = new ArrayList<>();

    // NUEVO: Variables para controlar el Mini Reproductor
    public static MediaPlayer mediaPlayerGlobal;
    public static int posicionActualGlobal = -1;
    public static boolean isPlaying = false;
}