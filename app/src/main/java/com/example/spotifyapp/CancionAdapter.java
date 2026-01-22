package com.example.spotifyapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class CancionAdapter extends ArrayAdapter<Cancion> {

    private ArrayList<Cancion> listaCanciones_RGG;
    private Context context_RGG;

    public CancionAdapter(Context context, ArrayList<Cancion> canciones) {
        super(context, 0, canciones);
        this.context_RGG = context;
        this.listaCanciones_RGG = canciones;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Obtener la canción actual
        Cancion cancionActual = getItem(position);

        // 2. Si no hay vista reutilizable, inflar la nueva (el diseño de la fila)
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cancion, parent, false);
        }

        // 3. Vincular los elementos del diseño item_cancion.xml
        TextView titulo = convertView.findViewById(R.id.txtTituloItem_RGG);
        TextView artista = convertView.findViewById(R.id.txtArtistaItem_RGG);
        ImageView imagen = convertView.findViewById(R.id.imgCaratulaItem_RGG);

        // 4. Rellenar con datos
        titulo.setText(cancionActual.getTitulo());
        artista.setText(cancionActual.getArtista());

        if (cancionActual.getCaratula() != null) {
            imagen.setImageBitmap(cancionActual.getCaratula());
        } else {
            imagen.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 5. Configurar el clic para abrir el reproductor
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context_RGG, PlayerActivity.class);
            intent.putExtra("posicion_cancion", position); // Pasamos qué canción tocar

            // Importante: Aseguramos que la lista global esté actualizada
            DataHolder.listaCancionesGlobal = listaCanciones_RGG;

            context_RGG.startActivity(intent);
        });

        return convertView;
    }
}