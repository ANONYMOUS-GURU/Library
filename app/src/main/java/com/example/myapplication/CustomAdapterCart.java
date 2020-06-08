package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapterCart extends RecyclerView.Adapter<CustomAdapterCart.MyViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private DatabaseHelper dbHelper;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    String username;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textQuantity, textViewVersion, hidden_code, priceFromCartPage;
        ImageView imageViewIcon;
        Button bt1, bt2;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.textViewNameCart);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersionCart);
            this.imageViewIcon = itemView.findViewById(R.id.imageViewCart);
            this.hidden_code = itemView.findViewById(R.id.hidden_book_code_cart);
            this.bt1 = itemView.findViewById(R.id.buy_from_cart);
            this.bt2 = itemView.findViewById(R.id.remove_from_cart);
            this.textQuantity = itemView.findViewById(R.id.quantityCartPage);
            this.priceFromCartPage = itemView.findViewById(R.id.PriceCart);
            this.cardView = itemView.findViewById(R.id.card_view_cart);
        }
    }

    public CustomAdapterCart(Context context, Cursor cursor, String username) {
        mContext = context;
        mCursor = cursor;
        dbHelper = new DatabaseHelper(mContext);
        this.username = username;

    }


    @NonNull
    @Override
    public CustomAdapterCart.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_layout, parent, false);

        view.setOnClickListener(CartPage.myOnClickListener);

        CustomAdapterCart.MyViewHolder myViewHolder = new CustomAdapterCart.MyViewHolder(view);
        return myViewHolder;
    }

    @SuppressLint("Assert")
    @Override
    public void onBindViewHolder(@NonNull CustomAdapterCart.MyViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        final String book_code = mCursor.getString(mCursor.getColumnIndex("cart"));
        final int quantity = mCursor.getInt(mCursor.getColumnIndex("quantity"));

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor bookCursor = db.rawQuery("Select * from " + dbHelper.TABLE2 + " where book_code=?", new String[]{book_code});
        bookCursor.moveToFirst();

        String book_name = bookCursor.getString(bookCursor.getColumnIndex("bookName")).trim();
        String book_author = bookCursor.getString(bookCursor.getColumnIndex("book_author")).trim();
        String book_desc = bookCursor.getString(bookCursor.getColumnIndex("book_total_desc"));
        double book_price = bookCursor.getDouble(bookCursor.getColumnIndex("book_price"));
        int store_quantity = bookCursor.getInt(bookCursor.getColumnIndex("book_quantity"));
        byte[] stream = bookCursor.getBlob(bookCursor.getColumnIndex("book_img"));
        Bitmap book_img = mystorage.byteToImage(stream);


        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        TextView hidden = holder.hidden_code;
        ImageView imageView = holder.imageViewIcon;

        textViewName.setText(book_name);
        textViewVersion.setText(book_author);
        hidden.setText(book_code);

        if (store_quantity - quantity < 0) {
            holder.cardView.setCardElevation(0);
            // Do necessary changes.
        }
        holder.textQuantity.setText(String.valueOf(quantity));
        holder.priceFromCartPage.setText(String.valueOf(book_price));


        imageView.setImageBitmap(Bitmap.createScaledBitmap(book_img, 300, 300, false));

        holder.textQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("Enter Quantity");
                alertDialog.setMessage("\nQuantity");

                final EditText input = new EditText(mContext);
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
                                    Log.v("Cont", "new quant in if -- " + newQuantity);
                                    boolean a = dbHelper.alter_quantity_cart(Integer.parseInt(book_code), username, newQuantity);
                                    assert a;
                                    swapCursor(dbHelper.getWritableDatabase().rawQuery("Select * from " + DatabaseHelper.TABLE3 + " where username=?", new String[]{username}));
                                } else {
                                    Toast.makeText(mContext,
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


        if (store_quantity - Integer.parseInt(holder.textQuantity.getText().toString()) < 0) {
            holder.bt1.setEnabled(false);
        }

        holder.bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean state = dbHelper.buy(username, Integer.parseInt(book_code), quantity);
                Cursor retCursor = dbHelper.getWritableDatabase().rawQuery("Select * from " + DatabaseHelper.TABLE3 + " where username=?", new String[]{username});
                ;
                swapCursor(retCursor);
                if (state) {
                    Cursor bookCursor = dbHelper.getReadableDatabase().rawQuery("Select bookName from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{book_code});
                    bookCursor.moveToFirst();
                    String text = "You Bought book " + bookCursor.getString(0);
                    Toast.makeText(mContext, "Bought item", Toast.LENGTH_SHORT).show();
                    mystorage.displayNotification(mContext, username, 001, "personal_notifications", BuyView.class, text);
                    bookCursor.close();
                } else
                    Toast.makeText(mContext, "Cannot buy item", Toast.LENGTH_SHORT).show();
            }
        });

        holder.bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = dbHelper.removeFromCartSingle(username, Integer.parseInt(book_code));
                Cursor retCursor = dbHelper.getWritableDatabase().rawQuery("Select * from " + DatabaseHelper.TABLE3 + " where username=?", new String[]{username});
                swapCursor(retCursor);
                if (state)
                    Toast.makeText(mContext, "Removed item", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }

    }
}
