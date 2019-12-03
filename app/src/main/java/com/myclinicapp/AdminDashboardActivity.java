package com.myclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDashboardActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        database = FirebaseDatabase.getInstance();
        firebaseUsers = database.getReference(App.FIREBASE_DB_PATH_USERS);

        final TextView tvGreetings = findViewById(R.id.tvGreetings);
        tvGreetings.setText(App.getUserGreetings());

        View btnManageService = findViewById(R.id.btnManageService);
        View btnClinicList = findViewById(R.id.btnClinicList);
        View btnPatientList = findViewById(R.id.btnPatientList);

        btnManageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ServicesListActivity.class));
            }
        });

        btnClinicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ClinicUserListActivity.class));
            }
        });

        btnPatientList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PatientUserListActivity.class));
            }
        });

    }

}
