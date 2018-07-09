package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.ListClustersResult;

public class WorkflowCoordinator {
	private List<Workflow> workflows;
	private AmazonElasticMapReduceClient emr;
	public WorkflowCoordinator()
	{
		//getWorkflowsFromAWS();
		this.workflows = new ArrayList<Workflow>();
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
	public AmazonElasticMapReduce getClient()
	{
		return emr;
	}
	public void getWorkflowFromAWS(String clusterID)
	{
		DescribeClusterResult clusterinfo = emr.describeCluster(new DescribeClusterRequest().withClusterId(clusterID));
		Workflow wf = new Workflow(clusterinfo.getCluster().getName());
		workflows.add(wf);
	}
	public void runWorkflow(String name)
	{
		setEMRClient();
		//get the workflow by name
		Workflow wf = getWorkflow(name);
		if(wf!=null)
		{
			System.out.println("Starting workflow "+name);
			wf.setUpRequest();
		wf.result = emr.runJobFlow(wf.request);
		wf.status = "starting";
		}
		
	}
	public Workflow getWorkflow(String name)
	{
		Optional<Workflow> matches = workflows.stream()
				.filter(t -> t.name ==name)
				.findAny(); 
		if(matches.isPresent())
		{
			Workflow wf = matches.get();
			return wf;
		}
		else
		{
			return null;
		}
	}
	public void addPredfined(String testtype)
	{
		Workflow wf;
		switch(testtype)
		{
		case "Hadoop Map Reduce":
			wf = new Workflow(testtype);
			//use the default wf settings
			break;
		case "K-means clustering":
			wf = new Workflow(testtype);
			//wf.sparkTestVariables();
			//configure for kmeans
			break;
		case "Linear Regression":
			wf = new Workflow(testtype);
			//configure for linear classification
			break;
		case "Message log agregator":
			wf = new Workflow(testtype);
			//configure for ...
			wf.messageLogAgregator();
			//configure for linear classification
			break;
		case "Monthly records totals":
			wf = new Workflow(testtype);
			//configure for ...
			wf.monthlyResultsConfig();
			//configure for linear classification
			break;
			default:
				wf = new Workflow(testtype);
				break;
		}
		this.workflows.add(wf);
	}
	
	public List<Workflow> getWorkflows()
	{
		return workflows;
	}
}
