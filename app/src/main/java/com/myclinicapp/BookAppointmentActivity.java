package com.myclinicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class BookAppointmentActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference firebaseClinicReference;
    private DatabaseReference firebaseTsReference;
    String clinicID;
    private TextView tvWaitingTime;
    private Clinic clinic;
    View btnBookAppointment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        setTitle("Clinics");

        database = FirebaseDatabase.getInstance();
        tvWaitingTime = findViewById(R.id.tvWaitingTime);

        Intent intent = getIntent();
        clinicID = intent.getStringExtra("clinicID");
        firebaseClinicReference = database.getReference(App.FIREBASE_DB_PATH_CLINICS).child(clinicID);
        firebaseTsReference = database.getReference(App.FIREBASE_DB_PATH_TS);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(BookAppointmentActivity.this)
                        .setTitle("Confirm Appointment")
                        .setMessage("Are you sure you want to make an appointment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bookAppointment(clinicID, clinic);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // Fetch clinic
        firebaseClinicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                clinic = dataSnapshot.getValue(Clinic.class);
                searchForAlreadyBookedAppointments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load clinic list.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchForAlreadyBookedAppointments() {
        firebaseClinicReference.child("userAppointments").child(App.user.username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                    showAppointmentNotBookedUI();
                    return;
                }
                final long appointmentTime = dataSnapshot.getValue(Long.class);
                firebaseTsReference.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseTsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final long ts = dataSnapshot.getValue(Long.class);
                                if (appointmentTime >= ts) {
                                    showAppointmentAlreadyBookedUI(appointmentTime, ts);
                                } else {
                                    showAppointmentNotBookedUI();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showAppointmentAlreadyBookedUI(long appointmentTime, long ts) {
        long remainingTime = appointmentTime - ts;
        if (remainingTime < 0) {
            clinic.waitingHours = 0;
            remainingTime = 0;
            displayTiming(remainingTime, true);
        } else {
            Toast.makeText(getApplicationContext(), "You are already in the queue!", Toast.LENGTH_SHORT).show();
            displayTiming(remainingTime, false);
        }
    }

    private void showAppointmentNotBookedUI() {
        firebaseTsReference.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseTsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long ts = dataSnapshot.getValue(Long.class);
                        long remainingTime = clinic.waitingHours - ts;
                        if (remainingTime <= 0) {
                            clinic.waitingHours = 0;
                            remainingTime = 0;
                        }
                        displayTiming(remainingTime, true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTiming(long remainingTime, boolean showBookingButton) {
        tvWaitingTime.setText((remainingTime / 60000) + " minutes");
        btnBookAppointment.setVisibility(showBookingButton ? View.VISIBLE : View.GONE);
    }

    private void bookAppointment(String clinicID, final Clinic clinic) {
        firebaseTsReference.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseTsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final long ts = dataSnapshot.getValue(Long.class);
                        if (clinic.waitingHours <= 0) clinic.waitingHours = ts;
                        clinic.waitingHours = clinic.waitingHours;
                        firebaseClinicReference.child("waitingHours").setValue(clinic.waitingHours + 900000).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseClinicReference.child("userAppointments").child(App.user.username).setValue(clinic.waitingHours).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        displayTiming(clinic.waitingHours - ts, false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
