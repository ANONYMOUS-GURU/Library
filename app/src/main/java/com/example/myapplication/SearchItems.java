package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchItems extends AppCompatActivity {

    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;
    private SQLiteDatabase mDatabase;
    SearchView searchView;
    DatabaseHelper dbHelper;
    private String username;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    CustomAdapterSearch adapter;
    TextView txt;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_items);

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);
        dbHelper = new DatabaseHelper(this);
        txt = findViewById(R.id.search_match);

        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        mDatabase = dbHelper.getWritableDatabase();
        myOnClickListener = new SearchItems.MyOnClickListener(this);

        recyclerView = findViewById(R.id.my_recycler_view_search);
        searchView = findViewById(R.id.searchBar);
        recyclerView.setHasFixedSize(true);


        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new CustomAdapterSearch(this, null, username);
        recyclerView.setAdapter(adapter);

        if (adapter.getItemCount() == 0)
            txt.setVisibility(View.VISIBLE);
        else
            txt.setVisibility(View.GONE);


        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0)
                    txt.setVisibility(View.VISIBLE);
                else
                    txt.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                if(adapter.getItemCount()==0)
//                    txt.setVisibility(View.VISIBLE);
//                else
//                    txt.setVisibility(View.GONE);
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                if(adapter.getItemCount()==0)
//                    txt.setVisibility(View.VISIBLE);
//                else
//                    txt.setVisibility(View.GONE);
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private class MyOnClickListener implements View.OnClickListener {

        private MyOnClickListener(Context context) {
        }

        @Override
        public void onClick(View v) {
            goToItem(v);
        }

        private void goToItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            assert viewHolder != null;
            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code);
            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
            i.putExtra("book_code", String.valueOf(selectedItemId));
            startActivity(i);
        }
    }

    protected Cursor getAllItems() {
        return mDatabase.query(DatabaseHelper.TABLE2, null, null, null, null, null, null);
    }


}
