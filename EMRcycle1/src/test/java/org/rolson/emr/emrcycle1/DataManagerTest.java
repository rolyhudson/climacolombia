package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Test;

public class DataManagerTest {
	DataManager dm = mock(DataManager.class);
	@Test
	public void testIncorrectFileType() {
		when(dm.upload(new File("src\test\resources\testfiles\testfile.txt"))).thenReturn(true);
		assertTrue(dm.upload(new File("src\test\resources\testfiles\testfile.txt")));
	}

}
