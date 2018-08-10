package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private List<Variables> variables = new ArrayList<Variables>();
	private LocalDate start;
	private LocalDate end;
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
	private void setVariables(List<Variables> vars)
	{
		for(Variables v : vars)variables.add(v);
		
	}
	private void setDefaults()
	{
		dataset = Dataset.MONTHLY_GRID;
		analysisMethod = AnalysisMethod.K_MEANS;
		List<Variables> vars = Arrays.asList(Variables.TEMPERATURE,Variables.RELATIVE_HUMIDITY,Variables.WIND_SPEED);	
		setVariables(vars);
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
