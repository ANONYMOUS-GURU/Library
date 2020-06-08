package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserPage extends AppCompatActivity {

    TextView txt1, txt2, txt3;
    Button btn1, btn2;
    DatabaseHelper dbHelper;
    SQLiteDatabase mDatabase;
    ImageView img;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    private String username;

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getReadableDatabase();

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);
        dbHelper = new DatabaseHelper(this);
        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        txt1 = findViewById(R.id.Name);
        txt2 = findViewById(R.id.username_user);
        txt3 = findViewById(R.id.email_id_user);
        img = findViewById(R.id.imageView3);

        btn1 = findViewById(R.id.seeBooks);
        btn2 = findViewById(R.id.logout_bt_user);

        Cursor mCursor = mDatabase.rawQuery("Select email,name,userImage from " + DatabaseHelper.TABLE1 + " where username=?", new String[]{String.valueOf(username)});
        Log.v("Cony", "my username= " + username);
        mCursor.moveToFirst();

        byte[] stream = mCursor.getBlob(2);
        Bitmap user_img = mystorage.byteToImage(stream);

        txt1.setText(mCursor.getString(1));
        txt2.setText(username);
        txt3.setText(mCursor.getString(0));

        img.setImageBitmap(Bitmap.createScaledBitmap(user_img, 300, 300, false));


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), BuyView.class);
                i.putExtra("username", username);
                startActivity(i);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefs.edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        mCursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_icon:
                Intent searchIntent = new Intent(getApplicationContext(), SearchItems.class);
                startActivity(searchIntent);
                return true;
            case R.id.about:
                Intent j = new Intent(getApplicationContext(), UserPage.class);
                startActivity(j);
                finish();
                return true;
            case R.id.home:
                Intent k = new Intent(getApplicationContext(), ProductDisplay.class);
                startActivity(k);
                finish();
                return true;
            case R.id.cart:
                Intent i = new Intent(getApplicationContext(), CartPage.class);
                startActivity(i);
                finish();
                return true;
            case R.id.logout:
                mPrefs.edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.use_image:
                Intent l = new Intent(getApplicationContext(), imageToText.class);
                startActivity(l);
                finish();
                return true;
            case R.id.speechToOrder:
                Intent speechIntent = new Intent(getApplicationContext(), SpeechToOrder.class);
                startActivity(speechIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
