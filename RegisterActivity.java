package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText edt_email , edt_pass , edt_pas;
    private Button btn_dk;
    private TextView txt_out;
    //auth + database
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private EditText edt_username, edt_fullname, edt_country;
    private ProgressDialog loadingBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //auth
        mAuth = FirebaseAuth.getInstance();
        //database

        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_pass = (EditText) findViewById(R.id.edt_pass);
        edt_pas = (EditText) findViewById(R.id.edt_pas);
        btn_dk = (Button) findViewById(R.id.btn_dk);
        edt_username = (EditText) findViewById(R.id.edt_username);
        edt_fullname = (EditText) findViewById(R.id.edt_fullname);
        edt_country = (EditText) findViewById(R.id.edt_countruy);
        txt_out = (TextView) findViewById(R.id.txt_out);


        loadingBar = (ProgressDialog) new ProgressDialog(this);


        txt_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserLoginActivity();
            }
        });
        btn_dk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DangKy();
            }
        });
    }

    private void SendUserLoginActivity() {
        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class );
        startActivity(registerIntent);
    }

    private void DangKy() {
        String email = edt_email.getText().toString();
        String password = edt_pass.getText().toString();
        String cofirmpassword = edt_pas.getText().toString();
        String username = edt_username.getText().toString();
        String fullname = edt_fullname.getText().toString();
        String country = edt_country.getText().toString();


        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Nh???p email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Nh???p m???t kh???u", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cofirmpassword))
        {
            Toast.makeText(this, "Nh???p l???i m???t kh???u", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(cofirmpassword))
        {
            Toast.makeText(this, "M???t kh???u ch??a kh???p", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "M???i b???n nh???p t??n", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "M???i b???n nh???p ?????y ????? t??n", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "M???i b???n nh???p qu???c gia c???a b???n", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("????ng k??");
            loadingBar.setMessage("Vui l??ng ?????i trong gi??y l??t");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        // ph???n firebase user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userID = firebaseUser.getUid(); // d??ng trong h??m task.isSuccessful n???u ko b??? l???i

                        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        //?????y l??n c?? s??? d??? li???u
                        HashMap hashMap = new HashMap();
                        hashMap.put("uid",userID);
                        hashMap.put("admin","0");
                        hashMap.put("username",username);
                        hashMap.put("fullname",fullname);
                        hashMap.put("email", email);
                        hashMap.put("password",password);
                        hashMap.put("country",country);
                        hashMap.put("typingTo","noOne");
                        hashMap.put("status","offline");
                        hashMap.put("profileimage","https://firebasestorage.googleapis.com/v0/b/socialnetwork-8041e.appspot.com/o/Users.jpg?alt=media&token=c8b35121-37db-45cf-af17-515e1d7865d5");

                        UsersRef.child(userID).setValue(hashMap).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                loadingBar.dismiss();
                                Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(loginIntent);
                                Toast.makeText(RegisterActivity.this, "????ng k?? th??nh c??ng!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "L???i: "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }
}