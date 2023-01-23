package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.R;

public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;

    DatabaseReference adminCheck;
    ValueEventListener adminCheckListener;
    DataSnapshot adminSnapshot;

    String group_id;

    Button exitGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getUid();
        database=FirebaseDatabase.getInstance();
        group_id=getIntent().getStringExtra("group_id");


        adminCheck=database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_ADMINS);
        adminCheckListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminSnapshot=snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        adminCheck.addValueEventListener(adminCheckListener);



        exitGroup=findViewById(R.id.exit_group_btn);
        exitGroup.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adminCheck.removeEventListener(adminCheckListener);
    }

    @Override
    public void onClick(View view) {
        if(view==exitGroup){
            // todo
        }
    }
}