package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.InputType;
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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterSpeechToText extends RecyclerView.Adapter<CustomAdapterSpeechToText.MyViewHolder> {
    private Context mContext;
    String username;
    DatabaseHelper dbHelper;
    SQLiteDatabase mDatabase;
    ArrayList<myPair> listData;

    public CustomAdapterSpeechToText(Context context, String username, ArrayList<myPair> data) {
        mContext = context;
        this.username = username;
        dbHelper = new DatabaseHelper(mContext);
        mDatabase = dbHelper.getReadableDatabase();
        this.listData = data;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.imagee_speech_order_view, parent, false);
        view.setOnClickListener(imageToText.myOnClickListener);
        CustomAdapterSpeechToText.MyViewHolder myViewHolder = new CustomAdapterSpeechToText.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        myPair mp = listData.get(position);

        final int bCode = Integer.parseInt(mp.getName());
        Cursor mCursor = mDatabase.rawQuery("Select * from " + DatabaseHelper.TABLE2 + " where book_code=?", new String[]{String.valueOf(bCode)});
        mCursor.moveToFirst();
        String book_code = mCursor.getString(mCursor.getColumnIndex("book_code"));
        final String book_name = mCursor.getString(mCursor.getColumnIndex("bookName")).trim();
        String book_author = mCursor.getString(mCursor.getColumnIndex("book_author")).trim();
        double book_price = mCursor.getDouble(mCursor.getColumnIndex("book_price"));
        byte[] stream = mCursor.getBlob(mCursor.getColumnIndex("book_img"));
        Bitmap book_img = mystorage.byteToImage(stream);

        holder.price.setText(String.valueOf(book_price));
        holder.textViewName.setText(book_name);
        holder.textViewVersion.setText(book_author);
        holder.hidden_code.setText(book_code);
        holder.textQuantity.setText(String.valueOf(mp.getCode()));
        holder.imageViewIcon.setImageBitmap(Bitmap.createScaledBitmap(book_img, 300, 300, false));

        holder.bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remove_from_imgToText(position))
                    notifyDataSetChanged();
                else
                    Toast.makeText(mContext, "Cannot remove item", Toast.LENGTH_SHORT).show();
            }
        });

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

                                    if (change_quantity(position, newQuantity)) {
                                        notifyDataSetChanged();
                                    }
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

    }

    @Override
    public int getItemCount() {
        if (listData == null)
            return 0;
        return listData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewVersion, hidden_code, price, textQuantity;
        ImageView imageViewIcon;
        Button bt1;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.textViewName_ImageToText);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersion_ImageToText);
            this.imageViewIcon = itemView.findViewById(R.id.imageView_ImageToText);
            this.hidden_code = itemView.findViewById(R.id.hidden_book_code_ImageToText);
            this.price = itemView.findViewById(R.id.Price_ImageToText);
            this.bt1 = itemView.findViewById(R.id.remove_from_ImageToText);
            this.textQuantity = itemView.findViewById(R.id.quantity_ImageToText);
        }
    }


    public boolean remove_from_imgToText(int position) {
        listData.remove(position);
        mystorage.speechToTextOrder = listData;
        return true;
    }

    public boolean change_quantity(int position, int newQuantity) {

        listData.get(position).setCode(newQuantity);
        mystorage.speechToTextOrder = listData;
        return true;
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
