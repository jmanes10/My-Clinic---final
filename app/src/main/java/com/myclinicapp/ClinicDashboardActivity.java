package com.myclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClinicDashboardActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_dashboard);

        database = FirebaseDatabase.getInstance();
        firebaseUsers = database.getReference(App.FIREBASE_DB_PATH_USERS);

        final TextView tvGreetings = findViewById(R.id.tvGreetings);
        tvGreetings.setText(App.getUserGreetings());

        findViewById(R.id.btnEditProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ClinicEditProfileActivity.class));
            }
        });

    }

}
