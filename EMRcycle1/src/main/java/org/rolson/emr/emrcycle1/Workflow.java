package org.rolson.emr.emrcycle1;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.amazonaws.services.elasticmapreduce.model.*;



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
	private String Guid;
	private StepConfig stepConfig;
	private DateTime creationDate;
	private AnalysisParameters analysisParameters;
	private ActionOnFailure actionOnFailure;
	
	private List<String> commandArgs = new ArrayList<String>();
	
	public Workflow()
	{
		//create the analysis parameters
		defaultVariables();
		this.analysisParameters = new AnalysisParameters();
		setWorkflowFromAnalysisParams();
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
	
	public void copyWorkflow(Workflow toCopy)
	{
		this.name = toCopy.getName();
		this.Guid = toCopy.getGuid();
		this.status = toCopy.getStatus();
		this.actionOnFailure = toCopy.getActionOnFailure();
		this.analysisParameters = toCopy.getAnalysisParameters();
		this.setAwsID(toCopy.getAwsID());
		this.creationDate = toCopy.getCreationDate();
		this.analysisJAR = toCopy.getAnalysisJar();
		this.mainClassInJAR = toCopy.getMainClass();
		this.commandArgs = toCopy.getCommandArgs();
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
	public void setWorkflowFromJSON(String jsontext)
	{
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addDeserializer(Workflow.class, new WorkflowDeserializer());
			mapper.registerModule(module);
			copyWorkflow( mapper.readValue(jsontext, Workflow.class));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String seraliseWorkflow()
	{
		String serialized = "";
		ObjectMapper mapper = new ObjectMapper();

		mapper.registerModule(new JodaModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		try {
			
			serialized = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(serialized);
		return serialized;
	}
	public String getGuid()
	{
		return this.Guid;
	}
	public void setGuid(String uid)
	{
		this.Guid = uid;
	}
	public void generateNewGuid()
	{
		this.Guid = UUID.randomUUID().toString();
	}
	public String getAnalysisJar()
	{
		return this.analysisJAR;
	}
	public String getMainClass()
	{
		return this.mainClassInJAR;
	}
	public ActionOnFailure getActionOnFailure()
	{
		return this.actionOnFailure;
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
	public String getName()
	{
		return name;
	}
	public void setAwsID(String id)
	{
		awsID =id;
	}
	public void setName(String n)
	{
		name =n;
		this.debugName = this.name+" debug"; 
		this.outputFolder = "s3://clustercolombia/results/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
	}
	public void setStatus(String s)
	{
		status = s;
	}
	public void setApplication(Application app)
	{
		this.appType = app;
	}
	public void setAppTypeName(String apptype)
	{
		this.appType.setName(apptype);
	}
	public void setDebugName(String n)
	{
		this.debugName = n;
	}
	public void setDataSource(String source)
	{
		this.dataSource = source;
	}
	public void setOutputFolder(String folder)
	{
	this.outputFolder = folder;
	}
	public void setAnalysisJar (String jar)
	{
	this.analysisJAR = jar;
	}
	public void setSaprkJar(String jar)
	{
		this.sparkJAR = jar;
	}
	public void setMainClassInJar(String mainclass)
	{
		this.mainClassInJAR = mainclass;
	}
	public void setStepCongfig(StepConfig config)
	{
		this.stepConfig = config;
	}
	public void setCreationDate(DateTime date)
	{
		this.creationDate =date;
	}
	public void setAnalysisParameters(AnalysisParameters ap)
	{
		this.analysisParameters =ap;
	}
	public void setActionOnFailure(String action)
	{
		this.actionOnFailure = ActionOnFailure.valueOf(action);
	}
	public void setCommandArgs(List<String> args)
	{
		this.commandArgs =args;
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
		this.name = "New workflow";
		this.status = "INITIALISED";
		this.setAwsID("undefined");
		this.creationDate = new DateTime();
		this.actionOnFailure = ActionOnFailure.CANCEL_AND_WAIT;
		this.appType = new Application();
		this.appType.setName("Spark");
		this.Guid = UUID.randomUUID().toString();
		
	}
	public void setWorkflowFromAnalysisParams()
	{
		if(this.analysisParameters.getDataSet().equals("MONTHLY_GRID"))
		{
			//source monthly not avaialble yet
			this.dataSource = "s3://clustercolombia/data/monthly.csv";
		}
		else
		{
			this.dataSource = "s3://clustercolombia/data/flatdata.csv";;
		}
		this.analysisJAR = "command-runner.jar";
		switch(this.analysisParameters.getAnalysisMethod())
		{
		case "K_MEANS":
			this.mainClassInJAR = "climateClusters.Clustering";
			this.sparkJAR = "s3://clustercolombia/sparkJAR/climateClusters2.jar";
			break;
		case "BI_K_MEANS":
			break;
		case "POWER_ITERATION":
			break;
		case "GAUSSIAN_MIX":
			break;
		}
		this.debugName = this.name+" debug"; 
		this.outputFolder = "s3://clustercolombia/results/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		//some how the space and time params need to be passed to args
		this.commandArgs = Arrays.asList("spark-submit",
				"--deploy-mode",
				"cluster",
				"--class",
				this.mainClassInJAR,
				this.sparkJAR,
				this.dataSource,
				this.outputFolder,
				this.Guid);
		
		setSparkStepConfig();
	}
	private void mapReduceVariables()
	{
		//setup for basic MapReduce job
		this.name = "MapReduce Station Counter";
		this.status = "INITIALISED";
		this.setAwsID("undefined");
		this.dataSource = "s3://clustercolombia/data/VV50.txt";
		this.outputFolder = "s3://clustercolombia/results/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "s3://clustercolombia/data/stationAnalysis.jar";
		this.debugName = "Hadoop MR NOAA Counting"; 
		this.mainClassInJAR = "org.rolson.mapreduce.mapreduce2.StationAnalysisDriver";
		this.actionOnFailure = ActionOnFailure.CANCEL_AND_WAIT;
		this.commandArgs = Arrays.asList(dataSource,outputFolder);
		this.appType = new Application();
		this.appType.setName("Map Reduce");
		
		this.creationDate = new DateTime();
		this.analysisParameters = new AnalysisParameters();
		setStepConfig();
	}
	
	public void sparkClimateCluster()
	{
		this.name = "Spark climate clustering with kmeans";
		this.debugName = "Spark test debug"; 
		this.dataSource = "s3://clustercolombia/data/flatdata.csv";
		this.outputFolder = "s3://clustercolombia/results/"+generateUniqueOutputName(this.name+"_output_", new DateTime());
		this.analysisJAR = "command-runner.jar";
		this.mainClassInJAR = "climateClusters.Clustering";
		this.sparkJAR = "s3://clustercolombia/sparkJAR/climateClusters2.jar";
		this.commandArgs = Arrays.asList("spark-submit",
				"--deploy-mode",
				"cluster",
				"--class",
				this.mainClassInJAR,
				this.sparkJAR,
				this.dataSource,
				this.outputFolder);
		this.appType = new Application();
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
