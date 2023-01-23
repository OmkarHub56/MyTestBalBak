package com.myapps.bakbak.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.myapps.bakbak.Activities.FriendsListActivity;
import com.myapps.bakbak.Adapters.InviteAdapter;
import com.myapps.bakbak.Adapters.RequestAdapter;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.ArrayList;
import java.util.List;


public class ReceivedFragment extends Fragment implements View.OnClickListener {


    FirebaseAuth auth;
    String sender_uid;
    FirebaseDatabase database;
    DatabaseReference invitesListRef;
    ValueEventListener invitesListRefListener;
    DataSnapshot invitesDataSnapshot;

    ImageButton refreshButton;

    LinearLayout main_ll;
    TextView no_invites;

    RecyclerView searchUserRView;
    List<MyUsers> userList;
    InviteAdapter adapter;

    public ReceivedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        main_ll= (LinearLayout) inflater.inflate(R.layout.fragment_received, container, false);

        auth=FirebaseAuth.getInstance();
        sender_uid=auth.getUid();
        database=FirebaseDatabase.getInstance();

        // to get all the users in snapshot
        invitesListRef=database.getReference().child(ConstantsClass.USERS).child(sender_uid).child(ConstantsClass.INVITES);
        invitesListRefListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                invitesDataSnapshot=snapshot;
                refreshButton.performClick();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        invitesListRef.addValueEventListener(invitesListRefListener);

        refreshButton=main_ll.findViewById(R.id.refresh_friends_button);

        no_invites=main_ll.findViewById(R.id.no_invites);

        searchUserRView=main_ll.findViewById(R.id.search_user_rview);
        userList=new ArrayList<>();
        adapter=new InviteAdapter(userList,getActivity());
        searchUserRView.setAdapter(adapter);
        searchUserRView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchUserRView.setVisibility(View.VISIBLE);

        refreshButton.setOnClickListener(this);
        return main_ll;
    }

    @Override
    public void onClick(View view) {
        if(view==refreshButton){
            userList.clear();
            adapter.notifyDataSetChanged();
            long childCount=invitesDataSnapshot.getChildrenCount();
            if(childCount==0){
                no_invites.setVisibility(View.VISIBLE);
                searchUserRView.setVisibility(View.GONE);
            }
            else{
                no_invites.setVisibility(View.GONE);
                searchUserRView.setVisibility(View.VISIBLE);
            }
            for(DataSnapshot snapshot:invitesDataSnapshot.getChildren()){
                String uid=snapshot.getKey();
                database.getReference().child(ConstantsClass.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MyUsers currUser=snapshot.getValue(MyUsers.class);
                        currUser.setUserId(uid);
                        userList.add(currUser);
                        Log.i("tyc","out");
                        adapter.notifyItemInserted(userList.size()-1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        invitesListRef.removeEventListener(invitesListRefListener);
    }
}