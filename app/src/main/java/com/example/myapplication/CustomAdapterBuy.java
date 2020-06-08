package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapterBuy extends RecyclerView.Adapter<CustomAdapterBuy.MyViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private DatabaseHelper dbHelper;
    private String username;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewVersion, hidden_code, quantityFromBuyPage, priceFromBuyPage;
        ImageView imageViewIcon;
        Button bt1;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.textViewNameBuy);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersionBuy);
            this.imageViewIcon = itemView.findViewById(R.id.imageViewBuy);
            this.hidden_code = itemView.findViewById(R.id.hidden_book_code_buy);
            this.bt1 = itemView.findViewById(R.id.return_from_buy);
            this.quantityFromBuyPage = itemView.findViewById(R.id.quantityBuyPageView);
            this.priceFromBuyPage = itemView.findViewById(R.id.PriceBuy);
        }
    }

    public CustomAdapterBuy(Context context, Cursor cursor, String username) {
        mContext = context;
        mCursor = cursor;
        dbHelper = new DatabaseHelper(mContext);
        this.username = username;
    }


    @NonNull
    @Override
    public CustomAdapterBuy.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.buy_layout, parent, false);

//        view.setOnClickListener(BuyView.myOnClickListener);

        CustomAdapterBuy.MyViewHolder myViewHolder = new CustomAdapterBuy.MyViewHolder(view);
        return myViewHolder;
    }

    @SuppressLint("Assert")
    @Override
    public void onBindViewHolder(@NonNull CustomAdapterBuy.MyViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        final String book_code = mCursor.getString(mCursor.getColumnIndex("buy"));
        final int quantity = mCursor.getInt(mCursor.getColumnIndex("quantity"));

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor bookCursor = db.rawQuery("Select * from " + dbHelper.TABLE2 + " where book_code=?", new String[]{book_code});
        bookCursor.moveToFirst();

        String book_name = bookCursor.getString(bookCursor.getColumnIndex("bookName")).trim();
        String book_author = bookCursor.getString(bookCursor.getColumnIndex("book_author")).trim();
        String book_desc = bookCursor.getString(bookCursor.getColumnIndex("book_total_desc"));
        double book_price = bookCursor.getDouble(bookCursor.getColumnIndex("book_price"));
        int book_quantity = bookCursor.getInt(bookCursor.getColumnIndex("book_quantity"));
        byte[] stream = bookCursor.getBlob(bookCursor.getColumnIndex("book_img"));

        Bitmap book_img = mystorage.byteToImage(stream);

        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        TextView hidden = holder.hidden_code;
        ImageView imageView = holder.imageViewIcon;
        TextView quantityInBuyPage = holder.quantityFromBuyPage;

        textViewName.setText(book_name);
        textViewVersion.setText(book_author);
        hidden.setText(book_code);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(book_img, 300, 300, false));
        quantityInBuyPage.setText(String.valueOf(quantity));
        holder.priceFromBuyPage.setText(String.valueOf(book_price));

        holder.bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = dbHelper.remove_from_buy(username, Integer.parseInt(book_code));
                Cursor retCursor = dbHelper.getWritableDatabase().rawQuery("Select * from " + DatabaseHelper.TABLE4 + " where username=?", new String[]{username});
                swapCursor(retCursor);
                Cursor bookCursor = dbHelper.getReadableDatabase().rawQuery("Select bookName from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{book_code});
                bookCursor.moveToFirst();
                String text = "You returned book " + bookCursor.getString(0);
                mystorage.displayNotification(mContext, username, 001, "personal_notifications", BuyView.class, text);

                if (state)
                    Toast.makeText(mContext, "Returned Item", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "Cannot return item", Toast.LENGTH_SHORT).show();
                bookCursor.close();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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
