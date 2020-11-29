package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OwnerFilterActivity extends AppCompatActivity {
    private List<Book> bookList = new ArrayList<>();
    private CollectionReference booksCollection = FirebaseFirestore.getInstance().collection("Books");



}
