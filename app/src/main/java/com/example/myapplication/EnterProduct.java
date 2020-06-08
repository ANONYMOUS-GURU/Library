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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EnterProduct extends AppCompatActivity {

    EditText t1,t2,t3,t5,t6,t7;
    Button b1,b2,b3;
    DatabaseHelper db;
    ImageView img;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY_IMAGE = 0;
    static final int PERMISSION_CODE_GALLERY = 2;
    static final int CAMERA_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_product);
        Intent i = getIntent();
        final String username=i.getStringExtra("name");

        t1=findViewById(R.id.book_name);
        t2=findViewById(R.id.book_author);
        t3=findViewById(R.id.book_descr_detail);
        t5=findViewById(R.id.book_price);
        t6=findViewById(R.id.productQuantity);
        t7=findViewById(R.id.bookLink);

        db=new DatabaseHelper(this);
        b1=findViewById(R.id.exit_from_admin);
        b2=findViewById(R.id.Enter_values);
        b3=findViewById(R.id.book_pic_name);
        img=findViewById(R.id.book_pic_admin);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),AdminLogin.class);
                startActivity(i);
                finish();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1=t1.getText().toString();
                String s2=t2.getText().toString();
                String s3=t3.getText().toString();
                byte[] s4=mystorage.imageViewToByte(img);
                double s5=Double.parseDouble(t5.getText().toString());
                int s6=Integer.parseInt(t6.getText().toString());
                String s7=t7.getText().toString();
                Log.d("Cont",s1);
                Log.d("Cont",s2);
                Log.d("Cont",s3);
                Log.d("Cont",String.valueOf(s5));
                if(db.add_book_to_db(username,s1,s2,s3,s4,s5,s6,s7)) {
                    Toast.makeText(getApplicationContext(), "Book added successfully", Toast.LENGTH_SHORT).show();
                    t1.setText("");
                    t2.setText("");
                    t3.setText("");
                    t5.setText("");
                    t6.setText("");
                    t7.setText("");
                    img.setImageResource(android.R.color.transparent);
                }else
                    Toast.makeText(getApplicationContext(),"Book was not added",Toast.LENGTH_SHORT).show();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(EnterProduct.this);
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
