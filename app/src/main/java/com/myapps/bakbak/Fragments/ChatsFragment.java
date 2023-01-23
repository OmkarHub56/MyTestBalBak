package com.myapps.bakbak.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Adapters.MyUserlistAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyGroups;
import com.myapps.bakbak.Models.MyGroupsWrapper;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.Models.MyUsersWrapper;
import com.myapps.bakbak.R;
import com.myapps.bakbak.TimeUtility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatsFragment extends Fragment implements View.OnClickListener{

    FirebaseAuth auth;
    String sender_uid;
    FirebaseDatabase database;

    LinearLayout rootElement;

    ImageButton refreshFriendsButton;

    RecyclerView list_of_friends;
    MyUserlistAdapter md;
    List<MyUsersWrapper> list=new ArrayList<>();
    List<MyGroupsWrapper> list0fGroups=new ArrayList<>();

    TextView no_friends_alert;

    public ChatsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootElement= (LinearLayout) inflater.inflate(R.layout.fragment_chats, container, false);

        auth=FirebaseAuth.getInstance();
        sender_uid=auth.getUid();
        database=FirebaseDatabase.getInstance();

        refreshFriendsButton=rootElement.findViewById(R.id.refresh_friends_button);
        refreshFriendsButton.setOnClickListener(this);

        list_of_friends=rootElement.findViewById(R.id.recycler_view);
        list_of_friends.setLayoutManager(new LinearLayoutManager(getActivity()));
        md=new MyUserlistAdapter(list,list0fGroups,getActivity());
        list_of_friends.setAdapter(md);

        // text to show if user has no friends
        no_friends_alert=rootElement.findViewById(R.id.no_friend_text);

        refreshFriendsButton.performClick();
        return rootElement;
    }

    public void addFriendsAndGroupsToList(DataSnapshot friendsSnapshot,DataSnapshot groupsSnapshot){
        list.clear();
        list0fGroups.clear();
        md.notifyDataSetChanged();
        Log.i("cv","aua");

        if(!friendsSnapshot.exists() && !groupsSnapshot.exists()){
            list_of_friends.setVisibility(View.GONE);
            no_friends_alert.setVisibility(View.VISIBLE);
            return;
        }

        for(DataSnapshot dtp:friendsSnapshot.getChildren()){
            DatabaseReference usersNode=database.getReference().child(ConstantsClass.USERS).child(dtp.getKey());
            ValueEventListener oneFriend=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        return;
                    }
                    MyUsers user=snapshot.getValue(MyUsers.class);
                    user.setUserId(dtp.getKey());
                    MyUsersWrapper newUser=new MyUsersWrapper(user);
                    list.add(newUser);


                    DatabaseReference usersNodeChats=database.getReference().child(ConstantsClass.CHATS).child(sender_uid+dtp.getKey()).child(ConstantsClass.LAST_MESSAGE_DETAILS);
                    ValueEventListener oneFriendChatsListener=new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                Log.i("mkl","aya");
                                newUser.setLastMessageText("Last message appears here ...");
                                newUser.setLastMessageTime("--/--");
                                newUser.setNewMessageCount(0);
                                md.notifyItemChanged(list.size()-1);

                                return;
                            }

                            String lastMessageTime=snapshot.child(ConstantsClass.LAST_MESSAGE_TIME).getValue(String.class);
                            String lastMessageText=snapshot.child(ConstantsClass.LAST_MESSAGE_TEXT).getValue(String.class);
                            String lastMessageUser=snapshot.child(ConstantsClass.LAST_MESSAGE_USER).getValue(String.class);

                            if(lastMessageUser==null){
                                return;
                            }

                            // setting the last message data
                            String toShow;
                            if(lastMessageUser.equals(sender_uid)){
                                toShow="You : "+lastMessageText;
                            }
                            else{
                                toShow=user.getUsername()+" : "+lastMessageText;
                            }
                            if(toShow.length()>32){
                                toShow=toShow.substring(0,32)+" ...";
                            }
                            newUser.setLastMessageText(toShow);
                            md.notifyItemChanged(list.size()-1);



                            Date date = new Date();

                            // in future I can include dates as well along with time
                            DateFormat df = new SimpleDateFormat("HH:mm");
                            df.setTimeZone(TimeZone.getTimeZone("Europe/London"));
                            String currentLondonTime=df.format(date);

                            DateFormat df2 = new SimpleDateFormat("HH:mm");
                            df2.setTimeZone(TimeZone.getDefault());
                            String currentThisTime=df2.format(date);

                            int mins_diff= TimeUtility.getTimeDifference(currentThisTime,currentLondonTime);

                            String ampm="";
                            String messageCurrentTime=TimeUtility.getCurrTime(mins_diff,lastMessageTime);
                            if(getActivity()!=null){
                                if(getActivity().getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_TIME_FORMAT_KEY,false)){
                                    ampm=" AM";
                                    int hour=Integer.parseInt(messageCurrentTime.substring(0,2));
                                    if(hour>=13){
                                        ampm=" PM";
                                        messageCurrentTime= (hour-12)+messageCurrentTime.substring(2,5);
                                    }
                                }
                            }

                            newUser.setLastMessageTime(messageCurrentTime+ampm);
                            md.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    usersNodeChats.addValueEventListener(oneFriendChatsListener);


                    // new Chats node
                    DatabaseReference newChatsNode=database.getReference().child(ConstantsClass.CHATS).child(sender_uid+dtp.getKey()).child(ConstantsClass.UNREAD_MESSAGE_COUNT);
                    ValueEventListener newChatsListener=new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                int count=snapshot.getValue(Integer.class);
                                Log.i("Kl",String.valueOf(count));
                                newUser.setNewMessageCount(count);
                                md.notifyDataSetChanged();
//                                if(count==0){
////                                    user.rnsetVisibility(View.GONE);
//                                }
//                                else{
//                                    holder.newMessageShower.setVisibility(View.VISIBLE);
//                                    holder.newChatCount.setText(String.valueOf(snapshot.getValue(Integer.class)));
//                                    holder.lastMessageTime.setTextColor(context.getResources().getColor(R.color.my_green));
//                                }
                            }
                            else{
                                newUser.setNewMessageCount(0);
                                md.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    newChatsNode.addValueEventListener(newChatsListener);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            usersNode.addValueEventListener(oneFriend);
            new Handler().postDelayed(() -> usersNode.removeEventListener(oneFriend),1000);
        }
        md.notifyDataSetChanged();


        for(DataSnapshot dtp:groupsSnapshot.getChildren()){
            DatabaseReference groupsNode=database.getReference().child(ConstantsClass.GROUP_CHATS).child(dtp.getKey());
            ValueEventListener oneGroup=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        return;
                    }
                    MyGroups group=snapshot.getValue(MyGroups.class);
                    MyGroupsWrapper groupsWrapper=new MyGroupsWrapper(group);
//                    MyUsersWrapper newUser=new MyUsersWrapper(user);
                    list0fGroups.add(groupsWrapper);
                    Log.i("jkn","jelly");
//                    md.notifyItemInserted(list.size()-1);
                    md.notifyDataSetChanged();

                    DatabaseReference usersNodeChats=database.getReference().child(ConstantsClass.GROUP_CHATS).child(dtp.getKey()).child(ConstantsClass.LAST_MESSAGE_DETAILS);
                    ValueEventListener oneFriendChatsListener=new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                Log.i("mkl","aya");
                                groupsWrapper.setLastMessageText("Last message appears here ...");
                                groupsWrapper.setLastMessageTime("--/--");
                                groupsWrapper.setNewMessageCount(0);
                                md.notifyDataSetChanged();

                                return;
                            }

                            String lastMessageTime=snapshot.child(ConstantsClass.LAST_MESSAGE_TIME).getValue(String.class);
                            String lastMessageText=snapshot.child(ConstantsClass.LAST_MESSAGE_TEXT).getValue(String.class);
                            String lastMessageUser=snapshot.child(ConstantsClass.LAST_MESSAGE_USER).getValue(String.class);

                            if(lastMessageUser==null){
                                return;
                            }

                            // setting the last message data
                            String toShow;
                            if(lastMessageUser.equals(sender_uid)){
                                toShow="You : "+lastMessageText;
                            }
                            else{
                                toShow="todo : "+lastMessageText;
                            }
                            if(toShow.length()>32){
                                toShow=toShow.substring(0,32)+" ...";
                            }
                            groupsWrapper.setLastMessageText(toShow);
                            md.notifyDataSetChanged();



                            Date date = new Date();

                            // in future I can include dates as well along with time
                            DateFormat df = new SimpleDateFormat("HH:mm");
                            df.setTimeZone(TimeZone.getTimeZone("Europe/London"));
                            String currentLondonTime=df.format(date);

                            DateFormat df2 = new SimpleDateFormat("HH:mm");
                            df2.setTimeZone(TimeZone.getDefault());
                            String currentThisTime=df2.format(date);

                            int mins_diff= TimeUtility.getTimeDifference(currentThisTime,currentLondonTime);

                            String ampm="";
                            String messageCurrentTime=TimeUtility.getCurrTime(mins_diff,lastMessageTime);
                            if(getActivity()!=null){
                                if(getActivity().getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_TIME_FORMAT_KEY,false)){
                                    ampm=" AM";
                                    int hour=Integer.parseInt(messageCurrentTime.substring(0,2));
                                    if(hour>=13){
                                        ampm=" PM";
                                        messageCurrentTime= (hour-12)+messageCurrentTime.substring(2,5);
                                    }
                                }
                            }

                            groupsWrapper.setLastMessageTime(messageCurrentTime+ampm);
                            md.notifyDataSetChanged();


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    usersNodeChats.addValueEventListener(oneFriendChatsListener);


                    // new Chats node
                    DatabaseReference newChatsNode=database.getReference().child(ConstantsClass.GROUP_CHATS).child(group.getGroup_id()).child(ConstantsClass.GROUP_MEMBERS_INFO).child(sender_uid).child(ConstantsClass.UNREAD_MESSAGE_COUNT);
                    ValueEventListener newChatsListener=new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                int count=snapshot.getValue(Integer.class);
                                Log.i("Kl",String.valueOf(count));
                                groupsWrapper.setNewMessageCount(count);
                                md.notifyDataSetChanged();
//                                if(count==0){
////                                    user.rnsetVisibility(View.GONE);
//                                }
//                                else{
//                                    holder.newMessageShower.setVisibility(View.VISIBLE);
//                                    holder.newChatCount.setText(String.valueOf(snapshot.getValue(Integer.class)));
//                                    holder.lastMessageTime.setTextColor(context.getResources().getColor(R.color.my_green));
//                                }
                            }
                            else{
                                groupsWrapper.setNewMessageCount(0);
                                md.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    newChatsNode.addValueEventListener(newChatsListener);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            groupsNode.addValueEventListener(oneGroup);
            new Handler().postDelayed(() -> groupsNode.removeEventListener(oneGroup),1000);
        }
        md.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(view==refreshFriendsButton){
            done=0;
            snapshot1=null;
            refreshFriendsButton.setEnabled(false);
            refreshFriendsButton.setClickable(false);
            DatabaseReference listOfFriends=database.getReference().child(ConstantsClass.USERS).child(sender_uid).child(ConstantsClass.FRIENDS);
            ValueEventListener listOfFriendsListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setupToAddFriendsAndGroups(snapshot,0);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            listOfFriends.addValueEventListener(listOfFriendsListener);

            DatabaseReference listOfGroups=database.getReference().child(ConstantsClass.USERS).child(sender_uid).child(ConstantsClass.GROUPS_ADDED_TO);
            ValueEventListener listOfGroupsListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setupToAddFriendsAndGroups(snapshot,1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            listOfGroups.addValueEventListener(listOfGroupsListener);

            new Handler().postDelayed(() -> {
                listOfFriends.removeEventListener(listOfFriendsListener);
                listOfGroups.removeEventListener(listOfGroupsListener);
                refreshFriendsButton.setEnabled(true);
                refreshFriendsButton.setClickable(true);
            },1000);

        }
    }

    int done=0;
    DataSnapshot snapshot1=null;
    public void setupToAddFriendsAndGroups(DataSnapshot snapshot,int type){
        done++;
        if(done==1){
            snapshot1=snapshot;
            return;
        }
        if(done==2){
            if(type==0){
                addFriendsAndGroupsToList(snapshot,snapshot1);
            }
            else{
                addFriendsAndGroupsToList(snapshot1,snapshot);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFriendsButton.performClick();
    }
}
