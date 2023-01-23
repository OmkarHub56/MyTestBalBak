package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Adapters.GroupMemberSelectAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.MemberAdminCountUpdater;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity implements MemberAdminCountUpdater, View.OnClickListener {

    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;
    int member_count=1,admin_count=1;

    EditText enterGroupName;

    TextView memberCountShower,adminCountShower;

    List<Boolean> isAdded,isAdmin;
    List<MyUsers> friends;
    RecyclerView usersToAdd;
    GroupMemberSelectAdapter adapter;

    Button createGroupBtn;

    String []colorNames={"yellow","light_blue","white","pink","green","gray"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getUid();
        database=FirebaseDatabase.getInstance();

        enterGroupName=findViewById(R.id.enter_group_name);

        memberCountShower=findViewById(R.id.member_count_shower);
        adminCountShower=findViewById(R.id.admin_count_shower);

        isAdded=new ArrayList<>();
        isAdmin=new ArrayList<>();
        friends=new ArrayList<>();
        usersToAdd=findViewById(R.id.friends_list_rv);
        adapter=new GroupMemberSelectAdapter(friends,isAdded,isAdmin,this,this);
        usersToAdd.setLayoutManager(new LinearLayoutManager(this));
        usersToAdd.setAdapter(adapter);

        createGroupBtn=findViewById(R.id.create_group_btn);
        createGroupBtn.setOnClickListener(this);

        database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.FRIENDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    database.getReference().child(ConstantsClass.USERS).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            MyUsers user=snapshot.getValue(MyUsers.class);
                            user.setUserId(snapshot.getKey());
                            friends.add(user);
                            isAdded.add(false);
                            isAdmin.add(false);
                            adapter.notifyItemInserted(friends.size()-1);
                            Log.i("yuv","aya");
                            Log.i("yuv",String.valueOf(friends.size()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                Log.i("yuv",String.valueOf(friends.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void update(int m, int a) {
        m=m+1;
        a=a+1;
        if(m==1){
            memberCountShower.setText("1 member");
        }
        else{
            memberCountShower.setText(m+" members");
        }
        if(a==1){
            adminCountShower.setText("1 admin");
        }
        else{
            adminCountShower.setText(a+" admins");
        }
        member_count=m;
        admin_count=a;
    }

    @Override
    public void onClick(View view) {
        if(view==createGroupBtn){
            if(member_count==1){
                Toast.makeText(this, "Can't create group with less than 3 members", Toast.LENGTH_SHORT).show();
                return;
            }
            String groupName=enterGroupName.getText().toString();
            if(groupName.length()<3 || groupName.length()>15){
                Toast.makeText(this, "Name of the group should at between 3 and 15 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            // generate group id by push method
            String key=database.getReference().child(ConstantsClass.GROUP_CHATS).push().getKey();
            database.getReference().child(ConstantsClass.GROUP_CHATS).child(key).child(ConstantsClass.GROUP_NAME).setValue(groupName);
            database.getReference().child(ConstantsClass.GROUP_CHATS).child(key).child(ConstantsClass.GROUP_ID).setValue(key);

            DatabaseReference thisGroupMembers=database.getReference().child(ConstantsClass.GROUP_CHATS).child(key).child(ConstantsClass.GROUP_MEMBERS_INFO);
            DatabaseReference thisGroupAdmins=database.getReference().child(ConstantsClass.GROUP_CHATS).child(key).child(ConstantsClass.GROUP_ADMINS);

            // adding current user to the group
            thisGroupMembers.child(sender_id).child(ConstantsClass.UNREAD_MESSAGE_COUNT).setValue(0);
            thisGroupMembers.child(sender_id).child(ConstantsClass.GROUP_MEMBERS_COLOR).setValue(colorNames[new Random().nextInt(5)]);
            database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.GROUPS_ADDED_TO).child(key).setValue(true);

            for(int i=0;i<friends.size();i++){
                if(isAdded.get(i)){

                    // the zero here also indicates the unread chats for the specific user
                    thisGroupMembers.child(friends.get(i).getUserId()).child(ConstantsClass.UNREAD_MESSAGE_COUNT).setValue(0);
                    thisGroupMembers.child(friends.get(i).getUserId()).child(ConstantsClass.GROUP_MEMBERS_COLOR).setValue(colorNames[new Random().nextInt(5)]);
                    database.getReference().child(ConstantsClass.USERS).child(friends.get(i).getUserId()).child(ConstantsClass.GROUPS_ADDED_TO).child(key).setValue(true);
                }
                if(isAdmin.get(i)){
                    thisGroupAdmins.child(friends.get(i).getUserId()).setValue(true);
                }
            }


        }
    }
}