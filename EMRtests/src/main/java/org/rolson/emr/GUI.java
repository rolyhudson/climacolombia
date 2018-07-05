package org.rolson.emr;

import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI extends JFrame {
	JFrame guiFrame;
	ClimaBucket s3b = new ClimaBucket();
	Cluster clus = new Cluster();
	public GUI()
	{
		super("Testing Buttons");
		setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton startBut = new JButton( "Start Cluster");
		add(startBut);
		ButtonHandler handler = new ButtonHandler();
		startBut.addActionListener(handler);
		
		JButton stopBut = new JButton( "Stop Cluster");
		add(stopBut);
		stopBut.addActionListener(handler);
		
		JButton startSparkBut = new JButton( "Start Spark Cluster");
		add(startSparkBut);
		startSparkBut.addActionListener(handler);
		
		JButton createBucketBut = new JButton( "Create Bucket");
		add(createBucketBut);
		createBucketBut.addActionListener(handler);
		
		JButton deleteBucketBut = new JButton( "Delete Bucket");
		add(deleteBucketBut);
		deleteBucketBut.addActionListener(handler);
		
		JButton listBucketBut = new JButton( "List all Buckets");
		add(listBucketBut);
		listBucketBut.addActionListener(handler);
		
		JButton clusterStatusBut = new JButton( "Cluster Status");
		add(clusterStatusBut);
		clusterStatusBut.addActionListener(handler);
		
		JButton terminateBut = new JButton( "Terminate all clusters");
		add(terminateBut);
		terminateBut.addActionListener(handler);
		
		JButton runStationCountBut = new JButton( "Count NOAA Stations");
		add(runStationCountBut);
		runStationCountBut.addActionListener(handler);
		
		JButton putFileBut = new JButton( "Put a file in bucket");
		add(putFileBut);
		putFileBut.addActionListener(handler);
	}
	// inner class for button event handling
	private class ButtonHandler implements ActionListener
	{
		// handle button event
		
		public void actionPerformed(ActionEvent event)
		{
			
		String cmd = event.getActionCommand();
		
		switch(cmd) {
		case "Create Bucket": actionMessage("Creating Bucket");
		
		s3b.createBucket("climacolombiabucket");
		break;
		case "Delete Bucket": actionMessage("Deleting Bucket");
		
		s3b.deleteBucket("climacolombiabucket");
		break;
		case "Start Cluster": actionMessage("Starting Cluster");
		
		clus.launch();
		break;
		case "Stop Cluster": actionMessage("Stopping Cluster");
		break;
		case "List all Buckets": actionMessage("Listing buckets");
		
		s3b.listBuckets();
		break;
			case "Cluster Status": actionMessage("Status of clusters");
		
		clus.clusterStatusReport();
		break;
		case "Terminate all clusters": actionMessage("Status of clusters");
		
		clus.terminateAllClusters();
		break;
			case "Start Spark Cluster": actionMessage("Starting a spark cluster");
		
		clus.launchSparkCluster();
		break;
		case "Count NOAA Stations": actionMessage("Starting a spark cluster");
		
		clus.launchNOAACounter();
		break;
case "Put a file in bucket": actionMessage("Adding file to bucket");
		
s3b.uploadMultiPart();
		break;
		}
		}
	}
	private void actionMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}
