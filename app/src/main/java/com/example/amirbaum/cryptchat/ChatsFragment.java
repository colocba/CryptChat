package com.example.amirbaum.cryptchat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
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

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;
    private RelativeLayout mNoChatsLayout;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseRecyclerAdapter<Conv, ConvViewHolder> mFirebaseRecyclerAdapter;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private String mMyName;

    private View mMainView;
    private SharedPreferences mSp;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mNoChatsLayout = (RelativeLayout) mMainView.findViewById(R.id.no_chats_layout);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        // GETTING MY NAME FOR SEARCH FILTER PURPOSES
        mUsersDatabase.child(mCurrent_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMyName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mSp = getContext().getSharedPreferences("myPref", Context.MODE_PRIVATE);

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

    private class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;
        String mPrivateKey;
        String mEncryptionType;
        String mType;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setEncryption(String encryptionType) {
            mEncryptionType = encryptionType;
        }

        public void setPrivateKey(String privateKey) {
            mPrivateKey = privateKey;
        }

        public void setMessage(String message, boolean isSeen) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.single_user_status);

            if (mEncryptionType.equals("RSA")) {
                if (mType.equals("text")) {
                    userStatusView.setText(EncryptionDecryptionUtility.RSAdecryptMessage(message, mPrivateKey));
                } else
                    userStatusView.setText("Image");
            } else {
                if (mType.equals("text")) {
                    userStatusView.setText(EncryptionDecryptionUtility.AESdecryptMessage(message, mPrivateKey));
                } else
                    userStatusView.setText("Image");
            }

            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.single_request_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.user_place_holder).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if (online_status.equals("true")) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }

        public void hide() {
            mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            mView.setVisibility(View.GONE);
        }

        public void setType(String type) {
            this.mType = type;
        }
    }

    public void startRecyclerView(final String filter) {

        Query query = mConvDatabase
                .orderByChild("timestamp")
                .limitToLast(50);

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(query, Conv.class)
                        .build();


        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {

                final String list_user_id = getRef(position).getKey();

                if (!model.getOther_user_name().toLowerCase().contains(filter.toLowerCase()) && !filter.equals("")) {
                    holder.hide();
                    return;
                }

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        //SharedPreferences sp = getContext().getSharedPreferences("myPref", Context.MODE_PRIVATE);
                        String from = dataSnapshot.child("from").getValue().toString();
                        String type = dataSnapshot.child("type").getValue().toString();
                        holder.setType(type);

                        if (from.equals(mCurrent_user_id)) {
                            String AESKey = mSp.getString("aes_key_" + mCurrent_user_id, "");
                            String data = dataSnapshot.child("aes_encrypted_message").getValue().toString();
                            holder.setEncryption("AES");
                            holder.setPrivateKey(AESKey);
                            holder.setMessage(data, model.isSeen());
                        } else {
                            String privateKey = mSp.getString("private_key_" + mCurrent_user_id, "");
                            String data = dataSnapshot.child("rsa_encrypted_message").getValue().toString();
                            holder.setEncryption("RSA");
                            holder.setPrivateKey(privateKey);
                            holder.setMessage(data, model.isSeen());
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();

                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);

                        }

                        holder.setName(userName);
                        holder.setUserImage(userThumb, getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                chatIntent.putExtra("my_name", mMyName);
                                startActivity(chatIntent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);
                return new ConvViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    mNoChatsLayout.setVisibility(View.VISIBLE);
                    mConvList.setVisibility(View.GONE);
                } else {
                    mNoChatsLayout.setVisibility(View.GONE);
                    mConvList.setVisibility(View.VISIBLE);
                }
            }
        };

        mFirebaseRecyclerAdapter.startListening();

        //mConvList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        mConvList.setAdapter(mFirebaseRecyclerAdapter);

    }
}
