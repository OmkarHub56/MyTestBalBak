package com.myapps.bakbak.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.myapps.bakbak.AdapterToActivityListener;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MessageModel;
import com.myapps.bakbak.R;
import com.myapps.bakbak.TimeUtility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TimeZone;


public class ChatsAdapter extends RecyclerView.Adapter{
    Context context;
    List<MessageModel> list;
    int mins;

    PriorityQueue<Integer> pq;
    boolean deleteStatus=false;
    Set<Integer> selectedIndices;

    int SENDER_VIEW_TYPE=1,RECEIVER_VIEW_TYPE=2,SENDER_VIEW_TYPE_CONT=3,RECEIVER_VIEW_TYPE_CONT=4;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String sender_id,receiver_id;
    AdapterToActivityListener listener;
    List<Integer> isSelected;

    public ChatsAdapter(Context context,List<MessageModel> list,String senderId,String receiverId,AdapterToActivityListener listener){
        this.context=context;
        this.list=list;
        isSelected=new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pq=new PriorityQueue<>(Collections.reverseOrder());
        }
        selectedIndices=new HashSet<>();

        Date date = new Date();

        // in future I can include dates as well along with time
        DateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        String currentLondonTime=df.format(date);

        DateFormat df2 = new SimpleDateFormat("HH:mm");
        df2.setTimeZone(TimeZone.getDefault());
        String currentThisTime=df2.format(date);

        mins=TimeUtility.getTimeDifference(currentThisTime,currentLondonTime);

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        sender_id=senderId;
        receiver_id=receiverId;
        this.listener=listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==SENDER_VIEW_TYPE){
            View view=LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderChatViewHolder(view);
        }
        else if(viewType==SENDER_VIEW_TYPE_CONT){
            View view=LayoutInflater.from(context).inflate(R.layout.sample_sender_cont,parent,false);
            return new SenderChatViewHolder(view);
        }
        else if(viewType==RECEIVER_VIEW_TYPE){
            View view=LayoutInflater.from(context).inflate(R.layout.sample_receiver,parent,false);
            return new ReceiverChatViewHolder(view);
        }
        else{
            View view=LayoutInflater.from(context).inflate(R.layout.sample_receiver_cont,parent,false);
            return new ReceiverChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        MessageModel mgl=list.get(position);
        if(position>=isSelected.size()){
            for(int i=isSelected.size();i<=position;i++){
                isSelected.add(i,0);
            }
        }

        if(holder.getClass()==SenderChatViewHolder.class){
            SenderChatViewHolder vh=(SenderChatViewHolder) holder;
            if(isSelected.get(position)==1){
                vh.par.setBackgroundResource(R.color.delete_chat_red);
            }
            else {
                vh.par.setBackgroundResource(R.color.close_to_black_gray);
            }

            vh.par.setOnClickListener(null);
            vh.par.setOnClickListener(view -> {
                if(deleteStatus){
                    if(selectedIndices.contains(position)){
                        listener.onChatSelected(false);
                        isSelected.set(position,0);
                        notifyItemChanged(position);
                        pq.remove(position);
                        selectedIndices.remove(position);
                    }
                    else{
                        listener.onChatSelected(true);
                        isSelected.set(position,1);
                        notifyItemChanged(position);
                        pq.add(position);
                        selectedIndices.add(position);
                    }
                }
            });

            vh.par.setOnLongClickListener(view -> {
                if(!deleteStatus){

                    listener.onChatSelected(true);
                    listener.onClickedChat();

                    isSelected.set(position,1);
                    notifyItemChanged(position);
                    pq.add(position);
                    selectedIndices.add(position);
                }
                return true;
            });
            vh.txtmessage.setText(mgl.getMessage());

            String messageLondonTime=list.get(position).getTime();


            String ampm="";
            String messageCurrentTime=TimeUtility.getCurrTime(mins,messageLondonTime);
            if(context.getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_TIME_FORMAT_KEY,false)){
                ampm=" AM";
                int hour=Integer.parseInt(messageCurrentTime.substring(0,2));
                if(hour>=13){
                    ampm=" PM";
                    messageCurrentTime= (hour-12)+messageCurrentTime.substring(2,5);
                }
            }

            vh.timeText.setText(messageCurrentTime+ampm);
        }
        else{
            ReceiverChatViewHolder vh=(ReceiverChatViewHolder) holder;
            if(isSelected.get(position)==1){
                vh.par.setBackgroundResource(R.color.delete_chat_red);
            }
            else {
                vh.par.setBackgroundResource(R.color.close_to_black_gray);
            }

            vh.par.setOnClickListener(null);
            vh.par.setOnClickListener(view -> {
                if(deleteStatus){
                    if(selectedIndices.contains(position)){
                        listener.onChatSelected(false);
                        isSelected.set(position,0);
                        notifyItemChanged(position);
                        pq.remove(position);
                        selectedIndices.remove(position);
                    }
                    else{
                        listener.onChatSelected(true);
                        isSelected.set(position,1);
                        notifyItemChanged(position);
                        pq.add(position);
                        selectedIndices.add(position);
                    }
                }
            });
//                hasListenerAttached.add((ConstraintLayout) vh.par);
//            }
            vh.par.setOnLongClickListener(view -> {
                if(!deleteStatus){
                    listener.onClickedChat();
                    listener.onChatSelected(true);
                    isSelected.set(position,1);
                    notifyItemChanged(position);
                    pq.add(position);
                    selectedIndices.add(position);
                }
                return true;
            });
            vh.txtmessage.setText(mgl.getMessage());

            String messageLondonTime=list.get(position).getTime();
//            Log.i("zx",messageLondonTime);

            String ampm="";
            String messageCurrentTime= TimeUtility.getCurrTime(mins,messageLondonTime);
            if(context.getSharedPreferences(ConstantsClass.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE).getBoolean(ConstantsClass.SHARED_PREF_TIME_FORMAT_KEY,false)){

                int hour=Integer.parseInt(messageCurrentTime.substring(0,2));
                if(hour>=13){
                    ampm=" PM";
                    messageCurrentTime= (hour-12)+messageCurrentTime.substring(2,5);
                }
                else{
                    ampm=" AM";
                }
            }

            vh.timeText.setText(messageCurrentTime+ampm);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            if(position==0 || !list.get(position).getuId().equals(list.get(position-1).getuId())){
                return SENDER_VIEW_TYPE;
            }
            else{
                return SENDER_VIEW_TYPE_CONT;
            }
        }
        else{
            if(position==0 || !list.get(position).getuId().equals(list.get(position-1).getuId())){
                return RECEIVER_VIEW_TYPE;
            }
            else{
                return RECEIVER_VIEW_TYPE_CONT;
            }
        }
    }

    public void setDeleteStatus(boolean status){
        deleteStatus=status;
    }

    public void deleteSelChats(){
//        int ptr=0;
        while(!pq.isEmpty()){
            int pos=pq.poll();
            MessageModel model=list.get(pos);
            Log.i("fgh",sender_id+receiver_id);
            Log.i("fgh",model.getmId());
            database.getReference().child(ConstantsClass.CHATS).child(sender_id+receiver_id).child(ConstantsClass.MESSAGES).child(model.getmId()).removeValue();
            list.remove(pos);
            isSelected.remove(pos);
            selectedIndices.clear();
            this.notifyItemRemoved(pos);
            this.notifyItemRangeChanged(pos, getItemCount()-pos);
        }
    }

    public void getBackFromDeleteMode(){
        while(!pq.isEmpty()){
            int pos=pq.poll();
            isSelected.set(pos,0);
            selectedIndices.clear();
            isSelected.remove(pos);
            this.notifyItemChanged(pos);
        }
    }

    public void selectAllChats(){
        pq.clear();
        for(int i=isSelected.size()-1;i>=0;i--){
            pq.add(i);
            selectedIndices.add(i);
            isSelected.set(i,1);
            this.notifyItemChanged(i);
        }
    }
    public void unselectAllChats(){
        while(!pq.isEmpty()){
            int pos=pq.poll();
            isSelected.set(pos,0);
            this.notifyItemChanged(pos);
        }
        selectedIndices.clear();
    }
}

class ReceiverChatViewHolder extends RecyclerView.ViewHolder{

    TextView txtmessage;
    TextView timeText;
    View par;
    LinearLayout messageBoxLL;
    public ReceiverChatViewHolder(@NonNull View itemView) {
        super(itemView);
        this.par=itemView;
        txtmessage=itemView.findViewById(R.id.rec_m);
        timeText=itemView.findViewById(R.id.rec_m_time);
        messageBoxLL=itemView.findViewById(R.id.message_box_ll);
    }
}

class SenderChatViewHolder extends RecyclerView.ViewHolder{

    TextView txtmessage;
    TextView timeText;
    View par;
    LinearLayout messageBoxLL;
    public SenderChatViewHolder(@NonNull View itemView) {
        super(itemView);
        this.par=itemView;
        txtmessage=itemView.findViewById(R.id.sen_m);
        timeText=itemView.findViewById(R.id.sen_m_time);
        messageBoxLL=itemView.findViewById(R.id.message_box_ll);
    }
}

