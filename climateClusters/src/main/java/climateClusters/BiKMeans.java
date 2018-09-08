package climateClusters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.BisectingKMeans;
import org.apache.spark.mllib.clustering.BisectingKMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SparkSession;

public class BiKMeans {
	public BiKMeans(String output,SparkSession spark,ClusterParams clusterParams,JavaRDD<Record> recorddata)
	{
		//get the vector attribute
	    JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVector());
	    
	    int numIterations = 20;
	    
	    List<Double> scores = new ArrayList<Double>();
	    int numClusters = clusterParams.getNClusters();
	    if(numClusters==0)
	    {
	    	for(int i=2;i<=100;i+=2)
	    	{
	    		BisectingKMeans bkm = new BisectingKMeans().setK(i);
	    		BisectingKMeansModel model = bkm.run(dataPoints.rdd());		  
	    		scores.add(model.computeCost(dataPoints.rdd()));
	    	}
	    	numClusters = scores.indexOf(Collections.min(scores));
	    }
	    
	    BisectingKMeans bkm = new BisectingKMeans().setK(numClusters);
		BisectingKMeansModel model = bkm.run(dataPoints.rdd());	
		
		Vector[] clusterCenters = model.clusterCenters();
	}
}
