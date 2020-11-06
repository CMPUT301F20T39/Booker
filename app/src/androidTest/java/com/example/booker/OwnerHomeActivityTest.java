package com.example.booker;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
	 *
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
		final List<Book> bookList = activity.bookList;
		// TODO: change this to get books from the recycler view like in the Lab
		Book book = bookList.get(0);
		assertEquals("TestBook title", book.getTitle());
		assertEquals("TestBook author", book.getAuthor());
		assertEquals("1234567890123", book.getISBN());
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
