package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BuyView extends AppCompatActivity {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    //    static View.OnClickListener myOnClickListener;
    private SQLiteDatabase mDatabase;
    DatabaseHelper dbHelper;
    private String username;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    Button bt1;

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_view);

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);

        dbHelper = new DatabaseHelper(this);
        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
//        myOnClickListener = new BuyView.MyOnClickListenerBuy(this);
        recyclerView = findViewById(R.id.my_recycler_view_buy);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CustomAdapterBuy(this, getAllItems(), username);
        recyclerView.setAdapter(adapter);

        bt1 = findViewById(R.id.return_from_buy);
    }


//    private class MyOnClickListenerBuy implements View.OnClickListener {
//
//        private final Context context;
//        private MyOnClickListenerBuy(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        public void onClick(View v)
//        {
//            goToItem(v);
//        }
//
//        private void goToItem(View v) {
//            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
//            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
//            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code_buy);
//            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
//            Intent i = new Intent(getApplicationContext(),ProductDetail.class);
//            Bundle b=new Bundle();
//            b.putStringArray("values", new String[]{username, String.valueOf(selectedItemId)});
//            i.putExtras(b);
//            startActivity(i);
//        }
//    }


    protected Cursor getAllItems() {
        Cursor retCursor = mDatabase.rawQuery("Select * from " + DatabaseHelper.TABLE4 + " where username=?", new String[]{username});
        return retCursor;
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
