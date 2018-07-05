package org.rolson.emr;

import static org.junit.Assert.*;
//http://junit.sourceforge.net/javadoc/org/junit/Assert.html


import org.junit.Test;

public class WorkflowTest {

	Workflow wf = new Workflow("wfname");
	@Test
	public void testDefaultSetUp() {
		
		assertNotNull(wf.getName());
		assertNull(wf.result);
		assertEquals(wf.logUri, "s3://rolyhudsontestbucket1/climateData");
		assertEquals(wf.masterInstance,"m3.xlarge");
		assertEquals(wf.slaveInstance,"m3.xlarge");
		assertEquals(wf.dataSource,"s3://rolyhudsontestbucket1/climateData/VV.txt");
		assertEquals(wf.appType,"Hadoop");
		assertEquals(wf.ec2KeyName,"monday");
		assertEquals(wf.serviceRole,"EMR_DefaultRole");
		assertEquals(wf.JobFlowRole,"EMR_EC2_DefaultRole");
		assertEquals(wf.subnetID,"subnet-da059bd5");
		assertEquals(wf.debugName,"Hadoop MR NOAA Counting");
		assertEquals(wf.mainClassInJAR,"org.rolson.mapreduce.mapreduce2.StationAnalysisDriver");
		assertEquals(wf.analysisJAR,"s3://rolyhudsontestbucket1/climateData/stationAnalysis.jar");
		assertEquals(wf.status,"NEW");
	}
	@Test
	public void testCommandArgs() {
		assertEquals(wf.getCommandArgs().get(0),wf.dataSource);
		assertEquals(wf.getCommandArgs().get(1),wf.outputFolder);
	}
}
