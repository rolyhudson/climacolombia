package org.rolson.emr.emrcycle1;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;

import com.amazonaws.services.elasticmapreduce.model.*;


public class Cluster {
	private String name;
	private String masterInstance;
	private String slaveInstance;
	private String logUri;
	private String ec2KeyName;
	private String serviceRole;
	private String jobFlowRole;
	private String subnetID;
	private String status; 
	private String releaseLabel;
	private String awsID;
	private RunJobFlowResult result;
	private RunJobFlowRequest request;
	private StepConfig stepConfig;
	private List<StepConfig> allSteps;
	private Collection<Application> applications;
	private List<Workflow> workflows;
	private DateTime creationDate;
	public Cluster() {
		workflows = new ArrayList<Workflow>();
		applications = new ArrayList<Application>();
		defaultVariables();
		stepConfig=new StepConfig();
	}
	public DateTime getCreationDate()
	{
		return this.creationDate;
	}
	public RunJobFlowResult getResult()
	{
		return result;
	}
	public void setResult(RunJobFlowResult rjfResult)
	{
		result = rjfResult;
		setAwsID(result.getJobFlowId());
	}
	public RunJobFlowRequest getRequest()
	{
		return request;
	}
	public void setRequest()
	{
		//set request for new cluster
		//note with steps can be a list of steps
		this.request = new RunJobFlowRequest()
		       .withName(this.name)
		       .withReleaseLabel(this.releaseLabel)
		       .withApplications(this.applications)
		       .withSteps(this.stepConfig)
		       .withLogUri(this.logUri)
		       .withServiceRole(this.serviceRole)
		       .withJobFlowRole(this.jobFlowRole)
		       .withInstances(new JobFlowInstancesConfig()
		           .withEc2KeyName(this.ec2KeyName)
		           .withInstanceCount(5)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType(this.masterInstance)
		           .withSlaveInstanceType(this.slaveInstance)
		           .withEc2SubnetId(this.subnetID));
	}
	public List<Workflow> getWorkflows()
	{
		return workflows;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String newname)
	{
		name = newname;
	}
	public String getAwsID()
	{
		return awsID;
	}
	public void setAwsID(String id)
	{
		awsID = id;
	}
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String newstatus)
	{
		status = newstatus;
	}
	private void defaultVariables()
	{
		this.masterInstance = "m4.large";
		this.slaveInstance = "m4.large";
		this.logUri = "s3://rolyhudsontestbucket1/climateData";
		this.ec2KeyName = "monday";
		this.serviceRole = "EMR_DefaultRole";
		this.jobFlowRole = "EMR_EC2_DefaultRole";
		this.subnetID = "subnet-da059bd5";
		this.status = "NEW"; 
		this.releaseLabel = "emr-5.14.0";
		
	}
	public void addPredfined(String testtype)
	{
		Workflow wf;
		switch(testtype)
		{
		
		case "K-means clustering":
			wf = new Workflow(testtype);
			wf.sparkClimateCluster();
			break;
		case "Linear Regression":
			wf = new Workflow(testtype);
			//configure for linear classification
			break;
		
			default:
				wf = new Workflow(testtype);
				break;
		}
		addWorkflow(wf);
		
	}
	public void addWorkflow(Workflow wf)
	{
		this.stepConfig = wf.getStepConfig();
		this.applications.add(wf.getApplication());
		this.workflows.add(wf);
		setRequest();
		//workflowUpdate();
	}
	public Workflow getWorkflow(String name)
	{
		Optional<Workflow> matches = workflows.stream()
				.filter(t -> t.getName() ==name)
				.findAny(); 
		if(matches.isPresent())
		{
			Workflow clus = matches.get();
			return clus;
		}
		else
		{
			return null;
		}
	}
}

