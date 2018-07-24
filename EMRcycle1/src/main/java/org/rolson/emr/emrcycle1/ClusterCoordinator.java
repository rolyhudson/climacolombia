package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;

import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.ListClustersResult;
import com.amazonaws.services.elasticmapreduce.model.ListStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.ListStepsResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;

import com.amazonaws.services.elasticmapreduce.model.StepSummary;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClusterCoordinator {
	private List<Cluster> clusters;
	private List<Workflow> allWorkflows;
	public ObservableList<Cluster> monitorResourceData = FXCollections.observableArrayList();
	public ObservableList<Workflow> monitorWorkflowData = FXCollections.observableArrayList();
	private AmazonElasticMapReduceClient emr;
	private DateTime monitorFrom = DateTime.now().minusDays(2);
	public ClusterCoordinator()
	{
		this.allWorkflows = new ArrayList<Workflow>();
		this.clusters = new ArrayList<Cluster>();
		setEMRClient();
	}
	public void updateAll()
	{
		//updates cannot run on frequent cycle geerates a throttling error
		//get the clusters
		updateResourceStatus();
		//based on clusters in time range get the workflows
		updateWorkflowStatus();

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
	public void setMonitorFrom(LocalDate date)
	{
		monitorFrom = new DateTime(date);
	}
	public DateTime getMonitorFrom()
	{
		return monitorFrom;
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
	private void updateWorkflowAwsIDs(Cluster c)
	{
		String clusterid = c.getAwsID();
		ListStepsResult steps = emr.listSteps(new ListStepsRequest().withClusterId(clusterid));
		List<StepSummary> stepsummaries = steps.getSteps();
		//need to match aws steps with out workflows
		for(StepSummary ss : stepsummaries)
		{
			String stepstatus = ss.getStatus().getState();
		    String id = ss.getId();
		    c.getWorkflows().get(0).setAwsID(id);
		    c.getWorkflows().get(0).setStatus(stepstatus);
		}
	}
	private void updateWorkflowStatus()
	{
		for(Cluster c: clusters)
		{
				ListStepsResult steps = emr.listSteps(new ListStepsRequest().withClusterId(c.getAwsID()));
			    
			    for(StepSummary step:steps.getSteps())
			    {
				    String stepstatus = step.getStatus().getState();
				    String id = step.getId();
					Optional<Workflow> wf = allWorkflows.stream().filter(x->id.equals(x.getAwsID())).findFirst();
					Workflow wflow;
					if(wf.isPresent())
					{
						//when the cluster was started in or found running by the current session
						wflow = wf.get();
					}
					else {
						//if the app starts and clusters are found ruuning
						//create step with details from aws
						wflow = new Workflow(step);
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
			DateTime creationDate = new DateTime(cs.getStatus().getTimeline().getCreationDateTime());
			if(creationDate.isAfter(monitorFrom))
			{
				String name = cs.getName();
				String id = cs.getId();
				Optional<Cluster> c = clusters.stream().filter(x->id.equals(x.getAwsID())).findFirst();
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
	public void stopCluster(Cluster c)
	{
		TerminateJobFlowsRequest request =new TerminateJobFlowsRequest().withJobFlowIds(c.getAwsID());
		emr.terminateJobFlows(request);
		updateAll();
	}
	public void stopAllClusters()
	{
		TerminateJobFlowsRequest request =new TerminateJobFlowsRequest();
		for(Cluster c:clusters)
		{
		
		request.withJobFlowIds(c.getAwsID());
		}
		emr.terminateJobFlows(request);
		updateAll();
	}
	public void runClusterByIndex(int index)
	{
		Cluster clus = clusters.get(index);
		if(clus!=null)
		{
			System.out.println("Starting workflow ");
			clus.setRequest();
			clus.setResult(emr.runJobFlow(clus.getRequest()));
			clus.setStatus("starting");
			updateAll();
		}
	}
	public void runWorkflow(Workflow wf)
	{
		//workflow is already in allWorkflows list
		//check for running cluster and give option to add to exsiting
		Cluster newclus = new Cluster();
		newclus.addWorkflow(getWorkflow(wf.getCreationDate()));
		newclus.setName("Cluster with workflow: "+wf.getName());
		addCluster(newclus);
		runCluster(newclus);
	}
	public void runCluster(Cluster clus)
	{
		if(clus!=null)
		{
			System.out.println("Starting cluster ");
			clus.setRequest();
			//setResult assigns awsid to cluster
			clus.setResult(emr.runJobFlow(clus.getRequest()));
			//
			updateWorkflowAwsIDs(clus);
			clus.setStatus("starting");
			updateAll();
		}
	}
	private Workflow getWorkflow(DateTime timecreated)
	{
		Optional<Workflow> matches = allWorkflows.stream()
				.filter(t -> t.getCreationDate() ==timecreated)
				.findAny(); 
		if(matches.isPresent())
		{
			Workflow w = matches.get();
			return w;
		}
		else
		{
			return null;
		}
	}
	public Cluster getCluster(DateTime timecreated)
	{
		Optional<Cluster> matches = clusters.stream()
				.filter(t -> t.getCreationDate() ==timecreated)
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
			//this is for the tests
			//this.allWorkflows.add(w);
		}
		
	}
	public void addWorkflow(Workflow wf)
	{
		this.allWorkflows.add(wf);
		monitorWorkflowData.setAll(allWorkflows);
	}
	public void addWorkflowToCluster(String jobflowid)
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
	
}