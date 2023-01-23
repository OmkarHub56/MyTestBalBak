package com.myapps.bakbak.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.myapps.bakbak.MemberAdminCountUpdater;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.Models.MyUsersWrapper;
import com.myapps.bakbak.R;

import java.util.List;

public class GroupMemberSelectAdapter extends RecyclerView.Adapter<GroupMemberSelectHolder>{

    List<MyUsers> list;
    Context context;
    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;
    List<Boolean> isAdded,isAdmin;
    MemberAdminCountUpdater updater;
    int memberCount=0,adminCount=0;


    public GroupMemberSelectAdapter(List<MyUsers> list,List<Boolean> isAdded,List<Boolean> isAdmin,Context context,MemberAdminCountUpdater updater){
        this.context=context;
        this.list=list;
        this.isAdded=isAdded;
        this.isAdmin=isAdmin;
        this.updater=updater;

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getUid();
        database=FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public GroupMemberSelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.one_user_layout_group_add,parent,false);
        GroupMemberSelectHolder holder=new GroupMemberSelectHolder(view);

        holder.addMemberLL.setOnClickListener(view1 -> {
            int pos=holder.getAdapterPosition();
            if(isAdded.get(pos)){
                memberCount--;
                isAdded.set(pos,false);
                if(isAdmin.get(pos)){
                    adminCount--;
                    isAdmin.set(pos,false);
                }
            }
            else{
                memberCount++;
                isAdded.set(pos,true);
            }
            updater.update(memberCount,adminCount);
            notifyItemChanged(pos);
        });

        holder.setAdminLL.setOnClickListener(view1 -> {
            int pos=holder.getAdapterPosition();
            if(isAdmin.get(pos)){
                adminCount--;
                isAdmin.set(pos,false);
            }
            else{
                adminCount++;
                isAdmin.set(pos,true);
                if(!isAdded.get(pos)){
                    isAdded.set(pos,true);
                    memberCount++;
                }
            }
            updater.update(memberCount,adminCount);
            notifyItemChanged(pos);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberSelectHolder holder, int position) {
        MyUsers user=list.get(position);
        Glide.with(context).load(list.get(position).getProfile_pic()).into(holder.image);
        holder.userName.setText(user.getUsername());

        if(isAdded.get(position)){
            holder.addMemberLL.setBackgroundResource(R.drawable.sent_req_bg);
            holder.addMSymbol.setImageResource(R.drawable.plus_symbol);
            holder.addMember.setText("Remove");
            holder.addMember.setTextColor(ContextCompat.getColor(context,R.color.white));
        }
        else{
            holder.addMemberLL.setBackgroundResource(R.drawable.send_req_bg);
            holder.addMSymbol.setImageResource(R.drawable.plus_symbol_black);
            holder.addMember.setText("Add");
            holder.addMember.setTextColor(ContextCompat.getColor(context,R.color.black));
        }

        if(isAdmin.get(position)){
            holder.setAdminLL.setBackgroundResource(R.drawable.sent_req_bg);
            holder.adminMSymbol.setImageResource(R.drawable.person_icon_white);
            holder.setAdmin.setText("Member");
            holder.setAdmin.setTextColor(ContextCompat.getColor(context,R.color.white));
        }
        else{
            holder.setAdminLL.setBackgroundResource(R.drawable.send_req_bg);
            holder.adminMSymbol.setImageResource(R.drawable.person_icon_black);
            holder.setAdmin.setText("Admin");
            holder.setAdmin.setTextColor(ContextCompat.getColor(context,R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class GroupMemberSelectHolder extends RecyclerView.ViewHolder{

    ImageView image;
    TextView userName;
    TextView addMember,setAdmin;
    MaterialCardView itemView;

    ImageView addMSymbol,adminMSymbol;

    LinearLayout addMemberLL,setAdminLL;

    public GroupMemberSelectHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView= (MaterialCardView) itemView;
        image=itemView.findViewById(R.id.profile_logo);
        userName=itemView.findViewById(R.id.username);
        addMember=itemView.findViewById(R.id.add_member_btn);
        setAdmin=itemView.findViewById(R.id.set_admin_btn);
        addMSymbol=itemView.findViewById(R.id.addm_symbol);
        adminMSymbol=itemView.findViewById(R.id.adminm_symbol);

        addMemberLL=itemView.findViewById(R.id.add_member_ll);
        setAdminLL=itemView.findViewById(R.id.set_admin_ll);

    }
}
