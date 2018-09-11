package climateClusters;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.feature.Normalizer;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class FilterData {
	
	
    JavaRDD<String> data; 
    JavaRDD<Record> filteredRecords; 
	public FilterData(ClusterParams clusterParams, SparkSession spark,String input)
	{
		DateTime start = clusterParams.getStartDate();
		DateTime end = clusterParams.getEndDate();
		int seasonStartMonth = clusterParams.getSeasonStartMonth();
		int seasonEndMonth = clusterParams.getSeasonEndMonth();
		int seasonStartDay = clusterParams.getSeasonStartDay();
		int seasonEndDay = clusterParams.getSeasonEndDay();
		int dailyHourStart = clusterParams.getDayStartHour();
		int dailyHourEnd = clusterParams.getDayEndHour();
		List<String> reqVars = ClusterUtils.convertParams(clusterParams.getVariables());
		List<double[]> bound = clusterParams.getSelectionCoords();
		int numClusters = clusterParams.getNClusters();
		
		
	    this.data = spark.read().textFile(input).toJavaRDD()
	    		.filter(line -> !ClusterUtils.isHeader(line))
	    		.filter(line -> ClusterUtils.inDateRange(line,start,end))
	            .filter(line -> ClusterUtils.inSeasonRange(line,seasonStartMonth,seasonEndMonth))
	            .filter(line -> ClusterUtils.requiredPoint(line, bound));
			
	  
		if(clusterParams.getDataset().equals("MONTHLY_GRID"))
		{
			filteredRecords = data.map(line -> ClusterUtils.createRecord(line, reqVars));
		}
		else
		{
			//extra hour based filter
			filteredRecords = data.filter(line->ClusterUtils.inHourlyRange(line, dailyHourStart,dailyHourEnd))
	    		.map(line -> ClusterUtils.createRecord(line, reqVars));
		}
	    
	}
	public JavaRDD<Record> getRecords()
	{
		return filteredRecords.map(f->ClusterUtils.normaliseVector(f));
	}
}
