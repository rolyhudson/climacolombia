package org.rolson.emr.emrcycle1;

import java.io.File;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.transfer.TransferManager;

public class DataManager {
	private AmazonS3 s3client;
	private TransferManager xfm;
	private String bucketName;
	private String keyName;
	private void setupClient(){
		s3client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
	}
	private void setupXFManger(){
		DefaultAWSCredentialsProviderChain credentialProviderChain = new DefaultAWSCredentialsProviderChain();
		xfm =  new TransferManager(credentialProviderChain.getCredentials());
	}
	public boolean upload(File f)
	{
		setupClient();
		setupXFManger();
		//see https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-transfermanager.html
		boolean result = false;
		try{
		 // TransferManager processes all transfers asynchronously,
        // so this call returns immediately.
        com.amazonaws.services.s3.transfer.Upload upload = xfm.upload(bucketName, keyName, f);
        System.out.println("Object upload started");

        // Optionally, wait for the upload to finish before continuing.
        upload.waitForCompletion();
        System.out.println("Object upload complete");
        
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
