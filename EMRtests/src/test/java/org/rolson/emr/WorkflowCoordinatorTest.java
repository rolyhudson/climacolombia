package org.rolson.emr;

import static org.junit.Assert.*;

import org.junit.Test;

public class WorkflowCoordinatorTest {

	WorkflowCoordinator wfc = new WorkflowCoordinator();
	@Test
	public void testWorkflowsList() {
		assertNotNull(wfc.getWorkflows());
	}
	@Test
	public void testClient() {
		wfc.setEMRClient();
		assertNotNull(wfc.getClient());
	}
	@Test
	public void testGetCluster()
	{
		wfc.setEMRClient();
		wfc.getWorkflowFromAWS("j-SOR0AFWJVGJN");
		assertEquals(wfc.getWorkflows().get(0).name,"newcluster");
	}
}
