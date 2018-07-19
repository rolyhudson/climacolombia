package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
@Ignore
public class IntegrationTests {
	ClusterCoordinator coord = new ClusterCoordinator();
	
	@Test
	public void testEMRClient() {
		coord.setEMRClient();
		assertNotNull(coord.getClient());
	}
	@Test
	public void testEMRRunHadoopMR() {
		coord.setEMRClient();
		//define cluster
		Cluster cluster = new Cluster();
		cluster.setName("Hadoop Map Reduce");
		//add a workfow
		cluster.addPredfined("Hadoop Map Reduce");
		coord.runCluster("Hadoop Map Reduce");
		
		
	}
}
