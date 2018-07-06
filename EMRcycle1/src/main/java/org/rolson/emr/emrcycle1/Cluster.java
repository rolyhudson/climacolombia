package org.rolson.emr;

import java.io.IOException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.AWSCredentials.*;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.*;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.amazonaws.services.elasticmapreduce.util.*;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.JsonObject;

public class Cluster {
	
	public Cluster() {
		//setCredentials();
	}
	public void launchNOAACounter() {
		//get the client
		   AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());
		   //ListClustersResult clusters = emr.listClusters();
		   
		//set up step data   
		   String COMMAND_RUNNER = "s3://rolyhudsontestbucket1/climateData/stationAnalysis.jar";
		   
		   String DEBUGGING_NAME = "Hadoop MR NOAA Counting"; 
		   String INPUTFILE = "s3://rolyhudsontestbucket1/climateData/VV.txt";
		   String OUTPUTFOLDER = "s3://rolyhudsontestbucket1/climateData/runfromJAVA";
		   List<String> commandArgs = Arrays.asList(INPUTFILE, OUTPUTFOLDER);
		   String MAINCLASS = "org.rolson.mapreduce.mapreduce2.StationAnalysisDriver";
		   

		   StepConfig enabledebugging = new StepConfig()
		       .withName(DEBUGGING_NAME)
		       .withActionOnFailure(ActionOnFailure.TERMINATE_CLUSTER)
		       .withHadoopJarStep(new HadoopJarStepConfig()
		           .withJar(COMMAND_RUNNER)
		           .withArgs(commandArgs)
		           .withMainClass(MAINCLASS));
		   //request new cluster
		   RunJobFlowRequest request = new RunJobFlowRequest()
		       .withName("NOAACounter")
		       .withReleaseLabel("emr-5.14.0")
		       .withSteps(enabledebugging)
		       .withLogUri("s3://rolyhudsontestbucket1/climateData")
		       .withServiceRole("EMR_DefaultRole")
		       .withJobFlowRole("EMR_EC2_DefaultRole")
		       .withInstances(new JobFlowInstancesConfig()
		           .withEc2KeyName("monday")
		           .withInstanceCount(5)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType("m3.xlarge")
		           .withSlaveInstanceType("m3.xlarge")
		           .withEc2SubnetId("subnet-da059bd5"));

		   RunJobFlowResult result = emr.runJobFlow(request);
		   result.getJobFlowId();
		   
		   //emr.terminateJobFlows();
	}
	public void launch() {
		//get the client
		   AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());
		   //ListClustersResult clusters = emr.listClusters();
		   
		//set up step data   
		   String COMMAND_RUNNER = "command-runner.jar";
		   String DEBUGGING_COMMAND = "state-pusher-script";
		   String DEBUGGING_NAME = "Setup Hadoop Debugging";   

		   StepFactory stepFactory = new StepFactory();

		   StepConfig enabledebugging = new StepConfig()
		       .withName(DEBUGGING_NAME)
		       .withActionOnFailure(ActionOnFailure.TERMINATE_CLUSTER)
		       .withHadoopJarStep(new HadoopJarStepConfig()
		           .withJar(COMMAND_RUNNER)
		           .withArgs(DEBUGGING_COMMAND));
		   //request new cluster
		   RunJobFlowRequest request = new RunJobFlowRequest()
		       .withName("Hive Interactive")
		       .withReleaseLabel("emr-5.14.0")
		       .withSteps(enabledebugging)
		       .withLogUri("s3://rolyhudsontestbucket1/")
		       .withServiceRole("EMR_DefaultRole")
		       .withJobFlowRole("EMR_EC2_DefaultRole")
		       .withInstances(new JobFlowInstancesConfig()
		           .withEc2KeyName("monday")
		           .withInstanceCount(5)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType("m4.large")
		           .withSlaveInstanceType("m4.large")
		           .withEc2SubnetId("subnet-da059bd5"));

		   RunJobFlowResult result = emr.runJobFlow(request);
		   result.getJobFlowId();
		   
		   //emr.terminateJobFlows();
	}
	public void launchSparkCluster()
	{
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());

		Application sparkApp = new Application()
		    .withName("Spark");
//		Applications myApps = new Applications();
//		myApps.add(sparkApp);

		RunJobFlowRequest request = new RunJobFlowRequest()
		    .withName("Spark Cluster")
		    .withApplications(sparkApp)
		    .withReleaseLabel("emr-5.15.0")
		    .withLogUri("s3://rolyhudsontestbucket1/")
	        .withServiceRole("EMR_DefaultRole")
		     .withJobFlowRole("EMR_EC2_DefaultRole")
		       .withInstances(new JobFlowInstancesConfig()
		           .withEc2KeyName("monday")
		           .withInstanceCount(5)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType("m4.large")
		           .withSlaveInstanceType("m4.large")
		           .withEc2SubnetId("subnet-da059bd5"));
		RunJobFlowResult result = emr.runJobFlow(request);
	}
	public void terminateAllClusters()
	{
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());
		ListClustersResult clusters = emr.listClusters();
		List<ClusterSummary> clusterlist = clusters.getClusters();
		
		List<String> nonTerminatedClusters = new ArrayList<String>();
		for(ClusterSummary cs: clusterlist)
		{
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			String status = cs.getStatus().getState();
			if(status.equals("STARTING")||
					status.equals("RUNNING")||
							status.equals("WAITING") ){
				nonTerminatedClusters.add(cs.getId());
			}
			
		}
		TerminateJobFlowsRequest request =new TerminateJobFlowsRequest()
				.withJobFlowIds(nonTerminatedClusters);
		emr.terminateJobFlows(request);
	}
	
	public void clusterStatusReport()
	{
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());
		ListClustersResult clusters = emr.listClusters();
		List<ClusterSummary> clusterlist = clusters.getClusters();
		for(ClusterSummary cs: clusterlist)
		{
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			System.out.println("ID: "+cs.getId()+", name: "+cs.getName()+" status: "+cs.getStatus().getState());
			
		}
	}
}

