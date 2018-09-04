package climateClusters;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;


public class Clustering {

	public static void main(String[] args) {
		// on EMR use
//		SparkSession spark = SparkSession
//                .builder()
//                .appName("SparkJob")
//                .getOrCreate();
		
		//local debug
		SparkSession spark = SparkSession.builder()
				  .master("local")
				  .appName("SparkJob")
				  .getOrCreate();
		args[1] = args[1]+"/" +DateTime.now().getMillisOfDay();
		//end local debug
		Dataset<Row> jsondata = spark.read().json(args[2]);
		Dataset<Row> ap = jsondata.select("analysisParameters");
		System.out.println(ap.toString());
		ClusterParams clusterParams = new ClusterParams(args[2]);
		switch(clusterParams.getClusteringMethod()) {
		case "K_MEANS":
			SimpleKMeans simpleKM = new SimpleKMeans(args[0],args[1],clusterParams,spark);
			break;
		}
	}
	
}
