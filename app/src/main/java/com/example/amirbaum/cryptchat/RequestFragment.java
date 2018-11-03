package com.example.amirbaum.cryptchat;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mRequestFriendshipList;
    private RelativeLayout mNoRequestLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mRootRef;

    private String mCurrent_user_id;
    private String mCurrentUserName;

    private View mMainView;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestFriendshipList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mNoRequestLayout = (RelativeLayout) mMainView.findViewById(R.id.no_requests_layout);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mRequestFriendshipList.setHasFixedSize(true);
        mRequestFriendshipList.setLayoutManager(new LinearLayoutManager(getContext()));

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRootRef.child("Users").child(mCurrent_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCurrentUserName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.keepSynced(true);

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        startRecyclerView("");

    }

    private void startRecyclerView(String filter) {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends_Req").child(mCurrent_user_id)
                .limitToLast(50);

        FirebaseRecyclerOptions<FriendRequest> options =
                new FirebaseRecyclerOptions.Builder<FriendRequest>()
                        .setQuery(query, FriendRequest.class)
                        .build();

        FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position, @NonNull FriendRequest model) {

                String req_type = model.getRequest_type();

                if (req_type.equals("received")) {

                    final String list_user_id = getRef(position).getKey();

                    mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            holder.setName(dataSnapshot.child("name").getValue().toString());
                            holder.setImage(dataSnapshot.child("picture").getValue().toString());
                            holder.setUserID(list_user_id);
                            holder.setButtons();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    holder.hide();
                }

            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_request_layout, parent, false);
                return new FriendRequestViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                int itemCount = getItemCount();
                if (getItemCount() == 0) {
                    mRequestFriendshipList.setVisibility(View.GONE);
                    mNoRequestLayout.setVisibility(View.VISIBLE);
                } else {
                    mRequestFriendshipList.setVisibility(View.VISIBLE);
                    mNoRequestLayout.setVisibility(View.GONE);
                }
            }
        };

        firebaseRecyclerAdapter.startListening();
        mRequestFriendshipList.setAdapter(firebaseRecyclerAdapter);

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
                if (!newText.equals("")) {
                    Toasty.info(getContext(), "There is nothing to search here...", Toast.LENGTH_SHORT, true).show();
                }
                return true;
            }
        });
    }

    private class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        Button mAcceptRequestBtn, mDeclineRequestBtn;
        String mUserID;
        String mUserName;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            mUserName = name;
            TextView nameOfSenderTV = (TextView)mView.findViewById(R.id.single_request_name_friend);
            nameOfSenderTV.setText(mUserName + " want's to be your friend!");
        }

        public void setImage(String picture) {
            CircleImageView senderImage = (CircleImageView)mView.findViewById(R.id.single_request_image);
            if (!picture.equals("default")) {
                Picasso.get().load(picture).placeholder(R.drawable.user_place_holder).into(senderImage);
            }
        }

        public void hide() {
            mView.setVisibility(View.GONE);
            //mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

        public void setButtons() {

            mAcceptRequestBtn = (Button)mView.findViewById(R.id.single_request_accept);
            mDeclineRequestBtn = (Button)mView.findViewById(R.id.single_request_decline);

            mAcceptRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();

                    // Entering the date of the recent friendship in respective friends fields
                    friendsMap.put("Friends/" + mCurrent_user_id + "/" + mUserID + "/date", currentDate);
                    friendsMap.put("Friends/" + mCurrent_user_id + "/" + mUserID + "/friends_name", mUserName);
                    friendsMap.put("Friends/" + mUserID + "/" + mCurrent_user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + mUserID + "/" + mCurrent_user_id + "/friends_name", mCurrentUserName);

                    // Deleting friend request between both of the users
                    friendsMap.put("Friends_Req/" + mCurrent_user_id + "/" + mUserID, null);
                    friendsMap.put("Friends_Req/" + mUserID + "/" + mCurrent_user_id, null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            Toasty.success(getContext(), "You are now friends with " + mUserName + "!", Toast.LENGTH_SHORT, true).show();
                        }
                    });

                }
            });

            mDeclineRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map requestMap = new HashMap();

                    // Deleting friend request between both of the users
                    requestMap.put("Friends_Req/" + mCurrent_user_id + "/" + mUserID, null);
                    requestMap.put("Friends_Req/" + mUserID + "/" + mCurrent_user_id, null);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            Toasty.success(getContext(), "You are not friend of " + mUserName + " anymore!", Toast.LENGTH_SHORT, true).show();
                        }
                    });

                }
            });
        }

        public void setUserID(String userID) {
            this.mUserID = userID;
        }
    }
}
