package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialnetwork.Adapter.ChatsAdapter;
import com.example.socialnetwork.Model.Chats;
import com.example.socialnetwork.Model.Users;
import com.example.socialnetwork.Notifications.APIService;
import com.example.socialnetwork.Notifications.Client;
import com.example.socialnetwork.Notifications.Data;
import com.example.socialnetwork.Notifications.Response;
import com.example.socialnetwork.Notifications.Sender;
import com.example.socialnetwork.Notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatsActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    CircleImageView profile_image;
    TextView txt_username,txt_status;
    EditText edt_mes;
    ImageButton btn_send, img_up;
    ImageView img_block;

    //Auth
    FirebaseAuth firebaseAuth;
    FirebaseUser fUser;
    FirebaseDatabase firebaseDatabase;

    //????? ki???m tra xem s??? d???ng c?? th???y th??ng b??o hay kh??ng
    ValueEventListener seenListener; // ki???m tra coi c?? xem tin nh???n ko
    String id = "";
    DatabaseReference reference;

    ChatsAdapter chatsAdapter;
    List<Chats> mChat;
    String hisUid;
    String myUid;
    String hisImage;


    // date + time
    String saveCurrentDate, saveCurrentTime, postName;
    boolean notify = false;

    APIService apiService;
    String currentUserID;

    private ProgressDialog loadingBar;
    AlertDialog alertDialog;

    // upload h??nh trong chat
    StorageReference chatsReference;
    private Uri imageUri;
    private String imageUrl = "" ;
    StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        profile_image= (CircleImageView) findViewById(R.id.profile_image);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_status = (TextView) findViewById(R.id.txt_status);
        edt_mes = (EditText) findViewById(R.id.edt_message);
        btn_send = (ImageButton) findViewById(R.id.btn_send);
        img_up = (ImageButton) findViewById(R.id.img_up);

        //upload h??nh chat l??n storage

        chatsReference = FirebaseStorage.getInstance().getReference("UploadImageChats");

        //api service
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        //linerLayout for RecycleView

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //RecycleView
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatsActivity.this, MenuActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid"); // m???i s???a
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        myUid = fUser.getUid();





        //m???i th??m
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");
         Query query = reference.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String username = ""+ds.child("username").getValue();
                    hisImage = ""+ds.child("profileimage").getValue();

                    String status = ""+ds.child("status").getValue();
                    if (status.equals("online")){
                        txt_status.setText(status); // online chats
                    }
                    else {
                        txt_status.setText("offline"); // offline trong chats
                    }

                    txt_username.setText(username);

                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.users).into(profile_image);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.users).into(profile_image);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = edt_mes.getText().toString().trim();
                notify=true;

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatsActivity.this,"Kh??ng th??? g???i tin nh???n tr???ng...",Toast.LENGTH_SHORT).show();
                }else {
                    sendMessage(message);
                }

            }
        });
        edt_mes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    TypingStatus("noOne");
                }else {
                    TypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // up ???nh l??n chat
        img_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(ChatsActivity.this);

            }
        });

        readMessages();// ?????c tin nh???n
        seenMessage(); // g???i tin nh???n v?? x??? l??
      

    }

    private void seenMessage() {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chats chat = ds.getValue(Chats.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chat = snapshot.getValue(Chats.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                        chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        mChat.add(chat);

                    }
                    chatsAdapter = new ChatsAdapter(ChatsActivity.this, mChat,hisImage);
                    chatsAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        //currentDate
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentData.format(calendar.getTime());
        //currentTime
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
        // ph???n name b???ng 2 c??i current c???ng l???i
        postName = saveCurrentDate+saveCurrentTime;


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("time" , saveCurrentTime);
        hashMap.put("date",saveCurrentDate);
        hashMap.put("isseen",false);
        hashMap.put("type","text");

        edt_mes.setText("");


        reference.push().setValue(hashMap);


        //token
        String msg = message;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (notify){
                    sendNotification(hisUid, user.getUsername(),msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //chatList
        // add user to chat fragment
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid)
                .child(hisUid);
        chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisUid)
                .child(myUid);
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


    // g???i th??ng b??o tin nh???n
    private void sendNotification(String hisUid,String username,String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(myUid, R.mipmap.ic_launcher, username+": "+message, "New Message", hisUid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            if (response.code() == 200){
                                if (response.body().success != 1){
                                    Toast.makeText(ChatsActivity.this, "Failed",Toast.LENGTH_SHORT).show();
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("T???i l??n");
        progressDialog.show();

        if (imageUri != null){
            final StorageReference filereference = chatsReference.child(System.currentTimeMillis()
                    +"."+ getFileExtension(imageUri));
            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){

                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();
                        //currentDate
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
                        saveCurrentDate = currentData.format(calendar.getTime());
                        //currentTime
                        Calendar calendar1 = Calendar.getInstance();
                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                        saveCurrentTime = currentTime.format(calendar.getTime());
                        // ph???n name b???ng 2 c??i current c???ng l???i
                        postName = saveCurrentDate+saveCurrentTime;

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender",myUid);
                        hashMap.put("receiver",hisUid);
                        hashMap.put("message", ""+myUrl);
                        hashMap.put("time", saveCurrentTime);
                        hashMap.put("date", saveCurrentDate);
                        hashMap.put("type","image");
                        hashMap.put("isseen",false);


                        reference.child("Chats").push().setValue(hashMap);
                        progressDialog.dismiss();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Users user = dataSnapshot.getValue(Users.class);
                                if(notify){
                                    sendNotification(hisUid,user.getUsername(),"???? g???i cho b???n 1 b???c ???nh");
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //chatList
                        // add user to chat fragment
                        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                                .child(myUid)
                                .child(hisUid);
                        chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    chatRef1.child("id").setValue(hisUid);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                                .child(hisUid)
                                .child(myUid);
                        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    chatRef2.child("id").setValue(myUid);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }else {
                        Toast.makeText(ChatsActivity.this,"Xin th??? l???i",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatsActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this,"Kh??ng c?? h??nh ???nh n??o ???????c ch???n",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            uploadImage();
        }else {
            Toast.makeText(this, "L???i!", Toast.LENGTH_SHORT ).show();
        }
    }



        //status online hay offline
    private void status (String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status" ,status);
        reference.updateChildren(hashMap);
    }

    private void TypingStatus (String typing){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo" ,typing);
        reference.updateChildren(hashMap);
    }
    private void checkUsersStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            myUid = user.getUid();
        }
        else {
            startActivity(new Intent(this,MenuActivity.class));
            finish();
        }
    }


    @Override
    protected void onStart() {
        checkUsersStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        TypingStatus("noOne");
        reference.removeEventListener(seenListener);
    }
}