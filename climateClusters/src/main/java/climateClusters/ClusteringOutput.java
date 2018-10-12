package climateClusters;

import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.SaveMode;
import climateClusters.ClusterSummary;
import climateClusters.ClusteringPerformance;
import climateClusters.ClusterUtils;
import climateClusters.ComfortIndices;
public class ClusteringOutput {
	SparkSession spk;
	public ClusteringOutput(JavaRDD<Record> records,String output,SparkSession spark,List<ClusteringPerformance> performance,ThermalZones thermalzones,int numClusters,Vector[] clusterCenters ) {
		//generate the top level reports
	    Dataset<Row> performanceDs = spark.createDataFrame(performance, ClusteringPerformance.class);
    	performanceDs.toDF().write().mode(SaveMode.Overwrite).json(output+"/stats/performanceDF");
	    thermalzones.reportMultiInclusion(records,spark,output+"/stats/strategyStats");
	    double[][] comfortIndices = ComfortIndices.getComfortIndicesClusters(records,numClusters);
	    ClusterSummary.reportClusterSummary(records,output+"/stats/clusterStats",comfortIndices,clusterCenters,spark);

    		JavaPairRDD<Vector, Iterable<Integer>> yearRecords = records
    				.mapToPair(f->ClusterUtils.getLocationCluster(f)).groupByKey();
    		
    		JavaRDD<String> typicalyear = yearRecords.map(d->{
    			Iterable<Integer> it = d._2;
    			double[] loc = d._1.toArray();
    			double sum = 0;
    			int count=0;
    			for (Integer number : it) {
    			    sum += number.doubleValue();
    			    count++;
    			}
    			int av = (int) Math.round(sum/count);
    			String result = "{\"lat\":"+Double.toString(loc[0])+",\"lon\":"+Double.toString(loc[1])+",\"clusternum\":"+Integer.toString(av)+"}";
    			return result; 
    		});
    		//write results to typical year 
    	
    		typicalyear.saveAsTextFile(output+"/stats/typicalYear");
	    //split results by year month this could be done from DB in the dashboard
	    int maxyear = records.max(new YearComparator()).getDatetime().getYear();
	    int minyear = records.min(new YearComparator()).getDatetime().getYear();
	    String path="";
	    for(int y = minyear;y<=maxyear;y++)
	    {
	    	for(int m=1;m<=12;m++)
	    	{
	    		final int yr = y;
	    		final int mon = m;
	    		path=output+"/"+y+"/"+m;
	    		JavaRDD<Record> temporalRecords = records.filter(f->ClusterUtils.matchYearMonth(f, yr, mon));
	    		
	    		JavaRDD<String> ymrecords = temporalRecords.map(f->f.toJSONString());
	    		ymrecords.saveAsTextFile(path);
	    		//could add indices per cluster per time step
	    		ClusterSummary.reportClusterSummary(temporalRecords,path+"/stats/clusterStats",comfortIndices,clusterCenters,spark);
	    		thermalzones.reportMultiInclusion(temporalRecords,spark,path+"/stats/strategyStats");
	    		for(int c=0;c<=numClusters;c++) {
	    			final int cnum =c;
	    			JavaRDD<Record> clusterRecords = temporalRecords.filter(f->ClusterUtils.matchCluster(f,cnum));
	    			thermalzones.reportMultiInclusion(clusterRecords,spark,path+"/stats/cluster"+cnum+"Stats");
	    		}
	    	}
	    	
	    }
	}
}
