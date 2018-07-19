package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

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
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClusterCoordinator {
	private List<Cluster> clusters;
	private List<Workflow> allWorkflows;
	public ObservableList<Workflow> monitorData = FXCollections.observableArrayList();
	private AmazonElasticMapReduceClient emr;
	private Timer timer;
	public ClusterCoordinator()
	{
		//
		this.allWorkflows = new ArrayList<Workflow>();
		this.clusters = new ArrayList<Cluster>();
		setEMRClient();
		setClusterMonitoring();
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
	public void setClusterMonitoring(){
	    TimerTask repeatedTask = new TimerTask() {
	        public void run() {
	            System.out.println("Task performed on " + new DateTime());
	            //testWorkflowMonitor();
	            updateStatus();
	        }
	    };
	    timer = new Timer("Timer");
	     
	    long delay  = 1000L;
	    long period = 10000L;
	    timer.scheduleAtFixedRate(repeatedTask, delay, period);
	}
	public void endTimer()
	{
		timer.cancel();
	}
	private void testWorkflowMonitor()
	{
		Workflow wf = new Workflow("workflow_"+new DateTime());
		allWorkflows.add(wf);
		monitorData.setAll(allWorkflows);
	}
	private void updateStatus()
	{
		//get clusters active
		
		ListClustersResult clusters = emr.listClusters();
		List<ClusterSummary> clusterlist = clusters.getClusters();
		List<String> nonTerminatedClusters = new ArrayList<String>();
		List<String> terminatedClusters = new ArrayList<String>();
		for(ClusterSummary cs: clusterlist)
		{
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			String status = cs.getStatus().getState();
			if(status.equals("STARTING")||
					status.equals("RUNNING")||
							status.equals("WAITING")||
							status.equals("BOOTSTRAPPING")){
				nonTerminatedClusters.add(cs.getId());
			}
			else terminatedClusters.add(cs.getId());
		}
//		for(String id : nonTerminatedClusters)
//		{
//			DescribeClusterResult clusterinfo = emr.describeCluster(new DescribeClusterRequest().withClusterId(id));
//			com.amazonaws.services.elasticmapreduce.model.Cluster c = clusterinfo.getCluster();
//			//c.
//		}
		allWorkflows.clear();
		for(String cId : nonTerminatedClusters)
		{
			DescribeStepRequest sr = new DescribeStepRequest().withStepId("s-F647AKLL3WG3");
			DescribeStepResult stepinfo = emr.describeStep(sr);
			StepStatus status = stepinfo.getStep().getStatus();
			 //currently cluster only has one workflow
			//c.getWorkflows().get(0).status = status.getState();
			//allWorkflows.add(c.getWorkflows().get(0));
		}
		//update the gui by adding the workflows again
		monitorData.setAll(allWorkflows);
		//from each cluster get the steps
		//match steps to allWorkflows
		
	}
//	public void removeWorkflow()
//	{
//		data.remove(0);
//	}
//	public void addWorkflow()
//	{
//		Workflow wf = new Workflow("workflow new");
//		data.add(wf );
//	}
//	public void updateWorkflow()
//	{
//		for(Workflow wf: workflows)
//		{
//			wf.status = "running";
//		}
//		//update cheat
//		
//		data.setAll(workflows);
//	}
//	private void dummyWorkflows()
//	{
//		for(int i=0;i<6;i++)
//		{
//			Workflow wf = new Workflow("workflow"+i);
//			wf.status = "starting";
//			wf.appType = "Spark";
//			workflows.add(wf );
//		}
//		data.setAll(workflows);
//	}
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
		monitorData.setAll(allWorkflows);
	}
}
