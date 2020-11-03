package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.booker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class user_profile extends AppCompatActivity {

    private String userID, userEmail;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth firebaseAuth;
    private TextView name, email, phone;
    private UserDB userDB;
    private Button saveBtn;
    private List<String> s;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("User Profile");
        saveBtn = findViewById(R.id.saveButton);

        final EditText nameEditText = findViewById(R.id.editTextName);
        final EditText emailEditText = findViewById(R.id.editTextEmail);
        final EditText phoneEditText = findViewById(R.id.editTextPhone);
        final EditText usernameEditText = findViewById(R.id.editTextUsername);

        // access user's document
        Query query = db.collection("Users")
                .whereEqualTo("email", firebaseUser.getEmail());

        // set edit text to current values
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                    nameEditText.setText(documentSnapshot.getString("name"));
                    emailEditText.setText(documentSnapshot.getString("email"));
                    phoneEditText.setText(documentSnapshot.getString("phone"));
                    usernameEditText.setText(documentSnapshot.getString("username"));
                }
            }
        });

        // save edit text values to firestore and exit
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> data = new HashMap<>();
                data.put("name", nameEditText.getText().toString());
                data.put("email", emailEditText.getText().toString());
                data.put("phone", phoneEditText.getText().toString());
                data.put("username", usernameEditText.getText().toString());

                // update in Users on firestore
                db.collection("Users").document(firebaseUser.getEmail()).set(data);

                // update email for FiresbaseAuth
                firebaseUser.updateEmail(emailEditText.getText().toString());

                // Update the DisplayName for FirebaseAuth
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(usernameEditText.getText().toString())
                        .build();

                firebaseUser.updateProfile(profileChangeRequest);

                finish();
            }
        });

    }

}
