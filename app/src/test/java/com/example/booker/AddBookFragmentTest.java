package com.example.booker;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddBookFragmentTest extends junit.framework.TestCase {
	@Test
	public void testUID() {
		String testUID = AddBookFragment.generateUID();
		assertEquals(20, testUID.length());
		
		Pattern uidRegExPattern = Pattern.compile("[A-Z|a-z|0-9]{20}");
		Matcher m = uidRegExPattern.matcher(testUID);
		assertTrue(m.matches());
	}
}
