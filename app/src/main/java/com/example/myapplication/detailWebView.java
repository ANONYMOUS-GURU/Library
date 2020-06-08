package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class detailWebView extends AppCompatActivity {

    WebView webView;
    String username;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME="PrefsFile";
    DatabaseHelper dbHelper;

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_web_view);

        Intent i=getIntent();
        String link=i.getStringExtra("bookLink");

        mPrefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username=mystorage.getUsername(mPrefs);
        dbHelper=new DatabaseHelper(this);
        if(dbHelper.check_unique_username(username))
        {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(link);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }
}
