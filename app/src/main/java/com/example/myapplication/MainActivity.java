package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText username,password;
    Button login,signIn,admin;
    private CheckBox remBox;
    DatabaseHelper db;

    private SharedPreferences mPrefs;
    private static final String PREFS_NAME="PrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        username=findViewById(R.id.username_txt);
        password=findViewById(R.id.password_txt);
        remBox=findViewById(R.id.remember_ch);
        login=findViewById(R.id.login_bt);
        signIn=findViewById(R.id.sign_bt);
        admin=findViewById(R.id.admin_button);
        db=new DatabaseHelper(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),LoginPage.class);
                startActivity(i);
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),AdminLogin.class);
                startActivity(i);
            }
        });

        String s1,s2;
        if(mPrefs.contains("pref_name") && mPrefs.contains("pref_pass") && mPrefs.contains("pref_check") && mPrefs.getBoolean("pref_check",false)) {
            s1 = mPrefs.getString("pref_name", "not found");
            s2=mPrefs.getString("pref_pass","not found");
            if(db.check_valid_signin(s1, s2)) {
                Intent i = new Intent(getApplicationContext(), ProductDisplay.class);
                startActivity(i);
                finish();
            }
        }
        else {
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s1 = username.getText().toString();
                    String s2 = password.getText().toString();

                    if (db.check_valid_signin(s1, s2)) {
                        Toast.makeText(getApplicationContext(), "Signed in", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), ProductDisplay.class);
                        if (remBox.isChecked()) {
                            mPrefs.edit().clear().apply();
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("pref_name", s1);
                            editor.putString("pref_pass", s2);
                            editor.putBoolean("pref_check", true);
                            editor.apply();
                            Toast.makeText(getApplicationContext(), "User remembered", Toast.LENGTH_SHORT).show();
                            startActivity(i);
                            finish();
                        } else {
                            mPrefs.edit().clear().apply();
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("pref_name_check", s1);
                            editor.apply();
                            startActivity(i);
                            username.setText("");
                            password.setText("");
                        }

                    } else
                        Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
