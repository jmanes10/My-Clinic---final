package com.myclinicapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ClinicEditProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference firebaseClinics, firebaseClinicUsers;
    EditText etName;
    EditText etAddress;
    EditText etPhone;
    AppCompatCheckBox cb1;
    AppCompatCheckBox cb2;
    AppCompatCheckBox cb3;
    AppCompatCheckBox cb4;
    AppCompatCheckBox cb5;
    AppCompatCheckBox cbPaymentMethodCash;
    AppCompatCheckBox cbPaymentMethodCreditDebit;
    AppCompatButton btnManageService;
    TextView tvOpeningHrs;
    TextView tvClosingHrs;
    AppCompatCheckBox cbMonday;
    AppCompatCheckBox cbTuesday;
    AppCompatCheckBox cbWednesday;
    AppCompatCheckBox cbThursday;
    AppCompatCheckBox cbFriday;
    AppCompatCheckBox cbSaturday;
    AppCompatCheckBox cbSunday;
    AppCompatButton btnSave;
    ArrayList<String> servicesIdsList = new ArrayList<>();
    DecimalFormat df = new DecimalFormat("00");
    private int openHour, openMinute, closeHour, closeMinute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_edit_profile);
        setTitle("Edit Clinic Profile");

        database = FirebaseDatabase.getInstance();
        firebaseClinics = database.getReference(App.FIREBASE_DB_PATH_CLINICS);
        firebaseClinicUsers = database.getReference(App.FIREBASE_DB_PATH_CLINIC_USERS);

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        cb1 = findViewById(R.id.cb1);
        cb2 = findViewById(R.id.cb2);
        cb3 = findViewById(R.id.cb3);
        cb4 = findViewById(R.id.cb4);
        cb5 = findViewById(R.id.cb5);
        cbPaymentMethodCash = findViewById(R.id.cbPaymentMethodCash);
        cbPaymentMethodCreditDebit = findViewById(R.id.cbPaymentMethodCreditDebit);
        btnManageService = findViewById(R.id.btnManageService);
        tvOpeningHrs = findViewById(R.id.tvOpeningHrs);
        tvClosingHrs = findViewById(R.id.tvClosingHrs);
        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        cbSaturday = findViewById(R.id.cbSaturday);
        cbSunday = findViewById(R.id.cbSunday);
        btnSave = findViewById(R.id.btnSave);

        btnManageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServicesListActivity.class);
                intent.putExtra("servicesIdsList", servicesIdsList);
                startActivityForResult(intent, 123);
            }
        });
        Calendar mcurrentTime = Calendar.getInstance();
        openHour = closeHour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        openMinute = closeMinute = mcurrentTime.get(Calendar.MINUTE);
        tvOpeningHrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ClinicEditProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        openHour = selectedHour;
                        openMinute = selectedMinute;
                        tvOpeningHrs.setText(df.format(selectedHour) + ":" + df.format(selectedMinute));
                    }
                }, openHour, openMinute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Opening Time");
                mTimePicker.show();
            }
        });
        tvClosingHrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ClinicEditProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        closeHour = selectedHour;
                        closeMinute = selectedMinute;
                        tvClosingHrs.setText(df.format(selectedHour) + ":" + df.format(selectedMinute));
                    }
                }, closeHour, closeMinute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Closing Time");
                mTimePicker.show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFields())
                    return;

                if (TextUtils.isEmpty(App.user.clinicID)) {
                    final DatabaseReference newRef = firebaseClinics.push();
                    App.user.clinicID = newRef.getKey();
                    firebaseClinicUsers.child(App.user.username).setValue(App.user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            addOrUpdateClinic();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            App.user.clinicID = "";
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    addOrUpdateClinic();
                }
            }
        });

        if (!TextUtils.isEmpty(App.user.clinicID))
            firebaseClinics.child(App.user.clinicID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) return;
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    etName.setText(clinic.name);
                    etAddress.setText(clinic.address);
                    etPhone.setText(clinic.phone);
                    cb1.setChecked(clinic.insuranceTypes.contains(getResources().getString(R.string.insurance_type_1)));
                    cb2.setChecked(clinic.insuranceTypes.contains(getResources().getString(R.string.insurance_type_2)));
                    cb3.setChecked(clinic.insuranceTypes.contains(getResources().getString(R.string.insurance_type_3)));
                    cb4.setChecked(clinic.insuranceTypes.contains(getResources().getString(R.string.insurance_type_4)));
                    cb5.setChecked(clinic.insuranceTypes.contains(getResources().getString(R.string.insurance_type_5)));
                    cbPaymentMethodCash.setChecked(clinic.paymentMethodAccepted == Clinic.PAYMENT_METHOD_BOTH || clinic.paymentMethodAccepted == Clinic.PAYMENT_METHOD_CASH);
                    cbPaymentMethodCreditDebit.setChecked(clinic.paymentMethodAccepted == Clinic.PAYMENT_METHOD_BOTH || clinic.paymentMethodAccepted == Clinic.PAYMENT_METHOD_CREDIT_DEBIT);
                    openHour = clinic.openHour;
                    openMinute = clinic.openMinute;
                    closeHour = clinic.closeHour;
                    closeMinute = clinic.closeMinute;
                    tvOpeningHrs.setText(clinic.openHour + ":" + clinic.openMinute);
                    tvClosingHrs.setText(clinic.closeHour + ":" + clinic.closeMinute);
                    if (clinic.workingDays.size() == 7) {
                        cbMonday.setChecked(clinic.workingDays.get(0));
                        cbTuesday.setChecked(clinic.workingDays.get(1));
                        cbWednesday.setChecked(clinic.workingDays.get(2));
                        cbThursday.setChecked(clinic.workingDays.get(3));
                        cbFriday.setChecked(clinic.workingDays.get(4));
                        cbSaturday.setChecked(clinic.workingDays.get(5));
                        cbSunday.setChecked(clinic.workingDays.get(6));
                    }
                    if (clinic.servicesIdsList != null && !clinic.servicesIdsList.isEmpty()) {
                        servicesIdsList.clear();
                        servicesIdsList.addAll(clinic.servicesIdsList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void addOrUpdateClinic() {
        ArrayList<String> insuranceTypeList = new ArrayList<>();
        if (cb1.isChecked()) insuranceTypeList.add(cb1.getText().toString());
        if (cb2.isChecked()) insuranceTypeList.add(cb2.getText().toString());
        if (cb3.isChecked()) insuranceTypeList.add(cb3.getText().toString());
        if (cb4.isChecked()) insuranceTypeList.add(cb4.getText().toString());
        if (cb5.isChecked()) insuranceTypeList.add(cb5.getText().toString());

        int paymentMethod = Clinic.PAYMENT_METHOD_DEFAULT;
        if (cbPaymentMethodCash.isChecked() && cbPaymentMethodCreditDebit.isChecked()) {
            paymentMethod = Clinic.PAYMENT_METHOD_BOTH;
        } else {
            if (cbPaymentMethodCash.isChecked())
                paymentMethod = Clinic.PAYMENT_METHOD_CASH;
            if (cbPaymentMethodCreditDebit.isChecked())
                paymentMethod = Clinic.PAYMENT_METHOD_CREDIT_DEBIT;
        }

        ArrayList<Boolean> workingDaysList = new ArrayList<>();
        workingDaysList.add(cbMonday.isChecked());
        workingDaysList.add(cbTuesday.isChecked());
        workingDaysList.add(cbWednesday.isChecked());
        workingDaysList.add(cbThursday.isChecked());
        workingDaysList.add(cbFriday.isChecked());
        workingDaysList.add(cbSaturday.isChecked());
        workingDaysList.add(cbSunday.isChecked());

        firebaseClinics.child(App.user.clinicID).setValue(new Clinic(
                etName.getText().toString().trim(),
                etAddress.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                insuranceTypeList,
                paymentMethod,
                openHour,
                openMinute,
                closeHour,
                closeMinute,
                workingDaysList,
                servicesIdsList
        )).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Clinic Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields() {
        if (etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter name", Toast.LENGTH_LONG).show();
            return false;
        } else if (etAddress.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter address", Toast.LENGTH_LONG).show();
            return false;
        } else if (etPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter contact", Toast.LENGTH_LONG).show();
            return false;
        } /*else if (!cb1.isChecked() && !cb2.isChecked() && !cb3.isChecked() && !cb4.isChecked() && !cb5.isChecked()) {
            Toast.makeText(getApplicationContext(), "Please select at least one insurance type", Toast.LENGTH_LONG).show();
            return false;
        }*/ else if (!cbPaymentMethodCash.isChecked() && !cbPaymentMethodCreditDebit.isChecked()) {
            Toast.makeText(getApplicationContext(), "Please select at least one payment method", Toast.LENGTH_LONG).show();
            return false;
        } else if (servicesIdsList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please add at least one Service", Toast.LENGTH_LONG).show();
            return false;
        } else if (tvOpeningHrs.getText().equals(getResources().getString(R.string.not_available))) {
            Toast.makeText(getApplicationContext(), "Please specify opening hours", Toast.LENGTH_LONG).show();
            return false;
        } else if (tvClosingHrs.getText().equals(getResources().getString(R.string.not_available))) {
            Toast.makeText(getApplicationContext(), "Please specify closing hours", Toast.LENGTH_LONG).show();
            return false;
        } else if (!cbMonday.isChecked() && !cbTuesday.isChecked() && !cbWednesday.isChecked() && !cbThursday.isChecked() && !cbFriday.isChecked() && !cbSaturday.isChecked() && !cbSunday.isChecked()) {
            Toast.makeText(getApplicationContext(), "Please select at least one weekly day", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 123) {
            if (data != null && data.hasExtra("servicesIdsList"))
                servicesIdsList = (ArrayList<String>) data.getSerializableExtra("servicesIdsList");
        }
    }
}
