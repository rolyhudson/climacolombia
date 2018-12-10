package Coordination;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;

import com.amazonaws.services.elasticmapreduce.model.*;

import Workflowbuilder.Workflow;


public class Cluster {
	private String name;
	private String masterInstance;
	private int instances;
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
	public List<String>instanceTypes=Arrays.asList("m4.large","m4.xlarge","m4.2xlarge","m4.4xlarge","m4.10xlarge","m4.16xlarge",
			"m5.xlarge","m5.2xlarge","m5.4xlarge","m5.12xlarge","m5.24xlarge","m5d.xlarge","m5d.2xlarge","m5d.4xlarge","m5d.12xlarge","m5d.24xlarge",
			"c4.large","c4.xlarge","c4.2xlarge","c4.4xlarge","c4.8xlarge",
			"c5.xlarge","c5.2xlarge","c5.4xlarge","c5.9xlarge","c5.18xlarge","c5d.xlarge","c5d.2xlarge","c5d.4xlarge","c5d.9xlarge","c5d.18xlarge",
			"r3.xlarge","r3.2xlarge","r3.4xlarge","r3.8xlarge",
			"r4.xlarge","r4.2xlarge","r4.4xlarge","r4.8xlarge","r4.16xlarge",
			"r5.xlarge","r5.2xlarge","r5.4xlarge","r5.12xlarge","r5d.xlarge","r5d.2xlarge","r5d.4xlarge","r5d.12xlarge","r5d.24xlarge",
			"i3.xlarge","i3.2xlarge","i3.4xlarge","i3.8xlarge","i3.16xlarge",
			"d2.xlarge","d2.2xlarge","d2.4xlarge","d2.8xlarge",
			"p2.xlarge","p2.8xlarge","p2.16xlarge","p3.2xlarge","p3.8xlarge","p3.16xlarge");
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
		           .withInstanceCount(this.instances)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType(this.masterInstance)
		           .withSlaveInstanceType(this.masterInstance)
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
	public void setMasterInstance(String master) {
		this.masterInstance= master;
	}
	public String getMasterInstance() {
		return this.masterInstance;
	}
	public void setInstances(int n) {
		this.instances=n;
	}
	public int getInstances() {
		return this.instances;
	}
	private void defaultVariables()
	{
		this.masterInstance = "m4.large";
		this.instances = 4;
		this.logUri = "s3://clustercolombia/logs";
		this.ec2KeyName = "monday";
		this.serviceRole = "EMR_DefaultRole";
		this.jobFlowRole = "EMR_EC2_DefaultRole";
		this.subnetID = "subnet-da059bd5";
		this.status = "NEW"; 
		this.releaseLabel = "emr-5.14.0";
		
	}
	
	public void addWorkflow(Workflow wf)
	{
		this.stepConfig = wf.getStepConfig();
		this.applications.add(wf.getApplication());
		this.workflows.add(wf);
		setRequest();
		
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

