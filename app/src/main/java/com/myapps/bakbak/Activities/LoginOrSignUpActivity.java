package com.myapps.bakbak.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;
import com.myapps.bakbak.databinding.ActivityLoginorsignupBinding;

public class LoginOrSignUpActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivityLoginorsignupBinding binding;

    TextView title_shower,sub_title_shower;

    Button switchSignInSignUpButton,loginOrRegisterButton;
    EditText enterUsername,enterEmail;
    EditText enterPassword,enterConfirmPassword;
    ImageView showPassword,showConfirmPassword;

    // made just to hide it
    LinearLayout confirmPassHolder;

    ImageView firstPage,secondPage;

    // register or sign in
    String curr_status="SIGN";

    //0 - not visible
    //1 - visible
    int showPassStatus=1;
    int showConfirmPassStatus=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityLoginorsignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        title_shower=binding.spc1;
        sub_title_shower=binding.spc2;

        switchSignInSignUpButton=binding.button100;
        switchSignInSignUpButton.setOnClickListener(this);

        loginOrRegisterButton=binding.button99;
        loginOrRegisterButton.setOnClickListener(this);

        enterUsername=binding.editTextTextPersonName100;
        enterEmail=binding.editTextTextPersonName99;

        enterPassword=findViewById(R.id.editTextTextPersonName98);
        showPassword=findViewById(R.id.show_pass_eye);
        showPassword.setOnClickListener(view -> {
            if(showPassStatus==1){
                showPassword.setImageResource(R.drawable.hide_password_icon);
                showPassStatus=0;
                enterPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            else{
                showPassword.setImageResource(R.drawable.show_password_icon);
                showPassStatus=1;
                enterPassword.setTransformationMethod(null);
            }
        });
        enterConfirmPassword=findViewById(R.id.editTextTextPersonName97);
        showConfirmPassword=findViewById(R.id.show_confirm_pass_eye);
        showConfirmPassword.setOnClickListener(view -> {
            if(showConfirmPassStatus==1){
                showConfirmPassword.setImageResource(R.drawable.hide_password_icon);
                showConfirmPassStatus=0;
                enterConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            else{
                showConfirmPassword.setImageResource(R.drawable.show_password_icon);
                showConfirmPassStatus=1;
                enterConfirmPassword.setTransformationMethod(null);
            }
        });
        confirmPassHolder=findViewById(R.id.confirm_pass_holder);

        firstPage=findViewById(R.id.first_page);
        secondPage=findViewById(R.id.second_page);

        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(LoginOrSignUpActivity.this,FriendsListActivity.class);
            startActivity(intent);

            // if user is already logged in no need to return to this activity unless user logs out again
            finish();
        }
    }

    @Override
    public void onClick(View view) {


        if(view==loginOrRegisterButton){
            String username=enterUsername.getText().toString();
            String email=enterEmail.getText().toString();
            String password=enterPassword.getText().toString();
            String confirmPassword=enterConfirmPassword.getText().toString();
            if(curr_status.equals("CREATE")){
                createNewAccount(username,email,password,confirmPassword);
            }
            else{
                signInWithAccount(email.trim(),password,username);
            }
        }
        else if(view==switchSignInSignUpButton){
            if(curr_status.equals("SIGN")){

                title_shower.setText("Register");
                sub_title_shower.setText("In you are new here");

                enterUsername.setVisibility(View.VISIBLE);
                confirmPassHolder.setVisibility(View.VISIBLE);
                loginOrRegisterButton.setText("REGISTER");
                switchSignInSignUpButton.setText("<-  Go back to sign in page");

                curr_status="CREATE";

                firstPage.getLayoutParams().width= (int) getResources().getDimension(com.intuit.sdp.R.dimen._10sdp);
                firstPage.getLayoutParams().height= (int) getResources().getDimension(com.intuit.sdp.R.dimen._10sdp);
                firstPage.requestLayout();
                secondPage.getLayoutParams().width= (int) getResources().getDimension(com.intuit.sdp.R.dimen._15sdp);
                secondPage.getLayoutParams().height= (int) getResources().getDimension(com.intuit.sdp.R.dimen._15sdp);
                secondPage.requestLayout();

            }
            else{

                title_shower.setText("Sign in");
                sub_title_shower.setText("If you already have an account");

                enterUsername.setVisibility(View.GONE);
                confirmPassHolder.setVisibility(View.GONE);
                loginOrRegisterButton.setText("LOG IN");
                switchSignInSignUpButton.setText("Create new account  ->");

                curr_status="SIGN";

                firstPage.getLayoutParams().width= (int) getResources().getDimension(com.intuit.sdp.R.dimen._15sdp);
                firstPage.getLayoutParams().height= (int) getResources().getDimension(com.intuit.sdp.R.dimen._15sdp);
                firstPage.requestLayout();
                secondPage.getLayoutParams().width= (int) getResources().getDimension(com.intuit.sdp.R.dimen._10sdp);
                secondPage.getLayoutParams().height= (int) getResources().getDimension(com.intuit.sdp.R.dimen._10sdp);
                secondPage.requestLayout();
            }
        }
    }


    public void createNewAccount(String username,String email,String password,String confirmPassword){

        //later I can add custom Toasts for all these conditions
        username=username.trim();
        if(username.length()<=2 || username.length()>=16){
            Toast.makeText(this, "Length of username is too short or too long", Toast.LENGTH_SHORT).show();
            enterUsername.setText("");
            return;
        }
        username=username.replace(" ","_");
        enterUsername.setText(username);
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length()<1){
            Toast.makeText(this, "This email is badly formatted", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.contains(" ")){
            Toast.makeText(this, "Password should not contain blank spaces", Toast.LENGTH_SHORT).show();
            enterPassword.setText("");
            return;
        }
        if(password.length()<6 || password.length()>15){
            Toast.makeText(this, "Length of password should be between 6 and 15", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Password and Confirm Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        final String finalUsername = username;
        ProgressDialog myPg=new ProgressDialog(this);
        myPg.setTitle("Creating account...");
        myPg.setCancelable(false);
        myPg.show();
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                String currentUserUid=auth.getUid();
                // storing users data in database
                database.getReference().child(ConstantsClass.USERS).child(currentUserUid).setValue(new MyUsers(finalUsername,email,password)).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        myPg.dismiss();
                        Toast.makeText(LoginOrSignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        // this is because after account creation user is automatically signed in
                        auth.signOut();
                    }
                    else{
                        Toast.makeText(this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        // todo, later when I learn about it more
                    }
                });
            }
            else{
                Toast.makeText(LoginOrSignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signInWithAccount(String email,String password,String username){

        ProgressDialog myPg=new ProgressDialog(this);
        myPg.setTitle("Logging in...");
        myPg.setCancelable(false);
        myPg.show();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("mdx", auth.getUid());
                database.getReference().child(ConstantsClass.USERS).child(auth.getUid()).child("online").setValue(true);
                Intent intent = new Intent(LoginOrSignUpActivity.this, FriendsListActivity.class);
                startActivity(intent);
                myPg.dismiss();
                finish();

            } else {
                Toast.makeText(LoginOrSignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}