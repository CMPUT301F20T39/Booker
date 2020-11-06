package com.example.booker;

import android.widget.EditText;
import android.widget.SearchView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BorrowerHomeActivityTest {
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
		
		solo.waitForText("Borrower");
		solo.clickOnButton("Borrower");
		
		solo.assertCurrentActivity("Wrong Activity", BorrowerHomeActivity.class);
	}
	
	/**
	 * Checks that a user is able to search all available books
	 * TODO: partial and full match checking
	 */
	@Test
	public void checkSearch(){
		solo.assertCurrentActivity("Wrong Activity", BorrowerHomeActivity.class);
		
		// from here: https://stackoverflow.com/questions/21476144/click-on-a-button-by-using-rosource-id-in-robotium-scripts
		SearchView searchview = (SearchView) solo.getView(R.id.searchView);
		solo.clickOnView(searchview);
		
		assertTrue(solo.waitForText("Displaying all available books", 1, 2000));
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
