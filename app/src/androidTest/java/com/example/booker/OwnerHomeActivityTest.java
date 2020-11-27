package com.example.booker;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OwnerHomeActivityTest {
	private Solo solo;
	
	@Rule
	public ActivityTestRule<MainActivity> rule =
			new ActivityTestRule<>(MainActivity.class, true, true);
	
	/**
	 * Runs before all tests and creates solo instance.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception{
		solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());
		
		solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
		
		solo.enterText((EditText) solo.getView(R.id.usernameEditText), "intent@test.com");
		solo.enterText((EditText) solo.getView(R.id.passwordEditText), "123456");
		solo.clickOnButton("Sign In");
		
		solo.waitForText("Owner");
		solo.clickOnButton("Owner");
		
		solo.assertCurrentActivity("Wrong Activity", OwnerHomeActivity.class);
	}
	/**
	 * Gets the Activity
	 * @throws Exception
	 */
	@Test
	public void start() throws Exception{
		Activity activity = rule.getActivity();
	}
	
	/**
	 * Adds a book to the user's owned books and checks the information is correct
	 * Deletes the book and checks the book is no longer displayed
	 * This is all done while the app communicates with the firestore cloud so that functionality is tested as well
	 */
	@Test
	public void checkBookDisplayAndDelete(){
		solo.assertCurrentActivity("Wrong Activity", OwnerHomeActivity.class);
		
		// from here: https://stackoverflow.com/questions/21476144/click-on-a-button-by-using-rosource-id-in-robotium-scripts
		FloatingActionButton addBookFAB = (FloatingActionButton) solo.getView(R.id.add_book_btn);
		solo.clickOnView(addBookFAB);
		
		solo.enterText((EditText) solo.getView(R.id.editTextTitle), "TestBook title");
		solo.enterText((EditText) solo.getView(R.id.editTextAuthor), "TestBook author");
		solo.enterText((EditText) solo.getView(R.id.editTextISBN), "1234567890123");
		solo.clickOnButton("OK");
		
		solo.waitForText("TestBook title", 1, 2000);
		
		// From Lab 7
		OwnerHomeActivity activity = (OwnerHomeActivity) solo.getCurrentActivity();

		// TODO: change this to get books from the recycler view like in the Lab
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		Query query = db.collection("Books")
				.whereEqualTo("ownerUsername", user.getDisplayName())
				.whereEqualTo("title", "TestBook title");

		query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
					Book book = queryDocumentSnapshot.toObject(Book.class);
					assertEquals("TestBook title", book.getTitle());
					assertEquals("TestBook author", book.getAuthor());
					assertEquals("1234567890123", book.getISBN());
					return;
				}
			}
		});
		// TODO: Maybe add test for other attributes like description
		
		solo.clickOnButton("Delete");
		assertFalse(solo.searchText("TestBook title"));
	}
	
	
	
	
	/**
	 * Closes the activity after each test
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception{
		solo.finishOpenedActivities();
	}
}
