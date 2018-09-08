package climateClusters;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.GaussianMixture;
import org.apache.spark.mllib.clustering.GaussianMixtureModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SparkSession;

public class GaussianMix {
	public GaussianMix(String output,SparkSession spark,ClusterParams clusterParams,JavaRDD<Record> recorddata)
	{
		JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVector());
		int numClusters = clusterParams.getNClusters();
		GaussianMixtureModel gmm = new GaussianMixture().setK(numClusters).run(dataPoints.rdd());
		
		
	}
}
