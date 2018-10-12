package org.rolson.emr.emrcycle1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

public class GeoVisualisation {
	Workflow workflow;
	private String dataSourceUri;
	private String outputfolder;
	private DataManager datamanager;
	private String datakey;
	private String dataBucket;
	
	public GeoVisualisation(Workflow wf)
	{
		this.dataBucket = "clustercolombia";
		workflow= wf;
		this.dataSourceUri = wf.getOutputFolder();
		
		datamanager = new DataManager();
		outputfolder = wf.getAnalysisParameters().getDashboardFolder();

		transferResultObjects();
		//move the index file
		datamanager.copyMove(this.dataBucket, "lacunae.io", "data/geovistemplates/index.html", outputfolder+"/index.html");
		//move and rename the workflowfile
		this.datamanager.uploadStringToFile(outputfolder+"/parameters.txt", workflow.seraliseWorkflow(),"lacunae.io","plain/text");
	}
	
	
	private void dumpResults(List<String> resultsObjects) {
		PrintWriter writer;
		try {
			writer = new PrintWriter("C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\s3Found.txt", "UTF-8");
			for(String s:resultsObjects) {
				writer.println(s);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private List<String> getTestList() throws IOException{
		List<String> lines = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\testObjects.txt"))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        lines.add(line);
		        line = br.readLine();
		    }
		   
		}
		return lines;
	}
	private void transferResultObjects() {
		String prefixOutput = "results"+workflow.getOutputFolder().substring(workflow.getOutputFolder().lastIndexOf('/'));
		List<String> resultsObjects = datamanager.listBucketContentsPrefixedV2(prefixOutput);
		//List<String> resultsObjects = new ArrayList<String>();
//		try {
//			resultsObjects = getTestList();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		List<String> jsonToCombine = new ArrayList<String>();
		dumpResults(resultsObjects);
		
		String rootKey="";
		String rootKeyPrev="";
		String filename ="";
		String resultKey ="";
		String resultPath ="";
		for(String line:resultsObjects) {
			filename = line.substring(line.lastIndexOf('/')+1);
			rootKey = line.substring(0,line.lastIndexOf('/')+1);
			resultKey=line.substring(prefixOutput.length());
			resultPath = resultKey.substring(0,resultKey.lastIndexOf('/'));
			if(filename.equals("_SUCCESS")) continue;
			if(filename.contains(".crc")) continue;
			if(filename.contains("part"))
			{
				if(rootKey.equals(rootKeyPrev))
				{
					jsonToCombine.add(line);
				}
				else {
					//process the previous json set
					if(jsonToCombine.size()>0)combineJSON(jsonToCombine,outputfolder,prefixOutput);
					jsonToCombine = new ArrayList<String>();
					jsonToCombine.add(line);
				}
			}

			rootKeyPrev=rootKey;
		}
		//add the last group
		if(jsonToCombine.size()>1)combineJSON(jsonToCombine,outputfolder,prefixOutput);
	}
	private void combineJSON(List<String> jsonToCombine,String destinationkeypath,String keyprefix) {
		StringBuilder sb = new StringBuilder();
		String resultPath = jsonToCombine.get(0).substring(keyprefix.length(),jsonToCombine.get(0).lastIndexOf('/'));
		for(String key : jsonToCombine) {
			try {
				List<String> lines = datamanager.readFromS3(key);
				for(String l :lines) {
					sb.append(l+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		datamanager.uploadStringToFile(destinationkeypath+resultPath+"/clusters.json",sb.toString(),"lacunae.io","application/json");
	}
	
	public void setDataSourceUri(String uri)
	{
		this.dataSourceUri = uri;
	}
	public String getDataKey()
	{
		return this.datakey;
	}
	public String getDataBucket()
	{
		return this.dataBucket;
	}
	private boolean resultsExist()
	{
		boolean exists = false;
		
		return exists;
	}
	public void extractBucketAndKey()
	{
		String bits[] =this.dataSourceUri.split("/");
		this.dataBucket = bits[2];
		StringBuilder key = new StringBuilder();
		for(int i=3;i<bits.length;i++)
		{
			key.append(bits[i]);
			if(i<bits.length-1)
			{
				key.append("/");
			}
		}
		this.datakey = key.toString();
	}
	//check results exist
	//gte the result
	//move results to lacunae.io
	//in folder with datasourcename
	//copy html and js to folder
	//set results to public access
	//generate html with ref to results
	//put html in s3 with public access
	//return url to gui
}
