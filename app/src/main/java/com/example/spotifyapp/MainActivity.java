package com.example.spotifyapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView_RGG;
    private CancionAdapter adapter_RGG;
    private static final int CODIGO_PERMISO_RGG = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView_RGG = findViewById(R.id.listaCanciones_RGG);

        checkPermisos_RGG();
    }

    private void checkPermisos_RGG() {
        // Lógica: Si es Android 13 (Tiramisu) o superior, usa el permiso nuevo
        String permisoNecesario;
        if (Build.VERSION.SDK_INT >= 33) {
            permisoNecesario = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permisoNecesario = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permisoNecesario) != PackageManager.PERMISSION_GRANTED) {
            // Pedimos el permiso
            ActivityCompat.requestPermissions(this, new String[]{permisoNecesario}, CODIGO_PERMISO_RGG);
        } else {
            // Ya tenemos permiso
            cargarMusica_RGG();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_RGG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cargarMusica_RGG();
            } else {
                Toast.makeText(this, "Es necesario dar permisos para ver la música", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cargarMusica_RGG() {
        DataHolder.listaCancionesGlobal = new ArrayList<>();
        File directorioDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Debug: Aviso si no existe la carpeta
        if (!directorioDownload.exists()) {
            Toast.makeText(this, "No encuentro la carpeta Download", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] archivos = directorioDownload.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.getName().toLowerCase().endsWith(".mp3")) {
                    agregarCancion_RGG(archivo);
                }
            }
        }

        // Si no hay canciones, avisamos
        if (DataHolder.listaCancionesGlobal.isEmpty()) {
            Toast.makeText(this, "0 canciones encontradas en Download", Toast.LENGTH_SHORT).show();
        }

        adapter_RGG = new CancionAdapter(this, DataHolder.listaCancionesGlobal);
        listView_RGG.setAdapter(adapter_RGG);
    }

    private void agregarCancion_RGG(File archivo) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(archivo.getAbsolutePath());
            String titulo = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            byte[] artBytes = mmr.getEmbeddedPicture();

            if (titulo == null) titulo = archivo.getName();
            if (artista == null) artista = "Desconocido";

            Bitmap caratula = null;
            if (artBytes != null) {
                caratula = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            }

            DataHolder.listaCancionesGlobal.add(new Cancion(titulo, artista, archivo.getAbsolutePath(), caratula));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}