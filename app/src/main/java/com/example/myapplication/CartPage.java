package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartPage extends AppCompatActivity {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;
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
        setContentView(R.layout.activity_cart_page);
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);

        dbHelper = new DatabaseHelper(this);

        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        mDatabase = dbHelper.getWritableDatabase();
        myOnClickListener = new MyOnClickListenerCart(this);
        recyclerView = findViewById(R.id.my_recycler_view_cart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CustomAdapterCart(this, getAllItems(), username);
        recyclerView.setAdapter(adapter);

        bt1 = findViewById(R.id.cart_buy);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0)
                    bt1.setEnabled(false);
                else
                    bt1.setEnabled(true);
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = confirm_buy_cart();
                buy_cart(status);
                if (status == 1) {
                    dbHelper.removeFromCartAll(username);
                    String text = "You Bought Entire Cart";
                    mystorage.displayNotification(getApplicationContext(), username, 001, "personal_notifications", BuyView.class, text);
                }
            }
        });
    }

    public int confirm_buy_cart() {
        Cursor cursor = mDatabase.rawQuery("Select cart from " + dbHelper.TABLE3 + " where username=?", new String[]{username});
        int k = 1;
        if (cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    int bCode = cursor.getInt(0);
                    Cursor getStoreCursor = mDatabase.rawQuery("Select book_quantity from " + dbHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(bCode)});
                    getStoreCursor.moveToFirst();
                    Cursor getQuantityCursor = mDatabase.rawQuery("Select quantity from " + dbHelper.TABLE3 + " where cart=? and username=?", new String[]{String.valueOf(bCode), username});
                    getQuantityCursor.moveToFirst();
                    if (getStoreCursor.getInt(0) - getQuantityCursor.getInt(0) < 0) {
                        k = 0;
                        break;
                    }
                }
            } finally {
                cursor.close();
            }

            return k;
        } else
            return -1;
    }

    public void buy_cart(int status) {
        if (status == 0)
            Toast.makeText(getApplicationContext(), "Remove Items from the cart which you cannot buy", Toast.LENGTH_SHORT).show();
        else if (status == -1)
            Toast.makeText(getApplicationContext(), "Cant Buy as nothing in the cart", Toast.LENGTH_SHORT).show();
        else {
            Cursor cursor = mDatabase.rawQuery("Select cart,quantity from " + dbHelper.TABLE3 + " where username=?", new String[]{username});

            try {
                while (cursor.moveToNext()) {
                    int bCode = cursor.getInt(0);
                    int quantity = cursor.getInt(1);
                    dbHelper.buy(username, bCode, quantity);

                }
            } finally {
                cursor.close();
                adapter = new CustomAdapterCart(this, getAllItems(), username);
                recyclerView.setAdapter(adapter);
            }
            Toast.makeText(getApplicationContext(), "Bought All Items From the cart", Toast.LENGTH_SHORT).show();
        }
    }


    private class MyOnClickListenerCart implements View.OnClickListener {

        private final Context context;

        private MyOnClickListenerCart(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            goToItem(v);
        }

        private void goToItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code_cart);
            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
            i.putExtra("book_code", String.valueOf(selectedItemId));
            startActivity(i);
        }
    }

    protected Cursor getAllItems() {
        Cursor retCursor = mDatabase.rawQuery("Select * from " + DatabaseHelper.TABLE3 + " where username=?", new String[]{username});
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
