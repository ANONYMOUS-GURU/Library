package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class imageToText extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY_IMAGE = 0;
    static final int PERMISSION_CODE_GALLERY = 2;
    static final int CAMERA_REQUEST_CODE = 3;

    Bitmap imageBitmap;
    Button btn1, btn2,btn3;
    ImageView imageView;
    String username;
    Uri image_uri;
    ImageButton imageButton;

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;
    ArrayList<myPair> bookNameAndCode = new ArrayList<>();
    DatabaseHelper dbHelper;
    Canvas canvas;
    Paint paint;
    private SQLiteDatabase mDatabase;
    private ArrayList<myPair> bookAndQuantity=new ArrayList<>();
    Bitmap tempBitmap;
    int nextLineConstraint = 28;
    double costThreshold = 0.35;

    private SharedPreferences mPrefs;
    private static final String PREFS_NAME="PrefsFile";

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);

        dbHelper=new DatabaseHelper(this);
        mDatabase=dbHelper.getWritableDatabase();

        mPrefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username = mystorage.getUsername(mPrefs);
        if(dbHelper.check_unique_username(username))
        {
            mPrefs.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        myOnClickListener = new imageToText.MyOnClickListener(imageToText.this);
        recyclerView = findViewById(R.id.my_recycler_view_ImageToText);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(imageToText.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CustomAdapterImageToText(imageToText.this,username,bookAndQuantity);
        recyclerView.setAdapter(adapter);

        imageView = findViewById(R.id.img1);
        btn1 = findViewById(R.id.snap);
        btn2 = findViewById(R.id.getText);
        btn3=findViewById(R.id.getFinalName);
        imageButton=findViewById(R.id.rotate_image);

        btn2.setEnabled(false);
        btn3.setEnabled(false);
        imageButton.setEnabled(false);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate_image_bitmap(90);

            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(imageToText.this);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAndQuantity=new ArrayList<>();
                mystorage.imageToTextOrder=bookAndQuantity;
                bookNameAndCode=mystorage.populateBooks(mDatabase.rawQuery("Select bookName,book_code from "+DatabaseHelper.TABLE2,null));
                adapter = new CustomAdapterImageToText(imageToText.this,username,bookAndQuantity);
                recyclerView.setAdapter(adapter);
                if (checkReadableImage(imageBitmap))
                    detectTextFromImage();
                else
                    requestNewImage();
                tempBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(tempBitmap);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            }
        });


        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAndQuantity=mystorage.imageToTextOrder;
                if(bookAndQuantity.size()==0)
                    Toast.makeText(imageToText.this,"No Item to add in the cart",Toast.LENGTH_SHORT).show();
                else
                {
                    for(int i=0;i<bookAndQuantity.size();i++)
                    {
                        dbHelper.add_to_cart(username,Integer.parseInt(bookAndQuantity.get(i).getName()),bookAndQuantity.get(i).getCode());
                    }
                    Intent i = new Intent(getApplicationContext(),CartPage.class);
                    startActivity(i);
                    mystorage.imageToTextOrder=new ArrayList<>();
                    finish();
                }
            }
        });
  }

    private void requestNewImage() {
    }
    private boolean checkReadableImage(Bitmap imageBitmap) {
        return true;
    }

    public void rotate_image_bitmap(int angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(imageBitmap);
        tempBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(tempBitmap);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(imageBitmap, 0, 0, paint);
    }
    private void setNextLineConstraint(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        int separation = 0;
        int lineCount = 0;
        if (blockList.size() != 0) {
            for (int i = 0; i < blockList.size(); i++) {
                List<FirebaseVisionText.Line> lines = blockList.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    separation += -(lines.get(j).getBoundingBox().top - lines.get(j).getBoundingBox().bottom);
                    Log.v("nextLineConstraint", "sep = " + separation);
                    lineCount += 1;
                }
            }
            separation = (int) (separation / (1.0 * lineCount));
            nextLineConstraint = (int) (separation * 0.5);
            Log.v("nextLineConstraint", "value =  " + nextLineConstraint);
        }
    }
    private LineContents getSeparated(ArrayList<FirebaseVisionText.Element> elements) {

        int left, right, bottom, top;

        left = elements.get(0).getBoundingBox().left;
        right = elements.get(0).getBoundingBox().right;
        bottom = elements.get(0).getBoundingBox().bottom;
        top = elements.get(0).getBoundingBox().top;
        String line = removeSpecialChar(elements.get(0).getText()) + " ";

        for (int i = 1; i < elements.size(); i++) {
            line = line + removeSpecialChar(elements.get(i).getText()) + " ";
            if (elements.get(i).getBoundingBox().left < left)
                left = elements.get(i).getBoundingBox().left;

            if (elements.get(i).getBoundingBox().right > right)
                right = elements.get(i).getBoundingBox().right;

            if (elements.get(i).getBoundingBox().bottom > bottom)
                bottom = elements.get(i).getBoundingBox().bottom;

            if (elements.get(i).getBoundingBox().top < top)
                top = elements.get(i).getBoundingBox().top;
        }
        line = line.trim();
        String[] part = line.split("[^a-zA-Z0-9]+|(?<=[a-zA-Z])(?=[0-9])|(?<=[0-9])(?=[a-zA-Z])");

        ArrayList<String> text = new ArrayList<>(Arrays.asList(part));
        Rect rect = new Rect(left, top, right, bottom);

        LineContents lineContents = new LineContents(text, rect);

        return lineContents;

    }
    private void detectTextFromImage() {
        final boolean[] state = {true};
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                getTextFromImage1(firebaseVisionText);
                double angleRotation = getRotationRequired(firebaseVisionText);
                if (Math.abs(angleRotation) > 45)
                    state[0] = false;
                if (state[0]) {
                    rotate_image_bitmap((int) (angleRotation));
                    Log.v("Slope", "Rotated Image by angle = " + angleRotation);
                    imageView.setImageBitmap(imageBitmap);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        Task<FirebaseVisionText> t1 = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
        t1.addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
            @Override
            public void onComplete(@NonNull Task<FirebaseVisionText> task) {
                if (state[0]) {
                    FirebaseVisionImage firebaseVisionImage1 = FirebaseVisionImage.fromBitmap(imageBitmap);
                    FirebaseVisionTextRecognizer firebaseVisionTextRecognizer1 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                    firebaseVisionTextRecognizer1.processImage(firebaseVisionImage1).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            setNextLineConstraint(firebaseVisionText);
                            getTextFromImage1(firebaseVisionText);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), " Rotate Image for Better Text Capture ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private double getRotationRequired(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        double angle;
        if (blockList.size() != 0) {
            angle = Math.atan(getAverageSlopeOfText(blockList)) * 180 / 3.14;
        } else
            angle = 90;

        Log.v("Slope", "Angle found  = " + angle);
        return angle;
    }
    private double getAverageSlopeOfText(List<FirebaseVisionText.TextBlock> blockList) {
        ArrayList<intPair> points;
        double slope = 0;
        int lineCount = 0;
        for (int i = 0; i < blockList.size(); i++) {
            List<FirebaseVisionText.Line> lines = blockList.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                //getSlope of each line
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                points = new ArrayList<>();
                for (int k = 0; k < elements.size(); k++) {
                    points.add(new intPair(elements.get(k).getBoundingBox().centerX(), elements.get(k).getBoundingBox().centerY()));
                }
                if (points.size() > 1) {
                    slope += getSlope(points);
                    lineCount += 1;
                    Log.v("Slope", "Current slope = " + getSlope(points));
                }

            }
        }

        if (lineCount > 0) {
            slope = slope / lineCount;
            Log.v("Slope", "Final slope = " + slope);
        } else {
            slope = 10;
            Log.v("Slope", "Final Slope sent is large to raise Toast");
        }
        return slope * -1;

    }
    private double getSlope(ArrayList<intPair> points) {
        int x1, y1, x2, y2;
        double slope = 0;
        double slopeCurr;
        for (int i = 0; i < points.size() - 1; i++) {
            x1 = points.get(i).getX();
            y1 = points.get(i).getY();

            x2 = points.get(i + 1).getX();
            y2 = points.get(i + 1).getY();

            slopeCurr = (y2 - y1) / (1.0 * (x2 - x1));
            Log.v("Slope", "points found in the line = (" + x1 + "," + y1 + ")  (" + x2 + "," + y2 + ")  and slope found = " + slopeCurr);

            slope = slope + slopeCurr;
        }
        slope = slope / (points.size() - 1);

        return slope;
    }
    private void getTextFromImage1(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();

        ArrayList<FirebaseVisionText.Element> listOfAllElements = new ArrayList<>();

        ArrayList<myPair> bookAndQuantity = new ArrayList<>();

        if (blockList.size() != 0) {
            for (int i = 0; i < blockList.size(); i++) {
                List<FirebaseVisionText.Line> lines = blockList.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        listOfAllElements.add(elements.get(k));
                    }
                }
            }

            // Solution to part 1
            ArrayList<ArrayList<FirebaseVisionText.Element>> myLines = getAllLines(listOfAllElements);

            // Seeing results in the below for loop
            String finalText = "";
            String line = "";
            for (int i = 0; i < myLines.size(); i++) {
                for (int j = 0; j < myLines.get(i).size(); j++) {
                    line = line + myLines.get(i).get(j).getText() + " ";
                }
                finalText = finalText + line + "\n\n";
                Log.v("Cony ", "line " + i + " = " + line);
                line = "";
            }
            //textView.setText(finalText);
            // After results to part 1


            // Solution to part 2
            for (int i = 0; i < myLines.size(); i++) {
                int quantity = 0;
                String name = "";
                LineContents lineContents = getSeparated(myLines.get(i));
                // return 1 for withNum makes sense 2 for withoutNum makes sense 3 for both makes sense 4 for no num but makes sense and
                // 0 for does not makes sense
                Log.v("Cony", "here1");
                int matchCheck = checkIfMatchToAnyProduct(lineContents.getText());
                Log.v("Cony", "matchCheck status = " + matchCheck);

                if (matchCheck == 1 || matchCheck == 2 || matchCheck == 3) {
                    // in case_2b check if numeric element itself does not itself contain a special char.
                    // If it does take caution with parsing to Integer
                    Log.v("Cony", " matchCheck status shows num at end detected");

                    int case_2a, case_2b;
                    // 2a ( average separation )
                    int mean = 0;
                    if (myLines.get(i).size() > 2 && isNotAlpha(myLines.get(i).get(myLines.get(i).size()-1).getText())){
                        for (int j = 0; j < myLines.get(i).size() - 2; j++) {
                            mean += myLines.get(i).get(j).getBoundingBox().right - myLines.get(i).get(j + 1).getBoundingBox().left;
                        }
                        mean /= myLines.get(i).size();
                        if (myLines.get(i).get(myLines.get(i).size() - 2).getBoundingBox().right - myLines.get(i).get(myLines.get(i).size() - 1).getBoundingBox().left > mean * 1.3)
                            case_2a = 1;
                        else
                            case_2a = -1;
                    } else
                        case_2a = 0;

                    Log.v("Cony", "case_2a checked = " + case_2a);

                    Log.v("Cony", "Checking for Special Chars " + myLines.get(i).get(myLines.get(i).size() - 2).getText() + "   and in " + myLines.get(i).get(myLines.get(i).size() - 1).getText());
                    if (isSpecialCharExists(myLines.get(i).get(myLines.get(i).size() - 2).getText(), "end") || isSpecialCharExists(myLines.get(i).get(myLines.get(i).size() - 1).getText(), "in"))
                        case_2b = 1;
                    else
                        case_2b = 0;
                    Log.v("Cony", "case_2b checked = " + case_2b);

                    // getSeparated returns LineContents  and check on


                    String lineWithNum = "";
                    String lineWithoutNum = "";
                    for (int j = 0; j < lineContents.getText().size(); j++) {
                        lineWithNum = lineWithNum + " " + lineContents.getText().get(j);
                        if (j < lineContents.getText().size() - 1) {
                            lineWithoutNum = lineWithoutNum + " " + lineContents.getText().get(j);
                        }
                    }
                    lineWithNum = lineWithNum.trim();
                    lineWithoutNum = lineWithoutNum.trim();


                    if (matchCheck == 1) {
                        quantity = 1;
                        name = lineWithNum;
                    } else if (matchCheck == 2) {
                        quantity = Integer.parseInt(lineContents.getText().get(lineContents.getText().size() - 1).trim());
                        name = lineWithoutNum;
                    } else {
                        if (case_2a == 1 || case_2b == 1) {
                            quantity = Integer.parseInt(lineContents.getText().get(lineContents.getText().size() - 1).trim());
                            name = lineWithoutNum;
                        } else {
                            quantity = 1;
                            name = lineWithNum;
                        }
                    }
//                    doGraphicOverlay(myLines.get(i), true);
                    doGraphicOverlay(lineContents, true);
                } else if (matchCheck == 4) {
                    name = "";
                    for (int j = 0; j < myLines.get(i).size(); j++) {
                        name = name + " " + removeSpecialChar(myLines.get(i).get(j).getText());
                        name = name.trim();
                    }
                    quantity = 1;
                    doGraphicOverlay(lineContents, true);
                } else
                    doGraphicOverlay(lineContents, false);

                if (name.length() > 0 && quantity > 0) {
                    bookAndQuantity.add(new myPair(getNearestProduct(name), quantity));
                    Log.v("Cony", "Adding BNAME =  " + getNearestProduct(name) + "  quantity = " + quantity);
                }
            }

            String txt2 = "";
            for (int i = 0; i < bookAndQuantity.size(); i++) {
                txt2 = txt2 + "Book Name = " + bookAndQuantity.get(i).getName() + "\n Quantity = " + bookAndQuantity.get(i).getCode() + "\n\n";
            }
            //textView2.setText(txt2);

            if(bookAndQuantity.size()>0)
                btn3.setEnabled(true);

            mystorage.imageToTextOrder=bookAndQuantity;
            adapter = new CustomAdapterImageToText(imageToText.this,username,bookAndQuantity);
            recyclerView.setAdapter(adapter);
        }
    }

    private boolean isNotAlpha(String text) {
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) >= 'a' && text.charAt(i) <= 'z')
                return false;
        }
        return true;
    }
    private String getNearestProduct(String name) {
        double min = getCost(name, 0);
        int minCode = 0;
        for (int i = 1; i < bookNameAndCode.size(); i++) {
            if (getCost(name, i) < min) {
                min = getCost(name, i);
                minCode = i;
            }
        }
        return String.valueOf(bookNameAndCode.get(minCode).getCode());
    }
    private String removeSpecialChar(String text) {
        String retText = "";
        char ch;
        for (int i = 0; i < text.length(); i++) {
            ch = text.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) {
                retText = retText + ch;
            }
        }
        return retText;
    }
    private String onlyNumeric(String text) {
        String retText = "";
        char ch;
        for (int i = 0; i < text.length(); i++) {
            ch = text.charAt(i);
            if ((ch > '0' && ch < '9')) {
                retText = retText + ch;
            }
        }
        return retText;
    }
    private boolean endsWithNum(String text) {
        return (text.charAt(text.length() - 1) >= '0' && text.charAt(text.length() - 1) <= '9');
    }
    private void doGraphicOverlay(LineContents lineContents, boolean status) {
        Log.v("Cony", "DOING GRAPHIC OVERLAY");
        if (status)
            paint.setColor(Color.BLUE);
        else
            paint.setColor(Color.RED);

        paint.setAlpha(50);
        Log.v("Cony","Doing Graphic Overlay for =  "+getStringFromLine(lineContents));
        canvas.drawRect(lineContents.getRect(), paint);    //Replace with rect
        imageView.setImageBitmap(tempBitmap);
    }

    private String getStringFromLine(LineContents lineContents) {
        String outLine="";
        for(int i=0;i<lineContents.getText().size();i++)
        {
            outLine=outLine+lineContents.getText().get(i)+" ";
        }
        return outLine.trim();
    }

    private int checkIfMatchToAnyProduct(ArrayList<String> line) {
        double a, b, c;
        a = b = c = 0;
        Log.v("Cony", "checkIfMatchToAnyProduct");
        if (endsWithNum(line.get(line.size() - 1))) {
            Log.v("Cony", "num at end detected");
            String lineWithNum = "";
            String lineWithoutNum = "";
            for (int j = 0; j < line.size(); j++) {

                lineWithNum = lineWithNum + " " + line.get(j);
                lineWithNum = lineWithNum.trim();
                if (j < line.size() - 1) {

                    lineWithoutNum = lineWithoutNum + " " + line.get(j);
                    lineWithoutNum = lineWithoutNum.trim();
                }
            }
            a = checkSimilarity(lineWithNum);
            b = checkSimilarity(lineWithoutNum);
            if (a < costThreshold || b < costThreshold) {
                if (a < b)
                    return 1;
                else if (b < a)
                    return 2;
                else
                    return 3;
            } else
                return 0;

        } else {
            Log.v("Cony", "num at end not detected");
            String lineFull = "";
            for (int j = 0; j < line.size(); j++) {
                lineFull = lineFull + " " + removeSpecialChar(line.get(j));
                lineFull = lineFull.trim();
            }

            c = checkSimilarity(lineFull);
        }

        Log.v("Cony", "checked states a=" + a + "   b=" + b + "   c=" + c);

        if (c < 0.3)
            return 4;
        else
            return 0;
    }
    private double checkSimilarity(String name) {
        Log.v("Cony", "Checking Similarity for =  " + name);
        double min = getCost(name, 0);
        int minCode = 0;
        for (int i = 1; i < bookNameAndCode.size(); i++) {
            if (getCost(name, i) < min) {
                min = getCost(name, i);
                minCode = i;
            }
        }

        Log.v("Cony", "Got Cost = " + min);
        return min;
    }
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
    private double getCost(String x, int element) {
        x = x.toLowerCase().trim();
        String y = bookNameAndCode.get(element).getName().toLowerCase().trim();

        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1),
                            dp[i][j - 1] + 1);
                }
            }
        }
        return (dp[x.length()][y.length()] * 1.0) / y.length();
    }
    private boolean isSpecialCharExists(String text, String position) {
        char ch;
        if (position.equals("start")) {
            ch = text.charAt(0);
            return (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9');
        } else if (position.equals("end")) {
            ch = text.charAt(text.length() - 1);
            return (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9');
        } else {
            for (int i = 0; i < text.length(); i++) {
                ch = text.charAt(i);
                if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9'))
                    return true;
            }
        }
        return false;
    }
    private ArrayList<ArrayList<FirebaseVisionText.Element>> getAllLines(ArrayList<FirebaseVisionText.Element> elements) {
        Collections.sort(elements, new SortY());
        ArrayList<ArrayList<FirebaseVisionText.Element>> myLines = new ArrayList<>();
        ArrayList<FirebaseVisionText.Element> tempLine = new ArrayList<>();
        tempLine.add(elements.get(0));
        for (int i = 1; i < elements.size(); i++) {
            if (elements.get(i).getBoundingBox().centerY() - elements.get(i - 1).getBoundingBox().centerY() > nextLineConstraint) {
                Collections.sort(tempLine, new SortX());
                myLines.add(tempLine);
                tempLine = new ArrayList<>();
                tempLine.add(elements.get(i));
            } else {
                tempLine.add(elements.get(i));
            }
        }
        Collections.sort(tempLine, new SortX());
        myLines.add(tempLine);

        return myLines;
    }
    private void imageView2Bitmap(ImageView view) {
        imageBitmap = ((BitmapDrawable) view.getDrawable()).getBitmap();
    }
    private void dispatchTakePictureIntent() {
        bookAndQuantity=new ArrayList<>();
        adapter = new CustomAdapterImageToText(imageToText.this,username,bookAndQuantity);
        recyclerView.setAdapter(adapter);
        mystorage.imageToTextOrder=bookAndQuantity;
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the Camera");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(image_uri);
                imageView2Bitmap(imageView);
                imageButton.setEnabled(true);
                btn2.setEnabled(true);
            }
        } else if (requestCode == REQUEST_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                imageView.setImageURI(selectedImage);
                imageView2Bitmap(imageView);
                imageButton.setEnabled(true);
                btn2.setEnabled(true);
            }
        }
    }
    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission, CAMERA_REQUEST_CODE);
                            dispatchTakePictureIntent();
                        } else {
                            dispatchTakePictureIntent();
                        }
                    } else {
                        dispatchTakePictureIntent();
                    }

                } else if (options[item].equals("Choose from Gallery")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permissions, PERMISSION_CODE_GALLERY);
                        } else {
                            getImageFromGallery();
                        }
                    } else {
                        getImageFromGallery();
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void getImageFromGallery() {
        bookAndQuantity=new ArrayList<>();
        adapter = new CustomAdapterImageToText(imageToText.this,username,bookAndQuantity);
        recyclerView.setAdapter(adapter);
        mystorage.imageToTextOrder=bookAndQuantity;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission Denied!!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Camera Permission Denied!!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
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
                Intent searchIntent = new Intent(getApplicationContext(),SearchItems.class);
                startActivity(searchIntent);
                return true;
            case R.id.about:
                Intent j = new Intent(getApplicationContext(),UserPage.class);
                startActivity(j);
                finish();
                return true;
            case R.id.home:
                Intent k = new Intent(getApplicationContext(),ProductDisplay.class);
                startActivity(k);
                finish();
                return true;
            case R.id.cart:
                Intent i = new Intent(getApplicationContext(),CartPage.class);
                startActivity(i);
                finish();
                return true;
            case R.id.logout:
                mPrefs.edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.use_image:
                Intent l = new Intent(getApplicationContext(),imageToText.class);
                startActivity(l);
                finish();
                return true;
            case R.id.speechToOrder:
                Intent speechIntent = new Intent(getApplicationContext(),SpeechToOrder.class);
                startActivity(speechIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;
        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v)
        {
            goToItem(v);
        }

        private void goToItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            TextView textViewName = viewHolder.itemView.findViewById(R.id.hidden_book_code_ImageToText);
            int selectedItemId = Integer.parseInt(String.valueOf(textViewName.getText()));
            Intent i = new Intent(getApplicationContext(),ProductDetail.class);
            i.putExtra("book_code",String.valueOf(selectedItemId));
            startActivity(i);
        }
    }
}

class SortY implements Comparator<FirebaseVisionText.Element> {
    @Override
    public int compare(FirebaseVisionText.Element o1, FirebaseVisionText.Element o2) {
        return o1.getBoundingBox().centerY() - o2.getBoundingBox().centerY();
    }
}
class SortX implements Comparator<FirebaseVisionText.Element> {
    @Override
    public int compare(FirebaseVisionText.Element o1, FirebaseVisionText.Element o2) {
        return o1.getBoundingBox().centerX() - o2.getBoundingBox().centerX();
    }
}
class intPair {
    int x, y;

    intPair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
class LineContents {
    ArrayList<String> text;
    Rect rect;

    LineContents(ArrayList<String> text, Rect rect) {
        this.rect = rect;
        this.text = text;
    }

    public ArrayList<String> getText() {
        return text;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setText(ArrayList<String> text) {
        this.text = text;
    }
}

//    private void detectTextFromImage()
//    {
//        FirebaseVisionImage firebaseVisionImage1 = FirebaseVisionImage.fromBitmap(imageBitmap);
//        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer1 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        firebaseVisionTextRecognizer1.processImage(firebaseVisionImage1).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                getTextFromImage1(firebaseVisionText);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//    private myPair getNearestBook(String bName)
//    {
//        Cursor cursor=mDatabase.rawQuery("Select bookName,book_code from "+DatabaseHelper.TABLE2,null);
//        mystorage.populate_books(cursor);
//        return mystorage.findNearestFromAll(bName);
//    }
//
//    private void setViewAndAdapter(Bitmap image,ArrayList<myPair> bookAndQuantityValue)
//    {
//        bookAndQuantity = bookAndQuantityValue;
//        mystorage.imageToTextOrder = bookAndQuantity;
//        adapter = new CustomAdapterImageToText(imageToText.this, username, bookAndQuantity);
//        recyclerView.setAdapter(adapter);
//        imageView.setImageBitmap(image);
//    }
//    private void detectTextFromImage1() {
//        final ArrayList[] tempBookList1 = new ArrayList[]{new ArrayList<>()};
//        final ArrayList[] tempBookList2 = new ArrayList[]{new ArrayList<>()};
//        final ArrayList[] tempBookList3 = new ArrayList[]{new ArrayList<>()};
//        final ArrayList[] tempBookList4 = new ArrayList[]{new ArrayList<>()};
//
//        final Bitmap imageBitmap1=imageBitmap;
//        final Bitmap imageBitmap2=rotate_image_bitmap(imageBitmap1);
//        final Bitmap imageBitmap3=rotate_image_bitmap(imageBitmap2);
//        final Bitmap imageBitmap4=rotate_image_bitmap(imageBitmap3);
//
//        FirebaseVisionImage firebaseVisionImage1 = FirebaseVisionImage.fromBitmap(imageBitmap1);
//        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer1 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        firebaseVisionTextRecognizer1.processImage(firebaseVisionImage1).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText1) {
//                tempBookList1[0] = getTextFromImage(firebaseVisionText1);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT);
//            }
//        });
//
//        FirebaseVisionImage firebaseVisionImage2 = FirebaseVisionImage.fromBitmap(imageBitmap2);
//        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer2 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        firebaseVisionTextRecognizer2.processImage(firebaseVisionImage2).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText2) {
//                tempBookList2[0] = getTextFromImage(firebaseVisionText2);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT);
//            }
//        });
//
//        FirebaseVisionImage firebaseVisionImage3 = FirebaseVisionImage.fromBitmap(imageBitmap3);
//        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer3 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        firebaseVisionTextRecognizer3.processImage(firebaseVisionImage3).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText3) {
//                tempBookList3[0] = getTextFromImage(firebaseVisionText3);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT);
//            }
//        });
//
//        FirebaseVisionImage firebaseVisionImage4 = FirebaseVisionImage.fromBitmap(imageBitmap4);
//        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer4 = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        firebaseVisionTextRecognizer4.processImage(firebaseVisionImage4).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText4) {
//                tempBookList4[0] = getTextFromImage(firebaseVisionText4);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(imageToText.this, "Failed " + e.toString(), Toast.LENGTH_SHORT);
//            }
//        });
//
//        Task<FirebaseVisionText> t1 = firebaseVisionTextRecognizer1.processImage(firebaseVisionImage1);
//        Task<FirebaseVisionText> t2 = firebaseVisionTextRecognizer2.processImage(firebaseVisionImage2);
//        Task<FirebaseVisionText> t3 = firebaseVisionTextRecognizer3.processImage(firebaseVisionImage3);
//        Task<FirebaseVisionText> t4 = firebaseVisionTextRecognizer4.processImage(firebaseVisionImage4);
//
//        Tasks.whenAll(t1,t2,t3,t4).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                int max_text_detected=Math.max(Math.max(tempBookList1[0].size(), tempBookList2[0].size()), Math.max(tempBookList3[0].size(), tempBookList4[0].size()));
//                if (max_text_detected>0)
//                {
//                    btn3.setEnabled(true);
//                    if (max_text_detected== tempBookList1[0].size()) {
//                        setViewAndAdapter(imageBitmap1,tempBookList1[0]);
//                    } else if (max_text_detected == tempBookList2[0].size()) {
//                        setViewAndAdapter(imageBitmap2,tempBookList2[0]);
//                    } else if (max_text_detected == tempBookList3[0].size()) {
//                        setViewAndAdapter(imageBitmap3,tempBookList3[0]);
//                    } else {
//                        setViewAndAdapter(imageBitmap4,tempBookList4[0]);
//                    }
//                }
//                else
//                    Toast.makeText(getApplicationContext(),"No Text Found",Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//    public Bitmap rotate_image_bitmap(Bitmap imgBitmap)
//    {
//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        return Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);
//    }
//    private boolean isNumeric(String s)
//    {
//        for(int i=0;i<s.length();i++)
//        {
//            if(!(s.charAt(i)>='0' && s.charAt(i)<='9'))
//                return false;
//        }
//        return true;
//    }
//    private ArrayList<myPair> getTextFromImage(FirebaseVisionText firebaseVisionText) {
//        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
//        bookAndQuantity=new ArrayList<>();
//        mystorage.imageToTextOrder=bookAndQuantity;
//        String outString="";
//        String nearestName;
//        int nearestCode;
//        String bName;
//        int flag=0;
//        int quantity;
//
//        if (blockList.size() != 0) {
//            for(int i=0;i<blockList.size();i++)
//            {
//                List<FirebaseVisionText.Line> lines=blockList.get(i).getLines();
//                for(int j=0;j<lines.size();j++)
//                {
//                    List<FirebaseVisionText.Element> elements=lines.get(j).getElements();
//                    if(isNumeric(elements.get(elements.size()-1).getText())) {
//                        flag = 1;
//                        quantity=Integer.parseInt(elements.get(elements.size()-1).getText());
//                    }
//                    else {
//                        flag = 0;
//                        quantity=1;
//                    }
//                    bName="";
//                    for(int k=0;k<elements.size()-flag;k++)
//                    {
//                        bName=bName+elements.get(k).getText();
//                    }
//                    myPair nearestBookPair=getNearestBook(bName);
//                    nearestName=nearestBookPair.getName();
//                    nearestCode=nearestBookPair.getCode();
//                    outString=outString+"Book = "+bName+"\n Nearest = "+nearestName+"\nQuantity = "+quantity +"\n\n\n\n";
//                    bookAndQuantity.add(new myPair(String.valueOf(nearestCode),quantity));
//                }
//            }
//        }
//        return bookAndQuantity;
//    }
