package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;


import java.io.File;

import org.junit.Test;

import Coordination.Utils;

public class UtilsTest {

	@Test
	public void testGetExtension() {
		File f = new File("src\test\resources\testfiles\testfile.txt");
		assertEquals(Utils.getFileExtension(f),"txt");
	}
}
