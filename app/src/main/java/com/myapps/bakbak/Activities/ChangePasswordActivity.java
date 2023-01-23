package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.R;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    String current_uid;
    FirebaseDatabase database;
    DatabaseReference password;
    ValueEventListener passwordList;
    String currentPassword="74hf645hs8vsrdikxig";

    ImageButton backBtn;

    EditText currentPasswordInput;
    EditText newPasswordInput;
    EditText newPasswordConfirmInput;

    ImageView showOldPassword,showPassword,showConfirmPassword;

    Button changePasswordBtn;

    //0 - not visible
    //1 - visible
    int showOldPassStatus=1;
    int showPassStatus=1;
    int showConfirmPassStatus=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth=FirebaseAuth.getInstance();
        current_uid=auth.getCurrentUser().getUid();
        database=FirebaseDatabase.getInstance();

        password=database.getReference().child(ConstantsClass.USERS).child(current_uid).child(ConstantsClass.PASSWORD);
        passwordList=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPassword=snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        password.addValueEventListener(passwordList);

        backBtn=findViewById(R.id.back_btn);
        backBtn.setOnClickListener(view -> onBackPressed());

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


        newPasswordInput=findViewById(R.id.new_pass);
        showPassword=findViewById(R.id.show_pass_eye);
        showPassword.setOnClickListener(view -> {
            if(showPassStatus==1){
                showPassword.setImageResource(R.drawable.hide_password_icon);
                showPassStatus=0;
                newPasswordInput.setTransformationMethod(new PasswordTransformationMethod());
            }
            else{
                showPassword.setImageResource(R.drawable.show_password_icon);
                showPassStatus=1;
                newPasswordInput.setTransformationMethod(null);
            }
        });

        newPasswordConfirmInput=findViewById(R.id.new_pass_confirm);
        showConfirmPassword=findViewById(R.id.show_confirm_pass_eye);
        showConfirmPassword.setOnClickListener(view -> {
            if(showConfirmPassStatus==1){
                showConfirmPassword.setImageResource(R.drawable.hide_password_icon);
                showConfirmPassStatus=0;
                newPasswordConfirmInput.setTransformationMethod(new PasswordTransformationMethod());
            }
            else{
                showConfirmPassword.setImageResource(R.drawable.show_password_icon);
                showConfirmPassStatus=1;
                newPasswordConfirmInput.setTransformationMethod(null);
            }
        });

        changePasswordBtn=findViewById(R.id.change_password_btn);
        changePasswordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==changePasswordBtn){
            String oldPassword=currentPasswordInput.getText().toString();
            if(!oldPassword.equals(currentPassword)){
                Toast.makeText(this, "The current password is wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            String password=newPasswordInput.getText().toString();
            String confirmPassword=newPasswordConfirmInput.getText().toString();
            if(password.contains(" ")){
                Toast.makeText(this, "New password should not contain blank spaces", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.length()<6 || password.length()>15){
                Toast.makeText(this, "New password should be between 6 and 15 characters long", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!password.equals(confirmPassword)){
                Toast.makeText(this, "New password and New confirm password doesn't match", Toast.LENGTH_SHORT).show();
                return;
            }

            changePasswordBtn.setEnabled(false);
            changePasswordBtn.setClickable(false);



            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        ProgressDialog myPg=new ProgressDialog(this);
                        myPg.setTitle("Changing password...");
                        myPg.setCancelable(false);
                        myPg.show();
                        changePassword(password,myPg);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        changePasswordBtn.setEnabled(true);
                        changePasswordBtn.setClickable(true);
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to change password ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            changePasswordBtn.setEnabled(true);
                            changePasswordBtn.setClickable(true);
                        }
                    }).show();
        }
    }

    public void changePassword(String password,ProgressDialog myPg){
        auth.getCurrentUser().updatePassword(password).addOnCompleteListener(task ->
                database.getReference().child(ConstantsClass.USERS).child(current_uid).child(ConstantsClass.PASSWORD).setValue(password).addOnCompleteListener(task1 -> {
            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            changePasswordBtn.setEnabled(true);
            changePasswordBtn.setClickable(true);
            myPg.dismiss();
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        password.removeEventListener(passwordList);
    }
}