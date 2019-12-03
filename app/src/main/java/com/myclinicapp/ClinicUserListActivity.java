package com.myclinicapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class ClinicUserListActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference firebaseClinicsReference, firebaseUsersReference;
    private RecyclerView rv;
    private RvAdapter adapter;
    private List<User> usersList = new ArrayList<>();
    private ChildEventListener childEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_list);

        setTitle("Clinic");

        database = FirebaseDatabase.getInstance();
        firebaseClinicsReference = database.getReference(App.FIREBASE_DB_PATH_CLINIC_USERS);
        firebaseUsersReference = database.getReference(App.FIREBASE_DB_PATH_USERS);

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapter = new RvAdapter(usersList, new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(User item, int adapterPosition) {

            }

            @Override
            public void onItemDeleteClicked(final User item, int adapterPosition) {
                new AlertDialog.Builder(ClinicUserListActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this clinic account?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUser(item.username);
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
                usersList.add(dataSnapshot.getValue(User.class));
                adapter.notifyItemInserted(usersList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                User newUser = dataSnapshot.getValue(User.class);
                adapter.updateItem(newUser);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getKey();
                adapter.removeItemWithId(userId);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        };
        firebaseClinicsReference.addChildEventListener(childEventListener);
    }

    private void deleteUser(String username) {
        firebaseClinicsReference.child(username).removeValue();
        firebaseUsersReference.child(username).removeValue();
    }

    @Override
    protected void onStop() {
        if (firebaseClinicsReference != null && childEventListener != null)
            firebaseClinicsReference.removeEventListener(childEventListener);
        super.onStop();
    }

    public static class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

        private List<User> list;
        private OnItemClickListener onItemClickListener;

        RvAdapter(List<User> list, OnItemClickListener onItemClickListener) {
            this.list = list;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false), onItemClickListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User item = list.get(position);
            holder.tvTitle.setText(item.fName + " " + item.lName);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        void removeItemWithId(String username) {
            for (int i = 0; i < list.size(); i++) {
                User user = list.get(i);
                if (user.username.equals(username)) {
                    list.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }

        void updateItem(User user) {
            for (int i = 0; i < list.size(); i++) {
                User user1 = list.get(i);
                if (user1.username.equals(user.username)) {
                    list.set(i, user);
                    notifyItemChanged(i);
                    break;
                }
            }
        }

        public interface OnItemClickListener {
            void onItemClicked(User item, int adapterPosition);

            void onItemDeleteClicked(User item, int adapterPosition);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvTitle;
            ImageView ivDelete;
            private OnItemClickListener listener;

            public ViewHolder(View itemView, OnItemClickListener listener1) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                ivDelete = itemView.findViewById(R.id.ivDelete);
                this.listener = listener1;
                itemView.setOnClickListener(this);
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getAdapterPosition() == -1 || listener == null) return;
                        listener.onItemDeleteClicked(list.get(getAdapterPosition()), getAdapterPosition());
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
