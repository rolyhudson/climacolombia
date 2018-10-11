package org.rolson.emr.emrcycle1;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;

import netscape.javascript.JSObject;

public class AnalysisParameters {
	public enum Dataset {
	    MONTHLY_GRID,
	    HOURLY_CITIES;
	}
	public enum AnalysisMethod{
		K_MEANS,
		POWER_ITERATION,
		GAUSSIAN_MIX,
		BI_K_MEANS;
	}
	public enum Variables{
	RADIATION_SOLAR,
		  WIND_DIRECTION,
		  CLOUD_COVER,
		  TEMPERATURE,
		  VAPOUR_PRESSURE,
		  RELATIVE_HUMIDITY,
		  TEMP_MIN,
		  TEMP_MAX,
		  TEMP_RANGE,
		  PRECIPITATION,
		  WIND_SPEED,
		  ALTITUDE,
		  LATITUDE,
		  LONGITUDE,
		  NONE,
	}
	private Dataset dataset;
	private AnalysisMethod analysisMethod;
	private List<Variables> variables; 
	private LocalDate start;
	private LocalDate end;
	private int seasonStartMonth;
	private int seasonStartDay;
	private int seasonEndMonth;
	private int seasonEndDay;
	private int dayStartHour;
	private int dayEndHour;
	private List<double[]> selectionCoords;
	private String selectionShape;
	private int nClusters;
	private String masterInstance;
	private int instances;
	private String dashboardURL;
	public AnalysisParameters()
	{
		setDefaults();
	}
	private void setDefaults()
	{
		dataset = Dataset.HOURLY_CITIES;
		analysisMethod = AnalysisMethod.K_MEANS;
		variables = Arrays.asList(Variables.TEMPERATURE,Variables.RELATIVE_HUMIDITY,Variables.WIND_SPEED,
				Variables.NONE,Variables.NONE,Variables.NONE);	
		
		this.start = LocalDate.of(2004,3,12);
		this.end = LocalDate.of(2012,2,13);
		this.seasonStartMonth = 1;
		this.seasonStartDay = 1;
		this.seasonEndMonth =12;
		this.seasonEndDay=31;
		this.dayStartHour=8;
		this.dayEndHour=16;
		this.selectionShape = "polygon";
		this.instances = 4;
		this.masterInstance= "m4.large";
		this.dashboardURL="";
		this.selectionCoords = new ArrayList<double[]>();
		this.selectionCoords.add(new double[] {8.766635, -78.221568});
		this.selectionCoords.add(new double[] {1.024341, -79.778153});
		this.selectionCoords.add(new double[] {-4.519759, -69.824647});
		this.selectionCoords.add(new double[] {1.114824, -66.675034});
		this.selectionCoords.add(new double[] {6.280859, -67.190206});
		this.selectionCoords.add(new double[] {13.615007, -71.219707});
		this.nClusters=10;
	}
	public String getMasterInstance() {
		return this.masterInstance;
	}
	public void setMasterInstance(String master) {
		this.masterInstance = master;
	}
	public int getInstances() {
		return this.instances;
	}
	public void setInstances(int n) {
		this.instances=n;
	}
	public void setDataSet(String data)
	{
		this.dataset = Dataset.valueOf(data);
	}
	public String getDashboardURL()
	{
		return this.dashboardURL;
	}
	public void setDashboardURL(String url)
	{
		this.dashboardURL= url;
	}
	public String getDataSet()
	{
		return this.dataset.toString();
	}
	public List<String> getVariablesAsString()
	{
		List<String> vstr = new ArrayList<String>();
		for(Variables v : variables)
		{
			vstr.add(v.toString());
		}
		return vstr;
	}
	public void setAnalysisMethod(String aMethod)
	{
		
		this.analysisMethod = AnalysisMethod.valueOf(aMethod);
	}
	public void setSelectionCoords(List<LatLong> coords)
	{
		this.selectionCoords.clear();
		for(LatLong ll : coords)
		{
			this.selectionCoords.add(new double[] {ll.getLatitude(),ll.getLongitude()});
		}
		
	}
	public void setSelectionCoordsDouble(List<double[]> coords)
	{
		this.selectionCoords.clear();
		this.selectionCoords = coords;
		
	}
	@JsonIgnore
	public List<LatLong> getSelectionCoordsLatLon()
	{
		List<LatLong> coords = new ArrayList<LatLong>();
		for(double[] p:this.selectionCoords)
		{
			coords.add(new LatLong(p[0],p[1]));
		}
		return coords;
	}
	public List<double[]> getSelectionCoords()
	{
		return this.selectionCoords;
	}
	public void setOneVariable(int i,String aVariable)
	{
		variables.set(i, Variables.valueOf(aVariable));
	}
	public String getAnalysisMethod()
	{
		return this.analysisMethod.toString();
	}
	public void setStartDate(int y,int m,int d)
	{
		this.start = LocalDate.of(y, m, d);
	}
	public LocalDate getStartDate()
	{
		return this.start;
	}
	
	public LocalDate getEndDate()
	{
		return this.end;
	}
	public void setSelectionShape(String shape)
	{
		this.selectionShape =shape;
	}
	public String getSelectionShape()
	{
		return this.selectionShape;
	}
	public void setEndDate(int y,int m,int d)
	{
		this.end = LocalDate.of(y, m, d);
	}
	public void setSeasonStartMonth(int i)
	{
		this.seasonStartMonth=i;
	}
	public int getSeasonStartMonth()
	{
		return this.seasonStartMonth;
	}
	public void setSeasonEndMonth(int i)
	{
		this.seasonEndMonth=i;
	}
	public int getSeasonEndMonth()
	{
		return this.seasonEndMonth;
	}
	public void setSeasonStartDay(int i)
	{
		this.seasonStartDay=i;
	}
	public int getSeasonStartDay()
	{
		return this.seasonStartDay;
	}
	public void setSeasonEndDay(int i)
	{
		this.seasonEndDay=i;
	}
	public int getSeasonEndDay()
	{
		return this.seasonEndDay;
	}
	public int getDayStartHour()
	{
		return this.dayStartHour;
	}
	public void setDayStartHour(int i)
	{
		this.dayStartHour = i;
	}
	public int getDayEndHour()
	{
		return this.dayEndHour;
	}
	public void setDayEndHour(int i)
	{
		this.dayEndHour = i;
	}
	
	public static List<String> enumToStringDataset()
	{
		List<String> enumNames = Stream.of(Dataset.values())
                .map(Enum::name)
                .collect(Collectors.toList());
		return enumNames;
	}
	public static List<String> enumToStringVariables()
	{
		List<String> enumNames = Stream.of(Variables.values())
                .map(Enum::name)
                .collect(Collectors.toList());
		return enumNames;
	}
	public static List<String> enumToStringAnalysisMethod()
	{
		List<String> enumNames = Stream.of(AnalysisMethod.values())
                .map(Enum::name)
                .collect(Collectors.toList());
		return enumNames;
	}
	public void setNClusters(int k)
	{
		this.nClusters =k;
	}
	public int getNClusters() {
		return this.nClusters;
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
		
		return serialized;
	}
}
