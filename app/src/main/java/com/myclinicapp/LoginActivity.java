package com.myclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = FirebaseDatabase.getInstance();
        firebaseUsers = database.getReference(App.FIREBASE_DB_PATH_USERS);

        final EditText etUsername = findViewById(R.id.etUsername);
        final EditText etPassword = findViewById(R.id.etPassword);
        final Button login = findViewById(R.id.login);
        final View containerRegister = findViewById(R.id.containerRegister);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                final String password = etPassword.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
                    return;
                } else if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
                    return;
                }

                firebaseUsers.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Toast.makeText(getApplicationContext(), "Either username or password is incorrect!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null || !user.password.equals(App.getSHA224String(password))) {
                            Toast.makeText(getApplicationContext(), "Either username or password is incorrect!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        switch (user.type) {
                            case "clinic":
                                database.getReference(App.FIREBASE_DB_PATH_CLINIC_USERS).child(user.username).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (user == null) {
                                            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        App.setUser(user);
                                        Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), ClinicDashboardActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case "patient":
                                database.getReference(App.FIREBASE_DB_PATH_PATIENT_USERS).child(user.username).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (user == null) {
                                            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        App.setUser(user);
                                        Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), PatientDashboardActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case "admin":
                                App.setUser(user);
                                Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), AdminDashboardActivity.class));
                                finish();
                                break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ERROR", databaseError.getMessage());
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        containerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });

    }

}
