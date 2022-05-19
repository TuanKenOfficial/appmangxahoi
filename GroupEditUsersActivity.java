package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditUsersActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private String groupId;

    private StorageReference Postreference;
    private Uri imageUri;
    private String imageUrl = "" ;
    StorageTask uploadTask;


    private FirebaseAuth firebaseAuth;
    private ImageView groupIconIv,close;
    private EditText edt_grouptitle, edt_groupdescription;
    private FloatingActionButton updateGroup;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit_users);


        //action bar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Groups");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupId = getIntent().getStringExtra("groupId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        groupIconIv = findViewById(R.id.groupIconIv);
        edt_grouptitle = findViewById(R.id.edt_grouptitle);
        edt_groupdescription = findViewById(R.id.edt_groupdescription);
        updateGroup = findViewById(R.id.updateGroup);


        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadGroupInfo();

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        updateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdateGroup();

            }
        });


    }

    private void startUpdateGroup() {
        String groupTitle = edt_grouptitle.getText().toString().trim();
        String groupDescription = edt_groupdescription.getText().toString().trim();

        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Tiêu đề nhóm là bắt buộc", Toast.LENGTH_SHORT).show();
            return;


        }
        progressDialog.setMessage("Update Groups");
        progressDialog.show();

        if(imageUri == null){


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle", groupTitle);
            hashMap.put("groupDescription",groupDescription);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
            reference.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditUsersActivity.this, "Upload Group thành công", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            String g_timestamp = ""+System.currentTimeMillis();
            String fileNameAndPath = "Group_Imgs/"+"image"+g_timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful());
                            Uri downloadUri = p_uriTask.getResult();
                            if(p_uriTask.isSuccessful()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("groupTitle", groupTitle);
                                hashMap.put("groupDescription",groupDescription);
                                hashMap.put("groupIcon",""+downloadUri);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                                reference.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditUsersActivity.this, "Upload Group thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();


                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String datetime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


                    edt_grouptitle.setText(groupTitle);
                    edt_groupdescription.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.users).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.users);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendToGroupChat() {
        Intent intent = new Intent(GroupEditUsersActivity.this,MenuActivity.class);
        startActivity(intent);
    }

    private void showImagePickDialog() {
        CropImage.activity().setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(GroupEditUsersActivity.this);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            groupIconIv.setImageURI(imageUri);


        } else {
            Toast.makeText(this, "Hình ảnh chưa có, tôi đưa bạn về màn hình chính và thử lại",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
    private void checkUser(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            actionBar.setSubtitle(user.getEmail());
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}