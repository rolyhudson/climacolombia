package org.rolson.emr.emrcycle1;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

import org.joda.time.DateTime;

import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GUILayout {
	
	ClimaBucket s3b = new ClimaBucket();
	ClusterTests clustest = new ClusterTests();
	private DataManager datamanager = new DataManager();
	private HashMap<String,Text> buttonLabelMap = new HashMap<String,Text>();
	int width = 1000;
	int height =700;
	private ClusterCoordinator coordinator;
	private TableView<Cluster> resourcetable = new TableView<Cluster>();
	private TableView<Workflow> workflowtable = new TableView<Workflow>();
	
	private List<Label> statuslabels = new ArrayList();
	
	Stage stage;
	public GUILayout(ClusterCoordinator wfc,Stage stg)
	{
		coordinator = wfc;
		stage = stg;
		
		StackPane root = new StackPane();

        root.getChildren().add(addTabs());

        Scene scene = new Scene(root, width, height);

        stage.setTitle("Clima Colombia");
        stage.setScene(scene);
        
        stage.show();
	}
	
	private void addTabWithButtons(int index, String name, TabPane pane)
	{
		Tab tab1 = new Tab();
		addButtonLabel(tab1,btnNames(name));
        tab1.setText(name);
        pane.getTabs().add(index, tab1);
	}
	private List<String> tabSet()
	{
		List<String> buttonCmds = new ArrayList<String>();
		buttonCmds.add("Workflows");
		buttonCmds.add("Resource monitor");
		buttonCmds.add("Workflow monitor");
		buttonCmds.add("Visualise");
		buttonCmds.add("Data manager");
		buttonCmds.add("AWS tests");
		
		return buttonCmds;
	}
	private List<String> btnNames(String tabname)
	{
		List<String> buttonCmds = new ArrayList<String>();
		switch(tabname)
		{
		case "Data Manager":
			buttonCmds.add("Upload dataset");
			buttonCmds.add("Upload JAR file");
			break;
		
		case "Workflows":
			buttonCmds.add("Hadoop Map Reduce");
			buttonCmds.add("K-means clustering");
			buttonCmds.add("Linear Regression");
			buttonCmds.add("Message log agregator");
			buttonCmds.add("Monthly records totals");
			buttonCmds.add("Spark word count");
			break;
		case "AWS tests":
			buttonCmds.add("Start Cluster");
			buttonCmds.add("Stop Cluster");
			buttonCmds.add("Start Spark Cluster");
			buttonCmds.add("Create Bucket");
			buttonCmds.add("Delete Bucket");
			buttonCmds.add("List all Buckets");
			buttonCmds.add("Cluster Status");
			buttonCmds.add("Terminate all clusters");
			buttonCmds.add("Count NOAA Stations");
			buttonCmds.add("Put a file in bucket");
			break;
		}
		return buttonCmds;
	}
	private TabPane addTabs()
	{
		TabPane tabPane = new TabPane();
		List<String> tabnames = tabSet();
		List<String> cols;
		for(int i=0; i<tabnames.size();i++)
		{
			if(tabnames.get(i).contains("monitor"))
			{
				
				if(tabnames.get(i).contains("Resource")) {
					cols = Arrays.asList("name", "status" , "awsID");
					addTabWithTableView(i,tabnames.get(i),tabPane, this.resourcetable,cols);
					
				}
				else {
					cols = Arrays.asList("name", "status" , "awsID","appType");
					addTabWithTableView(i,tabnames.get(i),tabPane, this.workflowtable,cols);
					
				}
				
			}
			else addTabWithButtons(i, tabnames.get(i), tabPane);
		}

        tabPane.getSelectionModel().select(0);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        return tabPane;
	}
	
	private TableColumn addColumn(String name,int width,String property)
	{
		 TableColumn col = new TableColumn(name);
		 col.setMinWidth(width);
	     col.setCellValueFactory(new PropertyValueFactory<Workflow, String>(property));
	     return col;
	}
	public void updateStatusLabel()
	{
		for(Label l:statuslabels) {
		l.setText(coordinator.EMRStatus());
		}
	}
	private void addTabWithTableView(int index,String name,TabPane tabpane,TableView table,List<String> columns)
	{
		if(name.contains("Resource"))table.setPlaceholder(new Label(name + " could not find any active clusters"));
		else table.setPlaceholder(new Label(name + " could not find any active workflows"));
		final Label label = new Label(name);
        label.setFont(new Font("Arial", 20));
        Label statuslabel = new Label("Connecting...");
        statuslabels.add(statuslabel);
        statuslabel.setFont(new Font("Arial", 15));
        table.setEditable(true);
 
        List<TableColumn> tabColumns = new ArrayList<TableColumn>();
        for(String col : columns)
        {
        	tabColumns.add(addColumn(col,200,col));
        }
        if(name.contains("Resource")) table.setItems(coordinator.monitorResourceData);
        else table.setItems(coordinator.monitorWorkflowData);
        table.getColumns().addAll(tabColumns);
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));

        vbox.getChildren().addAll(label,statuslabel, table);
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(vbox);
        tabpane.getTabs().add(index, tab);
        //((Group) scene.getRoot()).getChildren().addAll(vbox);
	}
	
	private void addButtonLabel(Tab tab,List<String> buttons)
	{
		//Creating a Grid Pane 
	      GridPane gridPane = new GridPane();    
	      
	      //Setting size for the pane  
	      gridPane.setMinSize(400, 200); 
	       
	      //Setting the padding  
	      gridPane.setPadding(new Insets(10, 10, 10, 10)); 
	      
	      //Setting the vertical and horizontal gaps between the columns 
	      gridPane.setVgap(5); 
	      gridPane.setHgap(5);
	      
		for(int i=0;i<buttons.size();i++)
		{
			Button button1 = new Button(buttons.get(i));
			button1.setOnAction(this::handler);
			button1.setMaxWidth(Double.MAX_VALUE);
			Text text1 = new Text("Result:");     
		    TextField textField1 = new TextField();
			gridPane.add(button1, 0, i); 
		    gridPane.add(text1, 2, i); 
		    gridPane.add(textField1, 1, i);  
		    buttonLabelMap.put(buttons.get(i), text1);
		}
	      //Setting the Grid alignment 
	      gridPane.setAlignment(Pos.TOP_LEFT); 
	      tab.setContent(gridPane);
	}
	public Text getComponentByName(String name) {
        if (buttonLabelMap.containsKey(name)) {
                return buttonLabelMap.get(name);
        }
        else return null;
	}
	private boolean alertMessage(String text)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("About to run: "+ text);
		alert.setContentText("Proceed?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
		   return true;
		} else {
		    return false;
		}
	}
	private void handler(ActionEvent event) {
		String cmd = ((Button)event.getSource()).getText();
		switch(cmd) {
		case "Create Bucket": if(alertMessage("Creating Bucket")) s3b.createBucket("climacolombiabucket");
			break;
		case "Delete Bucket":if(alertMessage("Deleting Bucket")) s3b.deleteBucket("climacolombiabucket");
			break;
		case "Start Cluster": if(alertMessage("Starting Cluster")) clustest.launch();
			break;
		case "Stop Cluster": if (alertMessage("Stopping Cluster"))
			break;
		case "List all Buckets": if(alertMessage("Listing buckets"))s3b.listBuckets();
			break;
		case "Cluster Status": if(alertMessage("Status of clusters"))clustest.clusterStatusReport();
			break;
		case "Terminate all clusters": if(alertMessage("Status of clusters"))clustest.terminateAllClusters();
			break;
		case "Start Spark Cluster": if(alertMessage("Starting a spark cluster"))clustest.launchSparkCluster();
			break;
		case "Count NOAA Stations": if(alertMessage("Starting a spark cluster"))clustest.launchNOAACounter();
			break;
		case "Put a file in bucket": if(alertMessage("Adding file to bucket"))s3b.uploadMultiPart();
			break;
		case "Hadoop Map Reduce": if(alertMessage(cmd))
			{
				Cluster clus = new Cluster();
				clus.setName("Map Reduce Cluster");
				clus.addPredfined(cmd);
				coordinator.addCluster(clus);
				coordinator.runCluster("Map Reduce Cluster");
			}
			break;
		case "Message log agregator": if(alertMessage(cmd))
			{
				Cluster clus = new Cluster();
				clus.setName("Message log");
				clus.addPredfined(cmd);
				coordinator.addCluster(clus);
				coordinator.runCluster("Message log");
			}
			break;
		case "Monthly records totals": if(alertMessage(cmd))
			{
				Cluster clus = new Cluster();
				clus.setName("Monthly records");
				clus.addPredfined(cmd);
				coordinator.addCluster(clus);
				coordinator.runCluster("Monthly records");
			}
		break;
		
		case "K-means clustering": if(alertMessage(cmd))
			{
				
			}
			break;
		case "Spark word count": if(alertMessage(cmd))
			{
			Cluster clus = new Cluster();
			clus.setName("Spark word count");
			clus.addPredfined(cmd);
			coordinator.addCluster(clus);
			coordinator.runCluster("Spark word count");
			}
			break;
		case "Upload dataset":if(alertMessage(cmd))
			{
				List<String> dataexts = Arrays.asList("csv", "txt");
				getFileForUpload(dataexts, cmd);
			}
		break;
		case "Upload JAR file": if(alertMessage(cmd))
			{
				List<String> processexts = Arrays.asList("jar");
				getFileForUpload(processexts, cmd);
			}
		break;
		
		}
	}
	public void getFileForUpload(List<String> extensions, String cmd)
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Select File");
		File file = fc.showOpenDialog(stage);

        if (file!=null) {
            
            boolean required =false;
            String foundext = Utils.getFileExtension(file);
            for(String ext:extensions){
            	if(ext.equals(foundext))required=true;
            }
            if(required) {
            	
            	String subfolderpath = getStringInput();
            	
	            	Text l = getComponentByName(cmd);
	            	if(datamanager.upload(file,subfolderpath))
	            	{
	            		//write result message
	            		l.setText("Result: "+file.getName()+ " uploaded");
	            	}
	            	else{
	                	//
	            		l.setText("Result: "+file.getName()+ " upload failed");
	                }
            	
            }
            else{
            	Alert alert = new Alert(AlertType.INFORMATION);
            	alert.setTitle("Information Dialog");
            	alert.setHeaderText("file extension not accepted");
            	alert.setContentText("file extension not accepted");

            	alert.showAndWait();
            	
            }	
            
        } 
        //Open command cancelled by user
	}
	private String getStringInput()
	{
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Set path in S3");
		dialog.setHeaderText("Set path in S3");
		dialog.setContentText("Please enter path in the format \"test/data/folder\":");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			return result.get();
		}
			return null;
	    
	}
}
