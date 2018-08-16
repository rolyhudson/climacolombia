package org.rolson.emr.emrcycle1;

import java.io.IOException;
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
	        JsonNode workflownode = jp.getCodec().readTree(jp);
	        String name = workflownode.get("name").asText();
	        Workflow wf = new Workflow(name);
	        wf.setAwsID(workflownode.get("awsID").asText());	
	        wf.setStatus(workflownode.get("status").asText());
	        
	        wf.setOutputFolder(workflownode.get("outputFolder").asText());
	        wf.setCommandArgs(workflownode.findValuesAsText("commandArgs"));
	        
	        JsonNode analysisParamsNode = workflownode.get("analysisParameters");
	        AnalysisParameters aparams = new AnalysisParameters();
	        aparams.setAnalysisMethod(analysisParamsNode.get("analysisMethod").asText());
	        aparams.setDataSet(analysisParamsNode.get("dataSet").asText());
	        aparams.setDayEndHour(analysisParamsNode.get("dayEndHour").asInt());
	        aparams.setDayStartHour(analysisParamsNode.get("dayStartHour").asInt());
	        //aparams.setEndDate(analysisParamsNode.get("endDate").as);
	       //aparams.setStartDate(analysisParamsNode.get("endDate").as);
	        JsonNode variables = analysisParamsNode.get("variablesAsString");
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
	        
	        JsonNode applicationNode = workflownode.get("application");
	        Application app = new Application();
	        app.setName(applicationNode.get("name").asText());
	        wf.setApplication(app);
	        
	        JsonNode stepConfigNode = workflownode.get("stepConfig");
	        JsonNode hadoopJarStepNode = stepConfigNode.get("hadoopJarStep");
	        StepConfig config  = new StepConfig()
	        		.withName(stepConfigNode.get("name").asText())
	        		.withActionOnFailure(stepConfigNode.get("actionOnFailure").asText())
	        		.withHadoopJarStep(new HadoopJarStepConfig()
	        				.withJar(hadoopJarStepNode.get("jar").asText())
	        				.withArgs(wf.getCommandArgs()));		
	        wf.setStepCongfig(config);				
	    	
	    	return wf;
	    }
}
