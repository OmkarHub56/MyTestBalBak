package com.myapps.bakbak.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myapps.bakbak.ConstantsClass;
import com.myapps.bakbak.Models.MyUsers;
import com.myapps.bakbak.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    String currentUserUId;
    FirebaseDatabase database;
    FirebaseStorage storage;

    CircleImageView profile_pic;

    int SELECT_IMAGE_CODE=200;

    TextView friendCountShower;

    LinearLayout addNewFriendBtn;

    Button logOutBtn;

    TextView user_username;

    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        currentUserUId=auth.getCurrentUser().getUid();
        storage=FirebaseStorage.getInstance();

        user_username=findViewById(R.id.textView3);
        enterNewUsername=findViewById(R.id.enter_new_username);
        changeDoneTick=findViewById(R.id.imageView2);
        changeDoneTick.setOnClickListener(this);
        editBtn=findViewById(R.id.imageView);
        editBtn.setOnClickListener(this);
        backBtn=findViewById(R.id.back_btn);
        backBtn.setOnClickListener(this);
        profile_pic=findViewById(R.id.profil_pic);

        friendCountShower=findViewById(R.id.friend_count_shower);
        database.getReference().child(ConstantsClass.USERS).child(currentUserUId).child(ConstantsClass.FRIENDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendCountShower.setText("You have total of "+ snapshot.getChildrenCount() +" friends");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addNewFriendBtn=findViewById(R.id.add_new_friends_btn);
        addNewFriendBtn.setOnClickListener(view -> {
            onBackPressed();
        });

        logOutBtn=findViewById(R.id.logoutbtn);
        logOutBtn.setOnClickListener(this);

//        currentUser=database.getReference().child("Users").child(auth.getCurrentUser().getUid());
        Log.i("mdx","dfd");

        database.getReference().child(ConstantsClass.USERS).child(currentUserUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyUsers mu=snapshot.getValue(MyUsers.class);
                user_username.setText(mu.getUsername());

                Log.i("mdx",mu.getUsername());
                Log.i("mdx","dfd");
                mu.setProfile_pic(snapshot.child(ConstantsClass.PROFILE_PIC).getValue(String.class));
                if(isValidContextForGlide(ProfileActivity.this)) {
                    Glide.with(ProfileActivity.this).load(mu.getProfile_pic()).into(profile_pic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        Glide.with(this).load(storage.getReference().child("Users/").child(auth.getCurrentUser().getUid()+"/").child("profilePic.jpeg/")).into(profile_pic);
    }

    public void updateProfilePic(View view){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.i("bv","1");
        startActivityForResult(Intent.createChooser(intent,"Select profile pic"),SELECT_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("gkl","aya1");

        if(resultCode==RESULT_OK){
            Log.i("gkl","aya");
            if (requestCode==SELECT_IMAGE_CODE){
                Log.i("gkl","aya2");

                Log.i("bv","2");
                Uri uri=data.getData();
//                profile_pic.setImageURI(uri);
                Glide.with(this).load(uri).into(profile_pic);
                Log.i("bv","3");


                StorageReference ref=storage.getReference().child(ConstantsClass.USERS).child(currentUserUId).child(ConstantsClass.PROFILE_PIC);
//                ref.putFile(uri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri1 -> database.getReference().child(ConstantsClass.USERS).child(currentUserUId).child(ConstantsClass.PROFILE_PIC).setValue(uri1.toString()).addOnCompleteListener(task -> {
//
//                    if(isValidContextForGlide(ProfileActivity.this)){
//                        Glide.with(ProfileActivity.this).load(uri1).into(profile_pic);
//                    }
//                })));

                ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("zxd","aya5");
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri1) {
                                Log.i("sd","aya4");
                                database.getReference().child(ConstantsClass.USERS).child(currentUserUId).child(ConstantsClass.PROFILE_PIC).setValue(uri1.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.i("zxd","aya");
                                        if(isValidContextForGlide(ProfileActivity.this)){
                                            Log.i("zxd","aya1");
                                            Glide.with(ProfileActivity.this).load(uri1).into(profile_pic);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

            }
        }
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    EditText enterNewUsername;
    ImageView changeDoneTick;
    ImageView editBtn;
    @Override
    public void onClick(View view) {
        if(view==editBtn){
            enterNewUsername.setVisibility(View.VISIBLE);
            user_username.setVisibility(View.GONE);
            changeDoneTick.setVisibility(View.VISIBLE);
        }
        else if(view==changeDoneTick){
            String newUsername=enterNewUsername.getText().toString();

            newUsername=newUsername.trim();
            if(newUsername.length()<3 || newUsername.length()>15){
                Toast.makeText(this, "Length of username is too short or too long", Toast.LENGTH_SHORT).show();
                enterNewUsername.setText("");
                return;
            }
            newUsername=newUsername.replace(" ","_");
            enterNewUsername.setText(newUsername);

            user_username.setVisibility(View.VISIBLE);
            enterNewUsername.setVisibility(View.GONE);
            changeDoneTick.setVisibility(View.GONE);

            String finalNewUsername = newUsername;
            database.getReference().child(ConstantsClass.USERS).child(currentUserUId).child("username").setValue(newUsername).addOnCompleteListener(task -> {
                user_username.setText(finalNewUsername);
                Toast.makeText(ProfileActivity.this, "Username changed successfully", Toast.LENGTH_SHORT).show();
            });

        }
        else if(view==backBtn){
            onBackPressed();
        }
        else if(view==logOutBtn){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        database.getReference().child(ConstantsClass.USERS).child(currentUserUId).child("online").setValue(false);
                        auth.signOut();
                        finish();

                        // to finish all activities related to the current user
                        Intent intent2 = new Intent("finish_activity");
                        sendBroadcast(intent2);


                        Intent intent=new Intent(ProfileActivity.this,LoginOrSignUpActivity.class);
                        startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }
}