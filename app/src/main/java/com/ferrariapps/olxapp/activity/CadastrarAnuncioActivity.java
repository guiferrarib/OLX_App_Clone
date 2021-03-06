package com.ferrariapps.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.ferrariapps.olxapp.R;
import com.ferrariapps.olxapp.helper.ConfiguracaoFirebase;
import com.ferrariapps.olxapp.helper.Permissoes;
import com.ferrariapps.olxapp.model.Anuncio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText editTitulo, editDescricao;
    private CurrencyEditText editValor;
    private MaskEditText editTelefone;
    private Anuncio anuncio;
    private Button buttonCadastrarAnuncio;
    private ImageView imageCadastro1, imageCadastro2, imageCadastro3;
    private Spinner spinnerEstado, spinnerCategoria;
    private android.app.AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotos = new ArrayList<>();

    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Cadastrar Anúncio");
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        Permissoes.validarPermissoes(permissoes, this, 1);
        inicializarComponentes();
        carregarDadosSpinner();

    }

    private void carregarDadosSpinner() {

        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);
    }

    public void salvarAnuncio() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúncio")
                .setCancelable(false)
                .build();
        dialog.show();

        for (int i=0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }
    }

    private void salvarFotoStorage(String url, int totalFotos, int contador) {

        final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);

        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(url));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uri = task.getResult();
                        assert uri != null;
                        String urlConvertida = uri.toString();
                        listaUrlFotos.add(urlConvertida);

                        if (totalFotos == listaUrlFotos.size()){
                            anuncio.setFotos(listaUrlFotos);
                            anuncio.salvar();
                            dialog.dismiss();
                            finish();
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload das imagens!");
            }
        });

    }

    private Anuncio configurarAnuncio() {
        String estado = spinnerEstado.getSelectedItem().toString();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String titulo = editTitulo.getText().toString();
        String valor = editValor.getText().toString();
        String telefone = editTelefone.getMasked();
        String descricao = editDescricao.getText().toString();
        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    public void validarDadosAnuncio(View view) {

        anuncio = configurarAnuncio();
        String valor = String.valueOf(editValor.getRawValue());

        if (listaFotosRecuperadas.size() != 0) {
            if (!anuncio.getEstado().isEmpty()) {
                if (!anuncio.getCategoria().isEmpty()) {
                    if (!anuncio.getTitulo().isEmpty()) {
                        if (!valor.isEmpty() && !valor.equals("0")) {
                            if (!anuncio.getTelefone().isEmpty() && anuncio.getTelefone().length() >= 14) {
                                if (!anuncio.getDescricao().isEmpty()) {
                                    salvarAnuncio();
                                } else {
                                    exibirMensagemErro("Preencha o campo descrição!");
                                }
                            } else {
                                exibirMensagemErro("Preencha o campo telefone!");
                            }
                        } else {
                            exibirMensagemErro("Preencha o campo valor!");
                        }
                    } else {
                        exibirMensagemErro("Preencha o campo título!");
                    }
                } else {
                    exibirMensagemErro("Preencha o campo categoria!");
                }
            } else {
                exibirMensagemErro("Preencha o campo estado!");
            }
        } else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }

    }

    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this,
                mensagem, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editTitulo = findViewById(R.id.editTitulo);
        editDescricao = findViewById(R.id.editDescricao);
        editValor = findViewById(R.id.editValor);
        editTelefone = findViewById(R.id.editTelefone);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        imageCadastro1 = findViewById(R.id.imageCadastro1);
        imageCadastro2 = findViewById(R.id.imageCadastro2);
        imageCadastro3 = findViewById(R.id.imageCadastro3);
        imageCadastro1.setOnClickListener(this);
        imageCadastro2.setOnClickListener(this);
        imageCadastro3.setOnClickListener(this);

        String telefone = editTelefone.getMasked();
        buttonCadastrarAnuncio = findViewById(R.id.buttonCadastrarAnuncio);
        Locale locale = new Locale("pt", "BR");
        editValor.setLocale(locale);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onClick(View v) {

        final int image1 = R.id.imageCadastro1;
        final int image2 = R.id.imageCadastro2;
        final int image3 = R.id.imageCadastro3;

        switch (v.getId()) {

            case image1:
                escolherImagem(1);
                break;

            case image2:
                escolherImagem(2);
                break;

            case image3:
                escolherImagem(3);
                break;

        }
    }

    public void escolherImagem(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            assert data != null;
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();
            if (requestCode == 1) {
                imageCadastro1.setImageURI(imagemSelecionada);
            } else if (requestCode == 2) {
                imageCadastro2.setImageURI(imagemSelecionada);
            } else if (requestCode == 3) {
                imageCadastro3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}