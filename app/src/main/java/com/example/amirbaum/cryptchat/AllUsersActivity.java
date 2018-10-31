package com.example.amirbaum.cryptchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private String mUid;
    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        mUid = mAuth.getCurrentUser().getUid();

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabase.child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        MenuItem ourSearchItem = menu.findItem(R.id.menu_item_search);
        SearchView sv = (SearchView) ourSearchItem.getActionView();

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                startRecyclerView(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_account) {
            Intent settings_intent = new Intent(AllUsersActivity.this, SettingsActivity.class);
            startActivity(settings_intent);
        }
        if (item.getItemId() == R.id.item_all_users) {

        }
        if (item.getItemId() == R.id.item_logout) {
            mAuth.signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
        } else
            return;

        startRecyclerView("");

        super.onStart();
    }

    public void startRecyclerView (final String filter) {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .limitToLast(50);

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                options
        ) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                if (!model.getName().toLowerCase().contains(filter.toLowerCase()) && !filter.equals("")) {
                    holder.hide();
                }

                final String user_id = getRef(position).getKey();

                if (user_id.equals(mUid)) {
                    holder.hide();
                }

                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setProfilePicture(model.getPicture());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent selected_user_intent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        selected_user_intent.putExtra("user_id", user_id);
                        selected_user_intent.putExtra("name", myName);
                        startActivity(selected_user_intent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);
                return new UsersViewHolder(view);
            }


        };

        firebaseRecyclerAdapter.startListening();
        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            mUsersDatabase.child(mUid).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUsersDatabase.child(mUid).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView singleUserImageTV;
            singleUserImageTV = (TextView) mView.findViewById(R.id.single_user_name);
            singleUserImageTV.setText(name);
        }

        public void setStatus(String status) {
            TextView singleUserStatusTV;
            singleUserStatusTV = (TextView) mView.findViewById(R.id.single_user_status);
            singleUserStatusTV.setText(status);
        }

        public void setProfilePicture(String picture) {
            CircleImageView singleUserCIV = (CircleImageView) mView.findViewById(R.id.single_request_image);
            if (!picture.equals("default")) {
                Picasso.get().load(picture).placeholder(R.drawable.user_place_holder).into(singleUserCIV);
            }
        }

        public void hide() {
            //mView.setVisibility(View.INVISIBLE);
            //mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            //mView.setVisibility(View.GONE);
            mView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
        }
    }
}
