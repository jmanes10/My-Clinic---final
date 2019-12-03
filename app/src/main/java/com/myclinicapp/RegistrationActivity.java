package com.myclinicapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setTitle("Registration");

        final RadioButton rbPatient = findViewById(R.id.rbPatient);
        final RadioButton rbClinic = findViewById(R.id.rbClinic);
        final EditText etFname = findViewById(R.id.etFname);
        final EditText etLname = findViewById(R.id.etLname);
        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etUsername = findViewById(R.id.etUsername);
        final EditText etPassword = findViewById(R.id.etPassword);
        final Button register = findViewById(R.id.register);

        database = FirebaseDatabase.getInstance();
        firebaseUsers = database.getReference(App.FIREBASE_DB_PATH_USERS);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fName = etFname.getText().toString().trim();
                final String lName = etLname.getText().toString().trim();
                final String email = etEmail.getText().toString().trim();
                final String username = etUsername.getText().toString().trim();
                final String password = etPassword.getText().toString();
                if (!validateFields(fName, lName, email, username, password, rbPatient, rbClinic))
                    return;

                createUser(fName, lName, email, username, password, rbClinic.isChecked());
            }
        });

    }

    private void createUser(final String fName, final String lName, final String email, final String username, final String password, final boolean isClinic) {
        firebaseUsers.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Toast.makeText(getApplicationContext(), "This username is already taken, please try again with different one!", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseUsers.child(username).setValue(new User(username, fName, lName, App.getSHA224String(password), isClinic ? "clinic" : "patient", email)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference(isClinic ? "clinic_users" : "patient_users").child(username).setValue(new User(username, fName, lName, App.getSHA224String(password), isClinic ? "clinic" : "patient", email)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERROR", databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields(String fName, String lName, String email, String username, String password, RadioButton rbPatient, RadioButton rbClinic) {
        if (!rbPatient.isChecked() && !rbClinic.isChecked()) {
            Toast.makeText(getApplicationContext(), "Please select user type", Toast.LENGTH_LONG).show();
            return false;
        } else if (fName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter first name", Toast.LENGTH_LONG).show();
            return false;
        } else if (lName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter last name", Toast.LENGTH_LONG).show();
            return false;
        } else if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter email", Toast.LENGTH_LONG).show();
            return false;
        } else if (username.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
            return false;
        } else if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isValidName(fName)) {
            Toast.makeText(getApplicationContext(), "Please enter valid first name", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isValidName(lName)) {
            Toast.makeText(getApplicationContext(), "Please enter valid last name", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isValidUsername(username)) {
            Toast.makeText(getApplicationContext(), "Please enter valid username. You can only use alphanumeric value", Toast.LENGTH_LONG).show();
            return false;
        } else if (password.length() < 4) {
            Toast.makeText(getApplicationContext(), "password should be at least 4 characters long", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean isValidName(String str) {
        return str.matches("^[a-zA-Z]+");
    }

    public boolean isValidEmail(String str) {
        return str.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$");
    }

    public boolean isValidUsername(String str) {
        return str.matches("^[a-zA-Z0-9]+");
    }

}
