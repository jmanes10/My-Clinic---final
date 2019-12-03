package com.myclinicapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ClinicSearchByHoursActivity extends AppCompatActivity {

    TextView tvOpeningHrs;

    SwitchCompat switchAnyDay;
    View containerDays;
    AppCompatCheckBox cbMonday;
    AppCompatCheckBox cbTuesday;
    AppCompatCheckBox cbWednesday;
    AppCompatCheckBox cbThursday;
    AppCompatCheckBox cbFriday;
    AppCompatCheckBox cbSaturday;
    AppCompatCheckBox cbSunday;
    AppCompatButton btnSave;
    DecimalFormat df = new DecimalFormat("00");
    private int openHour, openMinute;
    String hour, minute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_search_by_hours);
        setTitle("Search By Time");

        tvOpeningHrs = findViewById(R.id.tvOpeningHrs);
        switchAnyDay = findViewById(R.id.switchAnyDay);
        containerDays = findViewById(R.id.containerDays);
        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        cbSaturday = findViewById(R.id.cbSaturday);
        cbSunday = findViewById(R.id.cbSunday);
        btnSave = findViewById(R.id.btnSave);

        Calendar mcurrentTime = Calendar.getInstance();
        openHour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        openMinute = mcurrentTime.get(Calendar.MINUTE);
        tvOpeningHrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ClinicSearchByHoursActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        openHour = selectedHour;
                        openMinute = selectedMinute;
                        hour = df.format(selectedHour);
                        minute = df.format(selectedMinute);
                        tvOpeningHrs.setText(df.format(selectedHour) + ":" + df.format(selectedMinute));
                    }
                }, openHour, openMinute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Opening Time");
                mTimePicker.show();
            }
        });

        switchAnyDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                containerDays.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClinicListActivity.class);
                if (!switchAnyDay.isChecked()) {
                    ArrayList<Integer> workingDaysIndex = new ArrayList<>();
                    if (cbMonday.isChecked()) workingDaysIndex.add(0);
                    if (cbTuesday.isChecked()) workingDaysIndex.add(1);
                    if (cbWednesday.isChecked()) workingDaysIndex.add(2);
                    if (cbThursday.isChecked()) workingDaysIndex.add(3);
                    if (cbFriday.isChecked()) workingDaysIndex.add(4);
                    if (cbSaturday.isChecked()) workingDaysIndex.add(5);
                    if (cbSunday.isChecked()) workingDaysIndex.add(6);
                    if (workingDaysIndex.size() != 7)
                        intent.putExtra("workingDaysIndex", workingDaysIndex);
                }
                if (!TextUtils.isEmpty(hour)) {
                    intent.putExtra("hour", hour);
                    intent.putExtra("minute", minute);
                }
                startActivity(intent);
            }
        });

    }

}
