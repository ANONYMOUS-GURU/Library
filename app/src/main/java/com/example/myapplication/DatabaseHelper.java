package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "LibDb14.db";
    private static final int version = 1;
    static final String TABLE1 = "User_info";
    static final String TABLE2 = "Books";
    static final String TABLE3 = "Cart";
    static final String TABLE4 = "Buy";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + TABLE1 + "(username text primary key,password text,name text,email text,userImage BLOB)");
        db.execSQL("Create table " + TABLE2 + "(book_code INTEGER primary key Autoincrement,bookName text,book_author text," +
                "book_total_desc text,book_price REAL,book_img BLOB,book_quantity INTEGER,added_by text,linkBook text)");
        db.execSQL("Create table " + TABLE3 + "(username text,cart Integer,quantity INTEGER,FOREIGN KEY (username) REFERENCES User_info(username))");
        db.execSQL("Create table " + TABLE4 + "(username text,buy Integer,quantity INTEGER,FOREIGN KEY (username) REFERENCES User_info(username))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE1);
        db.execSQL("drop table if exists " + TABLE2);
        db.execSQL("drop table if exists " + TABLE3);
        db.execSQL("drop table if exists " + TABLE4);
    }

    public boolean insert_to_user(String username, String password, String name, String email, byte[] img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("userImage", img);

        long ins = db.insert(TABLE1, null, contentValues);
        if (ins == -1)
            return false;
        else
            return true;
    }

    public boolean check_unique_username(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + TABLE1 + " where username=?", new String[]{username});
        if (cursor.getCount() > 0)
            return false;
        else
            return true;
    }

    public boolean check_valid_signin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + TABLE1 + " where username=? and password=?", new String[]{username, password});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    @SuppressLint("Assert")
    public boolean add_to_cart(String username, int book_id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select book_quantity from " + TABLE2 + " where book_code=?", new String[]{String.valueOf(book_id)});
        cursor.moveToFirst();
        if (cursor.getInt(0) - quantity < 0)
            return false;

        long ins;
        ContentValues contentValues = new ContentValues();
        Cursor cursor2 = db.rawQuery("Select quantity from " + TABLE3 + " where cart=? and username=?", new String[]{String.valueOf(book_id), String.valueOf(username)});

        if (cursor2.getCount() == 1) {
            cursor2.moveToFirst();
            contentValues.put("quantity", cursor2.getInt(0) + quantity);
            String where = "cart=? and username=?";
            String whereArgs2[] = new String[]{String.valueOf(book_id), String.valueOf(username)};
            ins = db.update(TABLE3, contentValues, where, whereArgs2);
        } else {
            contentValues.put("username", username);
            contentValues.put("cart", book_id);
            contentValues.put("quantity", quantity);
            ins = db.insert(TABLE3, null, contentValues);
        }

        if (ins == -1)
            return false;
        else
            return true;
    }

    public boolean add_book_to_db(String username, String book_name, String book_author, String book_full_desc, byte[] book_pic, double book_price, int book_quantity, String linkBook) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("added_by", username);
        contentValues.put("bookName", book_name);
        contentValues.put("book_author", book_author);
        contentValues.put("book_total_desc", book_full_desc);
        contentValues.put("book_img", book_pic);
        contentValues.put("book_price", book_price);
        contentValues.put("book_quantity", book_quantity);
        contentValues.put("linkBook", linkBook);
        long ins = db.insert(TABLE2, null, contentValues);
        if (ins == -1)
            return false;
        else
            return true;
    }

    @SuppressLint("Assert")
    public boolean buy(String username, int book_id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select book_quantity from " + TABLE2 + " where book_code=?", new String[]{String.valueOf(book_id)});
        cursor.moveToFirst();
        if (cursor.getInt(0) - quantity < 0)
            return false;

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("book_quantity", cursor.getInt(0) - quantity);
        String where = "book_code=?";
        String whereArgs[] = new String[]{String.valueOf(book_id)};
        db.update(TABLE2, dataToInsert, where, whereArgs);

        cursor.close();

        long ins;
        ContentValues contentValues = new ContentValues();

        Cursor cursor2 = db.rawQuery("Select quantity from " + TABLE4 + " where buy=? and username=?", new String[]{String.valueOf(book_id), String.valueOf(username)});
        assert !(cursor2.getCount() == 1 || cursor2.getCount() == 0);

        if (cursor2.getCount() == 1) {
            cursor2.moveToFirst();
            contentValues.put("quantity", cursor2.getInt(0) + quantity);
            where = "buy=? and username=?";
            String whereArgs2[] = new String[]{String.valueOf(book_id), String.valueOf(username)};
            ins = db.update(TABLE4, contentValues, where, whereArgs2);
        } else {
            contentValues.put("username", username);
            contentValues.put("buy", book_id);
            contentValues.put("quantity", quantity);
            ins = db.insert(TABLE4, null, contentValues);
        }

        if (ins != -1 && removeFromCartSingle(username, book_id))
            return true;
        else
            return false;
    }

    public boolean removeFromCartSingle(String username, int book_code) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "username=? and cart=?";
        String[] whereArgs = new String[]{username, String.valueOf(book_code)};
        db.delete(TABLE3, where, whereArgs);

        return true;
    }

    public void removeFromCartAll(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "username=?";
        String[] whereArgs = new String[]{username};
        db.delete(TABLE3, where, whereArgs);
    }

    public boolean remove_from_buy(String username, int book_code) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select quantity from " + TABLE4 + " where username=? and buy=?", new String[]{String.valueOf(username), String.valueOf(book_code)});
        cursor.moveToFirst();
        int buyQuantity = cursor.getInt(0);

        cursor = db.rawQuery("Select book_quantity from " + TABLE2 + " where book_code=?", new String[]{String.valueOf(book_code)});
        cursor.moveToFirst();
        int storeQuantity = cursor.getInt(0);

        cursor.close();

        String where = "username=? and buy=?";
        String[] whereArgs = new String[]{username, String.valueOf(book_code)};
        db.delete(TABLE4, where, whereArgs);

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("book_quantity", buyQuantity + storeQuantity);
        where = "book_code=?";
        String whereArgs2[] = new String[]{String.valueOf(book_code)};
        db.update(TABLE2, dataToInsert, where, whereArgs2);

        return true;
    }

    public boolean alter_quantity_cart(int book_code, String username, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.v("Cont", "new quant in dbhelp = " + quantity);
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("quantity", quantity);
        String where = "cart=? and username=?";
        String whereArgs2[] = new String[]{String.valueOf(book_code), String.valueOf(username)};
        long ins = db.update(TABLE3, dataToInsert, where, whereArgs2);

        return ins != -1;
    }

}
