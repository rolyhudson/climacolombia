package org.rolson.emr.emrcycle1;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class GeoVisualisation {
	Workflow workflow;
	private String dataSourceUri;
	private String outputfolder;
	private DataManager datamanager;
	private String datakey;
	private String dataBucket;
	private String visResultUri;
	public GeoVisualisation(Workflow wf)
	{
		workflow= wf;
		this.dataSourceUri = wf.getOutputFolder();
		int startYear=wf.getAnalysisParameters().getStartDate().getYear();
		int endYear=wf.getAnalysisParameters().getEndDate().getYear();
		int startMonth=wf.getAnalysisParameters().getSeasonStartMonth();
		int endMonth=wf.getAnalysisParameters().getSeasonEndMonth();
		int startHour =wf.getAnalysisParameters().getDayStartHour();
		int endHour =wf.getAnalysisParameters().getDayEndHour();
		datamanager = new DataManager();
		outputfolder = Workflow.generateUniqueOutputName("geovis",new DateTime());
		listResultObjects();
//		extractBucketAndKey();
//		if(datamanager.accessObject(this.dataBucket, this.datakey+"/part-00000"))
//		{
//		//file exists move it to public folder
//		
//		datamanager.copyMove(this.dataBucket, "lacunae.io", this.datakey+"/part-00000", outputfolder+"/results");
//		movecopyVisTemplates();
//		this.visResultUri = "www.lacunae.io/"+outputfolder;
//		}
		//get bucketname from uri
		//https://s3.amazonaws.com/rolyhudsontestbucket1/climateData/K-means+clustering_output_2018_07_24_13_29_48/part-00000
		//datamanager.accessObject();
	}
	private void listResultObjects() {
		String prefixOutput = "results"+workflow.getOutputFolder().substring(workflow.getOutputFolder().lastIndexOf('/'));
		List<String> resultsObjects = datamanager.listBucketContentsPrefixed(prefixOutput);
		List<String> jsonToCombine = new ArrayList<String>();
		
		String rootKey="";
		String rootKeyPrev="";
		String filename ="";
		for(String line:resultsObjects) {
			filename = line.substring(line.lastIndexOf('/')+1);
			rootKey = line.substring(0,line.lastIndexOf('/')+1);
			if(filename.equals("_SUCCESS")) continue;
			if(filename.equals("part-00000"))
			{
				//move file and continue
			}
			else
			{
				if(filename.contains(".json")){
					if(rootKey.equals(rootKeyPrev))
					{
						jsonToCombine.add(line);
					}
					else {
						//process the previous json set
						jsonToCombine = new ArrayList<String>();
						jsonToCombine.add(line);
					}
				}
				
			}
			rootKeyPrev=rootKey;
		}
	}
	public String getVisResultUri()
	{
		return this.visResultUri;
	}
	private void movecopyVisTemplates()
	{
		datamanager.copyMove(this.dataBucket, "lacunae.io", "climateData/geovistemplates/kmeansout.js", outputfolder+"/kmeansout.js");
		datamanager.copyMove(this.dataBucket, "lacunae.io", "climateData/geovistemplates/index.html", outputfolder+"/index.html");
		datamanager.copyMove(this.dataBucket, "lacunae.io", "climateData/geovistemplates/regionsTopo.json", outputfolder+"/regionsTopo.json");
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
