package com.example.amirbaum.cryptchat;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;

import es.dmoral.toasty.Toasty;

public class UserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private NavigationView mNavigationView;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    private DatabaseReference mMyMessagesRef;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        mViewPager = (ViewPager)findViewById(R.id.main_tab_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mMyMessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid());
        }

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);

        mTabLayout.setupWithViewPager(mViewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_account) {
            Intent settings_intent = new Intent(UserActivity.this, SettingsActivity.class);
            startActivity(settings_intent);
        }
        if (item.getItemId() == R.id.item_all_users) {
            Intent settings_intent = new Intent(UserActivity.this, AllUsersActivity.class);
            startActivity(settings_intent);
        }
        if (item.getItemId() == R.id.item_logout) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent startIntent = new Intent(UserActivity.this, MainActivity.class);
            startActivity(startIntent);
            finish();
        } else {
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
