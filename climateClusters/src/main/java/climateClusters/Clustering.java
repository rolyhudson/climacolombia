package climateClusters;


import java.io.IOException;



import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;
import climateClusters.ClusterParams;
import climateClusters.FilterData;
import climateClusters.ThermalZones;
import climateClusters.SimpleKMeans;
public class Clustering {

	public static void main(String[] args) {
		// on EMR use
		SparkSession spark = SparkSession
                .builder()
                .appName("SparkJob")
                .getOrCreate();
		
		//local debug
//		SparkSession spark = SparkSession.builder()
//				  .master("local")
//				  .appName("SparkJob")
//				  .getOrCreate();
//		args[1] = args[1]+"/" +DateTime.now().getMillisOfDay();
//		//end local debug

		//ClusterParams clusterParams;
		
			ClusterParams clusterParams = new ClusterParams(args[2],spark);
			FilterData filterData = new FilterData(clusterParams,spark,args[0]);
			ThermalZones thermalzones = new ThermalZones(spark,args[3]);
			switch(clusterParams.getClusteringMethod()) {
			case "K_MEANS":
				SimpleKMeans simpleKM = new SimpleKMeans(args[1],spark,clusterParams.getNClusters(),thermalzones,filterData.getRecords());
				break;
			}
		
	}
	
}

