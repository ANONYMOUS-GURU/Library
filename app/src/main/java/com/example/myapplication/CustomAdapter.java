package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private Cursor mCursor;
    String username;
    DatabaseHelper dbHelper;
    SQLiteDatabase mDatabase;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewVersion, hidden_code, priceDisplayPage;
        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.textViewName);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersion);
            this.imageViewIcon = itemView.findViewById(R.id.imageView);
            this.hidden_code = itemView.findViewById(R.id.hidden_book_code);
            this.priceDisplayPage = itemView.findViewById(R.id.PriceDisplayView);
        }
    }

    public CustomAdapter(Context context, Cursor cursor, String username) {
        mContext = context;
        mCursor = cursor;
        this.username = username;
        dbHelper = new DatabaseHelper(mContext);
        mDatabase = dbHelper.getReadableDatabase();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        view.setOnClickListener(ProductDisplay.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        if (!mCursor.moveToPosition(listPosition))
            return;

        String book_code = mCursor.getString(mCursor.getColumnIndex("book_code"));
        String book_name = mCursor.getString(mCursor.getColumnIndex("bookName")).trim();
        String book_author = mCursor.getString(mCursor.getColumnIndex("book_author")).trim();
        double book_price = mCursor.getDouble(mCursor.getColumnIndex("book_price"));
        byte[] stream = mCursor.getBlob(mCursor.getColumnIndex("book_img"));

        Bitmap book_img = mystorage.byteToImage(stream);

        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        TextView hidden = holder.hidden_code;
        ImageView imageView = holder.imageViewIcon;

        holder.priceDisplayPage.setText(String.valueOf(book_price));
        textViewName.setText(book_name);
        textViewVersion.setText(book_author);
        hidden.setText(book_code);

        imageView.setImageBitmap(Bitmap.createScaledBitmap(book_img, 300, 300, false));
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                Cursor filterCursor;
                if (constraint.toString().isEmpty())
                    filterCursor = mCursor;
                else {
                    filterCursor = mDatabase.rawQuery("Select * From " + DatabaseHelper.TABLE2 + " where bookName like ?", new String[]{"%" + constraint.toString() + "%"});
                }

                FilterResults results = new FilterResults();
                results.values = filterCursor;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                swapCursor((Cursor) results.values);
            }
        };

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

