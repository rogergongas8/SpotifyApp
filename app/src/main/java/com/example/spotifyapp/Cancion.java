package com.example.spotifyapp;

import android.graphics.Bitmap;

public class Cancion {
    private String titulo_RGG;
    private String artista_RGG;
    private String pathArchivo_RGG;
    private Bitmap caratula_RGG;

    // Constructor para crear una nueva canción fácilmente
    public Cancion(String titulo, String artista, String path, Bitmap caratula) {
        this.titulo_RGG = titulo;
        this.artista_RGG = artista;
        this.pathArchivo_RGG = path;
        this.caratula_RGG = caratula;
    }

    // Getters
    public String getTitulo() { return titulo_RGG; }
    public String getArtista() { return artista_RGG; }
    public String getPath() { return pathArchivo_RGG; }
    public Bitmap getCaratula() { return caratula_RGG; }
}