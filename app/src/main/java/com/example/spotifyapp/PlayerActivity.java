package com.example.spotifyapp;

import android.media.AudioAttributes;
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
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitulo_RGG;
    private ImageView imgCaratula_RGG;
    private SeekBar seekBar_RGG;
    private Button btnPlay_RGG, btnStop_RGG, btnNext_RGG, btnPrev_RGG,
            btnForward_RGG, btnRewind_RGG, btnBack_RGG;

    private ArrayList<Cancion> listaCanciones_RGG;
    private int posicionActual_RGG;
    private Handler handler_RGG = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        inicializarVistas_RGG();
        listaCanciones_RGG = DataHolder.listaCancionesGlobal;

        int posicionSolicitada = getIntent().getIntExtra("posicion_cancion", -1);

        if (DataHolder.mediaPlayerGlobal != null &&
                DataHolder.posicionActualGlobal == posicionSolicitada) {
            posicionActual_RGG = DataHolder.posicionActualGlobal;
            actualizarInterfazUsuario();
            if (DataHolder.mediaPlayerGlobal.isPlaying()) {
                btnPlay_RGG.setText("PAUSA");
                actualizarSeekBar_RGG();
            }
        } else {
            posicionActual_RGG = posicionSolicitada;
            reproducirCancion_RGG();
        }

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
                    actualizarSeekBar_RGG();
                }
                actualizarNotificacion_RGG(DataHolder.isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            }
        });

        btnStop_RGG.setOnClickListener(v -> {
            if (DataHolder.mediaPlayerGlobal != null) {
                DataHolder.mediaPlayerGlobal.pause();
                DataHolder.mediaPlayerGlobal.seekTo(0);
                DataHolder.isPlaying = false;
                btnPlay_RGG.setText("PLAY");
                actualizarNotificacion_RGG(android.R.drawable.ic_media_play);
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
                int newPos = Math.max(0, currentPos - 10000);
                DataHolder.mediaPlayerGlobal.seekTo(newPos);
            }
        });

        btnNext_RGG.setOnClickListener(v -> {
            if (!listaCanciones_RGG.isEmpty()) {
                posicionActual_RGG = (posicionActual_RGG + 1) % listaCanciones_RGG.size();
                reproducirCancion_RGG();
            }
        });

        btnPrev_RGG.setOnClickListener(v -> {
            if (!listaCanciones_RGG.isEmpty()) {
                posicionActual_RGG = (posicionActual_RGG - 1 + listaCanciones_RGG.size()) % listaCanciones_RGG.size();
                reproducirCancion_RGG();
            }
        });

        btnBack_RGG.setOnClickListener(v -> finish());

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
        if (DataHolder.mediaPlayerGlobal != null) {
            try {
                DataHolder.mediaPlayerGlobal.stop();
                DataHolder.mediaPlayerGlobal.release();
            } catch (Exception ignored) {}
            DataHolder.mediaPlayerGlobal = null;
        }

        DataHolder.posicionActualGlobal = posicionActual_RGG;
        Cancion cancionActual = listaCanciones_RGG.get(posicionActual_RGG);
        actualizarInterfazUsuario();

        try {
            DataHolder.mediaPlayerGlobal = new MediaPlayer();
            
            // CONFIGURACIÓN DE AUDIO PROFESIONAL
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            DataHolder.mediaPlayerGlobal.setAudioAttributes(audioAttributes);
            
            DataHolder.mediaPlayerGlobal.setDataSource(cancionActual.getPath());
            DataHolder.mediaPlayerGlobal.prepareAsync();
            DataHolder.mediaPlayerGlobal.setOnPreparedListener(mp -> {
                mp.setVolume(1.0f, 1.0f);
                mp.start();
                DataHolder.isPlaying = true;
                btnPlay_RGG.setText("PAUSA");
                seekBar_RGG.setMax(mp.getDuration());
                actualizarSeekBar_RGG();
                CreateNotification.createNotification(this, cancionActual, android.R.drawable.ic_media_pause, posicionActual_RGG, listaCanciones_RGG.size());
            });
            DataHolder.mediaPlayerGlobal.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Error de reproducción", Toast.LENGTH_SHORT).show();
                return false;
            });
            DataHolder.mediaPlayerGlobal.setOnCompletionListener(mp -> btnNext_RGG.performClick());
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo cargar el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarNotificacion_RGG(int icon) {
        Cancion c = listaCanciones_RGG.get(posicionActual_RGG);
        CreateNotification.createNotification(this, c, icon, posicionActual_RGG, listaCanciones_RGG.size());
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
        handler_RGG.removeCallbacksAndMessages(null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (DataHolder.mediaPlayerGlobal != null) {
                    try {
                        seekBar_RGG.setProgress(DataHolder.mediaPlayerGlobal.getCurrentPosition());
                    } catch (Exception ignored) {}
                }
                if (!isFinishing()) {
                    handler_RGG.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler_RGG.removeCallbacksAndMessages(null);
    }
}