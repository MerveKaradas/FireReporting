package com.mervekaradas.yanginvardemo.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mervekaradas.yanginvardemo.R;
import com.mervekaradas.yanginvardemo.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;

    FirebaseAuth firebaseAuth;


    //KLAVYE KESİNLİKLE DÜZENLE


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(Login.this, Dashboard.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> {
            login();
        });

        binding.btnRegister.setOnClickListener(v -> {
            register();
        });

        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, ForgotPassword.class));
        });
    }

    private void login()
    {
        String email = binding.editTextTextEmailAddress.getText().toString().trim();
        String password = binding.editTextTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || email == null || password == null){
            Toast.makeText(this, "Lütfen eksik alanları doldurunuz!", Toast.LENGTH_LONG).show();
        }
        else {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(Login.this, "Lütfen e-posta kimliğinizi doğrulayın!", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Intent intent = new Intent(Login.this, Dashboard.class);
                                    startActivity(intent);
                                    System.out.println("loginden"+firebaseAuth.getCurrentUser().toString());
                                    finish();
                                }

                            } else {
                                Toast.makeText(Login.this, "Kimlik doğrulama başarısız oldu.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void register()
    {
        startActivity(new Intent(Login.this, Register.class));
    }
}