package com.example.booker;

import android.accounts.Account;
import android.app.Activity;
import android.widget.EditText;

import com.robotium.solo.Solo;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MainActivityTest {
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
	 * Enters valid username and password information and checks if it is validated correctly
	 */
	@Test
	public void checkSignIn(){
		solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
		
		solo.enterText((EditText) solo.getView(R.id.usernameEditText), "intent@test.com");
		solo.enterText((EditText) solo.getView(R.id.passwordEditText), "123456");
		solo.clickOnButton("Sign In");
		
		assertTrue(solo.waitForActivity(AccountTypeActivity.class));
	}
	
	/**
	 * Enters invalid information and makes sure this does not let you enter the app
	 */
	@Test
	public void checkInvalidSignIn(){
		solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

		solo.clickOnButton("Sign In");
		assertTrue(solo.waitForActivity(MainActivity.class));

		solo.enterText((EditText) solo.getView(R.id.usernameEditText), "intent@test.com");
		solo.enterText((EditText) solo.getView(R.id.passwordEditText), "1234");
		solo.clickOnButton("Sign In");
		assertTrue(solo.waitForActivity(MainActivity.class));

		solo.clearEditText((EditText) solo.getView(R.id.usernameEditText));
		solo.clearEditText((EditText) solo.getView(R.id.passwordEditText));
		
		solo.enterText((EditText) solo.getView(R.id.usernameEditText), "not a username");
		solo.enterText((EditText) solo.getView(R.id.passwordEditText), "not a password");
		solo.clickOnButton("Sign In");
		
		assertTrue(solo.waitForActivity(MainActivity.class));
	}
	
	/**
	 * Checks sign up sends to correct activity
	 */
	@Test
	public void checkSignUp(){
		solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
		
		solo.clickOnButton("Sign Up");
		
		assertTrue(solo.waitForActivity(SignUpActivity.class));
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
