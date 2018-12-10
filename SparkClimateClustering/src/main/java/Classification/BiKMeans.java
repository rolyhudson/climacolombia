package Classification;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.BisectingKMeans;
import org.apache.spark.mllib.clustering.BisectingKMeansModel;
import org.apache.spark.mllib.linalg.Vector;

public class BiKMeans {
	Vector[] clusterCenters;
	BisectingKMeansModel clusters;
	public BiKMeans(int numClusters,JavaRDD<Vector> dataPoints)
	{
	    BisectingKMeans bkm = new BisectingKMeans().setK(numClusters);
		clusters = bkm.run(dataPoints.rdd());	
		clusterCenters = clusters.clusterCenters();
	}
	public Vector[]  getCentroids() {
		return this.clusterCenters;
	}
	public BisectingKMeansModel getModel() {
		return this.clusters;
	}
}
