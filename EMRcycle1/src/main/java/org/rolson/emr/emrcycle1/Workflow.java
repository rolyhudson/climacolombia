package org.rolson.emr.emrcycle1;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.elasticmapreduce.model.*;

public class Workflow {
	public String name;
	public String appType;
	public String debugName; 
	public String dataSource;
	public String outputFolder;
	public String analysisJAR;
	public String mainClassInJAR;
	
	public String masterInstance;
	public String slaveInstance;
	public String logUri;
	
	public String ec2KeyName;
	public String serviceRole;
	public String jobFlowRole;
	public String subnetID;
	
	public String status; 
	public String releaseLabel;
	
	
	public RunJobFlowResult result;
	public RunJobFlowRequest request;
	private ActionOnFailure actionOnFailure;
	
	private StepConfig stepConfig;
	private List<String> commandArgs;
	private Collection<Application> applications;
	public Workflow(String workflowname)
	{
	defaultVariables();
	this.name = workflowname;
	commandArgs = Arrays.asList(dataSource,outputFolder);
	}
	public String getName()
	{
		return name;
	}
	public List<String> getCommandArgs(){
		return commandArgs;
	}
	private void defaultVariables()
	{
		//setup for basic MapReduce job
		this.name = "MapReduce Station Counter";
		this.dataSource = "s3://rolyhudsontestbucket1/climateData/VV50.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/climateData/"+generateUniqueOutputName(this.name+"_output_", LocalDateTime.now());
		this.analysisJAR = "s3://rolyhudsontestbucket1/climateData/stationAnalysis.jar";
		this.debugName = "Hadoop MR NOAA Counting"; 
		this.mainClassInJAR = "org.rolson.mapreduce.mapreduce2.StationAnalysisDriver";
		this.actionOnFailure = ActionOnFailure.TERMINATE_CLUSTER;
		
		this.masterInstance = "m4.large";
		this.slaveInstance = "m4.large";
		this.logUri = "s3://rolyhudsontestbucket1/climateData";
		this.appType = "Hadoop";
		
		this.ec2KeyName = "monday";
		this.serviceRole = "EMR_DefaultRole";
		this.jobFlowRole = "EMR_EC2_DefaultRole";
		this.subnetID = "subnet-da059bd5";
		
		this.status = "NEW"; 
		this.releaseLabel = "emr-5.14.0";
	}
	public void monthlyResultsConfig()
	{
		this.name = "Monthly records totals";
		this.debugName = "Monthly records totals debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/climateData/VV50.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/climateData/"+generateUniqueOutputName(this.name+"_output_", LocalDateTime.now());
		this.analysisJAR = "s3://rolyhudsontestbucket1/climateData/monthlyrecords.jar";
		this.mainClassInJAR = "org.rolson.emr.groupStationByMonthYear.App";
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
	}
	public void messageLogAgregator()
	{
		this.name = "Message log agregator";
		this.debugName = "Agregator debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/cookbookexamples/access_log_Jul95.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/cookbookexamples/"+generateUniqueOutputName(this.name+"_output_", LocalDateTime.now());
		this.analysisJAR = "s3://rolyhudsontestbucket1/cookbookexamples/messageSize.jar";
		this.mainClassInJAR = "org.rolson.emr.messageSize.App";
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
	}
	public void sparkTestVariables()
	{
		Application app = new Application().withName("Spark");
		this.applications.add(app);
		this.name = "Spark test";
		this.debugName = "Spark test debug"; 
		this.dataSource = "";
		this.outputFolder = ""+generateUniqueOutputName(this.name+"_output_", LocalDateTime.now());
		this.analysisJAR = "";
		this.mainClassInJAR = "";
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
	}
	public static String generateUniqueOutputName(String prefix,LocalDateTime timePoint)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
		String s = prefix+timePoint.format(formatter);
		return s;
	}
	private void setStepConfig()
	{
		this.stepConfig = new StepConfig()
			       .withName(this.debugName)
			       .withActionOnFailure(this.actionOnFailure)
			       .withHadoopJarStep(new HadoopJarStepConfig()
			           .withJar(this.analysisJAR)
			           .withArgs(this.commandArgs)
			           .withMainClass(this.mainClassInJAR));
	}
	public void setUpRequest()
	{
		setStepConfig();  
		//set request for new cluster
		this.request = new RunJobFlowRequest()
		       .withName(this.name)
		       .withReleaseLabel(this.releaseLabel)
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
}
