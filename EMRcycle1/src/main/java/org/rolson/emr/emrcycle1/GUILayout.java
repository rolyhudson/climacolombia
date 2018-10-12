package org.rolson.emr.emrcycle1;


import java.io.File;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.guigarage.flatterfx.FlatterFX;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GUILayout {
	

	private DataManager datamanager = new DataManager();
	private HashMap<String,Text> buttonLabelMap = new HashMap<String,Text>();
	int width = 1400;
	int height =1040;
	private ClusterCoordinator coordinator;
	private TableView<Cluster> resourcetable = new TableView<Cluster>();
	private TableView<Workflow> workflowtable = new TableView<Workflow>();
	private SelectionMap selectionMap = new SelectionMap();
	private WebEngine webEngine;
	private  WebView browser;
	Label statuslabel = new Label();
	Stage stage;
	public GUILayout(ClusterCoordinator wfc,Stage stg)
	{
		coordinator = wfc;
		stage = stg;
		
		StackPane root = new StackPane();
		Button statusbutton = new Button("Update status");
		statusbutton.setOnAction(this::handler);
	    statuslabel.setFont(new Font("Arial", 15));
	    
	    browser = new WebView();
		webEngine = browser.getEngine();
	    
	    HBox hbox = new HBox();
	    hbox.setAlignment(Pos.CENTER_LEFT);
	    hbox.getChildren().addAll(statusbutton,statuslabel);
	    hbox.setSpacing(5);
        //hbox.setPadding(new Insets(10, 0, 0, 10));
		VBox vbox = new VBox();
		
		vbox.getChildren().addAll(hbox,addTabs());

        root.getChildren().add(vbox);

        Scene scene = new Scene(root, width, height);

        stage.setTitle("Clima Cluster Colombia");
        stage.setScene(scene);
        startMonitor();
        stage.show();
        //FlatterFX.style();
	}
	public void startMonitor() 
	{
		// Create a Runnable
		Runnable task = new Runnable()
		{
			public void run()
			{
				runMonitor();
			}
		};
		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();
	}
	public void runMonitor() 
	{
		int i=0;
		while(true) 
		{
			try 
			{
				// Update the Label on the JavaFx Application Thread		
				Platform.runLater(new Runnable() 
				{
		            @Override 
		            public void run() 
		            {
		            	
		            	System.out.println("update thread called ");
		            	coordinator.updateOnce();
		            	updateStatusLabel();
		            }
		        });
				Thread.sleep(60000);
				i++;
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}	
	private void addTabWithButtons(int index, String name, TabPane pane)
	{
		Tab tab1 = new Tab();
		addButtonLabel(tab1,btnNames(name));
        tab1.setText(name);
        pane.getTabs().add(index, tab1);
	}
	
	private List<String> btnNames(String tabname)
	{
		List<String> buttonCmds = new ArrayList<String>();
		switch(tabname)
		{
		case "Data manager":
			buttonCmds.add("Upload dataset");
			buttonCmds.add("Upload JAR file");
			buttonCmds.add("Upload text");
			break;
		}
		return buttonCmds;
	}
	private List<String> tabSet()
	{
		List<String> buttonCmds = new ArrayList<String>();
		
		buttonCmds.add("Workflow builder");
		buttonCmds.add("Design strategy editor");
		buttonCmds.add("Monitor");
		buttonCmds.add("Visualise");
		buttonCmds.add("Data manager");
		buttonCmds.add("Settings");
		return buttonCmds;
	}
	private void addWebView(int index,String name,TabPane tabpane)
	{
		
		webEngine.load("http://lacunae.io/geovis2018_09_29_13_35_00/");
		Tab tab = new Tab();
		tab.setText(name);
		tab.setContent(browser);
		tabpane.getTabs().add(index, tab);
	}
	private TabPane addTabs()
	{
		TabPane tabPane = new TabPane();
		List<String> tabnames = tabSet();
		
		for(int i=0; i<tabnames.size();i++)
		{
			String tabname = tabnames.get(i);
			switch(tabname)
			{
			case "Monitor":
				makeMonitorTab(tabname,tabPane,i );
				
				break;
			case "Design strategy editor":
				addDesignStratgeyTab(i,tabname,tabPane );
				
				break;
			case "Settings":
				addSettingsTab(i,tabname,tabPane );
				break;
			case "Visualise":
				addWebView(i,tabname,tabPane);
			break;
			case "Workflow builder":
				
				GUIWorkflowBuilder gwfb = new GUIWorkflowBuilder(i,tabname,tabPane,this.coordinator,this.datamanager,selectionMap,webEngine);
				break;
			default:
				addTabWithButtons(i, tabname, tabPane);
				break;
			}
			
		}
        tabPane.getSelectionModel().select(0);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        return tabPane;
	}
	private void addDesignStratgeyTab(int index,String name,TabPane tabpane ) {
		File file = new File("C:/Users/Admin/Documents/projects/clusterColombia/climacolombia/documentation/writeUp/strategyEditor.png");
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setImage(image);
        Tab tab = new Tab();
		tab.setText(name);
		tab.setContent(imageView);
		tabpane.getTabs().add(index, tab);
	}
	private void addSettingsTab(int index,String name,TabPane tabpane )
	{
		Label daterangelabel = new Label("Monitor clusters and workflows from: ");
		DatePicker startdatePicker = new DatePicker();
		LocalDate now = LocalDate.now();
		startdatePicker.setValue(now.minusDays(1)); 
		startdatePicker.setOnAction(event -> {
		    LocalDate date = startdatePicker.getValue();
		    this.coordinator.setMonitorFrom(date);
		    System.out.println("Monitor from date changed to: " + date);
		});
		final HBox monitordaterange = new HBox();
		monitordaterange.setSpacing(5);
		monitordaterange.setPadding(new Insets(10, 0, 0, 10));
		monitordaterange.getChildren().addAll(daterangelabel,startdatePicker);
		final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));

        vbox.getChildren().addAll(monitordaterange);
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(vbox);
        tabpane.getTabs().add(index, tab);
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
		statuslabel.setText(coordinator.EMRStatus());
				
	}

	@SuppressWarnings("unchecked")
	private TableColumn addClusterActionColumn(TableView tblView,String buttonName,String colName)
	{
		TableColumn c3 = new TableColumn<>(colName);
        c3.setSortable(false);
        c3.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Cluster, Boolean>,
                        ObservableValue<Boolean>>() {
     
                    @Override
                    public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Cluster, Boolean> p) {
                        return new SimpleBooleanProperty(p.getValue() != null);
                    }
                });
     
        c3.setCellFactory(
                new Callback<TableColumn<Cluster, Boolean>, TableCell<Cluster, Boolean>>() {
     
                    @Override
                    public TableCell<Cluster, Boolean> call(TableColumn<Cluster, Boolean> p) {
                        return new ClusterActionCell(tblView,buttonName,coordinator);
                    }
                });
        
        return c3;
	}
	private void makeMonitorTab(String name,TabPane tabpane,int index ) {
		Tab tab = new Tab();
        tab.setText(name);
        List<String> cols = Arrays.asList("name", "status" , "awsID");
		VBox resource = addTabWithTableView(this.resourcetable,cols,"Resources");
		cols = Arrays.asList("name", "status" ,"creationDate", "awsID","appType");
		VBox workflow =addTabWithTableView(this.workflowtable,cols,"Workflows");
		VBox monitor = new VBox();
		monitor.getChildren().addAll(resource,workflow);
        tab.setContent(monitor);
        tabpane.getTabs().add(index, tab);
	}
	private VBox addTabWithTableView(TableView table,List<String> columns,String name)
	{
		
        //placeholder for no contents
		if(name.contains("Resource"))table.setPlaceholder(new Label("no active clusters found in date range"));

		else table.setPlaceholder(new Label("no active workflows found in date range")); 
		//title
		final Label label = new Label(name);
        label.setFont(new Font("Arial", 20));
        final HBox hbox = new HBox();
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.BOTTOM_LEFT);
        
        if(name.contains("Resource")) {
        Button terminateBtn = new Button("Terminate all clusters");
        terminateBtn.setOnAction(this::handler);
        hbox.getChildren().addAll(label,terminateBtn);
        }
        else
        {
        	hbox.getChildren().addAll(label);
        }
        table.setEditable(true);
 
        List<TableColumn> tabColumns = new ArrayList<TableColumn>();
        for(String col : columns)
        {
        	tabColumns.add(addColumn(col,150,col));
        }
        if(!name.contains("Workflow")) {

        	TableColumn stopbuttons = addClusterActionColumn(table,"stop","Terminate");
        	tabColumns.add(stopbuttons);
        }
        
        if(name.contains("Resource")) table.setItems(coordinator.monitorResourceData);
        else table.setItems(coordinator.monitorWorkflowData);
        table.getColumns().addAll(tabColumns);
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));

        vbox.getChildren().addAll(hbox, table);
        return vbox;
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
		case "Terminate all clusters":
			if(alertMessage(cmd)) coordinator.stopAllClusters();
			
			break;
		case "Update status":
			updateStatusLabel();
			coordinator.updateOnce();
			
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
		case "Upload text": if(alertMessage(cmd))
		{
			Workflow wfjson = new Workflow();
			if(this.datamanager.uploadStringToFile("jsontest/test1.txt", wfjson.seraliseWorkflow(),"clustercolombia","plain/text"))
			{
				System.out.println("Testworkflow uploaded with success");
			}
			else
			{
				System.out.println("Testworkflow upload failed");
			}
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
