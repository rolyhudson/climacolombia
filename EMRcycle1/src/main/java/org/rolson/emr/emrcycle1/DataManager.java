package org.rolson.emr.emrcycle1;

import java.io.File;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.transfer.TransferManager;

public class DataManager {
	private AmazonS3 s3client;
	private TransferManager xfm;
	private String bucketName = "rolyhudsontestbucket1";
	private String keyName = "climateData";
	public void setupClient(){
		s3client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
	}
	private void setupXFManger(){
		DefaultAWSCredentialsProviderChain credentialProviderChain = new DefaultAWSCredentialsProviderChain();
		xfm =  new TransferManager(credentialProviderChain.getCredentials());
	}
	public void listBucketContents(String container)
	{
		ObjectListing objectListing = s3client.listObjects(bucketName);
		for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
		    System.out.print((os.getKey()));
		}
	}
	public boolean accessObject(String bucket, String key)
	{
		boolean exists = false;
		try
		{
		s3client.getObject(bucket, key);
		exists = true;
		}
		catch(SdkClientException e)
		{
			e.printStackTrace();
		}
		//S3ObjectInputStream inputStream = s3object.getObjectContent();
		return exists;
	}
	public boolean copyMove(String originBucket,String destinationBucket,String originKey, String destinationKey) throws AmazonServiceException
	{
		boolean result = false;
		try{
		CopyObjectResult copyresult = s3client.copyObject(
				originBucket, 
				originKey, 
				destinationBucket, 
				destinationKey
				);
		result = true;
		}
		catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    }
		return result;
	}
	public boolean upload(File f,String keypath)
	{
		this.keyName=keypath;
		setupClient();
		setupXFManger();
		//see https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-transfermanager.html
		boolean result = false;
		try{
		 // TransferManager processes all transfers asynchronously,
        // so this call returns immediately.
        com.amazonaws.services.s3.transfer.Upload upload = xfm.upload(bucketName, keyName+"/"+f.getName(), f);
        System.out.println("Object upload started");

        // Optionally, wait for the upload to finish before continuing.
        upload.waitForCompletion();
        System.out.println("Object upload complete");
        result = true;
	    }
	    catch(AmazonServiceException e) {
	        // The call was transmitted successfully, but Amazon S3 couldn't process 
	        // it, so it returned an error response.
	        e.printStackTrace();
	    }
	    catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    } catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	 public String getBucketName(){
		 return bucketName;
	 }
	 public void setBucketName(String bucket){
		 bucketName = bucket;
	 }
	 public String getKeyName(){
		 return keyName;
	 }
	 public void setKeyName(String key){
		 keyName = key;
	 }
}
