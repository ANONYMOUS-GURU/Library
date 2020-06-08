package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

class myPair
{
    int code;
    String name;

    myPair(String name,int code)
    {
        this.code=code;
        this.name=name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(int code){
        this.code=code;
    }
}

public class mystorage {

    public static HashMap<String,String> storage = new HashMap<>();
    public static ArrayList<myPair> book_name = new ArrayList<>();
    public static ArrayList<myPair> imageToTextOrder=new ArrayList<>();
    public static ArrayList<myPair> speechToTextOrder=new ArrayList<>();
    public static String getUsername(SharedPreferences mPrefs)
    {
        if(mPrefs.contains("pref_name")) {
             String x= mPrefs.getString("pref_name", "");
             return x;

        }else if(mPrefs.contains("pref_name_check")){
            String x = mPrefs.getString("pref_name_check", "");
            return x;
        }
        return "";
    }

    public static void displayNotification(Context myContext, String username, int NOTIFICATION_ID, String CHANNEL_ID, Class myClass,String text)
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name="Personal Notification";
            String description="Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,name,importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager=(NotificationManager)myContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_message);
        builder.setContentTitle("New Notification");
        builder.setContentText(text);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent i=new Intent(myContext,myClass);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(myContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(myContext);
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());
    }

    public static byte[] imageViewToByte(ImageView  imageView)
    {
        Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,stream);

        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap byteToImage(byte[] bitMapData)
    {
        return BitmapFactory.decodeByteArray(bitMapData, 0, bitMapData.length);
    }

    public static void populate_books(Cursor cursor)
    {
        while (cursor.moveToNext())
        {
            int bCode = cursor.getInt(1);
            String bName=cursor.getString(0);
            book_name.add(new myPair(bName, bCode));
        }
        cursor.close();
    }



    public static int calculate(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1),y.charAt(j - 1)),
                            dp[i - 1][j] + 1),
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static myPair findNearestFromAll(String x)
    {
        if(book_name.size()>0)
        {
            int min = calculate(x, book_name.get(0).getName());
            int bCode = book_name.get(0).getCode();
            String bName = book_name.get(0).getName();
            int val;
            for (int i = 1; i < book_name.size(); i++) {
                val=calculate(x, book_name.get(i).getName());
                if (val<min) {
                    bCode = book_name.get(i).getCode();
                    bName = book_name.get(i).getName();
                    min=val;
                }
            }

            return new myPair(bName, bCode);
        }
        else
        {
            return null;
        }
    }


    public static ArrayList<myPair> populateBooks(Cursor cursor)
    {
        ArrayList<myPair> bookName = new ArrayList<>();
        while (cursor.moveToNext())
        {
            int bCode = cursor.getInt(1);
            String bName=cursor.getString(0);
            bookName.add(new myPair(bName, bCode));
        }
        cursor.close();

        return bookName;
    }
}

