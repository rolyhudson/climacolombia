package climateClusters;


import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.BisectingKMeansModel;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;


import climateClusters.ClusterParams;
import climateClusters.FilterData;
import climateClusters.ThermalZones;
import climateClusters.BiKMeans;
import climateClusters.SimpleKMeans;
import climateClusters.KMeansPerformance;
import climateClusters.ClusteringOutput;
import climateClusters.TimeLog;
public class Clustering {
	static List<climateClusters.TimeLog> timelog = new ArrayList<climateClusters.TimeLog>();
	static DateTime start = DateTime.now();
	public static void main(String[] args) {
		start = DateTime.now();
		//
		logEvent("job started");
//		// on EMR use
//		SparkSession spark = SparkSession
//                .builder()
//                .appName("SparkJob")
//                .getOrCreate();
//		
//		local debug
//		
		SparkSession spark = SparkSession.builder()
				  .master("local[4]")
				  .appName("SparkJob")
				  .getOrCreate();
		args[1] = args[1]+"/" +DateTime.now().getMillisOfDay();
//		//end local debug
		
		ClusterParams clusterParams = new ClusterParams(args[2],spark);
		FilterData filterData = new FilterData(clusterParams,spark,args[0]);
		JavaRDD<Record> recorddata = filterData.getRecords();
		ThermalZones thermalzones = new ThermalZones(spark,args[3]);
		logEvent("params read, data filtered, thermal zones read");
		String method = clusterParams.getClusteringMethod();
		
		KMeansPerformance kmPerf = new KMeansPerformance(spark,clusterParams.getNClusters(),filterData.getRecords(),method);
		logEvent("perfomance processed");
		SimpleKMeans simpleKM;
		BiKMeans BiKM;
		JavaRDD<Record> records;
		Vector[] clusterCentres;
		ClusteringOutput clusterOut;
		int numClusters=kmPerf.getNclusters();
		JavaRDD<Vector> data = filterData.getRecords().map(f->f.getVectorNorm());
		data.cache();
		KMeansModel kmodel;
		BisectingKMeansModel bkmodel;
		switch(method) {
		case "K_MEANS":
			simpleKM = new SimpleKMeans( numClusters,data);
			clusterCentres =simpleKM.getCentroids();
			//sorted by year and with cluster id and associated strategies
			kmodel = simpleKM.getModel();
		    records = recorddata.map(f->ClusterUtils.classifyKmeans(f,kmodel))
		    		.map(f->thermalzones.testZones(f))
		    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
		    logEvent("data classified");
		    clusterOut = new ClusteringOutput(records,args[1],spark,kmPerf.getPerformance(),thermalzones, numClusters,clusterCentres);
		    logEvent("output written");
			break;
		case "BISECTING_K_MEANS":
			BiKM = new BiKMeans( numClusters,data);
			clusterCentres =BiKM.getCentroids();
			//sorted by year and with cluster id and associated strategies
			bkmodel = BiKM.getModel();
		    records = recorddata.map(f->ClusterUtils.classifyBKmeans(f,bkmodel))
		    		.map(f->thermalzones.testZones(f))
		    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
		    logEvent("data classified");
		    clusterOut = new ClusteringOutput(records,args[1],spark,kmPerf.getPerformance(),thermalzones, numClusters,clusterCentres);
		    logEvent("output written");
			break;
		case "BISECTING_K_MEANS_AND_K_MEANS":
			BiKM = new BiKMeans( numClusters,data);
			clusterCentres =BiKM.getCentroids();
			simpleKM = new SimpleKMeans(clusterCentres);
			//sorted by year and with cluster id and associated strategies
			kmodel = simpleKM.getModel();
		    records = recorddata.map(f->ClusterUtils.classifyKmeans(f,kmodel))
		    		.map(f->thermalzones.testZones(f))
		    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
		    logEvent("data classified");
		    clusterOut = new ClusteringOutput(records,args[1],spark,kmPerf.getPerformance(),thermalzones, numClusters,clusterCentres);
		    logEvent("output written");
			break;
		}
		
		logEvent("job finished");
//		////generate the top level reports
	    Dataset<Row> timelogfile = spark.createDataFrame(timelog, TimeLog.class);
	    timelogfile.toDF().write().mode(SaveMode.Overwrite).json(args[1]+"/stats/timeline");
		spark.stop();
		
	}
	private static void logEvent(String description) {
	TimeLog tl = new TimeLog(start, description);
	timelog.add(tl);
	}
}

