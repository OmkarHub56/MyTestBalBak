package com.myapps.bakbak.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.myapps.bakbak.Activities.ChatActivity;
import com.myapps.bakbak.Activities.GroupChatActivity;
import com.myapps.bakbak.Models.MyGroupsWrapper;
import com.myapps.bakbak.Models.MyUsersWrapper;
import com.myapps.bakbak.R;

import java.util.List;

public class MyUserlistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<MyUsersWrapper> list;
    List<MyGroupsWrapper> groupsList;
    Context context;
    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;

    int SINGLE_CHAT_TYPE=1,GROUP_CHAT_TYPE=2;


    public MyUserlistAdapter(List<MyUsersWrapper> list,List<MyGroupsWrapper> groupsList,Context context){
        this.context=context;
        this.list=list;
        this.groupsList=groupsList;

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getUid();
        database=FirebaseDatabase.getInstance();
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==SINGLE_CHAT_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.one_user_layout,parent,false);
            return new MyUserlistViewholder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.one_group_layout,parent,false);
            return new MyGrouplistViewholder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position) {

        if(holder.getClass()==MyUserlistViewholder.class){
            MyUserlistViewholder vh=(MyUserlistViewholder) holder;
            MyUsersWrapper user=list.get(position);

            Glide.with(context).load(list.get(position).getUser().getProfile_pic()).into(vh.image);
            vh.userName.setText(user.getUser().getUsername());
            vh.lastMessageTime.setText(user.getLastMessageTime());
            vh.lastMessageText.setText(user.getLastMessageText());

            int count=user.getNewMessageCount();
            vh.newChatCount.setText(String.valueOf(count));
            if(count==0){
                vh.lastMessageTime.setTextColor(context.getResources().getColor(R.color.gray));
                vh.newMessageShower.setVisibility(View.GONE);
                vh.image.setBackgroundResource(R.drawable.profile_bgs_thin);
            }
            else{
                vh.image.setBackgroundResource(R.drawable.profile_bgs_thin_green_border);
                vh.lastMessageTime.setTextColor(context.getResources().getColor(R.color.my_green));
                vh.newMessageShower.setVisibility(View.VISIBLE);
            }


            holder.itemView.setOnClickListener(view -> {
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("userId",user.getUser().getUserId());
                intent.putExtra("profilePic",user.getUser().getProfile_pic());
                intent.putExtra("username",user.getUser().getUsername());
                context.startActivity(intent);
            });
        }
        else{
            MyGrouplistViewholder vh=(MyGrouplistViewholder) holder;
            MyGroupsWrapper group=groupsList.get(position-list.size());


            Glide.with(context).load(groupsList.get(position-list.size()).getGroup().getGroup_profile_pic()).into(vh.image);
            vh.groupName.setText(group.getGroup().getGroup_name());
            vh.lastMessageTime.setText(group.getLastMessageTime());
            vh.lastMessageText.setText(group.getLastMessageText());

            int count=group.getNewMessageCount();
            vh.newChatCount.setText(String.valueOf(count));
            Log.i("count_sh",String.valueOf(count));
            if(count==0){
                vh.lastMessageTime.setTextColor(context.getResources().getColor(R.color.gray));
                vh.newMessageShower.setVisibility(View.GONE);
                vh.image.setBackgroundResource(R.drawable.profile_bgs_thin);
            }
            else{
                vh.image.setBackgroundResource(R.drawable.profile_bgs_thin_green_border);
                vh.lastMessageTime.setTextColor(context.getResources().getColor(R.color.my_green));
                vh.newMessageShower.setVisibility(View.VISIBLE);
            }


            holder.itemView.setOnClickListener(view -> {
                Intent intent=new Intent(context, GroupChatActivity.class);
                intent.putExtra("group_id",group.getGroup().getGroup_id());
                intent.putExtra("group_profile_pic",group.getGroup().getGroup_profile_pic());
                intent.putExtra("group_name",group.getGroup().getGroup_name());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position>list.size()-1){
            return GROUP_CHAT_TYPE;
        }
        else{
            return SINGLE_CHAT_TYPE;
        }
//        if(list.get(position).getUser().getUserId().equals(FirebaseAuth.getInstance().getUid())){
//            return SENDER_VIEW_TYPE;
//        }
//        else{
//            return RECEIVER_VIEW_TYPE;
//        }
    }

    @Override
    public int getItemCount() {
        return list.size()+groupsList.size();
    }
}

class MyUserlistViewholder extends RecyclerView.ViewHolder{

    ImageView image;
    TextView userName;
    View itemView;

    TextView lastMessageText,lastMessageTime,newChatCount;
    CardView newMessageShower;
    public MyUserlistViewholder(@NonNull View itemView) {
        super(itemView);
        this.itemView=itemView;
        image=this.itemView.findViewById(R.id.profile_logo);
        userName=this.itemView.findViewById(R.id.username);
        lastMessageText=this.itemView.findViewById(R.id.latest_chat_message);
        lastMessageTime=this.itemView.findViewById(R.id.latest_chat_timing);
        newChatCount=this.itemView.findViewById(R.id.new_chat_count);
        newMessageShower=this.itemView.findViewById(R.id.new_message_shower_cv);
    }
}

class MyGrouplistViewholder extends RecyclerView.ViewHolder{

    ImageView image;
    TextView groupName;
    View itemView;

    TextView lastMessageText,lastMessageTime,newChatCount;
    CardView newMessageShower;
    public MyGrouplistViewholder(@NonNull View itemView) {
        super(itemView);
        this.itemView=itemView;
        image=this.itemView.findViewById(R.id.profile_logo);
        groupName=this.itemView.findViewById(R.id.username);
        lastMessageText=this.itemView.findViewById(R.id.latest_chat_message);
        lastMessageTime=this.itemView.findViewById(R.id.latest_chat_timing);
        newChatCount=this.itemView.findViewById(R.id.new_chat_count);
        newMessageShower=this.itemView.findViewById(R.id.new_message_shower_cv);
    }
}
