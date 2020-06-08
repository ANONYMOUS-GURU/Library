package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    EditText usr,password,conf_password,name,email;
    Button conf,exit,upImage;
    DatabaseHelper db;
    ImageView img;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY_IMAGE = 0;
    static final int PERMISSION_CODE_GALLERY = 2;
    static final int CAMERA_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        usr=findViewById(R.id.username_txt_log);
        password=findViewById(R.id.password_txt_log);
        conf_password=findViewById(R.id.conf_password_txt_log);
        name=findViewById(R.id.name_login);
        email=findViewById(R.id.email_login);
        img=findViewById(R.id.imageUserLogin);
        conf=findViewById(R.id.confirm_bt_log);
        exit=findViewById(R.id.exit_bt_log);
        upImage=findViewById(R.id.submitUserImageLogin);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        db=new DatabaseHelper(this);

        conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1=usr.getText().toString();
                String s2=password.getText().toString();
                String s3= conf_password.getText().toString();
                String s4=name.getText().toString();
                String s5= email.getText().toString();
                byte[] imageUser=mystorage.imageViewToByte(img);

                if(s1.equals("")||s2.equals("")||s3.equals(""))
                    Toast.makeText(getApplicationContext(),"Some Fields are empty",Toast.LENGTH_SHORT).show();

                else
                {
                    if(s3.equals(s2))
                    {
                        boolean x=db.check_unique_username(s1);
                        if(x)
                        {
                            boolean insert=db.insert_to_user(s1,s2,s4,s5,imageUser);
                            if(insert)
                            {
                                Toast.makeText(getApplicationContext(),"Registered Successful",Toast.LENGTH_SHORT).show();
                                Intent i= new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Username already exists",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Passwords do not Match",Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        upImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(LoginPage.this);
            }
        });
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                    {
                        if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED)
                        {
                            String[] permission={Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission,CAMERA_REQUEST_CODE);
                            dispatchTakePictureIntent();
                        }
                        else
                        {
                            dispatchTakePictureIntent();
                        }
                    }
                    else
                    {
                        dispatchTakePictureIntent();
                    }

                } else if (options[item].equals("Choose from Gallery")) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                    {
                        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                        {
                            String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permissions,PERMISSION_CODE_GALLERY);
                        }
                        else
                        {
                            getImageFromGallery();
                        }
                    }
                    else
                    {
                        getImageFromGallery();
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                img.setImageBitmap((Bitmap) data.getExtras().get("data"));
            }
        } else if (requestCode == REQUEST_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void getImageFromGallery()
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_GALLERY_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case PERMISSION_CODE_GALLERY:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    getImageFromGallery();
                }
                else
                {
                    Toast.makeText(this,"Permission Denied!!",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case CAMERA_REQUEST_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED)
                {
                    dispatchTakePictureIntent();
                }
                else
                {
                    Toast.makeText(this,"Camera Permission Denied!!",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
