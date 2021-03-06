package com.ferrariapps.olxapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.ferrariapps.olxapp.adapter.AdapterAnuncios;
import com.ferrariapps.olxapp.helper.ConfiguracaoFirebase;
import com.ferrariapps.olxapp.helper.RecyclerItemClickListener;
import com.ferrariapps.olxapp.model.Anuncio;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.ferrariapps.olxapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class MeusAnunciosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private AlertDialog dialog;
    private ValueEventListener valueEventListenerMeusAnuncios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child(ConfiguracaoFirebase.getIdUsuario());
        inicializarComponentes();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));
            }
        });

        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios,this);
        recyclerAnuncios.setAdapter(adapterAnuncios);

        recyclerAnuncios.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                recyclerAnuncios,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Anuncio anuncioSelecionado = anuncios.get(position);
                        anuncioSelecionado.remover();
                        adapterAnuncios.notifyDataSetChanged();
                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

    }

    private void recuperarAnuncios() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        dialog.show();
        valueEventListenerMeusAnuncios = anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                anuncios.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    anuncios.add(ds.getValue(Anuncio.class));
                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recuperarAnuncios();
    }

    @Override
    protected void onPause() {
        super.onPause();
        anuncioUsuarioRef.removeEventListener(valueEventListenerMeusAnuncios);
    }
}