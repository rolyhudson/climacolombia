package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
@Ignore
public class IntegrationTests {
	WorkflowCoordinator coord = new WorkflowCoordinator();
	
	@Test
	public void testEMRClient() {
		coord.setEMRClient();
		assertNotNull(coord.getClient());
	}
	@Test
	public void testEMRRunHadoopMR() {
		coord.setEMRClient();
		//add a workfow
		coord.addPredfined("Hadoop Map Reduce");
		coord.runWorkflow("Hadoop Map Reduce");
		Workflow wf = coord.getWorkflow("Hadoop Map Reduce");
		assertNotNull(wf.result);
		
	}
}
