package com.myclinicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ServicesListActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference firebaseServicesReference;
    private RecyclerView rv;
    private RvAdapter adapter;
    private List<Service> servicesList = new ArrayList<>();
    private ChildEventListener childEventListener;
    private ArrayList<String> servicesIdsList = new ArrayList<>();
    private boolean isAdmin = true;
    private boolean fromPatientSearchScreen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_list);

        setTitle("Services");

        database = FirebaseDatabase.getInstance();
        firebaseServicesReference = database.getReference(App.FIREBASE_DB_PATH_SERVICES);

        Intent intent = getIntent();
        if (intent.hasExtra("servicesIdsList")) {
            isAdmin = false;
            servicesIdsList = (ArrayList<String>) intent.getSerializableExtra("servicesIdsList");
        } else if (intent.hasExtra("fromPatientSearchScreen")) {
            isAdmin = false;
            fromPatientSearchScreen = true;
        }

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapter = new RvAdapter(isAdmin, servicesList, servicesIdsList, new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(Service item, int adapterPosition) {
                if (servicesIdsList.contains(item.id)) {
                    servicesIdsList.remove(item.id);
                } else {
                    servicesIdsList.add(item.id);
                }
                adapter.notifyItemChanged(adapterPosition);
            }

            @Override
            public void onItemEditClicked(Service item, int adapterPosition) {
                editServiceAlertDialog(item);
            }

            @Override
            public void onItemDeleteClicked(final Service item, int adapterPosition) {
                new AlertDialog.Builder(ServicesListActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this service?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteService(item.id);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                servicesList.add(dataSnapshot.getValue(Service.class));
                adapter.notifyItemInserted(servicesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Service newService = dataSnapshot.getValue(Service.class);
                adapter.updateItem(newService);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String serviceId = dataSnapshot.getKey();
                adapter.removeItemWithId(serviceId);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load services.", Toast.LENGTH_SHORT).show();
            }
        };
        firebaseServicesReference.addChildEventListener(childEventListener);
    }

    private void deleteService(String serviceID) {
        firebaseServicesReference.child(serviceID).removeValue();
    }

    @Override
    protected void onStop() {
        if (firebaseServicesReference != null && childEventListener != null)
            firebaseServicesReference.removeEventListener(childEventListener);
        super.onStop();
    }

    public static class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

        private boolean isAdmin;
        private List<Service> list;
        private ArrayList<String> servicesIdsList;
        private OnItemClickListener onItemClickListener;

        RvAdapter(boolean isAdmin, List<Service> list, ArrayList<String> servicesIdsList, OnItemClickListener onItemClickListener) {
            this.isAdmin = isAdmin;
            this.list = list;
            this.servicesIdsList = servicesIdsList;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false), onItemClickListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Service item = list.get(position);
            holder.tvTitle.setText(item.name);
            if (!isAdmin) {
                holder.cbSelect.setChecked(servicesIdsList.contains(item.id));
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        void removeItemWithId(String serviceId) {
            for (int i = 0; i < list.size(); i++) {
                Service service = list.get(i);
                if (service.id.equals(serviceId)) {
                    list.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }

        void updateItem(Service service) {
            for (int i = 0; i < list.size(); i++) {
                Service service1 = list.get(i);
                if (service1.id.equals(service.id)) {
                    list.set(i, service);
                    notifyItemChanged(i);
                    break;
                }
            }
        }

        public interface OnItemClickListener {
            void onItemClicked(Service item, int adapterPosition);

            void onItemEditClicked(Service item, int adapterPosition);

            void onItemDeleteClicked(Service item, int adapterPosition);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvTitle;
            ImageView ivEdit, ivDelete;
            AppCompatCheckBox cbSelect;
            private OnItemClickListener listener;

            public ViewHolder(View itemView, OnItemClickListener listener1) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                ivEdit = itemView.findViewById(R.id.ivEdit);
                ivDelete = itemView.findViewById(R.id.ivDelete);
                cbSelect = itemView.findViewById(R.id.cbSelect);
                this.listener = listener1;
                itemView.setOnClickListener(this);
                if (isAdmin) {
                    cbSelect.setVisibility(View.GONE);
                    ivEdit.setVisibility(View.VISIBLE);
                    ivDelete.setVisibility(View.VISIBLE);
                    ivEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getAdapterPosition() == -1 || listener == null) return;
                            listener.onItemEditClicked(list.get(getAdapterPosition()), getAdapterPosition());
                        }
                    });
                    ivDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getAdapterPosition() == -1 || listener == null) return;
                            listener.onItemDeleteClicked(list.get(getAdapterPosition()), getAdapterPosition());
                        }
                    });
                } else {
                    cbSelect.setVisibility(View.VISIBLE);
                    cbSelect.setClickable(false);
                    ivEdit.setVisibility(View.GONE);
                    ivDelete.setVisibility(View.GONE);
                }
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() == -1 || listener == null) return;
                listener.onItemClicked(list.get(getAdapterPosition()), getAdapterPosition());
            }
        }
    }

    private void addServiceAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setTitle("Add New Service");
        alert.setView(edittext);
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                addService(edittext.getText().toString().trim());
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    private void addService(String service) {
        if (service.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter service name", Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseReference newRef = firebaseServicesReference.push();
        newRef.setValue(new Service(newRef.getKey(), service));
    }

    private void editServiceAlertDialog(final Service service) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setTitle("Edit Service");
        alert.setView(edittext);
        edittext.setText(service.name);
        alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                editService(service, edittext.getText().toString().trim());
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    private void editService(Service service, String serviceStr) {
        if (serviceStr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter service name", Toast.LENGTH_LONG).show();
            return;
        }
        service.name = serviceStr;
        firebaseServicesReference.child(service.id).setValue(service);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_service, menu);
        menu.findItem(R.id.menuItemAdd).setVisible(isAdmin);
        menu.findItem(R.id.menuItemDone).setVisible(!isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemAdd:
                addServiceAlertDialog();
                return true;
            case R.id.menuItemDone:
                Intent intent = new Intent();
                intent.putExtra("servicesIdsList", servicesIdsList);
                if (fromPatientSearchScreen) {
                    intent.setClass(getApplicationContext(), ClinicListActivity.class);
                    startActivity(intent);
                } else {
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
