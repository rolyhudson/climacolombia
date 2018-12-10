package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import org.junit.Test;

import Coordination.ClusterCoordinator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClusterCoordinatorTest {

	@Test
	public void testCoonection() {
		ClusterCoordinator cc = new ClusterCoordinator();
		 
		
		assertTrue(cc.EMRStatus().contains("connected"));
	}
//	@Test
//	public void testNoCoonection() {
//		ClusterCoordinator cc;
//		String connect = cc.EMRStatus();
//		assertEquals(connect,"connected");
//	}
}
