package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.AdapterToActivityListener;
import com.myapps.bakbak.Adapters.ChatsAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MessageModel;
import com.myapps.bakbak.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity implements View.OnClickListener, AdapterToActivityListener {

    // screen dimensions utility purposes
//    int screenWidth,screenHeight;
//    float screenDensity;
    FirebaseAuth auth;
    FirebaseDatabase database;

    String sender_id,receiver_id;
    String senderRoom,receiverRoom;

    ConstraintLayout nameBar;
    ImageView backButton;
    CircleImageView receiver_profile_pic;
    TextView receiverUsername;
    CircleImageView online_offline_status_symbol;
    TextView online_offline_status;
    ImageView openMenuButton;

    ConstraintLayout toolBar;
    ImageView backFromToolbar;
    TextView selectedChatsCount;
    ImageView unselectAllChats;
    ImageView selectAllChats;
    ImageView deleteSelChats;

    RecyclerView chatsView;
    ChatsAdapter adapter;
    List<MessageModel> messageList=new ArrayList<>();

    EditText enteredMessage;
    CircleImageView send_message_button;
    int chats_selected_count=0;

    int IUnfriended=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        Log.i("cx",TimeZone.getDefault().toString());

//        screenWidth = getResources().getDisplayMetrics().widthPixels;
//        screenHeight=getResources().getDisplayMetrics().heightPixels;
//        screenDensity=getResources().getDisplayMetrics().density;

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        // the sender and receiver
        sender_id=auth.getUid();
        receiver_id=getIntent().getStringExtra("userId");

        // sender and receiver chat rooms
        senderRoom=sender_id+receiver_id;
        receiverRoom=receiver_id+sender_id;

        nameBar=findViewById(R.id.name_bar);

        // back to friends list activity
        backButton=findViewById(R.id.back_btn);
        backButton.setOnClickListener(view -> onBackPressed());

        receiver_profile_pic=findViewById(R.id.receiver_profilepic);
        String profilePicture=getIntent().getStringExtra("profilePic");
        Glide.with(this).load(profilePicture==null?R.drawable.user_icon:profilePicture).into(receiver_profile_pic);

        receiverUsername=findViewById(R.id.receiver_username);
        receiverUsername.setText(getIntent().getStringExtra("username"));

        online_offline_status_symbol=findViewById(R.id.status_symbol);
        online_offline_status=findViewById(R.id.online_offline_status);
        database.getReference().child(ConstantsClass.USERS).child(receiver_id).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean status=false;
                if(snapshot.exists()){
                    status=snapshot.getValue(Boolean.class);
                }
                if(status){
                    online_offline_status.setText("Online");
                    online_offline_status_symbol.setBackgroundResource(R.color.green);
                }
                else{
                    online_offline_status.setText("Offline");
                    online_offline_status_symbol.setBackgroundResource(R.color.gray);
                }
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

        LinearLayoutManager lmg=new LinearLayoutManager(this);
        adapter=new ChatsAdapter(this,messageList,sender_id,receiver_id,this);
        chatsView=findViewById(R.id.chats_recyclerview);
        chatsView.setLayoutManager(lmg);
        chatsView.setItemAnimator(null);

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
        database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.UNREAD_MESSAGE_COUNT).setValue(0);
    }

    public void addValueEventListeners(){

        // update chats whenever either the sender or receiver sends messages
        database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.MESSAGES).addValueEventListener(new ValueEventListener() {
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

                database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TIME).setValue(lastMessageTime);
                database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TEXT).setValue(lastMessageText);
                database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TIME).setValue(lastMessageTime);
                database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_TEXT).setValue(lastMessageText);
                if(whichUser.equals("sender")){
                    database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(sender_id);
                    database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(sender_id);
                }
                else{
                    database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(lastMessageUserId);
                    database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.LAST_MESSAGE_DETAILS).child(ConstantsClass.LAST_MESSAGE_USER).setValue(lastMessageUserId);
                }

                adapter.notifyDataSetChanged();
                Log.i("se",String.valueOf(messageList.size()));
                chatsView.scrollToPosition(messageList.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // in case the user you are chatting with unfriends you
        DatabaseReference fref=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.FRIENDS).child(receiver_id);
        fref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){

                    // determine if the sender unfriended receiver or vice versa
                    if(IUnfriended==0){
                        finish();
                        Toast.makeText(ChatActivity.this, "This user unfriended you", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(ChatActivity.this,FriendsListActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    int count=0;
    public void sendMessage(){
        if(enteredMessage.getText().toString().equals("")){
            return;
        }

        count=0;
        String mess=enteredMessage.getText().toString();

        // to get current London date
        Date date=new Date();
        DateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Europe/London"));

        // putting the message in both the sender room and the receiver room
        String key=database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.MESSAGES).push().getKey();
        MessageModel messageSender=new MessageModel(sender_id,mess,date.getTime(),df.format(date),key);
        database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.MESSAGES).child(key).setValue(messageSender).addOnCompleteListener(task -> {
            String key2=database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.MESSAGES).push().getKey();
            MessageModel messageReceiver=new MessageModel(sender_id,mess,date.getTime(),df.format(date),key2);
            database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.MESSAGES).child(key2).setValue(messageReceiver);
        });

        DatabaseReference myRef=database.getReference().child(ConstantsClass.CHATS).child(receiverRoom).child(ConstantsClass.UNREAD_MESSAGE_COUNT);
        ValueEventListener unreadMessagesCountListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("cekio","called"+String.valueOf(Math.random())+count);
                if(snapshot.exists()){
                    count=snapshot.getValue(Integer.class);
                }
                else{
                    count=0;
                }
                Log.i("cekio",String.valueOf(count));
                Log.i("cekio","");
                new Handler().postDelayed(() -> {
                    myRef.setValue(count+1);
                    myRef.removeEventListener(this);
                },2000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myRef.addValueEventListener(unreadMessagesCountListener);



        if(getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME,MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_CLEAR_MESSAGE_KEY,false)){
            enteredMessage.setText("");
        }
    }




    @Override
    public void onClick(View view) {

        if(view==backFromToolbar){
            setDeleteStatus(false);
            chats_selected_count=0;
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
        menuDialog.setContentView(R.layout.setting_dialog);

        // this button clear all chats from the current users side i.e. the receiver can still see them
        TextView deleteAllChats=menuDialog.findViewById(R.id.textView4);
        deleteAllChats.setOnClickListener(view1 -> {
            Log.i("bnm","der");
            database.getReference().child(ConstantsClass.CHATS).child(senderRoom).child(ConstantsClass.MESSAGES).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(ChatActivity.this, "Cleared all messages", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // this button unfriends the current user and one he/she is chatting with
        TextView removeFriend=menuDialog.findViewById(R.id.textView5);
        removeFriend.setOnClickListener(view1 -> {
            IUnfriended=1;

            // remove receiver from sender friend list
            database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.FRIENDS).child(receiver_id).removeValue().addOnCompleteListener(task -> {

                // remove sender from receiver friend list
                database.getReference().child(ConstantsClass.USERS).child(receiver_id).child(ConstantsClass.FRIENDS).child(sender_id).removeValue().addOnCompleteListener(task1 -> {
                    finish();
                    Intent intent=new Intent(ChatActivity.this,FriendsListActivity.class);
                    startActivity(intent);
                });
            });
        });


        TextView deleteSelectedChats=menuDialog.findViewById(R.id.textView6);
        deleteSelectedChats.setOnClickListener(view1 -> startDeleteSelected());
        menuDialog.show();

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
        chats_selected_count=0;
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

    @Override
    public void onClickedChat() {
        startDeleteSelected();

    }

    @Override
    public void onChatSelected(boolean sel) {
        if(sel){
            chats_selected_count++;
        }
        else{
            chats_selected_count--;
        }
        SpannableString text=new SpannableString("Total "+chats_selected_count+" chats selected");
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this,R.color.red)),6,text.length()-14, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        selectedChatsCount.setText(text);
    }
}