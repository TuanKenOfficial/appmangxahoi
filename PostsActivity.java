package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;



public class PostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView img_btnanh;
    private EditText edt_anh;
    private Button btn_updateanh;

    private ProgressDialog loadingBar;
    //Storage
    private String Description;

    private StorageReference Postreference;
    private Uri imageUri;
    private String imageUrl = "" ;
    StorageTask uploadTask;
    //database
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private String saveCurrentData, saveCurrentTime, postName;
    private ImageView close;

    String country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);


        mAuth = FirebaseAuth.getInstance();
        Postreference = FirebaseStorage.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar =  findViewById(R.id.toolbar);
        img_btnanh = (ImageView) findViewById(R.id.img_btnanh);
        edt_anh = (EditText) findViewById(R.id.edt_anh);
        btn_updateanh = (Button) findViewById(R.id.btn_updateanh);
        close= findViewById(R.id.close);
        loadingBar = (ProgressDialog) new ProgressDialog(this); // progressDialog
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");




        //b???m v??o button h??nh
        img_btnanh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(PostsActivity.this);

            }
        });
        //b???m v??o button  update
        btn_updateanh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnh();

            }
        });

        edt_anh.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN  && i== KeyEvent.KEYCODE_ENTER){
                    String text = edt_anh.getText().toString() + "\n";
                    edt_anh.setText(text);
                    edt_anh.setSelection(edt_anh.getText().length());
                    return true;
                }
                return false;
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostsActivity.this, MenuActivity.class));
                finish();

            }
        });
    }



    private void updateAnh() {
        loadingBar.setTitle("Upload ???nh");
        loadingBar.setMessage("??ang nh???n ???nh v?? ????a ???nh l??n c?? s??? d??? li???u");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        if (imageUri != null){
                final StorageReference filereference = Postreference.child(System.currentTimeMillis()
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
                            //currentDate
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentData = new SimpleDateFormat("dd-MMMM-yyyy");
                            saveCurrentData = currentData.format(calendar.getTime());
                            //currentTime
                            Calendar calendar1 = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                            saveCurrentTime = currentTime.format(calendar.getTime());
                            // ph???n name b???ng 2 c??i current c???ng l???i
                            postName = saveCurrentData+saveCurrentTime;

                            //X??? l?? ???nh getUri c???a ???nh
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();


                            //description
                            String Description = edt_anh.getText().toString();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                            String postId = reference.push().getKey();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("image",""+imageUrl);
                            hashMap.put("postid",postId); // id c???a ???nh ???? ????ng
                            hashMap.put("date", saveCurrentData);
                            hashMap.put("time", saveCurrentTime);
                            hashMap.put("description",Description);
                            hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                            reference.child(postId).setValue(hashMap); // ?????y l??n realtime database
                            // c??u l???nh th??ng b??o v?? chuy???n activity , k???t th??c c??i progressbar
                            Toast.makeText(PostsActivity.this, "Upload th??nh c??ng", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                            loadingBar.dismiss();
                        }else {
                            Toast.makeText(PostsActivity.this,"Xin th??? l???i",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostsActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(this,"Upload kh??ng th??nh c??ng!! B???n ch??a ch???n ???nh",Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
    }
    //l???y gi?? tr??? ??u??i c???a ???nh
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            img_btnanh.setImageURI(imageUri);

        } else {
            Toast.makeText(this, "H??nh ???nh ch??a c??, t??i ????a b???n v??? m??n h??nh ch??nh v?? th??? l???i",Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
    }


    private void SendUserToMainActivity() {
        Intent postIntent = new Intent(PostsActivity.this, MenuActivity.class);
        startActivity(postIntent);
    }

   
}