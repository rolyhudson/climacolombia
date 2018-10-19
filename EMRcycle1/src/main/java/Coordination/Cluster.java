package Coordination;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.rolson.emr.emrcycle1.Workflow;

import com.amazonaws.services.elasticmapreduce.model.*;


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
	public List<String>instanceTypes=Arrays.asList("m1.small","m1.medium","m1.large","m1.xlarge","m3.xlarge","m3.2xlarge",
			"c1.medium","c1.xlarge","c3.xlarge","c3.2xlarge","c3.4xlarge","c3.8xlarge","cc1.4xlarge","cc2.8xlarge","c4.large","c4.xlarge","c4.2xlarge","c4.4xlarge","c4.8xlarge",
			"m2.xlarge","m2.2xlarge","m2.4xlarge","r3.xlarge","r3.2xlarge","r3.4xlarge","r3.8xlarge","cr1.8xlarge","m4.large","m4.xlarge","m4.2xlarge","m4.4xlarge","m4.10xlarge",
			"m4.16large","r4.large","r4.xlarge","r4.2xlarge","r4.4xlarge","r4.8xlarge","r4.16xlarge",
			"h1.4xlarge","hs1.2xlarge","hs1.4xlarge","hs1.8xlarge","i2.xlarge","i2.2xlarge","i2.4large","i2.8xlarge","d2.xlarge","d2.2xlarge","d2.4xlarge","d2.8xlarge",
			"g2.2xlarge","cg1.4xlarge");
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

