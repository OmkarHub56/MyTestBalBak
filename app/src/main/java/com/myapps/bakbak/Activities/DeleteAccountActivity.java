package com.myapps.bakbak.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.R;

public class DeleteAccountActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    String sender_id;
    FirebaseDatabase database;

    DatabaseReference password;
    ValueEventListener passwordList;
    String currentPassword="74hf645hs8vsrdikxig";

    EditText currentPasswordInput;
    ImageView showOldPassword;
    int showOldPassStatus=1;


    ImageView backBtn;

    Button deleteAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        auth=FirebaseAuth.getInstance();
        sender_id=auth.getUid();
        database=FirebaseDatabase.getInstance();

        password=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.PASSWORD);
        passwordList=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPassword=snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        password.addValueEventListener(passwordList);

        currentPasswordInput=findViewById(R.id.old_pass);
        showOldPassword=findViewById(R.id.show_old_pass_eye);
        showOldPassword.setOnClickListener(view -> {
            if(showOldPassStatus==1){
                showOldPassword.setImageResource(R.drawable.hide_password_icon);
                showOldPassStatus=0;
                currentPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
            }
            else{
                showOldPassword.setImageResource(R.drawable.show_password_icon);
                showOldPassStatus=1;
                currentPasswordInput.setTransformationMethod(null);
            }
        });

        backBtn=findViewById(R.id.back_btn);
        backBtn.setOnClickListener(view -> onBackPressed());

        deleteAccountBtn=findViewById(R.id.delete_account_btn);
        deleteAccountBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view==deleteAccountBtn){
            showDeleteAccountDialog();
        }
    }

    public void showDeleteAccountDialog(){

        String oldPassword=currentPasswordInput.getText().toString();
        if(!oldPassword.equals(currentPassword)){
            Toast.makeText(this, "The current password is wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteAccountBtn.setEnabled(false);
        deleteAccountBtn.setClickable(false);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch(which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    ProgressDialog myPg=new ProgressDialog(this);
                    myPg.setTitle("Deleting account...");
                    myPg.setCancelable(false);
                    myPg.show();
                    deleteAccount(myPg);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    deleteAccountBtn.setEnabled(true);
                    deleteAccountBtn.setClickable(true);
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete your account permanently ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        deleteAccountBtn.setEnabled(true);
                        deleteAccountBtn.setClickable(true);
                    }
                }).show();


    }

    public void deleteAccount(ProgressDialog myPg){

        // deleting all friends
        DatabaseReference friendsList=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.FRIENDS);
        ValueEventListener friendsListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    database.getReference().child(ConstantsClass.USERS).child(dataSnapshot.getKey()).child(ConstantsClass.FRIENDS).child(sender_id).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        friendsList.addValueEventListener(friendsListener);
        new Handler().postDelayed(() -> friendsList.removeEventListener(friendsListener),1500);

        // deleting all groups
        DatabaseReference groupsList=database.getReference().child(ConstantsClass.USERS).child(sender_id).child(ConstantsClass.GROUPS_ADDED_TO);
        ValueEventListener groupsListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    database.getReference().child(ConstantsClass.GROUP_CHATS).child(dataSnapshot.getKey()).child(ConstantsClass.GROUP_MEMBERS_INFO).child(sender_id).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupsList.addValueEventListener(groupsListener);
        new Handler().postDelayed(() -> groupsList.removeEventListener(groupsListener),1500);

        // finishing all activities
        Intent intent2 = new Intent("finish_activity");
        sendBroadcast(intent2);

        // temp to be removed later
        auth.signOut();
        finish();
        Intent intent=new Intent(DeleteAccountActivity.this,LoginOrSignUpActivity.class);
        startActivity(intent);


//        auth.getCurrentUser().delete();
//        database.getReference().child(ConstantsClass.USERS).child(sender_id).removeValue().addOnCompleteListener(task -> {
//            finish();
//            Intent intent=new Intent(DeleteAccountActivity.this,LoginOrSignUpActivity.class);
//            startActivity(intent);
//        });
    }
}