package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToOrder extends AppCompatActivity {

    TextView txt;
    ImageButton imageButton;
    static final int REQUEST_CODE_SPEECH = 0;
    DatabaseHelper dbHelper;
    SQLiteDatabase mDatabase;
    String username;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    CardView cardView;
    TextView txt1, txt2, txt3, txt4, txt5;
    Button bt1, bt2, bt3;
    ImageView img;
    ArrayList<myPair> listData = new ArrayList<>();
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_order);

        txt = findViewById(R.id.order_speech);
        imageButton = findViewById(R.id.micButton);

        dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getReadableDatabase();


        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);
        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        cardView = findViewById(R.id.card_view_SpeechToOrder);
        txt1 = findViewById(R.id.textViewName_SpeechToOrder);
        txt2 = findViewById(R.id.textViewVersion_SpeechToOrder);
        txt3 = findViewById(R.id.Price_SpeechToOrder);
        txt4 = findViewById(R.id.quantity_SpeechToOrder);
        txt5 = findViewById(R.id.hidden_book_code_SpeechToOrder);
        img = findViewById(R.id.imageView_SpeechToOrder);
        bt1 = findViewById(R.id.remove_from_SpeechToOrder);
        bt2 = findViewById(R.id.add_SpeechToOrder);
        bt3 = findViewById(R.id.add_to_cart_SpeechToOrder);


        myOnClickListener = new SpeechToOrder.MyOnClickListener(SpeechToOrder.this);
        recyclerView = findViewById(R.id.my_recycler_view_SpeechToOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(SpeechToOrder.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CustomAdapterSpeechToText(SpeechToOrder.this, username, listData);
        recyclerView.setAdapter(adapter);


        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ProductDetail.class);
                i.putExtra("book_code", txt5.getText().toString());
                startActivity(i);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextFromSpeech();
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.GONE);
                txt.setText("Press Mic To Record Order");
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listData.add(new myPair(txt5.getText().toString(), Integer.parseInt(txt4.getText().toString())));
                mystorage.speechToTextOrder = listData;
                adapter = new CustomAdapterSpeechToText(SpeechToOrder.this, username, listData);
                recyclerView.setAdapter(adapter);
                cardView.setVisibility(View.GONE);
                txt.setText("Press Mic To Record Order");
            }
        });

//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                if(adapter.getItemCount()==0)
//                    bt3.setEnabled(false);
//                else
//                    bt3.setEnabled(true);
//            }
//        });


        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listData = mystorage.speechToTextOrder;
                if (listData.size() == 0)
                    Toast.makeText(SpeechToOrder.this, "No Item to add in the cart", Toast.LENGTH_SHORT).show();
                else {
                    for (int i = 0; i < listData.size(); i++) {
                        dbHelper.add_to_cart(username, Integer.parseInt(listData.get(i).getName()), listData.get(i).getCode());
                    }
                    Intent i = new Intent(getApplicationContext(), CartPage.class);
                    startActivity(i);
                    mystorage.speechToTextOrder = new ArrayList<>();
                    finish();
                }
            }
        });

        txt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SpeechToOrder.this);
                alertDialog.setTitle("Enter Quantity");
                alertDialog.setMessage("\nQuantity");

                final EditText input = new EditText(SpeechToOrder.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String q = input.getText().toString();
                                if (isNumeric(q.trim())) {
                                    int newQuantity = Integer.parseInt(q.trim());
                                    txt4.setText(String.valueOf(newQuantity));
                                } else {
                                    Toast.makeText(SpeechToOrder.this,
                                            "Enter Valid Number", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void getTextFromSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private myPair getNearestBook(String bName) {
        Cursor cursor = mDatabase.rawQuery("Select bookName,book_code from " + DatabaseHelper.TABLE2, null);
        mystorage.populate_books(cursor);
        return mystorage.findNearestFromAll(bName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SPEECH) {
            assert data != null;
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            assert result != null;
            txt.setText(result.get(0));
            myPair mp = getNearestBook(result.get(0));
            int bCode = mp.getCode();

            Cursor bookCursor = mDatabase.rawQuery("Select * from " + dbHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(bCode)});
            bookCursor.moveToFirst();

            String book_name = bookCursor.getString(bookCursor.getColumnIndex("bookName"));
            String book_author = bookCursor.getString(bookCursor.getColumnIndex("book_author"));
            double book_price = bookCursor.getDouble(bookCursor.getColumnIndex("book_price"));
            byte[] stream = bookCursor.getBlob(bookCursor.getColumnIndex("book_img"));
            Bitmap book_img = mystorage.byteToImage(stream);
            bookCursor.close();

            cardView.setVisibility(View.VISIBLE);
            txt1.setText(book_name);
            txt2.setText(book_author);
            txt3.setText(String.valueOf(book_price));
            txt4.setText("1");
            txt5.setText(String.valueOf(bCode));
            img.setImageBitmap(book_img);

        }
    }

    public boolean isNumeric(String s) {
        if (s.length() == 0)
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= '0' && s.charAt(i) <= '9'))
                return false;
        }
        return Integer.parseInt(s) > 0;
    }

    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            goToItem(v);
        }

        private void goToItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code_SpeechToOrder);
            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
            i.putExtra("book_code", String.valueOf(selectedItemId));
            startActivity(i);
        }
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
