package com.myclinicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PatientDashboardActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        database = FirebaseDatabase.getInstance();
        firebaseUsers = database.getReference(App.FIREBASE_DB_PATH_USERS);

        final TextView tvGreetings = findViewById(R.id.tvGreetings);
        tvGreetings.setText(App.getUserGreetings());

        findViewById(R.id.btnSearchClinicByAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PatientDashboardActivity.this);
                final EditText edittext = new EditText(getApplicationContext());
                edittext.setHint("Enter text here");
                alert.setTitle("Search By Address");
                alert.setView(edittext);
                alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String clinicAddress = edittext.getText().toString().trim();
                        if (clinicAddress.isEmpty())
                            Toast.makeText(getApplicationContext(), "Showing all Clinics", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Showing results with address: " + clinicAddress, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), ClinicListActivity.class);
                        intent.putExtra("clinicAddress", clinicAddress);
                        startActivity(intent);
                    }
                });
                alert.setNegativeButton("Cancel", null);
                alert.show();
            }
        });

        findViewById(R.id.btnSearchClinicByHours).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ClinicSearchByHoursActivity.class));
            }
        });

        findViewById(R.id.btnSearchClinicByTypeOfService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServicesListActivity.class);
                intent.putExtra("fromPatientSearchScreen", true);
                startActivity(intent);
            }
        });

    }

}
