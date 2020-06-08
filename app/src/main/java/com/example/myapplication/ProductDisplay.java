package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ProductDisplay extends AppCompatActivity {

    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;
    private SQLiteDatabase mDatabase;
    DatabaseHelper dbHelper;
    private String username;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME="PrefsFile";
    CustomAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_display);
        mPrefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username=mystorage.getUsername(mPrefs);
        dbHelper = new DatabaseHelper(this);

        if(dbHelper.check_unique_username(username))
        {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        mDatabase = dbHelper.getWritableDatabase();
        myOnClickListener = new MyOnClickListener(this);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new CustomAdapter(this, getAllItems(),username);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_icon:
                Intent searchIntent = new Intent(getApplicationContext(),SearchItems.class);
                startActivity(searchIntent);
                return true;
            case R.id.about:
                Intent j = new Intent(getApplicationContext(),UserPage.class);
                startActivity(j);
                finish();
                return true;
            case R.id.home:
                Intent k = new Intent(getApplicationContext(),ProductDisplay.class);
                startActivity(k);
                finish();
                return true;
            case R.id.cart:
                Intent i = new Intent(getApplicationContext(),CartPage.class);
                startActivity(i);
                finish();
                return true;
            case R.id.logout:
                mPrefs.edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.use_image:
                Intent l = new Intent(getApplicationContext(),imageToText.class);
                startActivity(l);
                finish();
                return true;
            case R.id.speechToOrder:
                Intent speechIntent = new Intent(getApplicationContext(),SpeechToOrder.class);
                startActivity(speechIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyOnClickListener implements View.OnClickListener {

        private MyOnClickListener(Context context) {
        }

        @Override
        public void onClick(View v)
        {
            goToItem(v);
        }

        private void goToItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            assert viewHolder != null;
            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code);
            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
            Intent i = new Intent(getApplicationContext(),ProductDetail.class);
            i.putExtra("book_code",String.valueOf(selectedItemId));
            startActivity(i);
        }
    }


    protected Cursor getAllItems()
    {
        return mDatabase.query(DatabaseHelper.TABLE2,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onBackPressed() {
        if(mPrefs.contains("pref_check") && mPrefs.getBoolean("pref_check",false))
            super.onBackPressed();
        else
        {
            final CharSequence[] options = {"Yes", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit and Logout");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if(options[item].equals("Yes")) {
                        mPrefs.edit().clear().apply();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }


    }
}
