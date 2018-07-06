package org.rolson.emr.emrcycle1;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.elasticmapreduce.model.*;

public class Workflow {
	public String name;
	public String dataSource = "s3://rolyhudsontestbucket1/climateData/VV.txt";
	public String outputFolder = "s3://rolyhudsontestbucket1/climateData";
	public String masterInstance = "m3.xlarge";
	public String slaveInstance = "m3.xlarge";
	public String logUri = "s3://rolyhudsontestbucket1/climateData";
	public String appType = "Hadoop";
	public RunJobFlowResult result;
	public String ec2KeyName = "monday";
	public String serviceRole = "EMR_DefaultRole";
	public String JobFlowRole = "EMR_EC2_DefaultRole";
	public String subnetID = "subnet-da059bd5";
	public String analysisJAR = "s3://rolyhudsontestbucket1/climateData/stationAnalysis.jar";
	public String debugName = "Hadoop MR NOAA Counting"; 
	public String mainClassInJAR = "org.rolson.mapreduce.mapreduce2.StationAnalysisDriver";
	public String status = "NEW"; 
	private  List<String> commandArgs;
	
	public Workflow(String workflowname)
	{
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
}
