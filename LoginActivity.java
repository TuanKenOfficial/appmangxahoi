package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btn_dn;
    private EditText edt_email, edt_pass;
    private TextView txt_in, txt_admin,txt_mk,txt_admin1;
    private CheckBox chk_nho;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private String username = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        btn_dn = (Button) findViewById(R.id.btn_dn);
        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_pass = (EditText) findViewById(R.id.edt_pass);
        txt_in = (TextView) findViewById(R.id.txt_in);
        txt_admin = (TextView) findViewById(R.id.txt_admin);
        txt_mk = (TextView) findViewById(R.id.txt_mk);
//        chk_nho = (CheckBox) findViewById(R.id.chk_nho);
//
//        Paper.init(this);//  phần nhớ mật khẩu


        loadingBar = (ProgressDialog) new ProgressDialog(this);


        txt_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserRegisterActivity();
            }
        });
        btn_dn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLogin();
            }
        });
        txt_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_dn.setText("Login Admin");
                txt_admin.setVisibility(View.INVISIBLE);
                username = "Admin";


            }
        });

        //quên mật khẩu
        txt_mk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailIT = new EditText(this);
        emailIT.setHint("Email");
        emailIT.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailIT.setMinEms(16);

        linearLayout.addView(emailIT);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);
        //Confirm : xác nhận
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailIT.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //cancel : huỷ bỏ
        builder.setNegativeButton("Huỷ bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery (String email){
        ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Đang gửi email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Gửi email thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi ! Kiểm tra lại", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void UserLogin() {

        String email = edt_email.getText().toString();
        String password = edt_pass.getText().toString();
//        if (chk_nho.isChecked()){
//            Paper.book().write(Prevalent.UserEmailKey,email);
//            Paper.book().write(Prevalent.UserPasswordKey,password);
//
//        }


        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Nhập email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Nhập mật khẩu ", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Đăng nhập");
            loadingBar.setMessage("Vui lòng đợi đang lấy dữ liệu từ cơ sở dữ liệu");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        if (username.equals("Admin")){
                            SendUserToMeActivity();
                            Toast.makeText(LoginActivity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else if(username.equals("Users")){
                            SendUserToMainActivity();
                            Toast.makeText(LoginActivity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Lỗi" +message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null)
        {
            SendUserToMainActivity(); //chuyển tới giao diện chính
//            SendUserToMeActivity(); //chuyển tới giao diện chính admin
        }
    }
    private void SendUserToMeActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MenuActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class );
        startActivity(registerIntent);
    }
}