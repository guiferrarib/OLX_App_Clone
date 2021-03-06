package com.ferrariapps.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.ferrariapps.olxapp.R;
import com.ferrariapps.olxapp.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private Button buttonAcessar;
    private EditText editCadastroEmail, editCadastroSenha;
    private Switch switchAcesso;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        buttonAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editCadastroEmail.getText().toString();
                String senha = editCadastroSenha.getText().toString();

                if (!email.isEmpty()){
                    if (!senha.isEmpty()){
                        if (switchAcesso.isChecked()){
                            autenticacao.createUserWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(CadastroActivity.this,
                                                "Cadastro realizado com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                    }else {
                                        String erroExcecao = "";
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        }catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Por favor, digite um e-mail válido";
                                        }catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Esta conta já foi cadastrada";
                                        }catch (Exception e){
                                            erroExcecao = "Erro ao cadastrar usuário: "+e.getMessage();
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(CadastroActivity.this,
                                                erroExcecao,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else {
                            autenticacao.signInWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(CadastroActivity.this,
                                                "Logado com sucesso!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),AnunciosActivity.class));
                                    }else {
                                        String erroExcecao = "";
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Email e senha não concidem";
                                        }catch (FirebaseAuthInvalidUserException e){
                                            erroExcecao = "Esta conta não existe. faça o cadastro.";
                                        }catch (Exception e){
                                            erroExcecao = "Erro ao cadastrar usuário: "+e.getMessage();
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(CadastroActivity.this,
                                                erroExcecao,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }else {
                        Toast.makeText(CadastroActivity.this,
                                "Preencha a senha!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o E-mail!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void inicializarComponentes() {
        buttonAcessar = findViewById(R.id.buttonAcesso);
        editCadastroEmail = findViewById(R.id.editCadastroEmail);
        editCadastroSenha = findViewById(R.id.editCadsatroSenha);
        switchAcesso = findViewById(R.id.switchAcesso);
    }
}