package climateClusters;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClusterParams {
	
	//this is all incoming with args
	private List<String> variables = new ArrayList<String>(); 
	private LocalDate start;
	private LocalDate end;
	private int seasonStartMonth;
	private int seasonStartDay;
	private int seasonEndMonth;
	private int seasonEndDay;
	private int dayStartHour;
	private int dayEndHour;
	private List<double[]> selectionCoords = new ArrayList<double[]>();
	private String jsontext;
	private String clusteringMethod;
	private int nclusters;
	public ClusterParams(String path,SparkSession spark) {
		
		Dataset<Row> jsondata = spark.read().json(path);
		Dataset<String> ap = jsondata.selectExpr("analysisParameters").toJSON();
		List<String> allap = ap.collectAsList();
		this.jsontext = allap.get(0);
		
		//getParamsFromJSON();
	}
	private void getParamsFromJSON() {
		JSONObject obj = new JSONObject(jsontext);
		JSONObject apObj = obj.getJSONObject("analysisParameters");
		this.seasonStartMonth = apObj.getInt("seasonStartMonth");
		this.seasonEndMonth = apObj.getInt("seasonEndMonth");
		this.seasonStartDay = apObj.getInt("seasonStartDay");
		this.seasonEndDay = apObj.getInt("seasonEndDay");
		this.dayStartHour = apObj.getInt("dayStartHour");
		this.dayEndHour = apObj.getInt("dayEndHour");
		this.clusteringMethod =apObj.getString("analysisMethod");
		this.nclusters = apObj.getInt("nclusters");
		JSONObject startObj = apObj.getJSONObject("startDate");
		this.start = new LocalDate(startObj.getInt("year"),startObj.getInt("monthValue"),startObj.getInt("dayOfMonth"));
		JSONObject endObj = apObj.getJSONObject("endDate");
		this.end = new LocalDate(endObj.getInt("year"),endObj.getInt("monthValue"),endObj.getInt("dayOfMonth"));
		
		JSONArray varObj = apObj.getJSONArray("variablesAsString");
		for(int i=0;i<varObj.length();i++)
		{
			this.variables.add(varObj.getString(i));
		}
		
		JSONArray coordObj = apObj.getJSONArray("selectionCoords");
		for(int i =0;i<coordObj.length();i++)
		{
			JSONArray c = coordObj.getJSONArray(i);
			double[] coord = {c.getDouble(0),c.getDouble(1)};
			this.selectionCoords.add(coord);
		}
	}
	public String getClusteringMethod() {
		return this.clusteringMethod;
	}
	public LocalDate getStartDate() {
		return this.start;
	}
	public LocalDate getEndDate() {
		return this.end;
	}
	public int getSeasonStartMonth() {
		return this.seasonStartMonth;
	}
	public int getSeasonEndMonth() {
		return this.seasonEndMonth;
	}
	public int getSeasonStartDay() {
		return this.seasonStartDay;
	}
	public int getSeasonEndDay() {
		return this.seasonEndDay;
	}
	public int getDayStartHour() {
		return this.dayStartHour;
	}
	public int getDayEndHour() {
		return this.dayEndHour;
	}
	public List<double[]> getSelectionCoords(){
		return this.selectionCoords;
	}
	public List<String> getVariables()
	{
		return this.variables;
	}
	public int getNClusters() {
		return this.nclusters;
	}
}
