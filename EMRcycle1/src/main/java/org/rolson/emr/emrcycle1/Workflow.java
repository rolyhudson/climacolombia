package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.elasticmapreduce.model.*;

public class Workflow {
	private String name;
	private Application appType;
	private String debugName; 
	private String dataSource;
	private String outputFolder;
	private String analysisJAR;
	private String mainClassInJAR;
	private String status;
	private String awsID;
	private StepConfig stepConfig;
	private ActionOnFailure actionOnFailure;
	
	private List<String> commandArgs;
	
	public Workflow(String workflowname)
	{
		commandArgs = new ArrayList<String>();
		appType = new Application();
		defaultVariables();
		this.name = workflowname;
	}
	
	public StepConfig getStepConfig()
	{
		return stepConfig;
	}
	public String getAwsID()
	{
		return awsID;
	}
	public void setAwsID(String id)
	{
		awsID =id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String n)
	{
		name =n;
	}
	public void setStatus(String s)
	{
		status = s;
	}
	public String getStatus()
	{
		return status;
	}
	public Application getApplication()
	{
		return appType;
	}
	public String getAppType()
	{
		return appType.getName();
	}
	public List<String> getCommandArgs(){
		return commandArgs;
	}
	private void defaultVariables()
	{
		//setup for basic MapReduce job
		this.name = "MapReduce Station Counter";
		this.dataSource = "s3://rolyhudsontestbucket1/climateData/VV50.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/climateData/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "s3://rolyhudsontestbucket1/climateData/stationAnalysis.jar";
		this.debugName = "Hadoop MR NOAA Counting"; 
		this.mainClassInJAR = "org.rolson.mapreduce.mapreduce2.StationAnalysisDriver";
		this.actionOnFailure = ActionOnFailure.TERMINATE_CLUSTER;
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
		this.appType.setName("Hadoop");
		setStepConfig();
	}
	public void monthlyResultsConfig()
	{
		this.name = "Monthly records totals";
		this.debugName = "Monthly records totals debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/climateData/VV50.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/climateData/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "s3://rolyhudsontestbucket1/climateData/monthlyrecords.jar";
		this.mainClassInJAR = "org.rolson.emr.groupStationByMonthYear.App";
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
		this.appType.setName("Hadoop");
		setStepConfig();
	}
	public void messageLogAgregator()
	{
		this.name = "Message log agregator";
		this.debugName = "Agregator debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/cookbookexamples/access_log_Jul95.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/cookbookexamples/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "s3://rolyhudsontestbucket1/cookbookexamples/messageSize.jar";
		this.mainClassInJAR = "org.rolson.emr.messageSize.App";
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
		this.appType.setName("Hadoop");
		setStepConfig();
	}
	public void sparkWordCount()
	{

		this.name = "Spark test";
		this.debugName = "Spark test debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/sparkTests/count.txt";
		this.outputFolder = "s3://rolyhudsontestbucket1/sparkTests/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "command-runner.jar";//"s3://rolyhudsontestbucket1/sparkTests/SparkWordCount2.jar";
		this.mainClassInJAR = "org.rolson.emr.sparkTest.SparkJob";
		this.commandArgs = Arrays.asList("spark-submit",
				"--deploy-mode",
				"cluster",
				"--class",
				"org.rolson.emr.sparkTest.SparkJob",
				"s3://rolyhudsontestbucket1/sparkTests/SparkWordCount2.jar",
				dataSource,
				outputFolder);
		this.appType.setName("Spark");
		setSparkStepConfig();
//		spark-submit --deploy-mode cluster --class org.rolson.emr.sparkTest.SparkJob s3://rolyhudsontestbucket1/sparkTests/SparkWordCount.jar s3://rolyhudsontestbucket1/sparkTests/count.txt s3://rolyhudsontestbucket1/sparkTests/output
//
//		AddJobFlowStepsRequest req = new AddJobFlowStepsRequest();
//		req.withJobFlowId("j-1K48XXXXXXHCB");
//
//		List<StepConfig> stepConfigs = new ArrayList<StepConfig>();
//				
//		HadoopJarStepConfig sparkStepConf = new HadoopJarStepConfig()
//					.withJar("command-runner.jar")
//					.withArgs("spark-submit","--executor-memory","1g","--class","org.apache.spark.examples.SparkPi","/usr/lib/spark/lib/spark-examples.jar","10");			
//				
//		StepConfig sparkStep = new StepConfig()
//					.withName("Spark Step")
//					.withActionOnFailure("CONTINUE")
//					.withHadoopJarStep(sparkStepConf);
//
//		stepConfigs.add(sparkStep);
//		req.withSteps(stepConfigs);
//		AddJobFlowStepsResult result = emr.addJobFlowSteps(req);
	}
	private void setSparkStepConfig()
	{
		this.stepConfig = new StepConfig()
			       .withName(this.name)
			       .withActionOnFailure(this.actionOnFailure)
			       .withHadoopJarStep(new HadoopJarStepConfig()
			           .withJar(this.analysisJAR)
			           .withArgs(this.commandArgs));
	}
	public static String generateUniqueOutputName(String prefix,DateTime timePoint)
	{
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss");
		String s = prefix+fmt.print(timePoint);
				//timePoint.format(formatter);
		return s;
	}
	private void setStepConfig()
	{
		this.stepConfig = new StepConfig()
	       .withName(this.name)
	       .withActionOnFailure(this.actionOnFailure)
	       .withHadoopJarStep(new HadoopJarStepConfig()
	           .withJar(this.analysisJAR)
	           .withArgs(this.commandArgs)
	           .withMainClass(this.mainClassInJAR));
	}
	
}
