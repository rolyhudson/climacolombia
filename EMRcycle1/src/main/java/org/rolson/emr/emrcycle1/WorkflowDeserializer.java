package org.rolson.emr.emrcycle1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.Application;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class WorkflowDeserializer extends StdDeserializer<Workflow>{
	 public WorkflowDeserializer() { 
	        this(null); 
	    } 
	 
	    public WorkflowDeserializer(Class<?> vc) { 
	        super(vc); 
	    }
	 
	    @Override
	    public Workflow deserialize(JsonParser jp, DeserializationContext ctxt) 
	      throws IOException, JsonProcessingException {
	    	//get the nodes for each object
	        JsonNode workflownode = jp.getCodec().readTree(jp);
	        JsonNode analysisParamsNode = workflownode.get("analysisParameters");
	        JsonNode variables = analysisParamsNode.get("variablesAsString");
	        JsonNode args = workflownode.get("commandArgs");
	        JsonNode applicationNode = workflownode.get("application");
	        JsonNode stepConfigNode = workflownode.get("stepConfig");
	        JsonNode hadoopJarStepNode = stepConfigNode.get("hadoopJarStep");
	        //set the workflow up
	        Workflow wf = new Workflow();
	        wf.setName(workflownode.get("name").asText());
	        wf.setAwsID(workflownode.get("awsID").asText());	
	        wf.setStatus(workflownode.get("status").asText());
	        wf.setCreationDate(new DateTime(workflownode.get("creationDate").asText()));
	        wf.setOutputFolder(workflownode.get("outputFolder").asText());
	        wf.setActionOnFailure(stepConfigNode.get("actionOnFailure").asText());
	        wf.setAnalysisJar(hadoopJarStepNode.get("jar").asText());
	        	if(args.isArray())
	        	{
	        		List<String> comargs = new ArrayList<String>();
	        		for(JsonNode objNode : args)
			        {
			        	comargs.add(objNode.asText());
			        }
	        		wf.setCommandArgs(comargs);
	        	}
	        //set the analysis parameters
	        AnalysisParameters aparams = new AnalysisParameters();
	        aparams.setAnalysisMethod(analysisParamsNode.get("analysisMethod").asText());
	        aparams.setDataSet(analysisParamsNode.get("dataSet").asText());
	        aparams.setDayEndHour(analysisParamsNode.get("dayEndHour").asInt());
	        aparams.setDayStartHour(analysisParamsNode.get("dayStartHour").asInt());
//	        aparams.setEndDate(new LocalDate(analysisParamsNode.get("endDate").asText()));
//	        aparams.setStartDate(new LocalDate(analysisParamsNode.get("endDate").asText()));
	        
	        if (variables.isArray()) {
	        	int count =0;
		        for(JsonNode objNode : variables)
		        {
		        	aparams.setOneVariable(count,objNode.asText());
		        	count++;
		        }
	        }
	        aparams.setSeasonEndDay(analysisParamsNode.get("seasonEndDay").asInt());
	        aparams.setSeasonEndMonth(analysisParamsNode.get("seasonEndMonth").asInt());
	        aparams.setSeasonStartDay(analysisParamsNode.get("seasonStartDay").asInt());
	        aparams.setSeasonStartMonth(analysisParamsNode.get("seasonStartMonth").asInt());
	        wf.setAnalysisParameters(aparams);
	        //set the application
	        Application app = new Application();
	        app.setName(applicationNode.get("name").asText());
	        wf.setApplication(app);
	    	return wf;
	    }
}
