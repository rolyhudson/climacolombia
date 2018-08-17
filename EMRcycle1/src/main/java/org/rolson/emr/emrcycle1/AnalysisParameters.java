package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.DateTime;

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
	private DateTime start;
	private DateTime end;
	private int seasonStartMonth;
	private int seasonStartDay;
	private int seasonEndMonth;
	private int seasonEndDay;
	private int dayStartHour;
	private int dayEndHour;
	public AnalysisParameters()
	{
		setDefaults();
	}
	
	private void setDefaults()
	{
		dataset = Dataset.MONTHLY_GRID;
		analysisMethod = AnalysisMethod.K_MEANS;
		variables = Arrays.asList(Variables.TEMPERATURE,Variables.RELATIVE_HUMIDITY,Variables.WIND_SPEED);	
		
		this.start = new DateTime(2008, 1, 1, 0, 0, 0, 0);
		this.end = new DateTime();
		this.seasonStartMonth = 1;
		this.seasonStartDay = 1;
		this.seasonEndMonth =12;
		this.seasonEndDay=31;
		this.dayStartHour=1;
		this.dayEndHour=24;
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
	public void setOneVariable(int i,String aVariable)
	{
		variables.set(i, Variables.valueOf(aVariable));
	}
	public String getAnalysisMethod()
	{
		return this.analysisMethod.toString();
	}
	public void setStartDate(DateTime dt)
	{
		this.start = dt;
	}
	public DateTime getStartDate()
	{
		return this.start;
	}
	public void setEndDate(DateTime dt)
	{
		this.end = dt;
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
	public DateTime getEndDate()
	{
		return this.end;
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
