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
		TEMPERATURE,
		RELATIVE_HUMIDITY,
		WIND_SPEED,
		WIND_DIRECTION,
		RADIATION_SOLAR,
		CLOUD_COVER,
		PRECIPITATION,
		NONE;
	}
	private Dataset dataset;
	private AnalysisMethod analysisMethod;;
	private List<Variables> variables; 
	private LocalDate start;
	private LocalDate end;
	private int seasonStartMonth;
	private int seasonStartDay;
	private int seasonEndMonth;
	private int seasonEndDay;
	private int dayStartHour;
	private int dayEndHour;
	private List<LatLong> selectionCoords;
	private String selectionShape;
	public AnalysisParameters()
	{
		setDefaults();
	}
	
	private void setDefaults()
	{
		dataset = Dataset.HOURLY_CITIES;
		analysisMethod = AnalysisMethod.K_MEANS;
		variables = Arrays.asList(Variables.TEMPERATURE,Variables.RELATIVE_HUMIDITY,Variables.WIND_SPEED);	
		
		this.start = LocalDate.of(2010,3,12);
		this.end = LocalDate.of(2012,2,13);
		this.seasonStartMonth = 1;
		this.seasonStartDay = 1;
		this.seasonEndMonth =12;
		this.seasonEndDay=31;
		this.dayStartHour=8;
		this.dayEndHour=16;
		this.selectionShape = "polygon";
		LatLong p1 = new LatLong(8.766635, -78.221568);
		LatLong p2 = new LatLong(1.024341, -79.778153);
		LatLong p3 = new LatLong(-4.519759, -69.824647);
		LatLong p4 = new LatLong(1.114824, -66.675034);
		LatLong p5 = new LatLong(6.280859, -67.190206);
		LatLong p6 = new LatLong(13.615007, -71.219707);
		this.selectionCoords = new ArrayList<LatLong>();
		this.selectionCoords.add(p1);
		this.selectionCoords.add(p2);
		this.selectionCoords.add(p3);
		this.selectionCoords.add(p4);
		this.selectionCoords.add(p5);
		this.selectionCoords.add(p6);
	}
	public void setDataSet(String data)
	{
		this.dataset = Dataset.valueOf(data);
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
		this.selectionCoords = coords;
	}
	public List<LatLong> getSelectionCoords()
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
}
