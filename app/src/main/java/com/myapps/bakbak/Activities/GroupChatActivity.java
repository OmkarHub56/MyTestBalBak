package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Adapters.ChatsAdapter;
import com.myapps.bakbak.Adapters.GroupChatsAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MessageModel;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseDatabase database;

    String sender_id,group_id;
    String senderRoom,receiverRoom;

    ConstraintLayout nameBar;
    ImageView backButton;
    CircleImageView group_profile_pic;
    TextView groupName;
    CircleImageView online_offline_status_symbol;
    TextView online_count_shower;
    Map<String,Boolean> onlineOfflineSaver;
    int online_count=0;
    ImageView openMenuButton;

    ConstraintLayout toolBar;
    ImageView backFromToolbar;
    TextView selectedChatsCount;
    ImageView unselectAllChats;
    ImageView selectAllChats;
    ImageView deleteSelChats;

    RecyclerView chatsView;
    GroupChatsAdapter adapter;
    List<MessageModel> messageList=new ArrayList<>();
    Map<String,String> userColor=new HashMap<>();
    Map<String, MyUsers> userMap=new HashMap<>();

    EditText enteredMessage;
    CircleImageView send_message_button;
    String []colorNames={"yellow","light_blue","white","pink","green","gray"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        // the sender id
        sender_id=auth.getUid();
        group_id=getIntent().getStringExtra("group_id");

        nameBar=findViewById(R.id.name_bar);

        // back to friends list activity
        backButton=findViewById(R.id.back_btn);
        backButton.setOnClickListener(view -> onBackPressed());

        group_profile_pic=findViewById(R.id.receiver_profilepic);
        String profilePicture=getIntent().getStringExtra("group_profile_pic");
        Glide.with(this).load(profilePicture==null?R.drawable.user_icon:profilePicture).into(group_profile_pic);

        groupName=findViewById(R.id.receiver_username);
        groupName.setText(getIntent().getStringExtra("group_name"));

        online_offline_status_symbol=findViewById(R.id.status_symbol);
        online_count_shower=findViewById(R.id.online_count);
        onlineOfflineSaver=new HashMap<>();
        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean status=false;
//                if(snapshot.exists()) {
//
//                    // todo later
//
//                    status=snapshot.getValue(Boolean.class);
//                }

                // going through all group members
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String userid=dataSnapshot.getKey();
                    if(userid.equals(sender_id)){
                        continue;
                    }
                    database.getReference().child(ConstantsClass.USERS).child(userid).child("online").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue(Boolean.class)){

                                if(!onlineOfflineSaver.containsKey(userid) || !onlineOfflineSaver.get(userid)){
                                    online_count++;
                                }
                                onlineOfflineSaver.put(userid,true);

                            }
                            else{
                                if(onlineOfflineSaver.containsKey(userid) && onlineOfflineSaver.get(userid)){
                                    online_count--;
                                }
                                onlineOfflineSaver.put(userid,false);
                            }
                            updateOnlineCountShower();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                // todo later
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // opens the menu
        openMenuButton=findViewById(R.id.menu_btn);
        openMenuButton.setOnClickListener(view -> openMenu());

        toolBar=findViewById(R.id.tools_bar);

        backFromToolbar=findViewById(R.id.back_from_toolbar);
        backFromToolbar.setOnClickListener(this);

        selectedChatsCount=findViewById(R.id.selected_chats_count);

        unselectAllChats=findViewById(R.id.unselect_all_btn);
        unselectAllChats.setOnClickListener(this);

        selectAllChats=findViewById(R.id.select_all_btn);
        selectAllChats.setOnClickListener(this);

        deleteSelChats=findViewById(R.id.delete_btn);
        deleteSelChats.setOnClickListener(this);

        adapter=new GroupChatsAdapter(this,messageList,sender_id,userColor,userMap);
        chatsView=findViewById(R.id.chats_recyclerview);
        chatsView.setLayoutManager(new LinearLayoutManager(this));
        chatsView.setAdapter(adapter);

        enteredMessage=findViewById(R.id.message);

        send_message_button=findViewById(R.id.send_message_button);
        send_message_button.setOnClickListener(this);

        addValueEventListeners();

//        Date date = new Date();
        // in future I can include dates as well along with time
//        DateFormat df = new SimpleDateFormat("HH:mm");
//
//        df.setTimeZone(TimeZone.getTimeZone("Europe/London"));
//        Log.i("gf",df.format(date));
//
//        DateFormat df2 = new SimpleDateFormat("HH:mm");
//        df.setTimeZone(TimeZone.getDefault());
//        Log.i("gf",df2.format(date));




//        final Date currentTime = new Date();
//
//        final SimpleDateFormat sdf =
//                new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
//
//// Give it to me in GMT time.
//        sdf.setTimeZone(TimeZone.getDefault());
////        System.out.println();
//        Log.i("lkm","GMT time: " + sdf.format(currentTime));

        // set unread messages to 0 after opening chat activity
        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO).child(sender_id).child(ConstantsClass.UNREAD_MESSAGE_COUNT).setValue(0);
    }

    public void updateOnlineCountShower(){
        if(online_count==0){
            online_offline_status_symbol.setBackgroundColor(ContextCompat.getColor(this,R.color.gray));
            online_count_shower.setText(String.valueOf(online_count));
        }
        else{
            online_offline_status_symbol.setBackgroundColor(ContextCompat.getColor(this,R.color.green));
            online_count_shower.setText(String.valueOf(online_count));
        }
    }

    public void addValueEventListeners(){

        // update chats whenever either the sender or receiver sends messages
        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_CHATS_MESSAGES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists()){
                    return;
                }

                // temporary arrangement todo
                // I can add a new message to the list
                messageList.clear();

                String lastMessageText="";
                String lastMessageTime="";
                String whichUser="sender";
                String lastMessageUserId=auth.getUid();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    MessageModel msg=dataSnapshot.getValue(MessageModel.class);
                    lastMessageText=msg.getMessage();
                    lastMessageTime=msg.getTime();
                    if(!Objects.equals(msg.getuId(),sender_id)){
                        whichUser="receiver";
                    }
                    else{
                        whichUser="sender";
                    }
                    lastMessageUserId=msg.getuId();
                    messageList.add(msg);
                }

                database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TIME).setValue(lastMessageTime);
                database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TEXT).setValue(lastMessageText);
                if(whichUser.equals("sender")){
                    database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(sender_id);
                }
                else{
                    database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(lastMessageUserId);
                }

                adapter.notifyDataSetChanged();
                Log.i("se",String.valueOf(messageList.size()));
                chatsView.scrollToPosition(messageList.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // to fill all the users in the group
        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    DataSnapshot snp=dataSnapshot.child(ConstantsClass.GROUP_MEMBERS_COLOR);
                    if(!snp.exists()){
                        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO).child(sender_id).child(ConstantsClass.GROUP_MEMBERS_COLOR).setValue(colorNames[new Random().nextInt(5)]);
                    }
                    userColor.put(dataSnapshot.getKey(),dataSnapshot.child(ConstantsClass.GROUP_MEMBERS_COLOR).getValue(String.class));
                    database.getReference().child(ConstantsClass.USERS).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.i("fgh","aya");
                            MyUsers user=snapshot.getValue(MyUsers.class);
                            user.setUserId(snapshot.getKey());
                            Log.i("asd",user.getUsername());
                            Log.i("asd",user.getProfile_pic());
                            userMap.put(dataSnapshot.getKey(),user);
//                            Log.i("fgh",userName.toString());
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // in case the user you are chatting with unfriends you
//        DatabaseReference fref=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.FRIENDS).child(receiver_id);
//        fref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(!snapshot.exists()){
//
//                    // determine if the sender unfriended receiver or vice versa
//                    if(IUnfriended==0){
//                        finish();
//                        Toast.makeText(ChatActivity.this, "This user unfriended you", Toast.LENGTH_SHORT).show();
//                        Intent intent=new Intent(ChatActivity.this,FriendsListActivity.class);
//                        startActivity(intent);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    Map<String,Integer> new_chat_count=new HashMap<>();
    public void sendMessage(){
        if(enteredMessage.getText().toString().equals("")){
            return;
        }

        new_chat_count.clear();
        String mess=enteredMessage.getText().toString();

        // to get current London time
        Date date=new Date();
        DateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Europe/London"));

        // putting the message in both the sender room and the receiver room
        String key=database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_CHATS_MESSAGES).push().getKey();
        MessageModel messageSender=new MessageModel(sender_id,mess,date.getTime(),df.format(date),key);
        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_CHATS_MESSAGES).child(key).setValue(messageSender).addOnCompleteListener(task -> {

            // don't need this as there is no receiver room in group chat
//            String key2=database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.MESSAGES).push().getKey();
//            MessageModel messageReceiver=new MessageModel(sender_id,mess,date.getTime(),df.format(date),key2);
//            database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.MESSAGES).child(key2).setValue(messageReceiver);
        });

        DatabaseReference myRef=database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO);
        ValueEventListener unreadMessagesCountListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                        if(!dataSnapshot.getKey().equals(sender_id)){
                            if(dataSnapshot.child(ConstantsClass.UNREAD_MESSAGE_COUNT).exists()){
                                new_chat_count.put(dataSnapshot.getKey(),dataSnapshot.child(ConstantsClass.UNREAD_MESSAGE_COUNT).getValue(Integer.class));
                            }
                            else{
                                new_chat_count.put(dataSnapshot.getKey(),0);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myRef.addValueEventListener(unreadMessagesCountListener);
        new Handler().postDelayed(() -> {

            myRef.removeEventListener(unreadMessagesCountListener);
            Iterator<String> it=new_chat_count.keySet().iterator();
            while(it.hasNext()){
                String lol=it.next();
                int ct=new_chat_count.get(lol)+1;
                myRef.child(lol).child(ConstantsClass.UNREAD_MESSAGE_COUNT).setValue(ct);
            }

        },2000);

        if(getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME,MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_CLEAR_MESSAGE_KEY,false)){
            enteredMessage.setText("");
        }
    }

    @Override
    public void onClick(View view) {

        if(view==backFromToolbar){
            setDeleteStatus(false);
            adapter.getBackFromDeleteMode();
            nameBar.setVisibility(View.VISIBLE);
            nameBar.setAlpha(0f);
            nameBar.animate().alpha(1f).setDuration(300).setListener(null);

            toolBar.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    toolBar.setVisibility(View.GONE);
                }
            });
        }
        else if(view==deleteSelChats){
            deleteSelectedChats();
        }
        else if(view==selectAllChats){
            adapter.selectAllChats();
        }
        else if(view==unselectAllChats){
            adapter.unselectAllChats();
        }
        else if (view==send_message_button){
            sendMessage();
        }
    }

    BottomSheetDialog menuDialog;
    public void openMenu(){

        // open the bottom dialog with unfriend, delete all chats, delete selected chats function
        menuDialog=new BottomSheetDialog(this,R.style.MyDialogStyle);
        menuDialog.setContentView(R.layout.group_setting_dialog);

        // this button clear all chats from the current users side i.e. the receiver can still see them
        TextView deleteAllChats=menuDialog.findViewById(R.id.textView4);
        deleteAllChats.setOnClickListener(view1 -> {
            Log.i("bnm","der");
            database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.MESSAGES).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                    Toast.makeText(ChatActivity.this, "Cleared all messages", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //this button removes you out of the group
        TextView exitGroup=menuDialog.findViewById(R.id.textView5);
        exitGroup.setOnClickListener(view1 -> {



            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // removing the user from the group
                        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_ADMINS).child(sender_id).removeValue();
                        database.getReference().child(ConstantsClass.GROUP_CHATS).child(group_id).child(ConstantsClass.GROUP_MEMBERS_INFO).child(sender_id).removeValue();
                        database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.GROUPS_ADDED_TO).child(group_id).removeValue();

                        // exiting the group
                        finish();

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to leave this group ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();




        });


        TextView deleteSelectedChats=menuDialog.findViewById(R.id.textView6);
        deleteSelectedChats.setOnClickListener(view1 -> startDeleteSelected());
        menuDialog.show();

        TextView openGroupInfoIcon=menuDialog.findViewById(R.id.textView7);
        openGroupInfoIcon.setOnClickListener(view -> {
            Intent intent=new Intent(GroupChatActivity.this,GroupInfoActivity.class);
            intent.putExtra("group_id",group_id);
            startActivity(intent);
        });


        // closes the menu dialog
        ImageView closeDialog=menuDialog.findViewById(R.id.close_dialog_btn);
        closeDialog.setOnClickListener(view12 -> {
            menuDialog.dismiss();
        });
    }

    public void startDeleteSelected(){
        setDeleteStatus(true);
        if(menuDialog!=null){
            menuDialog.dismiss();
        }

        toolBar.setVisibility(View.VISIBLE);
        toolBar.setAlpha(0f);
        toolBar.animate().alpha(1f).setDuration(300).setListener(null);

        nameBar.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                nameBar.setVisibility(View.GONE);
            }
        });
    }

    public void setDeleteStatus(boolean status){
        adapter.setDeleteStatus(status);
    }

    public void deleteSelectedChats(){
        adapter.deleteSelChats();
        setDeleteStatus(false);
        nameBar.setVisibility(View.VISIBLE);
        nameBar.setAlpha(0f);
//                nameBar.setVisibility(View.GONE);
        nameBar.animate().alpha(1f).setDuration(300).setListener(null);

        toolBar.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                toolBar.setVisibility(View.GONE);
            }
        });
    }
}