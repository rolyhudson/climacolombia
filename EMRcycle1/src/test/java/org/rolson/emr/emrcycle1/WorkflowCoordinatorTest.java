package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;

public class WorkflowCoordinatorTest {
	WorkflowCoordinator wfc =  new WorkflowCoordinator();
	@Test
	public void testSetUpList() {

		assertTrue(wfc.getWorkflows().size()==0 );
	}
	@Test
	public void testAddPredefined()
	{
		//add one with no name
		wfc.addPredfined("");
		assertTrue(wfc.getWorkflows().size()==1 );
		assertTrue(wfc.getWorkflows().get(0).name=="");
		//add one with name
		wfc.addPredfined("Hadoop Map Reduce");
		assertTrue(wfc.getWorkflows().get(1).name=="Hadoop Map Reduce");
		//check the setup is run
		wfc.getWorkflows().get(1).setUpRequest();
		assertNotNull(wfc.getWorkflows().get(1).request);
	}
	@Test
	public void testStreamFind()
	{
		wfc.addPredfined("Hadoop Map Reduce");
		Optional<Workflow> matches = wfc.getWorkflows().stream().filter(t -> t.name =="Hadoop Map Reduce").findAny();
		assertEquals(matches.get().name,"Hadoop Map Reduce");
		
	}
}
