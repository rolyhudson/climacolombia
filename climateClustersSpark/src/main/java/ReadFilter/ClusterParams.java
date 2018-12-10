package ReadFilter;

import java.util.ArrayList;

import java.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;


public class ClusterParams {
	
	//this is all incoming with args
	private List<String> variables = new ArrayList<String>(); 
	private DateTime start;
	private DateTime end;
	private int seasonStartMonth;
	private int seasonStartDay;
	private int seasonEndMonth;
	private int seasonEndDay;
	private int dayStartHour;
	private int dayEndHour;
	private List<double[]> selectionCoords = new ArrayList<double[]>();
	private Dataset<Row> jsontext;
	private String clusteringMethod;
	private String dataset;
	private int nclusters;
	private String name;
	public ClusterParams(String path,SparkSession spark) {
		
		Dataset<Row> jsondata = spark.read().format("json").load(path);
		
		this.jsontext = jsondata.selectExpr("analysisParameters");
		this.name = getValue2(jsondata.selectExpr("name").toJSON().collectAsList());
		getParamsFromJSON();
	}
	private String getValue(String key)
	{
		List<String> keyvalue = jsontext.selectExpr("analysisParameters."+key).toJSON().collectAsList();
		
		return getValue2(keyvalue);
	}
	private String getValue2(List<String> keyvalue)
	{
		String pair = keyvalue.get(0);
		pair = pair.substring(1, pair.length()-1);
		String thekey = pair.substring(0,pair.indexOf(":"));
		String thevalue = pair.substring(pair.indexOf(":")+1).replace("\"", "");
		return thevalue;
	}
	private DateTime getDate(String key)
	{
		Dataset<Row> dateObj = jsontext.selectExpr("analysisParameters."+key);
		List<String> yearvalue = dateObj.selectExpr(key+".year").toJSON().collectAsList();
		List<String> monthvalue = dateObj.selectExpr(key+".monthValue").toJSON().collectAsList();
		List<String> dayvalue = dateObj.selectExpr(key+".dayOfMonth").toJSON().collectAsList();
		int y = Integer.parseInt(getValue2(yearvalue));
		int m = Integer.parseInt(getValue2(monthvalue));
		int d = Integer.parseInt(getValue2(dayvalue));
		return new DateTime(y,m,d,0,0,0);
	}
	private String[] getArray(String key) {
		String asArray = getValue(key);
		asArray = asArray.substring(1, asArray.length()-1);
		String[] parts = asArray.split(",");
		return parts;
	}
	private List<double[]> getCoordArray(String key) {
		String asArray2d = getValue(key);
		StringBuilder sb = new StringBuilder();
		List<double[]> coords = new ArrayList<double[]>();
		for(int i=0;i<asArray2d.length();i++)
		{
			if(asArray2d.charAt(i)=='[')
			{
				sb = new StringBuilder();
			}
			else
			{
				if(asArray2d.charAt(i)==']')
				{
					String pair = sb.toString();
					String[] parts = pair.split(",");
					double[] coord =  { Double.parseDouble(parts[0]),Double.parseDouble(parts[1]) };
					coords.add(coord);
				}
				else
				{
					sb.append(asArray2d.charAt(i));
				}
			}
		}
		return coords;
	}
	private void getParamsFromJSON() {
		
		this.seasonStartMonth = Integer.parseInt(getValue("seasonStartMonth"));
		this.seasonEndMonth = Integer.parseInt(getValue("seasonEndMonth"));
		this.seasonStartDay = Integer.parseInt(getValue("seasonStartDay"));
		this.seasonEndDay = Integer.parseInt(getValue("seasonEndDay"));
		this.dayStartHour = Integer.parseInt(getValue("dayStartHour"));
		this.dayEndHour = Integer.parseInt(getValue("dayEndHour"));
		this.clusteringMethod =(getValue("analysisMethod"));
		this.dataset = getValue("dataSet");
		this.nclusters = Integer.parseInt(getValue("nclusters"));
		this.start = getDate("startDate");
		this.end = getDate("endDate");

		String[] varObj = getArray("variablesAsString");
		for(int i=0;i<varObj.length;i++)
		{
			this.variables.add(varObj[i]);
		}
		this.selectionCoords = getCoordArray("selectionCoords");

	}
	public String getDataset() {
		return this.dataset;
	}
	public String getName() {
		return this.name;
	}
	public String getClusteringMethod() {
		return this.clusteringMethod;
	}
	public DateTime getStartDate() {
		return this.start;
	}
	public DateTime getEndDate() {
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
