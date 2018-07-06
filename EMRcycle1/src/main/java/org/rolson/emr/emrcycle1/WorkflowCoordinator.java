package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.ClusterSummary;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.ListClustersResult;

public class WorkflowCoordinator {
	private List<Workflow> workflows= new ArrayList<Workflow>();
	private AmazonElasticMapReduceClient emr;
	public WorkflowCoordinator()
	{
		//getWorkflowsFromAWS();
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
	private void getAllWorkflows()
	{
		
		ListClustersResult clusters = emr.listClusters();
		List<ClusterSummary> clusterlist = clusters.getClusters();
		 
		for(ClusterSummary cs: clusterlist)
		{
			Workflow wf = new Workflow(cs.getName());
			wf.status = cs.getStatus().getState();
			workflows.add(wf);
			
			//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
			//System.out.println("ID: "+cs.getId()+", name: "+cs.getName()+" status: "+cs.getStatus().getState());
			
		}
	}
	public List<Workflow> getWorkflows()
	{
		return workflows;
	}
}
