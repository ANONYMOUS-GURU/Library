package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

public class ProductDetail extends AppCompatActivity {

    Button bt1, bt2, bt3, bt4;
    TextView txt1, txt2, txt3, txt4;
    ImageView img;
    DatabaseHelper dbHelper;
    SQLiteDatabase mDatabase;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    String username;
    String book_code;
    CardView cardView;
    LinearLayout linearLayout;
    int k = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);
        username = mystorage.getUsername(mPrefs);
        mDatabase = dbHelper.getReadableDatabase();

        if (dbHelper.check_unique_username(username)) {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        Uri uri = getIntent().getData();

        if (uri != null) {
            List<String> params = uri.getPathSegments();
            if (params.size() == 0) {
                Intent intent = new Intent(getApplicationContext(), ProductDisplay.class);
                startActivity(intent);
                k = 1;
                finish();
            } else if (params.size() == 1) {
                book_code = params.get(0);
                Cursor mkCursor = mDatabase.rawQuery("Select * from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(book_code)});
                if (mkCursor.getCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid Book", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ProductDisplay.class);
                    startActivity(intent);
                    k = 1;
                    mkCursor.close();
                    finish();
                }
                mkCursor.close();
            } else {
                Toast.makeText(getApplicationContext(), "Invalid Link", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ProductDisplay.class);
                startActivity(intent);
                k = 1;
                finish();
            }
        } else {
            Intent i = getIntent();
            book_code = i.getStringExtra("book_code");
        }

        txt1 = findViewById(R.id.textView3);
        txt2 = findViewById(R.id.textView4);
        txt3 = findViewById(R.id.price);
        txt4 = findViewById(R.id.textView_author);
        img = findViewById(R.id.imageView2);
        bt1 = findViewById(R.id.buy);
        bt2 = findViewById(R.id.add_cart);
        bt3 = findViewById(R.id.gotocart);
        bt4 = findViewById(R.id.webview_bt);
        cardView = findViewById(R.id.card_view_detail);
        linearLayout = findViewById(R.id.product_detail_linear_layout);

        Cursor mCursor = mDatabase.rawQuery("Select * from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(book_code)});
        mCursor.moveToFirst();

        String book_name = mCursor.getString(mCursor.getColumnIndex("bookName")).trim();
        final String linkOfBook = mCursor.getString(mCursor.getColumnIndex("linkBook")).trim();
        String book_author = mCursor.getString(mCursor.getColumnIndex("book_author")).trim();
        String book_desc = mCursor.getString(mCursor.getColumnIndex("book_total_desc")).trim();
        String book_price = mCursor.getString(mCursor.getColumnIndex("book_price")).trim();
        byte[] stream = mCursor.getBlob(mCursor.getColumnIndex("book_img"));
        Bitmap book_img = mystorage.byteToImage(stream);
        mCursor.close();
        setTitle(book_name);

        txt1.setText(book_name);
        txt4.setText(book_author);
        txt2.setText(book_desc);
        txt3.setText(book_price);

        img.setImageBitmap(Bitmap.createScaledBitmap(book_img, 300, 300, false));


        Cursor cursor = mDatabase.rawQuery("Select book_quantity from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(book_code)});

        cursor.moveToFirst();
        int init_quantity = cursor.getInt(0);
        if (init_quantity == 0) {
            bt1.setEnabled(false);
            bt2.setEnabled(false);
        }

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getApplicationContext();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Book Link", "https://www.myapplicationlibrary.com/" + book_code);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), " Link Copied to Clipboard", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean[] state = new boolean[1];
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductDetail.this);
                alertDialog.setTitle("Enter Quantity");
                alertDialog.setMessage("Quantity");

                final EditText input = new EditText(ProductDetail.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String q = input.getText().toString();
                                if (isNumeric(q.trim())) {
                                    int newQuantity = Integer.parseInt(q.trim());
                                    state[0] = dbHelper.buy(username, Integer.parseInt(book_code), newQuantity);
                                    if (state[0]) {
                                        Cursor bookCursor = dbHelper.getReadableDatabase().rawQuery("Select bookName from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{book_code});
                                        bookCursor.moveToFirst();
                                        String text = "You Bought book " + bookCursor.getString(0);
                                        Toast.makeText(getApplicationContext(), "Bought item", Toast.LENGTH_SHORT).show();
                                        mystorage.displayNotification(getApplicationContext(), username, 001, "personal_notifications", BuyView.class, text);
                                        bookCursor.close();
                                    } else
                                        Toast.makeText(getApplicationContext(), "Cannot Buy", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
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

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductDetail.this);
                alertDialog.setTitle("Enter Quantity");
                alertDialog.setMessage("\nQuantity");

                final EditText input = new EditText(ProductDetail.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                final boolean[] state = new boolean[1];
                alertDialog.setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String q = input.getText().toString();
                                if (isNumeric(q.trim())) {
                                    int newQuantity = Integer.parseInt(q.trim());
                                    state[0] = dbHelper.add_to_cart(username, Integer.parseInt(book_code), newQuantity);
                                    if (state[0])
                                        Toast.makeText(getApplicationContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getApplicationContext(), "Cannot add to cart", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
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

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CartPage.class);
                startActivity(i);
            }
        });

        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), detailWebView.class);
                i.putExtra("bookLink", linkOfBook);
                startActivity(i);
            }
        });
        cursor.close();

        linearLayout = findViewById(R.id.product_detail_linear_layout);

//                linearLayout.setOnTouchListener((new OnSwipeTouchListener(ProductDetail.this){
//                    @Override
//                    public  void onSwipeLeft()
//                    {
//                        super.onSwipeLeft();
//                        int last_book_code=mDatabase.rawQuery("Select book_code from "+DatabaseHelper.TABLE2,null).getCount();
//                        if(Integer.parseInt(book_code)==last_book_code)
//                        {
//                            Toast.makeText(getApplicationContext(),"First Book last",Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
//                            i.putExtra("book_code", "1");
//                            startActivity(i);
//                            finish();
//                        }
//                        else
//                        {
//                            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
//                            i.putExtra("book_code", String.valueOf(Integer.parseInt(book_code)+1));
//                            startActivity(i);
//                            finish();
//                        }
//                    }
//                }));
//
//                linearLayout.setOnTouchListener(new OnSwipeTouchListener(ProductDetail.this) {
//                    @Override
//                    public void onSwipeRight() {
//                        super.onSwipeRight();
//                        int last_book_code=mDatabase.rawQuery("Select book_code from "+DatabaseHelper.TABLE2,null).getCount();
//                        if(Integer.parseInt(book_code)==1)
//                        {
//                            Toast.makeText(getApplicationContext(),"First Book last",Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
//                            i.putExtra("book_code", String.valueOf(last_book_code));
//                            startActivity(i);
//                            finish();
//                        }
//                        else
//                        {
//                            Intent i = new Intent(getApplicationContext(), ProductDetail.class);
//                            i.putExtra("book_code", String.valueOf(Integer.parseInt(book_code)-1));
//                            startActivity(i);
//                            finish();
//                        }
//                    }
//                });
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

    public boolean isNumeric(String s) {
        if (s.length() == 0)
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= '0' && s.charAt(i) <= '9'))
                return false;
        }
        return Integer.parseInt(s) > 0;
    }
}
