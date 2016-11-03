package net.jahhan.test;

import org.junit.Before;
import org.junit.Test;

import net.jahhan.constant.enumeration.DBConnectionType;

public class SampleTest extends AbstractTestMethod{

	@Before
	public void setUp() throws Exception {
		setDBConnectionType(DBConnectionType.WEAK_WRITE);
		super.setUp();
	}

	@Test
	public void test() {
		
	}
}