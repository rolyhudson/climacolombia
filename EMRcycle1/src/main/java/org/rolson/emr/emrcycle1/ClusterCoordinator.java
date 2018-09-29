package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.ClusterState;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
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
	private DateTime monitorFrom = DateTime.now().minusDays(5);
	private DateTime lastUpdate;
	private DataManager dataManager = new DataManager();
	public ClusterCoordinator()
	{
		this.allWorkflows = new ArrayList<Workflow>();
		this.clusters = new ArrayList<Cluster>();
		setEMRClient();
		
	}
		
	public void updateOnce() {
		//get stored workflows from text files in s3 
		updateStoredWorkflows();
		//updates cannot run on frequent cycle generates a throttling error
		//get the clusters
		updateResourceStatus();
		//based on clusters in time range get the workflows from aws
		updateWorkflowStatus();
		//single update in background
		lastUpdate = new DateTime();
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
		monitorFrom = new DateTime(date.getYear(),date.getMonthValue(),date.getDayOfMonth(),0,0);
	}
	public DateTime getMonitorFrom()
	{
		return monitorFrom;
	}
	public String EMRStatus()
	{
		String status = ""; 
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
		String time = dtf.print(lastUpdate);
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
	public List<Cluster> clusterActive()
	{
		List<Cluster> activeClusters=new ArrayList<Cluster>();
		for(Cluster c : this.clusters)
		{
			if(c.getStatus().equals("WAITING")||c.getStatus().equals("STARTING")||c.getStatus().equals("RUNNING"))
			{
				activeClusters.add(c);
			}
		}
		return activeClusters;
	}
	private void updateWorkflowStatus()
	{
		HashMap<String,Date> foundSteps = new HashMap<>();
		HashMap<String,String> foundStepStatus = new HashMap<>();
		for(Cluster c: clusters)
		{
			ListStepsResult steps = emr.listSteps(new ListStepsRequest().withClusterId(c.getAwsID()));
			
		    for(StepSummary step:steps.getSteps())
		    {
			    //get the guid from the command args
			    List<String> args = step.getConfig().getArgs();
			    String wfJSONfile =args.get(args.size()-2);
			    String guid = wfJSONfile.substring(wfJSONfile.lastIndexOf("/")+1, wfJSONfile.lastIndexOf("."));
			    Date start = step.getStatus().getTimeline().getCreationDateTime();
			    String status = step.getStatus().getState();
		    	Date s = foundSteps.get(guid);
		    	if(s==null)
		    	{
		    		//add it to the map
		    		foundSteps.put(guid,start);
		    		foundStepStatus.put(guid, status);
		    	}
		    	else {
		    		//if the found date is earlier add the latest
		    		if(s.before(start)) {
		    			foundSteps.put(guid,start);
		    			foundStepStatus.put(guid, status);
		    		}
		    			
		    	}
		    }
		}
		for(Workflow wf : allWorkflows)
		{
			//update status with most recent found on clusters
			String status = foundStepStatus.get(wf.getGuid());
			if(status!=null)
			{
				wf.setStatus(status);
				//save workflows to aws s3 here could get expensive!
				//this.dataManager.uploadStringToFile("workflowJSON/"+wf.getGuid()+".txt", wf.seraliseWorkflow(),"clustercolombia","plain/text");
			}
		}
		monitorWorkflowData.setAll(allWorkflows);
	}
	public void updateStoredWorkflows()
	{
		List<String> keys = this.dataManager.listBucketContentsPrefixed("workflowJSON/");
		for(String k : keys)
		{
			if(k.contains("workflowJSON")&&k.contains("txt"))
			{
				//does the wf exsit?
				String guid  = k.substring(k.lastIndexOf("/")+1,k.lastIndexOf("."));
				//check if the wf is already loaded
				Optional<Workflow> owf = allWorkflows.stream().filter(x->guid.equals(x.getGuid())).findFirst();
				if(!owf.isPresent())
				{
					String jsonstring = this.dataManager.getString(k);
					if(!jsonstring.equals(""))
					{
						Workflow wf = new Workflow();
						wf.setWorkflowFromJSON(jsonstring);
						allWorkflows.add(wf);
					}
				}
			}
		}
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
		
	}
	public void stopAllClusters()
	{
		TerminateJobFlowsRequest request =new TerminateJobFlowsRequest();
		for(Cluster c:clusters)
		{
		
		request.withJobFlowIds(c.getAwsID());
		}
		emr.terminateJobFlows(request);
		
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
			updateOnce();
		}
	}
	public void runWorkflow(Workflow wf)
	{
		//workflow is already in allWorkflows list
		//check for running cluster and give option to add to exsiting
		String wfString = wf.seraliseWorkflow();
		
		this.dataManager.uploadStringToFile("workflowJSON/"+wf.getGuid()+".txt",wfString,"clustercolombia","plain/text");
		
		Cluster newclus = new Cluster();
		newclus.addWorkflow(getWorkflow(wf.getCreationDate()));
		newclus.setName("Cluster with workflow: "+wf.getName());
		addCluster(newclus);
		runCluster(newclus);
		
	}
	public void addWorkflowToCluster(Workflow wf)
	{
		AddJobFlowStepsRequest req = new AddJobFlowStepsRequest();
		List<Cluster> active = clusterActive();
		String id = active.get(0).getAwsID();
		for(Cluster c : active) {
			if(c.getStatus().equals("WAITING"))
			{
				id = c.getAwsID();
			}
		}
		req.withJobFlowId(id);
		List<StepConfig> stepConfigs = new ArrayList<StepConfig>();
		//need to change output if wf guid matches
		wf.generateNewOutputFolder();
		this.dataManager.uploadStringToFile("workflowJSON/"+wf.getGuid()+".txt",wf.seraliseWorkflow(),"clustercolombia","plain/text");
		stepConfigs.add(wf.getStepConfig());
		req.withSteps(stepConfigs);
		AddJobFlowStepsResult result = emr.addJobFlowSteps(req);
		
	}
	private void monitorWorkflow(String id)
	{
		DescribeClusterRequest desc = new DescribeClusterRequest().withClusterId(id);
		Runnable r = new Runnable() {
			public void run() {
				boolean flag = true;	
				int i = 0;
				while(flag){
					i++;
					System.out.println("Thread started... Counter ==> " + i);
					DescribeClusterResult clusterResult = emr.describeCluster(desc);
				      com.amazonaws.services.elasticmapreduce.model.Cluster cluster = clusterResult.getCluster();
				      String status = cluster.getStatus().getState();
				      System.out.printf("Status: %s\n", status);
				      //STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
				      if(status.equals(ClusterState.TERMINATED.toString()) || status.equals(ClusterState.TERMINATED_WITH_ERRORS.toString())) {
				        break;
				      }
				      if(status.equals(ClusterState.WAITING)) {
				    	  break;
				      }
				      try {
				        TimeUnit.SECONDS.sleep(45);
				      } catch (InterruptedException e) {
				        e.printStackTrace();
				      }
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
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
			updateOnce();
		}
	}
	public Workflow getWorkflow(DateTime timecreated)
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
	public void updateWorkflowList()
	{
		
		//update the observable
		monitorWorkflowData.setAll(allWorkflows);
	}
	public void addWorkflow(Workflow wf)
	{
		this.allWorkflows.add(wf);
		monitorWorkflowData.setAll(allWorkflows);
	}
	public void removeWorkflow(Workflow wf)
	{
		this.allWorkflows.remove(wf);
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
