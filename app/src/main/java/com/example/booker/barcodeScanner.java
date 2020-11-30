package com.example.booker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class barcodeScanner extends AppCompatActivity {

    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    //This class provides methods to play DTMF tones
    private ToneGenerator toneGen1;
    private TextView barcodeText;
    private String barcodeData;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userEmail = user.getEmail();
    private CollectionReference bookCollection = db.collection("Books");
    private String scanType;

    private boolean bookCheck;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,     100);
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);
        //name = findViewById(R.id.name);
        //author = findViewById(R.id.author);

        // Grabs the type of scan this activity is doing
        scanType = getIntent().getExtras().getString("ScanType");

        initialiseDetectorsAndSources();
    }

    /** Barcode Scanner from Hari Lee on Medium
     * https://medium.com/analytics-vidhya/creating-a-barcode-scanner-using-android-studio-71cff11800a2#id_token=eyJhbGciOiJSUzI1NiIsImtpZCI6ImRlZGMwMTJkMDdmNTJhZWRmZDVmOTc3ODRlMWJjYmUyM2MxOTcyNGQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJuYmYiOjE2MDU4NDMxODAsImF1ZCI6IjIxNjI5NjAzNTgzNC1rMWs2cWUwNjBzMnRwMmEyamFtNGxqZGNtczAwc3R0Zy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjExNzMzMDY2MzM1OTE1ODEzNjcxMSIsImVtYWlsIjoiYXJiZWxhZXpjaEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiMjE2Mjk2MDM1ODM0LWsxazZxZTA2MHMydHAyYTJqYW00bGpkY21zMDBzdHRnLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwibmFtZSI6IkFyYmVsYWV6Y2giLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2haZUxfUzg5VzY4NHgzMVhsU19JYWdJbmJfQ1pmakFHT18wR1JTPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IkFyYmVsYWV6Y2giLCJpYXQiOjE2MDU4NDM0ODAsImV4cCI6MTYwNTg0NzA4MCwianRpIjoiZTZkZGRmZGFhMDJiOGE2NzdhYzczNjJkMTNhNzAwMzk1NGMxNTg0MSJ9.QU_019yMX8sGRhQJIEQ0xnqykda_IkR241pU6AcXGFHMmRVPHoCVRxeIa77jqVNU1BRTGymuEXezj4jgu-tg6y17KVQP0Anb5aaPN3hRf8elJXfz3ynHOIuuemww6_u6dIU05e3dkVcSwtwzXUm7yE-d8UnzdpetxqjZL3MXGQ5GTWY_b4u4oe0CtgRFrOaUi2smGcthlulQ7nMTrBC6QhO7UMAYiLuSWVyNHQNLcdujBLCd1nboxasxi2aTkUfWBjqUl9CUtiJ3tZ7kcMPM5sQzrSmgIarPJrsRcgaq6ZP8z4yVo4SJFnmjJ6eRjxLCQPi2ne79miZQsifNUNHt6Q
     */
    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(barcodeScanner.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });




        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {


                    barcodeText.post(new Runnable() {

                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).email != null) { // I dont think this ever happens?
                                barcodeText.removeCallbacks(null);
                                barcodeData = barcodes.valueAt(0).email.address;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                stopCamera();
                                finish();
                            } else {

                                barcodeData = barcodes.valueAt(0).displayValue;
                                barcodeText.setText(barcodeData);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                stopCamera();

                                if (scanType.equals("BorrowerReceive")) {
                                    checkBookBorrowed(barcodeData);
                                } else if (scanType.equals("OwnerReceive")) {
                                    checkBookBorrowed(barcodeData);
                                } else if (scanType.equals("BorrowerHandOver")) {
                                    checkBookBorrowed(barcodeData);
                                } else if (scanType.equals("OwnerHandOver")) {
                                    checkBookAccepted(barcodeData);
                                }

                            }
                        }
                    });

                }
            }
        });
    }

    private void stopCamera() {
        cameraSource.stop();
    }

    private void checkBookAccepted(String ISBN) {
        // Checks if a book is accepted when an owner is handing over the book !! This happens in here
        // Checks if a book is borrowed when borrower is handing over the book
        // Checks if a book is accepted when borrower is receiving the book (must first be handed over by owner) !! This happens in here
        // Checks if a book is borrowed when an owner is receiving the book (must first be handed over by borrower)
        Query query = bookCollection.whereEqualTo("ownerEmail", userEmail).whereEqualTo("ISBN", ISBN).whereEqualTo("status", "Accepted");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    bookCheck = false;
                    for (final QueryDocumentSnapshot document : task.getResult()) {

                        final String bookID = document.getId();
                        bookCheck = true;
                        final Map<String, Object> book = document.getData();
                        List<String> requesterList = (List<String>) book.get("requesterList");
                        if (requesterList.size() > 0) {
                            String borrower = requesterList.get(0);
                            // from here https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
                            new AlertDialog.Builder(barcodeScanner.this)
                                    .setTitle("Hand book to this user?")
                                    .setMessage(borrower)

                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // TODO: update book status or other attribute so borrower cannot scan until owner does
                                            // the 4 scanTypes are: OwnerHandOver, OwnerReceive, BorrowerHandOver, BorrowerReceive
                                            if (scanType.equals("OwnerHandOver")) {
                                                // Make book "Borrowed"
                                                updateOwnerScanBool(bookID, true);
                                                updateBookStatus(bookID, "Borrowed");
                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(barcodeScanner.this)
                                    .setTitle("No requests on this book")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                        
                    }
                    if (!bookCheck) {
                        new AlertDialog.Builder(barcodeScanner.this)
                                .setTitle("This book has no accepted requests")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(barcodeScanner.this)
                            .setTitle("This book has no accepted requests")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        });

    }

    private void checkBookBorrowed(String ISBN) {
        // Checks if a book is accepted when an owner is handing over the book
        // Checks if a book is borrowed when borrower is handing over the book !! This happens in here
        // Checks if a book is accepted when borrower is receiving the book (must first be handed over by owner)
        // Checks if a book is borrowed when an owner is receiving the book (must first be handed over by borrower) !! This happens in here
        Query query = bookCollection.whereEqualTo("ISBN", ISBN).whereEqualTo("status", "Borrowed");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    bookCheck = false;
                    for (final QueryDocumentSnapshot document : task.getResult()) {

                        final String bookID = document.getId();
                        bookCheck = true;
                        Map<String, Object> book = document.getData();
                        List<String> requesterList = (List<String>) book.get("requesterList");
                        if (requesterList.size() > 0) {
                            String borrower = requesterList.get(0);
                            // from here https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
                            new AlertDialog.Builder(barcodeScanner.this)
                                    .setTitle("Receive/Hand over Book?")
                                    .setMessage("")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // TODO: update book status or other attribute so Owner cannot scan until Borrower does
                                            // the 4 scanTypes are: OwnerHandOver, OwnerReceive, BorrowerHandOver, BorrowerReceive
                                            if (scanType.equals("BorrowerHandOver")) {
                                                updateBorrowerScanBool(bookID, true);
                                                
                                                
                                            } else if (scanType.equals("OwnerReceive")) {
                                                if(document.get("scannedByBorrower").equals(true)) {
                                                    new AlertDialog.Builder(barcodeScanner.this)
                                                            .setTitle("Confirmation")
                                                            .setMessage("Successful")
                                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    updateBorrowerScanBool(bookID, false);
                                                                    updateBookStatus(bookID, "Available");
                                                                }
                                                            })
                                                            .show();
                                                } else if (scanType.equals("BorrowerReceive")) { // Fixed
                                                    if(document.get("scannedByOwner").equals(true)){
                                                        new AlertDialog.Builder(barcodeScanner.this)
                                                                .setTitle("Confirmation")
                                                                .setMessage("Successful")
                                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        updateBorrowerScanBool(bookID, false);
                                                                        updateOwnerScanBool(bookID, false);
                                                                        
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                    else{
                                                        new AlertDialog.Builder(barcodeScanner.this)
                                                                .setTitle("Confirmation")
                                                                .setMessage("There was an error")
                                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        finish();
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                }
                                            }
                                        }
                                    })

                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(barcodeScanner.this)
                                    .setTitle("No requests on this book")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                        
                    }
                    if (!bookCheck) {
                        new AlertDialog.Builder(barcodeScanner.this)
                                .setTitle("This book has no accepted requests")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(barcodeScanner.this)
                            .setTitle("This book has no accepted requests")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        });

    }

    private void updateBookStatus(String bookID, String status) {
        final String TAG = "updateBookStatus";

        // change book's status on firestore
        bookCollection.document(bookID).update("status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        // empty requester list if status is "Available" on firestore
        if (status.equals("Available")) {
            bookCollection.document(bookID).update("requesterList", Arrays.asList())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        }
    }

    private void updateOwnerScanBool(String bookID, boolean state) {
        final String TAG = "updateBookStatus";

        bookCollection.document(bookID).update("scannedByOwner", state).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }
    
    private void updateBorrowerScanBool(String bookID, boolean state) {
        final String TAG = "updateBookStatus";
        
        bookCollection.document(bookID).update("scannedByBorrower", state).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                return;
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

}
