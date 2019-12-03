package com.myclinicapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClinicListActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference firebaseClinicListReference;
    private DatabaseReference firebaseServiceListReference;
    private RecyclerView rv;
    private RvAdapter adapter;
    private List<Clinic> clinicList = new ArrayList<>();
    private List<String> clinicIdList = new ArrayList<>();
    private List<Service> serviceList = new ArrayList<>();
    private String clinicAddress;
    List<Integer> workingDaysIndex = new ArrayList<>();
    String hour, minute;
    private ArrayList<String> servicesIdsList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_list);

        setTitle("Clinics");

        database = FirebaseDatabase.getInstance();
        firebaseClinicListReference = database.getReference(App.FIREBASE_DB_PATH_CLINICS);
        firebaseServiceListReference = database.getReference(App.FIREBASE_DB_PATH_SERVICES);

        Intent intent = getIntent();
        if (intent.hasExtra("clinicAddress")) {
            clinicAddress = intent.getStringExtra("clinicAddress");
        }
        if (intent.hasExtra("workingDaysIndex")) {
            workingDaysIndex = (List<Integer>) intent.getSerializableExtra("workingDaysIndex");
        }
        if (intent.hasExtra("hour")) {
            hour = intent.getStringExtra("hour");
        }
        if (intent.hasExtra("minute")) {
            minute = intent.getStringExtra("minute");
        }
        if (intent.hasExtra("servicesIdsList")) {
            servicesIdsList = (ArrayList<String>) intent.getSerializableExtra("servicesIdsList");
        }

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapter = new RvAdapter(clinicList, serviceList, new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(Clinic item, int adapterPosition) {

            }

            @Override
            public void onBookClicked(final Clinic item, final int adapterPosition) {
                bookAppointment(clinicIdList.get(adapterPosition), item);
            }
        });
        rv.setAdapter(adapter);

        firebaseServiceListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) return;
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    serviceList.add(child.getValue(Service.class));
                }

                // Fetch clinic list
                firebaseClinicListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) return;
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Clinic clinicChild = child.getValue(Clinic.class);
                            if (!TextUtils.isEmpty(clinicAddress)) {
                                if (clinicChild.address.toLowerCase().contains(clinicAddress.toLowerCase())) {
                                    clinicList.add(clinicChild);
                                    clinicIdList.add(child.getKey());
                                }
                            } else if (!workingDaysIndex.isEmpty() || !TextUtils.isEmpty(hour) || !TextUtils.isEmpty(minute)) {
                                if (!workingDaysIndex.isEmpty() && !TextUtils.isEmpty(hour) && !TextUtils.isEmpty(minute)) {
                                    if (isClinicDayAvailable(clinicChild, workingDaysIndex) && isClinicTimeAvailable(clinicChild, hour, minute)) {
                                        clinicList.add(clinicChild);
                                        clinicIdList.add(child.getKey());
                                    }
                                } else {
                                    if (workingDaysIndex.isEmpty()) {
                                        if (isClinicTimeAvailable(clinicChild, hour, minute)) {
                                            clinicList.add(clinicChild);
                                            clinicIdList.add(child.getKey());
                                        }
                                    } else {
                                        if (isClinicDayAvailable(clinicChild, workingDaysIndex)) {
                                            clinicList.add(clinicChild);
                                            clinicIdList.add(child.getKey());
                                        }
                                    }
                                }
                            } else if (!servicesIdsList.isEmpty()) {
                                if (isServiceAvailable(clinicChild)) {
                                    clinicList.add(clinicChild);
                                    clinicIdList.add(child.getKey());
                                }
                            } else {
                                clinicList.add(clinicChild);
                                clinicIdList.add(child.getKey());
                            }
                        }
                        if (clinicList.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyItemInserted(clinicList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Failed to load clinic list.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load clinic list.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void bookAppointment(String clinicID, Clinic item) {
        Intent intent = new Intent(getApplicationContext(), BookAppointmentActivity.class);
        intent.putExtra("clinicID", clinicID);
        startActivity(intent);
    }

    private boolean isServiceAvailable(Clinic clinicChild) {
        for (String serviceID : servicesIdsList) {
            if (clinicChild.servicesIdsList.contains(serviceID))
                return true;
        }
        return false;
    }

    DecimalFormat df = new DecimalFormat("00");

    private boolean isClinicTimeAvailable(Clinic clinic, String hour, String minute) {
        try {
            Date startTime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(df.format(clinic.openHour) + ":" + df.format(clinic.openMinute) + ":00");
            Date endTime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(df.format(clinic.closeHour) + ":" + df.format(clinic.closeMinute) + ":00");
            Date myTime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(hour + ":" + minute + ":00");
            if (myTime.getTime() == startTime.getTime() || myTime.getTime() == endTime.getTime() || (myTime.after(startTime) && myTime.before(endTime))) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isClinicDayAvailable(Clinic clinic, List<Integer> workingDaysIndex) {
        for (Integer daysIndex : workingDaysIndex) {
            if (clinic.workingDays.get(daysIndex))
                return true;
        }
        return false;
    }

    public static class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

        private List<Clinic> list;
        List<Service> serviceList;
        private OnItemClickListener onItemClickListener;
        DecimalFormat df = new DecimalFormat("00");

        RvAdapter(List<Clinic> list, List<Service> serviceList, OnItemClickListener onItemClickListener) {
            this.list = list;
            this.serviceList = serviceList;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clinic, parent, false), onItemClickListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Clinic item = list.get(position);
            holder.tvTitle.setText(item.name);

            holder.tvAddress.setText(item.address);
            holder.tvPhone.setText(item.phone);
            holder.tvInsuranceTypes.setText(TextUtils.join(",\n", item.insuranceTypes));
            holder.tvPaymentMethod.setText(item.paymentMethodAccepted == Clinic.PAYMENT_METHOD_DEFAULT ? "-" : (item.paymentMethodAccepted == Clinic.PAYMENT_METHOD_BOTH ? "CASH & CARDS" : (item.paymentMethodAccepted == Clinic.PAYMENT_METHOD_CASH ? "CASH" : "CARDS")));
            holder.tvWorkingHours.setText(df.format(item.openHour) + ":" + df.format(item.openMinute) + " - " + df.format(item.closeHour) + ":" + df.format(item.closeMinute));
            holder.tvWorkingDays.setText(TextUtils.join(",\n", getWorkingDaysList(item.workingDays)));
            holder.tvServices.setText(TextUtils.join(",\n", getServiceNamesList(item.servicesIdsList)));

        }

        private List<String> getServiceNamesList(List<String> servicesIdsList) {
            List<String> tempList = new ArrayList<>();
            if (servicesIdsList.isEmpty()) return tempList;
            for (String serviceID : servicesIdsList) {
                for (Service service : serviceList) {
                    if (service.id.equals(serviceID)) {
                        tempList.add(service.name);
                        break;
                    }
                }
            }
            return tempList;
        }

        private List<String> getWorkingDaysList(List<Boolean> workingDays) {
            List<String> tempList = new ArrayList<>();
            if (workingDays.size() != 7) return tempList;
            if (workingDays.get(0)) tempList.add("Monday");
            if (workingDays.get(1)) tempList.add("Tuesday");
            if (workingDays.get(2)) tempList.add("Wednesday");
            if (workingDays.get(3)) tempList.add("Thursday");
            if (workingDays.get(4)) tempList.add("Friday");
            if (workingDays.get(5)) tempList.add("Saturday");
            if (workingDays.get(6)) tempList.add("Sunday");
            if (tempList.size() == 7) {
                tempList.clear();
                tempList.add("All Day");
            }
            return tempList;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public interface OnItemClickListener {
            void onItemClicked(Clinic item, int adapterPosition);

            void onBookClicked(Clinic item, int adapterPosition);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvTitle;
            View containerDetails;
            TextView tvAddress;
            TextView tvPhone;
            TextView tvInsuranceTypes;
            TextView tvPaymentMethod;
            TextView tvWorkingHours;
            TextView tvWorkingDays;
            TextView tvServices;
            AppCompatButton btnBookAppointment;
            private OnItemClickListener listener;

            public ViewHolder(View itemView, OnItemClickListener listener1) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                containerDetails = itemView.findViewById(R.id.containerDetails);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvInsuranceTypes = itemView.findViewById(R.id.tvInsuranceTypes);
                tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
                tvWorkingHours = itemView.findViewById(R.id.tvWorkingHours);
                tvWorkingDays = itemView.findViewById(R.id.tvWorkingDays);
                tvServices = itemView.findViewById(R.id.tvServices);
                btnBookAppointment = itemView.findViewById(R.id.btnBookAppointment);
                this.listener = listener1;
                itemView.setOnClickListener(this);
                btnBookAppointment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getAdapterPosition() == -1 || listener == null) return;
                        listener.onBookClicked(list.get(getAdapterPosition()), getAdapterPosition());
                    }
                });
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() == -1 || listener == null) return;
                listener.onItemClicked(list.get(getAdapterPosition()), getAdapterPosition());
            }
        }
    }

}
