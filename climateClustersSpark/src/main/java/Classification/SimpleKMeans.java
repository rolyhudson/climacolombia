package Classification;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;

public class SimpleKMeans {
	
	private Vector[] clusterCenters;
	int numIterations = 20;
	KMeansModel clusters; 
	public SimpleKMeans(int numClusters,JavaRDD<Vector> dataPoints) {
		clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
		clusterCenters = clusters.clusterCenters();
	}
	public SimpleKMeans(Vector[] centers) {
		clusters = new KMeansModel(centers);
	}
	public Vector[] getCentroids() {
		return this.clusterCenters;
	}
	public KMeansModel getModel() {
		return this.clusters;
	}
}

