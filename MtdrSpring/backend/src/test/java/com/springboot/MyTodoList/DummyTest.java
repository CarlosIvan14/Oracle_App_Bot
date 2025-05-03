package com.springboot.MyTodoList;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DummyTest {

	@Test
	public void testAddition() {
		int a = 5;
		int b = 7;
		int sum = a + b;
		Assert.assertEquals(sum, 12, "Sum should be 12");
	}

	@Test
	public void testString() {
		String greeting = "Hello, TestNG!";
		Assert.assertTrue(greeting.contains("TestNG"), "Greeting should contain 'TestNG'");
	}

}