package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Adapters.FragmentAdapter;
import com.myapps.bakbak.Adapters.MyUserlistAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListActivity extends AppCompatActivity{

    private static final String CHANNEL_ID="chat_notification";

    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;
    DatabaseReference userProfileRef;
    ValueEventListener profilePicListener;


    CircleImageView openSettings;
    CircleImageView openProfile;
    ImageView openMenuButton;

    EditText searchBar;

    TabLayout tabLayout;
    FragmentAdapter adapter;
    ViewPager2 viewPager2;

    String CHATS_TAB="Chats",SENT_TAB="Sent",RECEIVED_TAB="Received";

    // if user logs out or deletes account then finish this activity so that user can not return back to this activity
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getCurrentUser().getUid();
        database=FirebaseDatabase.getInstance();

        userProfileRef=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.PROFILE_PIC);
        profilePicListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    openProfile.setImageResource(R.drawable.user_icon);
                }
                else{
                    Glide.with(FriendsListActivity.this).load(snapshot.getValue(String.class)).into(openProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userProfileRef.addValueEventListener(profilePicListener);

        // this button opens settings
        openSettings=findViewById(R.id.open_settings_btn);
        openSettings.setOnClickListener(view -> {
            Intent intent=new Intent(FriendsListActivity.this,SettingsActivity.class);
            startActivity(intent);
        });

        openProfile=findViewById(R.id.open_profile_btn);
        openProfile.setOnClickListener(view -> {
            Intent intent=new Intent(FriendsListActivity.this,ProfileActivity.class);
            startActivity(intent);
        });

        openMenuButton=findViewById(R.id.menu_btn);
        openMenuButton.setOnClickListener(view -> openMenu());

        searchBar=findViewById(R.id.editTextTextPersonName);
        searchBar.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i==EditorInfo.IME_ACTION_SEARCH) {
                int pos=tabLayout.getSelectedTabPosition();
                if(pos==0){
                    // todo
                    // this will select only the required friends
                }
                else if(pos==1){
                    adapter.getSentFragment().searchUser(searchBar.getText().toString());
                }
                else if(pos==2){
                    // do nothing
                }

                return true;
            }
            return false;
        });

        tabLayout=findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(CHATS_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(SENT_TAB));
        tabLayout.addTab(tabLayout.newTab().setText(RECEIVED_TAB));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos=tab.getPosition();
                if(pos==0){
                    searchBar.setHint("Search for friends");
                }
                else if(pos==1){
                    searchBar.setHint("Search for potential friends");
                }
                else if(pos==2){
                    searchBar.setHint("Search for invites");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2=findViewById(R.id.view_pager);
        adapter=new FragmentAdapter(getSupportFragmentManager(),getLifecycle(),this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout,viewPager2, (tab, position) -> {
            if(position==0){
                tab.setText(CHATS_TAB);
            }
            else if(position==1){
                tab.setText(SENT_TAB);
            }
            else if(position==2){
                tab.setText(RECEIVED_TAB);
            }
        }).attach();

        // todo do this even when app is closed this is just test
//        database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.NEW_MESSAGES).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                showPushNotification(snapshot.getValue(String.class));
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        // receive broadcast from either profile activity or settings activity when user logs out
        // or deletes account to finish this activity as well
        broadcastReceiver= new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));
    }

//    public void showPushNotification(String message){
//
//        NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        Drawable drawable= ResourcesCompat.getDrawable(getResources(),R.drawable.user_icon,null);
//        BitmapDrawable bmd= (BitmapDrawable) drawable;
//        Bitmap largeIcon=bmd.getBitmap();
//
//        Notification notification;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            notification=new Notification.Builder(this)
//                    .setLargeIcon(largeIcon)
//                    .setSmallIcon(R.drawable.user_icon)
//                    .setAutoCancel(true)
//                    .setContentText(message)
//                    .setSubText("New message")
//                    .setChannelId(CHANNEL_ID)
//                    .build();
//
//            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"new_channel",NotificationManager.IMPORTANCE_HIGH));
//        }
//        else{
//            notification=new Notification.Builder(this)
//                    .setLargeIcon(largeIcon)
//                    .setSmallIcon(R.drawable.user_icon)
//                    .setAutoCancel(true)
//                    .setContentText(message)
//                    .setSubText("New message")
//                    .build();
//        }
//
//        manager.notify(100,notification);
//    }

    BottomSheetDialog menuDialog;
    public void openMenu(){

        // open the bottom dialog with create group button
        menuDialog=new BottomSheetDialog(this,R.style.MyDialogStyle);
        menuDialog.setContentView(R.layout.menu_dialog);

        // to create a new group
        TextView createNewGroup=menuDialog.findViewById(R.id.textView5);
        createNewGroup.setOnClickListener(view1 -> {
            createNewGroup();
        });
        menuDialog.show();

        // closes the menu dialog
        ImageView closeDialog=menuDialog.findViewById(R.id.close_dialog_btn);
        closeDialog.setOnClickListener(view12 -> {
            menuDialog.dismiss();
        });
    }

    public void createNewGroup(){
        Intent intent=new Intent(this,CreateGroupActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userProfileRef.removeEventListener(profilePicListener);
    }
}