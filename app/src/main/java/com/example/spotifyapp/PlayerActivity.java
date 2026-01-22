package com.example.spotifyapp;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitulo_RGG;
    private ImageView imgCaratula_RGG;
    private SeekBar seekBar_RGG;
    private Button btnPlay_RGG, btnStop_RGG, btnNext_RGG, btnPrev_RGG,
            btnForward_RGG, btnRewind_RGG, btnBack_RGG;

    private MediaPlayer mediaPlayer_RGG;
    private ArrayList<Cancion> listaCanciones_RGG;
    private int posicionActual_RGG;
    private Handler handler_RGG = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        inicializarVistas_RGG();

        listaCanciones_RGG = DataHolder.listaCancionesGlobal;
        posicionActual_RGG = getIntent().getIntExtra("posicion_cancion", 0);

        reproducirCancion_RGG();

        // --- BOTONES ---

        btnPlay_RGG.setOnClickListener(v -> {
            if (mediaPlayer_RGG != null) {
                if (mediaPlayer_RGG.isPlaying()) {
                    mediaPlayer_RGG.pause();
                    btnPlay_RGG.setText("PLAY");
                } else {
                    mediaPlayer_RGG.start();
                    btnPlay_RGG.setText("PAUSA");
                }
            }
        });

        btnStop_RGG.setOnClickListener(v -> {
            if (mediaPlayer_RGG != null) {
                mediaPlayer_RGG.pause();
                mediaPlayer_RGG.seekTo(0);
                btnPlay_RGG.setText("PLAY");
            }
        });

        btnForward_RGG.setOnClickListener(v -> {
            if (mediaPlayer_RGG != null) {
                int currentPos = mediaPlayer_RGG.getCurrentPosition();
                mediaPlayer_RGG.seekTo(currentPos + 10000);
            }
        });

        btnRewind_RGG.setOnClickListener(v -> {
            if (mediaPlayer_RGG != null) {
                int currentPos = mediaPlayer_RGG.getCurrentPosition();
                int newPos = currentPos - 10000;
                if (newPos < 0) newPos = 0;
                mediaPlayer_RGG.seekTo(newPos);
            }
        });

        // Siguiente Canción (Lógica circular)
        btnNext_RGG.setOnClickListener(v -> {
            if (listaCanciones_RGG.size() > 0) {
                posicionActual_RGG = (posicionActual_RGG + 1) % listaCanciones_RGG.size();
                reproducirCancion_RGG();
            }
        });

        // Anterior Canción (Lógica circular)
        btnPrev_RGG.setOnClickListener(v -> {
            if (listaCanciones_RGG.size() > 0) {
                posicionActual_RGG--;
                if (posicionActual_RGG < 0) {
                    posicionActual_RGG = listaCanciones_RGG.size() - 1;
                }
                reproducirCancion_RGG();
            }
        });

        btnBack_RGG.setOnClickListener(v -> finish());

        seekBar_RGG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer_RGG != null) {
                    mediaPlayer_RGG.seekTo(progress);
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
        if (mediaPlayer_RGG != null) {
            mediaPlayer_RGG.stop();
            mediaPlayer_RGG.release();
        }

        Cancion cancionActual = listaCanciones_RGG.get(posicionActual_RGG);

        txtTitulo_RGG.setText(cancionActual.getTitulo());
        if (cancionActual.getCaratula() != null) {
            imgCaratula_RGG.setImageBitmap(cancionActual.getCaratula());
        } else {
            imgCaratula_RGG.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        Uri uri = Uri.parse(cancionActual.getPath());
        mediaPlayer_RGG = MediaPlayer.create(this, uri);

        if (mediaPlayer_RGG != null) {
            // === LÍNEAS NUEVAS: DETECTAR EL FINAL ===
            mediaPlayer_RGG.setOnCompletionListener(mp -> {
                // Truco: Hacemos "clic" automático en el botón Siguiente
                btnNext_RGG.performClick();
            });
            // ========================================

            mediaPlayer_RGG.start();
            btnPlay_RGG.setText("PAUSA");
            seekBar_RGG.setMax(mediaPlayer_RGG.getDuration());
            actualizarSeekBar_RGG();
        }
    }

    private void actualizarSeekBar_RGG() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer_RGG != null && mediaPlayer_RGG.isPlaying()) {
                    seekBar_RGG.setProgress(mediaPlayer_RGG.getCurrentPosition());
                }
                handler_RGG.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer_RGG != null) {
            mediaPlayer_RGG.release();
            mediaPlayer_RGG = null;
        }
        handler_RGG.removeCallbacksAndMessages(null);
    }
}