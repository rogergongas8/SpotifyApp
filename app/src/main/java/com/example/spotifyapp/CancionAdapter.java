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
        Cancion cancionActual = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cancion, parent, false);
        }

        TextView titulo = convertView.findViewById(R.id.txtTituloItem_RGG);
        TextView artista = convertView.findViewById(R.id.txtArtistaItem_RGG);
        ImageView imagen = convertView.findViewById(R.id.imgCaratulaItem_RGG);

        titulo.setText(cancionActual.getTitulo());
        artista.setText(cancionActual.getArtista());

        if (cancionActual.getCaratula() != null) {
            imagen.setImageBitmap(cancionActual.getCaratula());
        } else {
            imagen.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context_RGG, PlayerActivity.class);
            intent.putExtra("posicion_cancion", position);
            DataHolder.listaCancionesGlobal = listaCanciones_RGG;
            context_RGG.startActivity(intent);
        });

        return convertView;
    }
}