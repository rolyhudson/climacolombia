package climaCluster;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
public class KMeansClimate {
	
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("App");
	    //val conf = new SparkConf().setMaster("local").setAppName("myApp")
	    JavaSparkContext jsc = new JavaSparkContext(conf);
	    
	    String path = args[0];
	    JavaRDD<String> data = jsc.textFile(path);
	    //format of csv is 
	    //latitude,longitude,yr,month,temp,vp,rh,tmin,tmax,trange,precip,windSpd,temp,rh,precip
	    //parse data to give vectors of temp, rh and wind
	    JavaRDD<Vector> parsedData = data.map(s -> {
		      String[] sarray = s.split(",");
		      double[] values = new double[3];
		      for (int i = 0; i < sarray.length; i++) {
		        if(i==4)values[0] = Double.parseDouble(sarray[i]);
		        if(i==6)values[1] = Double.parseDouble(sarray[i]);
		        if(i==11)values[2] = Double.parseDouble(sarray[i]);
		      }
		      return Vectors.dense(values);
		    });
		    parsedData.cache();
		    for (Vector line: parsedData.take(10)) {
		    	System.out.println(line);
		    	}
		 // Cluster the data into two classes using KMeans
		    int numClusters = 2;
		    int numIterations = 20;
		    KMeansModel clusters = KMeans.train(parsedData.rdd(), numClusters, numIterations);

		    System.out.println("Cluster centers:");
		    for (Vector center: clusters.clusterCenters()) {
		      System.out.println(" " + center);
		    }
		    double cost = clusters.computeCost(parsedData.rdd());
		    System.out.println("Cost: " + cost);

		    // Evaluate clustering by computing Within Set Sum of Squared Errors
		    double WSSSE = clusters.computeCost(parsedData.rdd());
		    System.out.println("Within Set Sum of Squared Errors = " + WSSSE);

		    // Save and load model
		    clusters.save(jsc.sc(), "target/org/apache/spark/JavaKMeansExample/KMeansModel");
		    KMeansModel sameModel = KMeansModel.load(jsc.sc(),
		      "target/org/apache/spark/JavaKMeansExample/KMeansModel");
	    jsc.stop();

	}
}
