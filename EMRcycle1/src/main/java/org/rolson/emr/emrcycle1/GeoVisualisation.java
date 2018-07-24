package org.rolson.emr.emrcycle1;

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
		datamanager = new DataManager();
		datamanager.setupClient();
		extractBucketAndKey();
		if(datamanager.accessObject(this.dataBucket, this.datakey+"/part-00000"))
		{
		//file exists move it to public folder
		outputfolder = Workflow.generateUniqueOutputName("geovis",new DateTime());
		datamanager.copyMove(this.dataBucket, "lacunae.io", this.datakey+"/part-00000", outputfolder+"/results");
		movecopyVisTemplates();
		this.visResultUri = "www.lacunae.io/"+outputfolder;
		}
		//get bucketname from uri
		//https://s3.amazonaws.com/rolyhudsontestbucket1/climateData/K-means+clustering_output_2018_07_24_13_29_48/part-00000
		//datamanager.accessObject();
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