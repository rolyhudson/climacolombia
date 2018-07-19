package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.*;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepResult;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.ListClustersResult;
import com.amazonaws.services.elasticmapreduce.model.ListStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.ListStepsResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepStatus;
import com.amazonaws.services.elasticmapreduce.model.StepSummary;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClusterCoordinator {
	private List<Cluster> clusters;
	private List<Workflow> allWorkflows;
	public ObservableList<Cluster> monitorResourceData = FXCollections.observableArrayList();
	public ObservableList<Workflow> monitorWorkflowData = FXCollections.observableArrayList();
	private AmazonElasticMapReduceClient emr;
	private Timer timer;
	private String connectionStatus;
	public ClusterCoordinator()
	{
		//
		this.allWorkflows = new ArrayList<Workflow>();
		this.clusters = new ArrayList<Cluster>();
		setEMRClient();
		
		//getWorkflowsFromAWS();
		//dummyWorkflows();
	}
	public void setEMRClient()
	{
		try{
			//need to update the method here
			emr = new AmazonElasticMapReduceClient(new DefaultAWSCredentialsProviderChain());
		}
		catch(Exception e){
			emr=null;
		}
		 
	}
	public String EMRStatus()
	{
		String status = ""; 
		DateTime dt = new DateTime();
		DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
		String time = dtf.print(dt);
		if(emr!=null) status = "Connection status: connected, last update: "+time;
		else status = "Connection status: not connected, last update: "+time;
		return status;
	}
	public void updateWorkflowStatus()
	{
		ListClustersResult awsclusters = emr.listClusters();
		List<ClusterSummary> clusterlist = awsclusters.getClusters();
		for(ClusterSummary cs: clusterlist)
		{
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			String status = cs.getStatus().getState();
			if(status.equals("STARTING")||
					status.equals("RUNNING")||
							status.equals("WAITING")||
							status.equals("BOOTSTRAPPING"))
			{
				String id = cs.getId();
				ListStepsResult steps = emr.listSteps(new ListStepsRequest().withClusterId(id));
			    StepSummary step = steps.getSteps().get(0);
			    String stepstatus = step.getStatus().getState();
			    String name = step.getName();
				Optional<Workflow> wf = allWorkflows.stream().filter(x->name.equals(x.getName())).findFirst();
				Workflow wflow;
				if(wf.isPresent())
				{
					//when the cluster was started in or found running by the current session
					wflow = wf.get();
					wflow.setAwsID(step.getId());
				}
				else {
					//if the app starts and clusters are found ruuning
					wflow = new Workflow(name);
					wflow.setAwsID(step.getId());
					allWorkflows.add(wflow);
				}
				wflow.setStatus(stepstatus);
			}
		}
		monitorWorkflowData.setAll(allWorkflows);
	}
	public void updateResourceStatus()
	{
		//get clusters active
		
		ListClustersResult awsclusters = emr.listClusters();
		List<ClusterSummary> clusterlist = awsclusters.getClusters();
		
		for(ClusterSummary cs: clusterlist)
		{
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			String status = cs.getStatus().getState();
			if(status.equals("STARTING")||
					status.equals("RUNNING")||
							status.equals("WAITING")||
							status.equals("BOOTSTRAPPING"))
			{
				String name = cs.getName();
				Optional<Cluster> c = clusters.stream().filter(x->name.equals(x.getName())).findFirst();
				Cluster clus;
				if(c.isPresent())
				{
					//when the cluster was started in or found running by the current session
					clus = c.get();
					clus.setAwsID(cs.getId());
				}
				else {
					//if the app starts and clusters are found ruuning
					clus = new Cluster();
					clus.setName(name);
					clus.setAwsID(cs.getId());
					clusters.add(clus);
				}
				clus.setStatus(status);
			}
		}
		monitorResourceData.setAll(clusters);
	}

	public AmazonElasticMapReduce getClient()
	{
		return emr;
	}
	public void getWorkflowFromAWS(String clusterID)
	{
//		DescribeClusterResult clusterinfo = emr.describeCluster(new DescribeClusterRequest().withClusterId(clusterID));
//		Workflow wf = new Workflow(clusterinfo.getCluster().getName());
//		addWorkflow(wf);
	}
	public void runCluster(String name)
	{
		//setEMRClient();
		//get the workflow by name
		Cluster clus = getCluster(name);
		if(clus!=null)
		{
			System.out.println("Starting workflow "+name);
			clus.setRequest();
			clus.setResult(emr.runJobFlow(clus.getRequest()));
			clus.setStatus("starting");
		}
		
	}
	public Cluster getCluster(String name)
	{
		Optional<Cluster> matches = clusters.stream()
				.filter(t -> t.getName() ==name)
				.findAny(); 
		if(matches.isPresent())
		{
			Cluster clus = matches.get();
			return clus;
		}
		else
		{
			return null;
		}
	}
	public void addCluster(Cluster c)
	{
		clusters.add(c);
		for(Workflow w: c.getWorkflows())
		{
			this.allWorkflows.add(w);
		}
		workflowUpdate();
	}
	public void addStepToCluster(String jobflowid)
	{
		AddJobFlowStepsRequest req = new AddJobFlowStepsRequest();
		req.withJobFlowId(jobflowid);

		List<StepConfig> stepConfigs = new ArrayList<StepConfig>();
				
		HadoopJarStepConfig sparkStepConf = new HadoopJarStepConfig()
					.withJar("command-runner.jar")
					.withArgs("spark-submit","--executor-memory","1g","--class","org.apache.spark.examples.SparkPi","/usr/lib/spark/lib/spark-examples.jar","10");			
				
		StepConfig sparkStep = new StepConfig()
					.withName("Spark Step")
					.withActionOnFailure("CONTINUE")
					.withHadoopJarStep(sparkStepConf);

		stepConfigs.add(sparkStep);
		req.withSteps(stepConfigs);
		AddJobFlowStepsResult result = emr.addJobFlowSteps(req);
	}
	private void workflowUpdate()
	{
		//called to trigger update on monitor
		//monitorData.setAll(allWorkflows);
	}
}
