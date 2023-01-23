package com.myapps.bakbak.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.Activities.ChatActivity;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<MyRequestViewholder>{

    public List<MyUsers> list;
    Context context;

    FirebaseAuth auth;
    String current_uid;
    FirebaseDatabase database;

    public RequestAdapter(List<MyUsers> list,Context context,String current_uid){
        this.context=context;
        this.list=list;

        auth=FirebaseAuth.getInstance();
        this.current_uid=current_uid;
        database=FirebaseDatabase.getInstance();

    }
    @NonNull
    @Override
    public MyRequestViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("b","times");
        View view= LayoutInflater.from(context).inflate(R.layout.one_user_layout_req,parent,false);
        final MyRequestViewholder holder = new MyRequestViewholder(view);

        holder.sendReqBtn.setOnClickListener(v -> {
            int pos=holder.getAdapterPosition();
            MyUsers user1 =list.get(pos);

            Log.i("xol","caled");

            // to check if user accepts invites
            DatabaseReference dbms= database.getReference().child(ConstantsClass.USERS).child(user1.getUserId()).child(ConstantsClass.BLOCK_INVITES);
            dbms.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dbms.removeEventListener(this);
                    if(!snapshot.exists() || (snapshot.exists() && !Boolean.TRUE.equals(snapshot.getValue(Boolean.class)))){
                        holder.sendReqBtn.setBackgroundResource(R.drawable.sent_req_bg);
                        holder.sendReqBtn.setText("Sent");
                        holder.sendReqBtn.setClickable(false);
                        holder.sendReqBtn.setEnabled(false);

                        Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show();
                        database.getReference().child(ConstantsClass.USERS).child(current_uid).child(ConstantsClass.REQUESTS).child(user1.getUserId()).setValue(true).addOnCompleteListener(task -> {

                        });
                        database.getReference().child(ConstantsClass.USERS).child(user1.getUserId()).child(ConstantsClass.INVITES).child(current_uid).setValue(true).addOnCompleteListener(task -> {

                        });
                    }
                    else{
                        Toast.makeText(context, "This user doesn't accept invites, maybe try later", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRequestViewholder holder, int position) {
        MyUsers user=list.get(position);
        Glide.with(context).load(user.getProfile_pic()).into(holder.image);
        holder.userName.setText(user.getUsername());

//        DatabaseReference checkReq= database.getReference().child(ConstantsClass.USERS).child(current_uid).child(ConstantsClass.REQUESTS).child(user.getUserId());
//        checkReq.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    checkReq.removeEventListener(this);
//                    holder.sendReqBtn.setBackgroundResource(R.drawable.sent_req_bg);
//                    holder.sendReqBtn.setText("Sent");
//                    holder.sendReqBtn.setClickable(false);
//                    holder.sendReqBtn.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}

class MyRequestViewholder extends RecyclerView.ViewHolder{

    ImageView image;
    TextView userName;
    Button sendReqBtn;
    public MyRequestViewholder(@NonNull View itemView) {
        super(itemView);
        image=itemView.findViewById(R.id.profile_logo);
        userName=itemView.findViewById(R.id.username);
        sendReqBtn=itemView.findViewById(R.id.send_req_btn);
    }
}

