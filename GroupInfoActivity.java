package com.example.socialnetwork;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialnetwork.Adapter.GroupAddUsersAdapter;
import com.example.socialnetwork.Model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {



    private String groupId;
    private String myGroupRole="";
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;

    private ImageView groupIconIv;
    private TextView createdByTv,descriptionTv,editGroupTv,addParticipantTv,leaveGroupTv,participantsTv;
    private RecyclerView participantsRv;

    private List<Users> usersList;
    private GroupAddUsersAdapter groupAddUsersAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);



        //action bar
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
//        participantsTv = findViewById(R.id.participantsTv);
//        participantsRv = findViewById(R.id.participantsRv);



        firebaseAuth = FirebaseAuth.getInstance();
        groupId = getIntent().getStringExtra("groupId");
        
        loadGroupInfo();
        loadMyGroupRole();

        // add users vào groups
        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupAddUsersActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        // edit group
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditUsersActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        // chọn rời khỏi groups và nếu bạn là người tạo group có thể xoá group
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if(myGroupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = "Bạn có chắc chắn muốn xóa nhóm vĩnh viễn không?";
                    positiveButtonTitle = "DELETE";

                }
                else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Bạn có chắc chắn muốn rời khỏi nhóm vĩnh viễn không?";
                    positiveButtonTitle = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(myGroupRole.equals("creator")){
                                    deleteGroup();
                                }
                                else {
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    private void leaveGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Rời nhóm thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, MenuActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Xoá nhóm thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, MenuActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

                    loadCreateInfo(datetime,createdBy);
                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);

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

    private void loadCreateInfo(String datetime, String createdBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String username = ""+ds.child("username").getValue();

                    createdByTv.setText("Create by "+username +" on "+datetime);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    myGroupRole = ""+ds.child("role").getValue();
                    actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() +" ("+myGroupRole+")");


                    if(myGroupRole.equals("participant")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.GONE);
                        leaveGroupTv.setText("Leave Group");
                    }else if(myGroupRole.equals("admin")){
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Leave Group");
                    }else if (myGroupRole.equals("creator")){
                        editGroupTv.setVisibility(View.VISIBLE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Delete Group");
                    }

                }
//                loadParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*
    * Phần dưới bị lỗi do ko load được recycle view lên nên phần này tạm thời đóng lại
    * Thứ nhất là do phần cây user ko xác định được người dùng nên load ko được phần username
    * Thứ hai em đã nghiên cứu rất nhiều và coi lỗi từ nhiều nguồn nhưng fix rồi nó vẫn ko ra được.*/
//    private void loadParticipants() {
//        usersList = new ArrayList<>();
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
//        reference.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                usersList.clear();
//                for (DataSnapshot ds: dataSnapshot.getChildren()){
//
//                    //get uid from Group > Participants
//                    String uid = ""+ds.child("uid").getValue();
//
//                    //get info of user using uid we got above
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot ds: dataSnapshot.getChildren()){
//                                Users user = ds.getValue(Users.class);
////                                //adapter
////                                groupAddUsersAdapter = new GroupAddUsersAdapter(GroupInfoActivity.this,usersList,""+groupId,""+myGroupRole);
////                                //set adapter
////                                participantsRv.setAdapter(groupAddUsersAdapter);
//
//
//                                usersList.add(user);
//                            }
//                            participantsTv.setText("Participants ("+usersList.size()+")");
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}