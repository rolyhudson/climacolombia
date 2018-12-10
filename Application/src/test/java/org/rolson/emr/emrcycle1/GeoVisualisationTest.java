package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import org.junit.Test;

import Coordination.DataManager;

public class GeoVisualisationTest {
DataManager datamanager = new DataManager();

	@Test
	public void testBucketKeyExtraction() {
		datamanager = new DataManager();
		
		String datasource = "s3://rolyhudsontestbucket1/climateData/other/K-means clustering_output_2018_07_24_13_29_48";
		String bits[] = datasource.split("/");
		String bucket = bits[2];
		StringBuilder key = new StringBuilder();
		for(int i=3;i<bits.length;i++)
		{
			key.append(bits[i]);
			if(i<bits.length-1)
			{
				key.append("/");
			}
		}
		//get bucketname from uri
		//https://s3.amazonaws.com/rolyhudsontestbucket1/climateData/K-means+clustering_output_2018_07_24_13_29_48/part-00000
		//datamanager.accessObject();
		assertEquals(bucket,"rolyhudsontestbucket1");
		assertEquals(key.toString(),"climateData/other/K-means clustering_output_2018_07_24_13_29_48");
		
	}
//	@Test
//	public void testBucketKeyExtractionFn() {
//		GeoVisualisation geovis = new GeoVisualisation(new Workflow("test"));
//		geovis.setDataSourceUri("s3://rolyhudsontestbucket1/climateData/other/K-means clustering_output_2018_07_24_13_29_48");
//		geovis.extractBucketAndKey();
//		assertEquals(geovis.getDataBucket(),"rolyhudsontestbucket1");
//		assertEquals(geovis.getDataKey(),"climateData/other/K-means clustering_output_2018_07_24_13_29_48");
//	}
}
