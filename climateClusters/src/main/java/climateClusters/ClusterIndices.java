package climateClusters;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.BisectingKMeans;
import org.apache.spark.mllib.clustering.BisectingKMeansModel;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.SparkSession;



public class ClusterIndices {
	private static double wss=0;
	private static double gss=0;
	private static double cohesion=0;
	private static double separation=0;
	
//	public static double pseudoF(JavaRDD<Record> records,KMeansModel clusters) {
//		wss=0;
//		gss=0;
//		cohesion =0;
//		separation =0;
//		double pF=0;
//		
//		JavaPairRDD<Record, Record> pairs = records.cartesian(records);
//		pairs.take(10).forEach(f->System.out.println(f));
//		pairs.foreach(f->{
//
//				if(f._1().getClusternum()==f._2().getClusternum()) {
//					
//					wss+=Vectors.sqdist(f._1().getVectorNorm(),f._2().getVectorNorm());
//					
//				}
//				else {
//					gss+=Vectors.sqdist(f._1().getVectorNorm(),f._2().getVectorNorm());
//					
//				}
//			
//		});
//		pF = (gss/(clusters.clusterCenters().length-1))/(wss/(records.count()-clusters.clusterCenters().length-1));
//		
//		return pF;
//	}
	public static double[] BDSilhouette(JavaRDD<Vector> records,int minClusters,int maxClusters,int numIterations,SparkSession spark,String method) {
		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		double[] silhouette;
		switch(method) {
			case "K_MEANS":
				silhouette = BDSilhouetteKM(jsc,records,minClusters,maxClusters,numIterations);
				break;
			case "BISECTING_K_MEANS":
				silhouette = BDSilhouetteBKM(jsc,records,minClusters,maxClusters);
				break;
			default:
				silhouette = new double [1];
					
			break;
		}
		return silhouette;
	}
	private static double[] BDSilhouetteKM(JavaSparkContext jsc,JavaRDD<Vector> records,int minClusters,int maxClusters,int numIterations ) {
		long totalData = records.count();
		KMeansModel clusters;
		KMeansModel clusterCentroids;
		
		double intraMean=0;
		double interMean=0;
		double max=0;
		double[] silhouette=new double[maxClusters-minClusters];
		JavaRDD<Vector> centroides;
		for(int i =minClusters;i<maxClusters;i++) {
			clusters = KMeans.train(records.rdd(), i, numIterations);		
			intraMean = clusters.computeCost(records.rdd())/totalData;
			centroides = jsc.parallelize(Arrays.asList(clusters.clusterCenters()));
			clusterCentroids = KMeans.train(centroides.rdd(), 1, numIterations);
			interMean = clusterCentroids.computeCost(centroides.rdd())/i;
			if(interMean>=intraMean) {
				max = interMean;
			}
			else {
				max = intraMean;
			}
			silhouette[i-minClusters] = (interMean - intraMean)/max;
		}
		return silhouette;
		
	}
	private static double[] BDSilhouetteBKM(JavaSparkContext jsc,JavaRDD<Vector> records,int minClusters,int maxClusters) {
		long totalData = records.count();
		BisectingKMeansModel clusters;
		BisectingKMeansModel clusterCentroids;
		
		double intraMean=0;
		double interMean=0;
		double max=0;
		double[] silhouette=new double[maxClusters-minClusters];
		JavaRDD<Vector> centroides;
		for(int i =minClusters;i<maxClusters;i++) {
			BisectingKMeans bkm = new BisectingKMeans().setK(i);
			clusters = bkm.run(records.rdd());		
			intraMean = clusters.computeCost(records.rdd())/totalData;
			centroides = jsc.parallelize(Arrays.asList(clusters.clusterCenters()));
			bkm = new BisectingKMeans().setK(1);
			clusterCentroids = bkm.run(centroides.rdd());	
			interMean = clusterCentroids.computeCost(centroides.rdd())/i;
			if(interMean>=intraMean) {
				max = interMean;
			}
			else {
				max = intraMean;
			}
			silhouette[i-minClusters] = (interMean - intraMean)/max;
		}
		return silhouette;
	}
	public static double[] costs(JavaRDD<Vector> records,int minClusters,int maxClusters,int numIterations,SparkSession spark,String method) {
		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		long totalData = records.count();
		KMeansModel clusters;
		BisectingKMeansModel bKclusters;
		double[] costs = new double[maxClusters-minClusters];
		
		switch(method) {
		case "K_MEANS":
			for(int i =minClusters;i<maxClusters;i++) {
				clusters = KMeans.train(records.rdd(), i, numIterations);
				costs[i-minClusters]=clusters.computeCost(records.rdd());
			}
			break;
		case "BISECTING_K_MEANS":
			for(int i =minClusters;i<maxClusters;i++) {
				BisectingKMeans bkm = new BisectingKMeans().setK(i);
				bKclusters = bkm.run(records.rdd());
				costs[i-minClusters]=bKclusters.computeCost(records.rdd());
			}
		case "BISECTING_K_MEANS_AND_K_MEANS":
			for(int i =minClusters;i<maxClusters;i++) {
				BisectingKMeans bkm = new BisectingKMeans().setK(i);
				bKclusters = bkm.run(records.rdd());
				//use bkm centroids for km model
				clusters = new KMeansModel(bKclusters.clusterCenters());
				costs[i-minClusters]=clusters.computeCost(records.rdd());
			}
			break;
		default:
			costs= new double [1];
		break;
		}
		
		return costs;
	}
	public static double[] BDDunn(JavaRDD<Vector> records,int minClusters,int maxClusters,int numIterations,SparkSession spark,String method) {
		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		double[] dunn;
		switch(method) {
			case "K_MEANS":
				dunn = BDdunnKM(jsc,records,minClusters,maxClusters,numIterations);
				break;
			case "BISECTING_K_MEANS":
				dunn = BDdunnBKM(jsc,records,minClusters,maxClusters);
				break;
			default:
				dunn = new double [1];
			break;
		}
		
		return dunn;
	}
	private static double[] BDdunnKM(JavaSparkContext jsc,JavaRDD<Vector> records,int minClusters,int maxClusters,int numIterations) {
		KMeansModel clusters;
		KMeansModel clusterCentroids;
		double intraMean=0;
		double interMean=0;
		double max;
		double min;
		double[] dunn=new double[maxClusters-minClusters];
		JavaRDD<Vector> centroides;
		for(int i =minClusters;i<maxClusters;i++) {
			clusters = KMeans.train(records.rdd(), i, numIterations);
			final KMeansModel c = clusters;
			max = records.map(f->Vectors.sqdist(f, c.clusterCenters()[c.predict(f)])).max(new MaxComparator());
			centroides = jsc.parallelize(Arrays.asList(clusters.clusterCenters()));
			clusterCentroids = KMeans.train(records.rdd(), 1, numIterations);
			final KMeansModel cent = clusterCentroids;
			min = records.map(f->Vectors.sqdist(f, cent.clusterCenters()[0])).min(new MinComparator());
			dunn[i-minClusters] = min/max;
		}
		
		return dunn;
		
	}
	private static double[] BDdunnBKM(JavaSparkContext jsc,JavaRDD<Vector> records,int minClusters,int maxClusters) {
		BisectingKMeansModel clusters;
		BisectingKMeansModel clusterCentroids;
		double intraMean=0;
		double interMean=0;
		double max;
		double min;
		double[] dunn=new double[maxClusters-minClusters];
		JavaRDD<Vector> centroides;
		for(int i =minClusters;i<maxClusters;i++) {
			BisectingKMeans bkm = new BisectingKMeans().setK(i);
			clusters = bkm.run(records.rdd());//BisectingKMeansModel.train(records.rdd(), i, numIterations);
			final BisectingKMeansModel c = clusters;
			max = records.map(f->Vectors.sqdist(f, c.clusterCenters()[c.predict(f)])).max(new MaxComparator());
			centroides = jsc.parallelize(Arrays.asList(clusters.clusterCenters()));
			bkm = new BisectingKMeans().setK(1);
			clusterCentroids = bkm.run(centroides.rdd());//
			final BisectingKMeansModel cent = clusterCentroids;
			min = records.map(f->Vectors.sqdist(f, cent.clusterCenters()[0])).min(new MinComparator());
			dunn[i-minClusters] = min/max;
		}
		
		return dunn;
	}
	
	  public static class MaxComparator implements Serializable, Comparator<Double> {
		  @Override
		  public int compare(Double a, Double b) {
	      if (a < b) return -1;
	      else if (a > b) return 1;
	      return 0;
	    }


	  }
	  public static class MinComparator implements Serializable, Comparator<Double> {
		  @Override
		  public int compare(Double a, Double b) {
	      if (a > b) return -1;
	      else if (a < b) return 1;
	      return 0;
	    }


	  }
}
