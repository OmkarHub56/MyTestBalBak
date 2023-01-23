package com.myapps.bakbak.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Activities.FriendsListActivity;
import com.myapps.bakbak.Adapters.RequestAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.ArrayList;
import java.util.List;

public class SentFragment extends Fragment {

    FirebaseAuth auth;
    String current_uid;
    FirebaseDatabase database;
    DatabaseReference usersListRef;
    ValueEventListener userListRefListener;
    DataSnapshot usersDataSnapshot;
    DatabaseReference friendsListRef;
    ValueEventListener friendsListRefListener;
    DataSnapshot friendsDataSnapshot;

    LinearLayout rootElement;

    TextView makeNewFr;

    RecyclerView searchUserRView;
    List<MyUsers> userList;
    RequestAdapter adapter;

    public SentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootElement= (LinearLayout) inflater.inflate(R.layout.fragment_sent, container, false);

        auth=FirebaseAuth.getInstance();
        current_uid=auth.getCurrentUser().getUid();
        database=FirebaseDatabase.getInstance();

        // to get all the users in snapshot
        usersListRef=database.getReference().child(ConstantsClass.USERS);
        userListRefListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersDataSnapshot=snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        usersListRef.addValueEventListener(userListRefListener);

        // to get all the friends
        friendsListRef=database.getReference().child(ConstantsClass.USERS).child(current_uid).child(ConstantsClass.FRIENDS);
        friendsListRefListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {friendsDataSnapshot=snapshot;}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        friendsListRef.addValueEventListener(friendsListRefListener);


        makeNewFr=rootElement.findViewById(R.id.make_new_fr);

        userList=new ArrayList<>();
        adapter=new RequestAdapter(userList,getActivity(),current_uid);
        searchUserRView=rootElement.findViewById(R.id.search_user_rview);
        searchUserRView.setAdapter(adapter);
        searchUserRView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootElement;
    }


    // this function is called from activity using the FragmentAdapter reference
    // this has to be improved later when there are more number of users
    // todo

    int friendCount=0;
    boolean searchableFriends=true;
    public void searchUser(String username){

        friendCount=0;
        searchableFriends=true;

        userList.clear();
        adapter.notifyDataSetChanged();
        searchUserRView.setVisibility(View.VISIBLE);
        makeNewFr.setVisibility(View.GONE);

        for(DataSnapshot dsp:usersDataSnapshot.getChildren()){
            MyUsers currUser=dsp.getValue(MyUsers.class);
            String user_id=dsp.getKey();
            currUser.setUserId(user_id);
            if(!searchableFriends){
                break;
            }

            // check for
            // 1) already a friend
            // 2) contains the keyword
            // 3) not the current the current user
            DataSnapshot checkForFriend=friendsDataSnapshot.child(current_uid);
            if(currUser.getUsername().contains(username) && !checkForFriend.exists() && !user_id.equals(current_uid)){
                friendCount++;
                userList.add(currUser);
                adapter.notifyItemInserted(friendCount-1);
                if(friendCount==100){
                    searchableFriends=false;
                }
            }
        }
        if(friendCount==0){
            searchUserRView.setVisibility(View.GONE);
            makeNewFr.setVisibility(View.VISIBLE);
            makeNewFr.setText("No such user found");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        usersListRef.removeEventListener(userListRefListener);
        friendsListRef.removeEventListener(friendsListRefListener);
    }
}