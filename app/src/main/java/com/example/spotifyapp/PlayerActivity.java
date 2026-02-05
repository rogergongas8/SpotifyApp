package com.example.spotifyapp;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitulo_RGG;
    private ImageView imgCaratula_RGG;
    private SeekBar seekBar_RGG;
    private Button btnPlay_RGG, btnStop_RGG, btnNext_RGG, btnPrev_RGG,
            btnForward_RGG, btnRewind_RGG, btnBack_RGG;

    // YA NO usamos mediaPlayer local, usamos el de DataHolder
    private ArrayList<Cancion> listaCanciones_RGG;
    private int posicionActual_RGG;
    private Handler handler_RGG = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        inicializarVistas_RGG();

        listaCanciones_RGG = DataHolder.listaCancionesGlobal;

        // Recuperamos qué canción se pidió
        int posicionSolicitada = getIntent().getIntExtra("posicion_cancion", -1);

        // LÓGICA INTELIGENTE:
        // Si ya está sonando la misma canción que pedimos, NO la reiniciamos.
        // Solo actualizamos la interfaz.
        if (DataHolder.mediaPlayerGlobal != null &&
                DataHolder.mediaPlayerGlobal.isPlaying() &&
                posicionSolicitada == DataHolder.posicionActualGlobal) {

            posicionActual_RGG = DataHolder.posicionActualGlobal;
            actualizarInterfazUsuario(); // Solo pintamos textos y fotos
            actualizarSeekBar_RGG();     // Enganchamos la barra de progreso
            btnPlay_RGG.setText("PAUSA");

        } else {
            // Si es una canción nueva, reproducimos desde cero
            posicionActual_RGG = posicionSolicitada;
            reproducirCancion_RGG();
        }

        // --- BOTONES ---

        btnPlay_RGG.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                if (DataHolder.mediaPlayerGlobal.isPlaying()) {
                    DataHolder.mediaPlayerGlobal.pause();
                    DataHolder.isPlaying = false;
                    btnPlay_RGG.setText("PLAY");
                } else {
                    DataHolder.mediaPlayerGlobal.start();
                    DataHolder.isPlaying = true;
                    btnPlay_RGG.setText("PAUSA");
                }
            }
        });

        btnStop_RGG.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                DataHolder.mediaPlayerGlobal.pause();
                DataHolder.mediaPlayerGlobal.seekTo(0);
                DataHolder.isPlaying = false;
                btnPlay_RGG.setText("PLAY");
            }
        });

        btnForward_RGG.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                int currentPos = DataHolder.mediaPlayerGlobal.getCurrentPosition();
                DataHolder.mediaPlayerGlobal.seekTo(currentPos + 10000);
            }
        });

        btnRewind_RGG.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                int currentPos = DataHolder.mediaPlayerGlobal.getCurrentPosition();
                int newPos = currentPos - 10000;
                if (newPos < 0) newPos = 0;
                DataHolder.mediaPlayerGlobal.seekTo(newPos);
            }
        });

        // Siguiente
        btnNext_RGG.setOnClickListener(v -> {
            if (listaCanciones_RGG.size() > 0) {
                posicionActual_RGG = (posicionActual_RGG + 1) % listaCanciones_RGG.size();
                reproducirCancion_RGG();
            }
        });

        // Anterior
        btnPrev_RGG.setOnClickListener(v -> {
            if (listaCanciones_RGG.size() > 0) {
                posicionActual_RGG--;
                if (posicionActual_RGG < 0) {
                    posicionActual_RGG = listaCanciones_RGG.size() - 1;
                }
                reproducirCancion_RGG();
            }
        });

        btnBack_RGG.setOnClickListener(v -> finish()); // Solo cierra la actividad, NO para la música

        seekBar_RGG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && DataHolder.mediaPlayerGlobal != null) {
                    DataHolder.mediaPlayerGlobal.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void inicializarVistas_RGG() {
        txtTitulo_RGG = findViewById(R.id.txtTitulo_RGG);
        imgCaratula_RGG = findViewById(R.id.imgCaratula_RGG);
        seekBar_RGG = findViewById(R.id.seekBar_RGG);
        btnPlay_RGG = findViewById(R.id.btnPlay_RGG);
        btnStop_RGG = findViewById(R.id.btnStop_RGG);
        btnNext_RGG = findViewById(R.id.btnNext_RGG);
        btnPrev_RGG = findViewById(R.id.btnPrev_RGG);
        btnForward_RGG = findViewById(R.id.btnForward_RGG);
        btnRewind_RGG = findViewById(R.id.btnRewind_RGG);
        btnBack_RGG = findViewById(R.id.btnBack_RGG);
    }

    private void reproducirCancion_RGG() {
        // 1. Limpiamos el anterior SI existe
        if (DataHolder.mediaPlayerGlobal != null) {
            DataHolder.mediaPlayerGlobal.stop();
            DataHolder.mediaPlayerGlobal.release();
        }

        // 2. Guardamos la posición global para que MainActivity sepa cuál es
        DataHolder.posicionActualGlobal = posicionActual_RGG;
        Cancion cancionActual = listaCanciones_RGG.get(posicionActual_RGG);

        // 3. Actualizamos UI
        actualizarInterfazUsuario();

        // 4. Creamos el MediaPlayer en la variable GLOBAL
        Uri uri = Uri.parse(cancionActual.getPath());
        DataHolder.mediaPlayerGlobal = MediaPlayer.create(this, uri);

        if (DataHolder.mediaPlayerGlobal != null) {
            DataHolder.mediaPlayerGlobal.setOnCompletionListener(mp -> {
                btnNext_RGG.performClick();
            });

            DataHolder.mediaPlayerGlobal.start();
            DataHolder.isPlaying = true;

            btnPlay_RGG.setText("PAUSA");
            seekBar_RGG.setMax(DataHolder.mediaPlayerGlobal.getDuration());
            actualizarSeekBar_RGG();
        }
    }

    private void actualizarInterfazUsuario() {
        Cancion cancionActual = listaCanciones_RGG.get(posicionActual_RGG);
        txtTitulo_RGG.setText(cancionActual.getTitulo());
        if (cancionActual.getCaratula() != null) {
            imgCaratula_RGG.setImageBitmap(cancionActual.getCaratula());
        } else {
            imgCaratula_RGG.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void actualizarSeekBar_RGG() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (DataHolder.mediaPlayerGlobal != null && DataHolder.mediaPlayerGlobal.isPlaying()) {
                    seekBar_RGG.setProgress(DataHolder.mediaPlayerGlobal.getCurrentPosition());
                }
                // Revisamos cada segundo si sigue vivo
                if (!isFinishing()) {
                    handler_RGG.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // IMPORTANTE: Ya NO hacemos release() aquí.
        // Solo paramos el handler de la barrita para que no de error de memoria.
        handler_RGG.removeCallbacksAndMessages(null);
    }
}