package com.example.spotifyapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

    // Mini Player
    private LinearLayout layoutMiniPlayer;
    private TextView txtMiniTitulo;
    private ImageView btnMiniPlay, btnMiniNext;

    // Navegación y Buscador
    private EditText etBuscador;
    private LinearLayout btnNavInicio, btnNavBuscar;
    private ImageView imgNavInicio, imgNavBuscar;
    private TextView txtNavInicio, txtNavBuscar;

    // Copia para filtrar sin perder datos
    private ArrayList<Cancion> listaCompletaOriginal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. VINCULAR VISTAS ---
        listView_RGG = findViewById(R.id.listaCanciones_RGG);

        // Mini Player
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        txtMiniTitulo = findViewById(R.id.txtMiniTitulo);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        btnMiniNext = findViewById(R.id.btnMiniNext);

        // Buscador
        etBuscador = findViewById(R.id.etBuscador);

        // Botones Menú Inferior
        btnNavInicio = findViewById(R.id.btnNavInicio);
        btnNavBuscar = findViewById(R.id.btnNavBuscar);

        // Iconos y Textos (Para cambiar color)
        imgNavInicio = findViewById(R.id.imgNavInicio);
        txtNavInicio = findViewById(R.id.txtNavInicio);
        imgNavBuscar = findViewById(R.id.imgNavBuscar);
        txtNavBuscar = findViewById(R.id.txtNavBuscar);

        // --- 2. CONFIGURAR LÓGICA ---
        configurarMiniPlayer();
        configurarNavegacion();

        checkPermisos_RGG();
    }

    private void configurarNavegacion() {
        // --- BOTÓN INICIO ---
        btnNavInicio.setOnClickListener(v -> {
            actualizarMenuInferior(0); // Poner blanco Inicio

            // Ocultar buscador y resetear lista
            etBuscador.setVisibility(View.GONE);
            etBuscador.setText("");

            // Ocultar teclado
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        // --- BOTÓN BUSCAR ---
        btnNavBuscar.setOnClickListener(v -> {
            actualizarMenuInferior(1); // Poner blanco Buscar

            if (etBuscador.getVisibility() == View.VISIBLE) {
                etBuscador.setVisibility(View.GONE);
            } else {
                etBuscador.setVisibility(View.VISIBLE);
                etBuscador.requestFocus();
                // Mostrar teclado
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(etBuscador, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // --- FILTRO DE BÚSQUEDA ---
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarCanciones(s.toString());
            }
        });
    }

    private void actualizarMenuInferior(int opcion) {
        int colorActivo = Color.WHITE;
        int colorInactivo = Color.parseColor("#B3B3B3");

        if (opcion == 0) { // INICIO ACTIVO
            imgNavInicio.setImageTintList(ColorStateList.valueOf(colorActivo));
            txtNavInicio.setTextColor(colorActivo);

            imgNavBuscar.setImageTintList(ColorStateList.valueOf(colorInactivo));
            txtNavBuscar.setTextColor(colorInactivo);
        } else if (opcion == 1) { // BUSCAR ACTIVO
            imgNavInicio.setImageTintList(ColorStateList.valueOf(colorInactivo));
            txtNavInicio.setTextColor(colorInactivo);

            imgNavBuscar.setImageTintList(ColorStateList.valueOf(colorActivo));
            txtNavBuscar.setTextColor(colorActivo);
        }
    }

    private void filtrarCanciones(String texto) {
        ArrayList<Cancion> listaFiltrada = new ArrayList<>();
        for (Cancion c : listaCompletaOriginal) {
            if (c.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(c);
            }
        }
        adapter_RGG = new CancionAdapter(MainActivity.this, listaFiltrada);
        listView_RGG.setAdapter(adapter_RGG);
    }

    private void cargarMusica_RGG() {
        // --- OPTIMIZACIÓN DE VELOCIDAD ---
        // Si ya tenemos canciones, NO volvemos a leer archivos.
        if (!DataHolder.listaCancionesGlobal.isEmpty()) {
            listaCompletaOriginal = new ArrayList<>(DataHolder.listaCancionesGlobal);
            adapter_RGG = new CancionAdapter(this, DataHolder.listaCancionesGlobal);
            listView_RGG.setAdapter(adapter_RGG);
            return;
        }

        File directorioDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (directorioDownload == null || !directorioDownload.exists()) {
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

        // Guardamos copia
        listaCompletaOriginal = new ArrayList<>(DataHolder.listaCancionesGlobal);
        adapter_RGG = new CancionAdapter(this, DataHolder.listaCancionesGlobal);
        listView_RGG.setAdapter(adapter_RGG);
    }

    // --- MÉTODOS ESTÁNDAR (IGUALES QUE ANTES) ---

    @Override
    protected void onResume() {
        super.onResume();
        actualizarVistaMiniPlayer();
    }

    private void configurarMiniPlayer() {
        btnMiniPlay.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                if (DataHolder.mediaPlayerGlobal.isPlaying()) {
                    DataHolder.mediaPlayerGlobal.pause();
                    DataHolder.isPlaying = false;
                    btnMiniPlay.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    DataHolder.mediaPlayerGlobal.start();
                    DataHolder.isPlaying = true;
                    btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        btnMiniNext.setOnClickListener(v -> {
            if (DataHolder.listaCancionesGlobal.isEmpty() || DataHolder.posicionActualGlobal == -1) return;

            // Usamos la lista COMPLETA original para calcular el siguiente,
            // así el orden no se rompe si estamos filtrando
            int size = DataHolder.listaCancionesGlobal.size();
            int nuevaPosicion = (DataHolder.posicionActualGlobal + 1) % size;

            DataHolder.posicionActualGlobal = nuevaPosicion;

            if (DataHolder.mediaPlayerGlobal != null) {
                DataHolder.mediaPlayerGlobal.stop();
                DataHolder.mediaPlayerGlobal.release();
            }
            try {
                Cancion siguienteCancion = DataHolder.listaCancionesGlobal.get(nuevaPosicion);
                Uri uri = Uri.parse(siguienteCancion.getPath());
                DataHolder.mediaPlayerGlobal = MediaPlayer.create(this, uri);
                DataHolder.mediaPlayerGlobal.start();
                DataHolder.isPlaying = true;

                DataHolder.mediaPlayerGlobal.setOnCompletionListener(mp -> btnMiniNext.performClick());

                txtMiniTitulo.setText(siguienteCancion.getTitulo());
                btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void actualizarVistaMiniPlayer() {
        if (DataHolder.mediaPlayerGlobal != null && DataHolder.posicionActualGlobal != -1) {
            layoutMiniPlayer.setVisibility(View.VISIBLE);
            try {
                String titulo = DataHolder.listaCancionesGlobal.get(DataHolder.posicionActualGlobal).getTitulo();
                txtMiniTitulo.setText(titulo);
            } catch (Exception e) { txtMiniTitulo.setText("..."); }

            if (DataHolder.mediaPlayerGlobal.isPlaying()) {
                btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                btnMiniPlay.setImageResource(android.R.drawable.ic_media_play);
            }
        } else {
            layoutMiniPlayer.setVisibility(View.GONE);
        }
    }

    private void checkPermisos_RGG() {
        String permisoNecesario;
        if (Build.VERSION.SDK_INT >= 33) {
            permisoNecesario = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permisoNecesario = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permisoNecesario) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permisoNecesario}, CODIGO_PERMISO_RGG);
        } else {
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
                Toast.makeText(this, "Es necesario dar permisos", Toast.LENGTH_LONG).show();
            }
        }
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
        } catch (Exception e) { e.printStackTrace(); }
    }
}