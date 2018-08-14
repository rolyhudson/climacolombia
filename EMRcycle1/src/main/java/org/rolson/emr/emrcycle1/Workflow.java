package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.elasticmapreduce.model.*;

import javafx.scene.control.Button;

public class Workflow {
	private String name;
	private Application appType;
	private String debugName; 
	private String dataSource;
	private String outputFolder;
	private String analysisJAR;
	private String sparkJAR;
	private String mainClassInJAR;
	private String status;
	private String awsID;
	private StepConfig stepConfig;
	private DateTime creationDate;
	private AnalysisParameters analysisParameters;
	private ActionOnFailure actionOnFailure;
	
	private List<String> commandArgs;
	
	public Workflow(String workflowname)
	{
		commandArgs = new ArrayList<String>();
		appType = new Application();
		defaultVariables();
		this.name = workflowname;
		this.status = "INITIALISED";
		this.setAwsID("undefined");
		this.creationDate = new DateTime();
		this.analysisParameters = new AnalysisParameters();
	}
	public Workflow(StepSummary step)
	{
		//workflow from stepsummary
		this.name = step.getName();
		this.status = step.getStatus().getState();
		switch(step.getActionOnFailure())
		{
			case "TERMINATE_CLUSTER":
				this.actionOnFailure = ActionOnFailure.TERMINATE_CLUSTER;
				break;
			case "CANCEL_AND_WAIT":
				this.actionOnFailure = ActionOnFailure.CANCEL_AND_WAIT;
				break;
			case "CONTINUE":
				this.actionOnFailure = ActionOnFailure.CONTINUE;
				break;
			case "TERMINATE_JOB_FLOW":
				this.actionOnFailure = ActionOnFailure.TERMINATE_JOB_FLOW;
				break;
				
		}
		this.setAwsID(step.getId());
		this.creationDate = new DateTime(step.getStatus().getTimeline().getCreationDateTime());
		this.analysisJAR = step.getConfig().getJar();
		this.mainClassInJAR = step.getConfig().getMainClass();
		this.commandArgs = step.getConfig().getArgs();
		if(this.commandArgs.size()==2)
		{
			//for previously defined hadoop map reduce jobs
			this.outputFolder = this.commandArgs.get(1);
			this.dataSource = this.commandArgs.get(0);
			this.appType = new Application();
			this.appType.setName("Hadoop Map Reduce");
			setStepConfig();
		}
		else
		{
			//otherwise a spark job with more args
			this.mainClassInJAR = this.commandArgs.get(4);
			this.outputFolder = this.commandArgs.get(7);
			this.dataSource = this.commandArgs.get(6);
			this.sparkJAR = this.commandArgs.get(5);
			this.appType = new Application();
			this.appType.setName("Spark");
			setSparkStepConfig();
		}
	}
	public AnalysisParameters getAnalysisParameters()
	{
		return this.analysisParameters;
	}
	public String getOutputFolder()
	{
		return this.outputFolder;
	}
	public DateTime getCreationDate()
	{
		return this.creationDate;
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
		this.actionOnFailure = ActionOnFailure.CANCEL_AND_WAIT;
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
		this.appType.setName("Spark");
		setStepConfig();
	}
	
	public void sparkClimateCluster()
	{
		//this.name = "Spark climate clustering with kmeans";
		this.debugName = "Spark test debug"; 
		this.dataSource = "s3://rolyhudsontestbucket1/climateData/flatClimateData.csv";
		this.outputFolder = "s3://rolyhudsontestbucket1/climateData/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "command-runner.jar";
		this.mainClassInJAR = "climateClusters.Clustering";
		this.sparkJAR = "s3://rolyhudsontestbucket1/climateData/climateClusters2.jar";
		this.commandArgs = Arrays.asList("spark-submit",
				"--deploy-mode",
				"cluster",
				"--class",
				this.mainClassInJAR,
				this.sparkJAR,
				dataSource,
				outputFolder);
		this.appType.setName("Spark");
		setSparkStepConfig();
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
