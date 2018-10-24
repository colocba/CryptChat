package com.example.amirbaum.cryptchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private RelativeLayout mNoFriendsLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String mCurrent_user_id;

    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mNoFriendsLayout = (RelativeLayout) mMainView.findViewById(R.id.no_friends_layout);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        setHasOptionsMenu(true);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        startRecyclerView("");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.navigation_menu, menu);


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
    }

    private void startRecyclerView(final String filter) {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends").child(mCurrent_user_id)
                .limitToLast(50);

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {

                holder.setStatus(model.getStatus());

                final String list_user_id = getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String friendsName = dataSnapshot.child("name").getValue().toString();

                        if (!friendsName.contains(filter) && !filter.equals("")) {
                            holder.hide();
                            return;
                        }

                        holder.setName(friendsName);
                        holder.setStatus(dataSnapshot.child("status").getValue().toString());
                        holder.setProfilePicture(dataSnapshot.child("picture").getValue().toString());
                        holder.setOnlineStatus(dataSnapshot.child("online").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Intent selected_user_intent = new Intent(getContext(), ProfileActivity.class);
                                    selected_user_intent.putExtra("user_id", list_user_id);
                                    startActivity(selected_user_intent);
                                }

                                if (which == 1) {
                                    Intent chat_intent = new Intent(getContext(), ChatActivity.class);
                                    chat_intent.putExtra("user_id", list_user_id);
                                    startActivity(chat_intent);
                                }
                            }
                        });

                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    mFriendsList.setVisibility(View.GONE);
                    mNoFriendsLayout.setVisibility(View.VISIBLE);
                } else {
                    mFriendsList.setVisibility(View.VISIBLE);
                    mNoFriendsLayout.setVisibility(View.GONE);
                }
            }
        };

        firebaseRecyclerAdapter.startListening();
        mFriendsList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mFriendsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setStatus(String status) {
            TextView singleUserImageTV;
            singleUserImageTV = (TextView) mView.findViewById(R.id.single_user_status);
            singleUserImageTV.setText(status);
        }

        public void setName(String name) {
            TextView singleUserImageTV;
            singleUserImageTV = (TextView) mView.findViewById(R.id.single_user_name);
            singleUserImageTV.setText(name);
        }

        public void setProfilePicture(String picture) {
            CircleImageView singleUserCIV = (CircleImageView) mView.findViewById(R.id.single_request_image);
            if (!picture.equals("default")) {
                Picasso.get().load(picture).placeholder(R.drawable.user_place_holder).into(singleUserCIV);
            }
        }

        public void setOnlineStatus(String onlineStatus) {
            ImageView onlineStatusImage = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if (onlineStatus.equals("true")) {
                onlineStatusImage.setVisibility(View.VISIBLE);
            } else {
                onlineStatusImage.setVisibility(View.INVISIBLE);
            }
        }

        public void hide() {
            //mView.setVisibility(View.INVISIBLE);
            //mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            mView.setVisibility(View.GONE);
        }

    }

}
