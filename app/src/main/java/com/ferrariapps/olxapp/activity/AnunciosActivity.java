package com.ferrariapps.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.ferrariapps.olxapp.R;
import com.ferrariapps.olxapp.adapter.AdapterAnuncios;
import com.ferrariapps.olxapp.helper.ConfiguracaoFirebase;
import com.ferrariapps.olxapp.helper.RecyclerItemClickListener;
import com.ferrariapps.olxapp.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private AdapterAnuncios adapterAnuncios;
    private final List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private ValueEventListener valueEventListenerAnuncios;
    private ValueEventListener valueEventListenerAnunciosPorEstado;
    private ValueEventListener valueEventListenerAnunciosPorCategoria;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);
        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);

        recyclerAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Anuncio anuncioSelecionado = listaAnuncios.get(position);
                                Intent intent = new Intent(AnunciosActivity.this, DetalhesProdutoActivity.class);
                                intent.putExtra("anuncioSelecionado",anuncioSelecionado);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    public void filtrarPorEstado(View view) {
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);
        dialogEstado.setView(viewSpinner);
        dialogEstado.setPositiveButton("Confirmar", (dialog, which) -> {
            filtroEstado = spinnerEstado.getSelectedItem().toString();
            recuperarAnunciosPorEstado();
            filtrandoPorEstado = true;
        });
        dialogEstado.setNegativeButton("Cancelar", null);

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    public void filtrarPorCategoria(View view) {
        if (filtrandoPorEstado) {
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejada");
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] estados = getResources().getStringArray(R.array.categorias);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, estados);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);
            dialogEstado.setView(viewSpinner);
            dialogEstado.setPositiveButton("Confirmar", (dialog, which) -> {
                filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                recuperarAnunciosPorCategoria();
            });
            dialogEstado.setNegativeButton("Cancelar",null);

            AlertDialog dialog = dialogEstado.create();
            dialog.show();
        } else {
            Toast.makeText(this,
                    "Escolha primeiro uma região!", Toast.LENGTH_SHORT).show();
        }
    }

    public void recuperarAnunciosPorCategoria() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);
        valueEventListenerAnunciosPorCategoria = anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAnuncios.clear();
                for (DataSnapshot anuncios : snapshot.getChildren()) {
                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add(anuncio);
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void recuperarAnunciosPorEstado() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);
        valueEventListenerAnunciosPorEstado = anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAnuncios.clear();
                for (DataSnapshot categorias : snapshot.getChildren()) {
                    for (DataSnapshot anuncios : categorias.getChildren()) {
                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                    }
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void recuperarAnunciosPublicos() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        dialog.show();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");
        listaAnuncios.clear();
        valueEventListenerAnuncios = anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot estados : snapshot.getChildren()) {
                    for (DataSnapshot categorias : estados.getChildren()) {
                        for (DataSnapshot anuncios : categorias.getChildren()) {
                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                filtrandoPorEstado = false;
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autenticacao.getCurrentUser() == null) {
            menu.setGroupVisible(R.id.group_deslogado, true);
        } else {
            menu.setGroupVisible(R.id.group_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        final int cadastrar = R.id.menu_cadastrar;
        final int sair = R.id.menu_sair;
        final int anuncios = R.id.menu_anuncios;

        switch (item.getItemId()) {

            case cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case anuncios:
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recuperarAnunciosPublicos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        anunciosPublicosRef.removeEventListener(valueEventListenerAnuncios);
        if (valueEventListenerAnunciosPorEstado != null)
            anunciosPublicosRef.removeEventListener(valueEventListenerAnunciosPorEstado);
        if (valueEventListenerAnunciosPorCategoria != null)
            anunciosPublicosRef.removeEventListener(valueEventListenerAnunciosPorCategoria);
    }
}