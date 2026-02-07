package com.example.spotifyapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private LinearLayout layoutMiniPlayer;
    private TextView txtMiniTitulo;
    private ImageView btnMiniPlay, btnMiniNext;

    private EditText etBuscador;
    private LinearLayout btnNavInicio, btnNavBuscar;
    private ImageView imgNavInicio, imgNavBuscar;
    private TextView txtNavInicio, txtNavBuscar;

    private ArrayList<Cancion> listaCompletaOriginal = new ArrayList<>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            String action = extras.getString("actionname");
            if (action != null) {
                switch (action) {
                    case CreateNotification.ACTION_PREVIOUS:
                        onTrackPrevious();
                        break;
                    case CreateNotification.ACTION_PLAY:
                        if (DataHolder.isPlaying) onTrackPause();
                        else onTrackPlay();
                        break;
                    case CreateNotification.ACTION_NEXT:
                        onTrackNext();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CreateNotification.createNotificationChannel(this);
        
        IntentFilter filter = new IntentFilter("TRACKS_TRACKS");
        ContextCompat.registerReceiver(this, broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

        listView_RGG = findViewById(R.id.listaCanciones_RGG);
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        txtMiniTitulo = findViewById(R.id.txtMiniTitulo);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        btnMiniNext = findViewById(R.id.btnMiniNext);
        etBuscador = findViewById(R.id.etBuscador);
        btnNavInicio = findViewById(R.id.btnNavInicio);
        btnNavBuscar = findViewById(R.id.btnNavBuscar);
        imgNavInicio = findViewById(R.id.imgNavInicio);
        txtNavInicio = findViewById(R.id.txtNavInicio);
        imgNavBuscar = findViewById(R.id.imgNavBuscar);
        txtNavBuscar = findViewById(R.id.txtNavBuscar);

        configurarMiniPlayer();
        configurarNavegacion();
        checkPermisos_RGG();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // RECUERDA: Esto hace que el mini-player aparezca al volver al listado
        actualizarVistaMiniPlayer();
    }

    private void onTrackPlay() {
        if (DataHolder.mediaPlayerGlobal != null) {
            DataHolder.mediaPlayerGlobal.start();
            DataHolder.isPlaying = true;
            actualizarVistaMiniPlayer();
            actualizarNotificacion_RGG(android.R.drawable.ic_media_pause);
        }
    }

    private void onTrackPause() {
        if (DataHolder.mediaPlayerGlobal != null) {
            DataHolder.mediaPlayerGlobal.pause();
            DataHolder.isPlaying = false;
            actualizarVistaMiniPlayer();
            actualizarNotificacion_RGG(android.R.drawable.ic_media_play);
        }
    }

    private void onTrackNext() {
        if (DataHolder.listaCancionesGlobal.isEmpty()) return;
        int nuevaPos = (DataHolder.posicionActualGlobal + 1) % DataHolder.listaCancionesGlobal.size();
        reproducirNueva_RGG(nuevaPos);
    }

    private void onTrackPrevious() {
        if (DataHolder.listaCancionesGlobal.isEmpty()) return;
        int nuevaPos = (DataHolder.posicionActualGlobal - 1 + DataHolder.listaCancionesGlobal.size()) % DataHolder.listaCancionesGlobal.size();
        reproducirNueva_RGG(nuevaPos);
    }

    private void reproducirNueva_RGG(int pos) {
        DataHolder.posicionActualGlobal = pos;
        if (DataHolder.mediaPlayerGlobal != null) {
            try {
                DataHolder.mediaPlayerGlobal.stop();
                DataHolder.mediaPlayerGlobal.release();
            } catch (Exception ignored) {}
        }
        try {
            Cancion c = DataHolder.listaCancionesGlobal.get(pos);
            DataHolder.mediaPlayerGlobal = new MediaPlayer();
            DataHolder.mediaPlayerGlobal.setDataSource(c.getPath());
            DataHolder.mediaPlayerGlobal.prepareAsync();
            DataHolder.mediaPlayerGlobal.setOnPreparedListener(mp -> {
                mp.setVolume(1.0f, 1.0f);
                mp.start();
                DataHolder.isPlaying = true;
                actualizarVistaMiniPlayer();
                CreateNotification.createNotification(this, c, android.R.drawable.ic_media_pause, pos, DataHolder.listaCancionesGlobal.size());
            });
            DataHolder.mediaPlayerGlobal.setOnCompletionListener(mp -> onTrackNext());
        } catch (Exception ignored) { }
    }

    private void configurarMiniPlayer() {
        btnMiniPlay.setOnClickListener(v -> {
            if (DataHolder.isPlaying) onTrackPause();
            else onTrackPlay();
        });
        btnMiniNext.setOnClickListener(v -> onTrackNext());
        
        // Abrir el reproductor al tocar el mini-player
        layoutMiniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("posicion_cancion", DataHolder.posicionActualGlobal);
            startActivity(intent);
        });
    }

    private void actualizarNotificacion_RGG(int icon) {
        if (DataHolder.posicionActualGlobal != -1) {
            Cancion c = DataHolder.listaCancionesGlobal.get(DataHolder.posicionActualGlobal);
            CreateNotification.createNotification(this, c, icon, DataHolder.posicionActualGlobal, DataHolder.listaCancionesGlobal.size());
        }
    }

    private void actualizarVistaMiniPlayer() {
        if (DataHolder.mediaPlayerGlobal != null && DataHolder.posicionActualGlobal != -1) {
            layoutMiniPlayer.setVisibility(View.VISIBLE);
            txtMiniTitulo.setText(DataHolder.listaCancionesGlobal.get(DataHolder.posicionActualGlobal).getTitulo());
            btnMiniPlay.setImageResource(DataHolder.isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        } else {
            layoutMiniPlayer.setVisibility(View.GONE);
        }
    }

    private void configurarNavegacion() {
        btnNavInicio.setOnClickListener(v -> {
            actualizarMenuInferior(0);
            etBuscador.setVisibility(View.GONE);
            etBuscador.setText("");
        });
        btnNavBuscar.setOnClickListener(v -> {
            actualizarMenuInferior(1);
            etBuscador.setVisibility(etBuscador.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
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
        imgNavInicio.setImageTintList(ColorStateList.valueOf(opcion == 0 ? colorActivo : colorInactivo));
        txtNavInicio.setTextColor(opcion == 0 ? colorActivo : colorInactivo);
        imgNavBuscar.setImageTintList(ColorStateList.valueOf(opcion == 1 ? colorActivo : colorInactivo));
        txtNavBuscar.setTextColor(opcion == 1 ? colorActivo : colorInactivo);
    }

    private void filtrarCanciones(String texto) {
        ArrayList<Cancion> listaFiltrada = new ArrayList<>();
        for (Cancion c : listaCompletaOriginal) {
            if (c.getTitulo().toLowerCase().contains(texto.toLowerCase())) listaFiltrada.add(c);
        }
        adapter_RGG = new CancionAdapter(this, listaFiltrada);
        listView_RGG.setAdapter(adapter_RGG);
    }

    private void cargarMusica_RGG() {
        if (!DataHolder.listaCancionesGlobal.isEmpty()) {
            listaCompletaOriginal = new ArrayList<>(DataHolder.listaCancionesGlobal);
            adapter_RGG = new CancionAdapter(this, DataHolder.listaCancionesGlobal);
            listView_RGG.setAdapter(adapter_RGG);
            actualizarVistaMiniPlayer(); // Asegurar mini-player al recargar
            return;
        }
        File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (d != null && d.exists()) {
            File[] archivos = d.listFiles();
            if (archivos != null) {
                for (File f : archivos) {
                    if (f.getName().toLowerCase().endsWith(".mp3")) agregarCancion_RGG(f);
                }
            }
        }
        listaCompletaOriginal = new ArrayList<>(DataHolder.listaCancionesGlobal);
        adapter_RGG = new CancionAdapter(this, DataHolder.listaCancionesGlobal);
        listView_RGG.setAdapter(adapter_RGG);
    }

    private void checkPermisos_RGG() {
        ArrayList<String> permisos = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            permisos.add(Manifest.permission.READ_MEDIA_AUDIO);
            permisos.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        ArrayList<String> noConcedidos = new ArrayList<>();
        for (String p : permisos) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) noConcedidos.add(p);
        }

        if (!noConcedidos.isEmpty()) {
            ActivityCompat.requestPermissions(this, noConcedidos.toArray(new String[0]), CODIGO_PERMISO_RGG);
        } else cargarMusica_RGG();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_RGG) {
            boolean todosAceptados = true;
            for (int res : grantResults) if (res != PackageManager.PERMISSION_GRANTED) todosAceptados = false;
            if (todosAceptados) cargarMusica_RGG();
        }
    }

    private void agregarCancion_RGG(File archivo) {
        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
            mmr.setDataSource(archivo.getAbsolutePath());
            String t = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            byte[] b = mmr.getEmbeddedPicture();
            Bitmap caratula = b != null ? BitmapFactory.decodeByteArray(b, 0, b.length) : null;
            DataHolder.listaCancionesGlobal.add(new Cancion(t != null ? t : archivo.getName(), a != null ? a : "Desconocido", archivo.getAbsolutePath(), caratula));
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(broadcastReceiver); } catch (Exception ignored) {}
    }
}