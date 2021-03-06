package com.ferrariapps.olxapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ferrariapps.olxapp.R;
import com.ferrariapps.olxapp.model.Anuncio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Objects;

public class DetalhesProdutoActivity extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView textTituloDetalhe, textPrecoDetalhe, textEstadoDetalhe, textDescricaoDetalhe;
    private Anuncio anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_produto);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detalhes do Produto");
        inicializarComponentes();

        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");
        if (anuncioSelecionado != null){
            textTituloDetalhe.setText(anuncioSelecionado.getTitulo());
            textPrecoDetalhe.setText(anuncioSelecionado.getValor());
            textEstadoDetalhe.setText(anuncioSelecionado.getEstado());
            textDescricaoDetalhe.setText(anuncioSelecionado.getDescricao());

            ImageListener imageListener = (position, imageView) -> {
                String urlString = anuncioSelecionado.getFotos().get(position);
                Picasso.get().load(urlString).into(imageView);
            };

            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);
        }

    }

    public void visualizarTelefone(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", anuncioSelecionado.getTelefone(),null));
        startActivity(intent);
    }

    private void inicializarComponentes() {
        carouselView = findViewById(R.id.carouselView);
        textTituloDetalhe = findViewById(R.id.textTituloDetalhe);
        textPrecoDetalhe = findViewById(R.id.textPrecoDetalhe);
        textEstadoDetalhe = findViewById(R.id.textEstadoDetalhe);
        textDescricaoDetalhe = findViewById(R.id.textDescricaoDetalhe);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}