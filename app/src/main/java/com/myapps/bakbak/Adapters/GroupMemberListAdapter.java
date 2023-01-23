package com.myapps.bakbak.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberViewholder> {

    public List<MyUsers> list;
    Context context;
    FirebaseDatabase database;
    FirebaseAuth auth;
    DataSnapshot adminCheck;

    public GroupMemberListAdapter(List<MyUsers> list, Context context, DataSnapshot adminCheck){
        this.context=context;
        this.list=list;
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        this.adminCheck=adminCheck;
    }
    @NonNull
    @Override
    public GroupMemberViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("b","times");
        View view= LayoutInflater.from(context).inflate(R.layout.one_group_member_layout,parent,false);
        return new GroupMemberViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewholder holder, @SuppressLint("RecyclerView") int position) {
        MyUsers user=list.get(position);
//        Glide.with(context).load(user.profilePic).into(holder.image);
        Glide.with(context).load(list.get(position).getProfile_pic()).into(holder.image);
        holder.userName.setText(user.getUsername());

        holder.acceptReqBtn.setOnClickListener(view -> {
            Log.i("cd","yaha");
            database.getReference().child(ConstantsClass.USERS).child(list.get(position).getUserId()).child(ConstantsClass.REQUESTS).child(auth.getUid()).removeValue();
            database.getReference().child(ConstantsClass.USERS).child(auth.getUid()).child(ConstantsClass.INVITES).child(list.get(position).getUserId()).removeValue();
            database.getReference().child(ConstantsClass.USERS).child(list.get(position).getUserId()).child(ConstantsClass.FRIENDS).child(auth.getUid()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            database.getReference().child(ConstantsClass.USERS).child(auth.getUid()).child(ConstantsClass.FRIENDS).child(list.get(position).getUserId()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            list.remove(position);
            notifyDataSetChanged();
        });


        holder.rejectReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                view.setBackgroundResource(R.drawable.sent_req_bg);
//                ((Button)view).setText("Sent");
                MyUsers user=list.get(position);
                database.getReference().child(ConstantsClass.USERS).child(auth.getUid()).child(ConstantsClass.INVITES).child(user.getUserId()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
                database.getReference().child(ConstantsClass.USERS).child(user.getUserId()).child(ConstantsClass.INVITES).child(auth.getUid()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}

class GroupMemberViewholder extends RecyclerView.ViewHolder{

    ImageView image;
    TextView userName;
    CircleImageView acceptReqBtn,rejectReqBtn;
    public GroupMemberViewholder(@NonNull View itemView) {
        super(itemView);
        image=itemView.findViewById(R.id.profile_logo);
        userName=itemView.findViewById(R.id.username);
        acceptReqBtn=itemView.findViewById(R.id.accept_req_btn);
        rejectReqBtn=itemView.findViewById(R.id.reject_req_btn);
    }
}