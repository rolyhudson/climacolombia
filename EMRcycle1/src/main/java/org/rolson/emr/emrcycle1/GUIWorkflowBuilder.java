package org.rolson.emr.emrcycle1;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class GUIWorkflowBuilder {
	private ClusterCoordinator coordinator;
	private DataManager datamanager;
	private HBox mainBox;
	private VBox wfSelectorBox;
	private VBox wfEditorBox;
	private VBox configTools;
	VBox mappingBox = new VBox();
	private TextField wfNameTextField;
	private ComboBox<String> datasetCB;
	private DatePicker startdatePicker;
	private DatePicker enddatePicker;
	private ComboBox<String> seasonStartMonth;
	private ComboBox<String> seasonEndMonth;
	private ComboBox<String> seasonStartDay;
	private ComboBox<String> seasonEndDay;
	private ComboBox<String> variableCB1;
	private ComboBox<String> variableCB2;
	private ComboBox<String> variableCB3;
	private ComboBox<String> variableCB4;
	private ComboBox<String> variableCB5;
	private ComboBox<String> variableCB6;
	private ComboBox<String> analysisMethodCB;
	private ComboBox<String> nClustersCB;
	private ComboBox<String> dayStartHour;
	private ComboBox<String> dayEndHour;
	private boolean newWorkflow;
	private Label statusLbl;
	private Button copyBtn;
	private Button deleteBtn;
	private Button saveBtn;
	private Button runBtn;
	private Button stopBtn;
	private Button map2dBtn;
	private Button map3dBtn;
	private Button statsBtn;
	private SelectionMap selectionMap; 
	
	
	private TableView<Workflow> workflowtable = new TableView<Workflow>();
	
	private  ObservableList<Workflow> data = FXCollections.observableArrayList();
	private Workflow forAction;//currently editing this workflow
	int[] daysInMonths = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	List<String> months = Arrays.asList("January","February","March","April","May","June","July","August","September","October","November","December");

	List<String> hours;
	
	public GUIWorkflowBuilder(int index, String name,TabPane tabpane,ClusterCoordinator coord,DataManager dm,SelectionMap sm)
	{
		selectionMap = sm;
		
		this.datamanager = dm;
		coordinator = coord;
		setHours();
		Tab tab = new Tab();
		tab.setText(name);
		tab.setContent(setLayout());
		tabpane.getTabs().add(index, tab);

	}
	private List<String> setDays(int month)
	{
		List<String> days = new ArrayList<String>();
		for(int i=1;i<=daysInMonths[month];i++)days.add(Integer.toString(i));
		return days;
	}
	private void setHours()
	{
		hours = new ArrayList<String>();
		for(int i=1;i<25;i++)hours.add(Integer.toString(i));
	}
	private HBox setLayout()
	{
		mainBox = new HBox();
		wfSelectorBox = new VBox();
		wfSelectorBox.setStyle("-fx-padding: 6;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: grey;");
		mainBox.setSpacing(5);
		mainBox.setPadding(new Insets(10, 0, 0, 10));
		addWorkflowList(this.workflowtable);
		
		wfEditorBox = new VBox();
		wfEditorBox.setStyle("-fx-padding: 6;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: grey;");
		addCurrentWorkflowStatus();
		addWorkflowDefinitionTools();
		mainBox.getChildren().addAll(wfSelectorBox,wfEditorBox);
		return mainBox;
	}
	private void addWorkflowDefinitionTools()
	{
		HBox def = new HBox();
		
		BorderPane bp = new BorderPane();
		this.configTools = configTools();

		mappingBox.getChildren().addAll(selectionMap.mapTools(),selectionMap.getMapView());
		def.getChildren().setAll(this.configTools,mappingBox);
		
		wfEditorBox.getChildren().add(def);
		
	}
	
	private HBox workFlowTools()
	{
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setStyle("-fx-padding: 6;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: rgb(220,220,220);");
		box.setSpacing(5);
		box.setPadding(new Insets(10, 0, 0, 10));
		GridPane tools = new GridPane();    
	    
	    //name editor
			Label nameLbl = new Label("Name: ");
			tools.add(nameLbl,0,0);
		wfNameTextField = new TextField ();
		wfNameTextField.textProperty().addListener((obs, oldText, newText) -> {
		   if(!this.newWorkflow) {
			forAction.setName(newText);
			
			this.coordinator.updateWorkflowList();
		   }
		});
		tools.add(wfNameTextField, 1, 0);
		
		//copy button
		
		this.copyBtn = new Button("Copy");
		copyBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		copyBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			Workflow wf = new Workflow();
			wf.setWorkflowFromJSON(forAction.seraliseWorkflow());
			
			wf.setName(forAction.getName()+" copy");
			wf.setStatus("INITIALISED");
			wf.generateNewGuid();
			coordinator.addWorkflow(wf);
			this.workflowtable.getSelectionModel().select(wf);
			}
		});
		tools.add(copyBtn,2,0);
		
		//save button
		this.saveBtn = new Button("Save");
		saveBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		saveBtn.setOnAction((event) -> {

			if(forAction!=null)
			{
				if(this.datamanager.uploadTextToFile("workflowJSON/"+forAction.getGuid()+".txt", forAction.seraliseWorkflow()))
				{
					System.out.println(forAction.getName()+" uploaded with success");
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Success");
					alert.setHeaderText("Workflow saved");
					alert.showAndWait();
					
				}
				else
				{
					System.out.println(forAction.getName()+" upload failed");
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Failure");
					alert.setHeaderText("Workflow not saved");
					alert.showAndWait();
				}
			}
		});
		tools.add(saveBtn,3,0);
		//delete button
		
		this.deleteBtn = new Button("Delete");
		deleteBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		deleteBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText("Delete workflow local and remote?");
				alert.setContentText("Proceed?");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					//remove from list
					this.coordinator.removeWorkflow(forAction);
					//keep a copy if its on AWS
					String jsonfile = "workflowJSON/"+forAction.getGuid()+".txt";
					if(this.datamanager.copyMove("clustercolombia", "clustercolombia", jsonfile, "workflowJSON/deleted/"+forAction.getGuid()))
					{
						this.datamanager.delete(jsonfile);
						System.out.println(forAction.getName()+" deleted with success");
						
						
					}
					else {
						System.out.println(forAction.getName()+" delete failed");
						
					}
				} 
			}
		});
		tools.add(deleteBtn,4,0);
		//run button
		this.runBtn = new Button("Run");
		runBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		runBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
				boolean newcluster =true;
				if(this.coordinator.clusterActive().size()>0)
				{
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Confirmation Dialog");
					alert.setHeaderText("Active clusters exisit");
					alert.setContentText("Add to active cluster?");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK){
						newcluster =false;
					} else {
						newcluster =true;
					}
				}
				if(newcluster) {
					//run if new cluster
				this.coordinator.runWorkflow(forAction);
				}
				else{
				this.coordinator.addWorkflowToCluster(forAction);	
				}
			}
		});
		tools.add(runBtn,5,0);
		//stop button
		this.stopBtn = new Button("Stop");
		stopBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		stopBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(stopBtn,6,0);
		//2d map
		this.map2dBtn = new Button("2d map");
		map2dBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		map2dBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			GeoVisualisation gvis = new GeoVisualisation(forAction);
			}
		});
		tools.add(map2dBtn,3,1);
		//res lbl
		Label resLbl = new Label("Results: ");
		tools.add(resLbl,2,1);
		//3d map
		this.map3dBtn = new Button("3d map");
		map3dBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		map3dBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(map3dBtn,4,1);
		//stats
		this.statsBtn = new Button("Stats");
		statsBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		statsBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(statsBtn,5,1);
		//status label
		Label status = new Label("STATUS: ");
				this.statusLbl = new Label("");
				tools.add(status,0,1);
				tools.add(statusLbl,1,1);
				box.getChildren().add(tools);
		return box;
	}
	private VBox configTools()
	{
		VBox tools = new VBox();
		List<String> panels = Arrays.asList("Dataset","Variables", "Clustering method", "N clusters","Date range","Season range","Daily range","Map refresh");

		for(String p : panels)
		{
			VBox toolpanel = new VBox();
			toolpanel.setStyle("-fx-padding: 6;" + "-fx-border-style: solid inside;"
			        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
			        + "-fx-border-color: rgb(220,220,220);");
			if(!p.equals("Map refresh")) {
			Label nameLbl = new Label(p);
			nameLbl.setFont(Font.font (15));
			toolpanel.getChildren().add(nameLbl);
			}
			switch(p)
			{
			case "Map refresh":	
				Button mapUpdateBtn = new Button(p);
				toolpanel.getChildren().add(mapUpdateBtn);
				 mapUpdateBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
				 mapUpdateBtn.setOnAction((event) -> {
				 this.selectionMap = new SelectionMap();
				 mappingBox.getChildren().remove(0);
				 mappingBox.getChildren().remove(0);
				 mappingBox.getChildren().addAll(selectionMap.mapTools(),selectionMap.getMapView());
				});
				break;
			
			case "Dataset":
				
				this.datasetCB = createCombo( AnalysisParameters.enumToStringDataset(),0);
				
				toolpanel.getChildren().add(datasetCB);
				datasetCB.setOnAction((event) -> {
					String data = (String)datasetCB.getSelectionModel().getSelectedItem();
					if(data.equals("MONTHLY_GRID")) this.selectionMap.drawGridMarkers();
				    else this.selectionMap.drawCityMarkers();
					if(forAction!=null&&!this.newWorkflow)
					{
				    //data set should determine variables available
				    AnalysisParameters ap = forAction.getAnalysisParameters();
				    System.out.println("dataset is "+ap.getDataSet());
				    ap.setDataSet(data);
				    System.out.println("dataset changed to "+ap.getDataSet());
					}
				    
				});
				break;
			case "Variables":
				
				//add vertical
				this.variableCB1 = createCombo(AnalysisParameters.enumToStringVariables(),0);
				this.variableCB2 = createCombo(AnalysisParameters.enumToStringVariables(),1);
				this.variableCB3 = createCombo(AnalysisParameters.enumToStringVariables(),2);
				this.variableCB4 = createCombo(AnalysisParameters.enumToStringVariables(),3);
				this.variableCB5 = createCombo(AnalysisParameters.enumToStringVariables(),4);
				this.variableCB6 = createCombo(AnalysisParameters.enumToStringVariables(),5);
				variableCB1.setOnAction((event) -> {updateVariables(0,(String)variableCB1.getSelectionModel().getSelectedItem());});
				variableCB2.setOnAction((event) -> {updateVariables(1,(String)variableCB2.getSelectionModel().getSelectedItem());});
				variableCB3.setOnAction((event) -> {updateVariables(2,(String)variableCB3.getSelectionModel().getSelectedItem());});
				variableCB4.setOnAction((event) -> {updateVariables(3,(String)variableCB4.getSelectionModel().getSelectedItem());});
				variableCB5.setOnAction((event) -> {updateVariables(4,(String)variableCB5.getSelectionModel().getSelectedItem());});
				variableCB6.setOnAction((event) -> {updateVariables(5,(String)variableCB6.getSelectionModel().getSelectedItem());});
				toolpanel.getChildren().addAll(variableCB1,variableCB2,variableCB3,variableCB4,variableCB5,variableCB6);		
				break;
			case "Clustering method":
				this.analysisMethodCB = createCombo(AnalysisParameters.enumToStringAnalysisMethod(),0);
				toolpanel.getChildren().add(analysisMethodCB);
				analysisMethodCB.setOnAction((event) -> {
					if(forAction!=null&&!this.newWorkflow)
					{
				    String aMethod = (String)analysisMethodCB.getSelectionModel().getSelectedItem();
				    AnalysisParameters ap = forAction.getAnalysisParameters();
				    System.out.println("analysis method is "+ap.getAnalysisMethod());
				    ap.setAnalysisMethod(aMethod);
				    System.out.println("analysis method changed to "+ap.getAnalysisMethod());
					}
				    
				});
				break;
			case "N clusters":
				List<String> kclusters =  Arrays.asList("Optimise", "2", "3","4", "5", "6", "7","8", "9", "10", "11","12", "13", "14", "15","16", "17", "18", "19","20");
					    
				this.nClustersCB = createCombo(kclusters,0);
				toolpanel.getChildren().add(nClustersCB);
				nClustersCB.setOnAction((event) -> {
					if(forAction!=null&&!this.newWorkflow)
					{
					
				    String k = (String)nClustersCB.getSelectionModel().getSelectedItem();
				    AnalysisParameters ap = forAction.getAnalysisParameters();
				    System.out.println("nclusters is "+ap.getNClusters());
				    if(k.equals("Optimise")) ap.setNClusters(0);
				    else ap.setNClusters(Integer.parseInt(k));
				    System.out.println("nclusters changed to "+ap.getNClusters());
					}
				    
				});
				break;
			case "Date range":	
				Label stLbl = new Label("Start: ");
				Label enLbl = new Label("End: ");
				startdatePicker = new DatePicker();
				enddatePicker = new DatePicker();
				LocalDate now = LocalDate.now();
				enddatePicker.setValue(now);
				LocalDate first = LocalDate.of(2000, 1, 1);
				startdatePicker.setValue(first);
				GridPane dateGridPane = new GridPane();
				dateGridPane.add(stLbl, 0, 0);
				dateGridPane.add(startdatePicker, 1, 0);
				dateGridPane.add(enLbl, 0, 1);
				dateGridPane.add(enddatePicker, 1, 1);
				toolpanel.getChildren().addAll(dateGridPane);
				
				startdatePicker.setOnAction(this::handleDateRangeEvent);
				
				enddatePicker.setOnAction(this::handleDateRangeEvent);
		        
				break;
			case "Season range":	
				GridPane gridPane = new GridPane();
				Label startLbl = new Label("Start: ");
				Label endLbl = new Label("End: ");
				seasonStartMonth = createCombo(months,0);
				seasonEndMonth = createCombo(months,11);
				seasonStartDay = createCombo(setDays(0),0);
				seasonEndDay = createCombo(setDays(11),30);
				gridPane.add(startLbl, 0, 0);
				gridPane.add(seasonStartMonth, 1, 0);
				gridPane.add(seasonStartDay , 2, 0);
				gridPane.add(endLbl, 0, 1);
				gridPane.add(seasonEndMonth, 1, 1);
				gridPane.add(seasonEndDay, 2, 1);
				toolpanel.getChildren().addAll(gridPane);
				seasonStartMonth.setOnAction(this::handleSeasonRangeEvent);
				seasonEndMonth.setOnAction(this::handleSeasonRangeEvent);
				seasonStartDay.setOnAction(this::handleSeasonRangeEvent);
				seasonEndDay.setOnAction(this::handleSeasonRangeEvent);
				break;
			case "Daily range":
				GridPane dailyGridPane = new GridPane();
				Label sLbl = new Label("Start hour: ");
				Label eLbl = new Label("End hour: ");
				dayStartHour = createCombo(hours,0);
				dayEndHour = createCombo(hours,23);
				dailyGridPane.add(sLbl, 0, 0);
				dailyGridPane.add(dayStartHour, 1, 0);
				dailyGridPane.add(eLbl, 0, 1);
				dailyGridPane.add(dayEndHour, 1, 1);
				toolpanel.getChildren().add(dailyGridPane);
				dayStartHour.setOnAction(this::handleDayRangeEvent);
				dayEndHour.setOnAction(this::handleDayRangeEvent);
				break;
			}
			tools.getChildren().add(toolpanel);
	        
		}
		return tools;
	}
	private void handleDayRangeEvent(Event event)
	{
		if(forAction!=null&&!this.newWorkflow) {
			int starthour = Integer.parseInt(dayStartHour.getValue());
			int endhour = Integer.parseInt(dayEndHour.getValue());
			if(starthour>=endhour)
			{
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Daily range error");
				alert.setHeaderText("Start hour must be before end hour");
				alert.setContentText("Choose different hours");
				alert.showAndWait();
				return;
			}
			else
			{
				AnalysisParameters ap = forAction.getAnalysisParameters();
				System.out.println("Current day range from: "+ap.getDayStartHour()+" to:"+ap.getDayEndHour());
				ap.setDayStartHour(starthour);
				ap.setDayEndHour(endhour);
				System.out.println("New day range from: "+ap.getDayStartHour()+" to:"+ap.getDayEndHour());
			}
		}
	}
	private void handleDateRangeEvent(Event event)
	{
		if(forAction!=null&&!this.newWorkflow) {
		LocalDate s = this.startdatePicker.getValue();
		LocalDate e = this.enddatePicker.getValue();
		if(s.isAfter(e))
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Date range error");
			alert.setHeaderText("Start date must be before end date");
			alert.setContentText("Choose different dates");
			alert.showAndWait();
			return;
		}
		else {
			AnalysisParameters ap = forAction.getAnalysisParameters();
			System.out.println("Current date range from: "+ap.getStartDate()+" to:"+ap.getEndDate());
			
			ap.setStartDate(s.getYear(), s.getMonthValue(), s.getDayOfMonth());
			ap.setEndDate(e.getYear(),e.getMonthValue(),e.getDayOfMonth());
			System.out.println("New date range from: "+ap.getStartDate()+" to:"+ap.getEndDate());
		}
		}
	}
	private void handleSeasonRangeEvent(Event event)
	{
		if(forAction!=null&&!this.newWorkflow) {
		int sMonth	= months.indexOf(seasonStartMonth.getValue())+1;
		int eMonth = months.indexOf(seasonEndMonth.getValue())+1;
		int sDay = Integer.parseInt(seasonStartDay.getValue());
		int eDay = Integer.parseInt(seasonEndDay.getValue());
		if(sMonth>eMonth)
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Season range error");
			alert.setHeaderText("Start month must be before end month");
			alert.setContentText("Choose different months");
			alert.showAndWait();
			return;
		}
		else {
			AnalysisParameters ap = forAction.getAnalysisParameters();
			System.out.println("Season is "+months.get(ap.getSeasonStartMonth()-1)+" "+ ap.getSeasonStartDay() +" to "
					+months.get(ap.getSeasonEndMonth()-1)+" "+ ap.getSeasonEndDay());
			ap.setSeasonEndDay(eDay);
			ap.setSeasonEndMonth(eMonth);
			ap.setSeasonStartDay(sDay);
			ap.setSeasonStartMonth(sMonth);
			System.out.println("Season changed to "+months.get(ap.getSeasonStartMonth()-1)+" "+ ap.getSeasonStartDay() +" to "
					+months.get(ap.getSeasonEndMonth()-1)+" "+ ap.getSeasonEndDay());
			
		}
		}
	}
	private void updateVariables(int i,String v)
	{
		if(forAction!=null&&!this.newWorkflow)
		{
			AnalysisParameters ap = forAction.getAnalysisParameters();
			System.out.println("variables are "+ap.getVariablesAsString());
			ap.setOneVariable(i, v);
			System.out.println("variables selected changed to "+ap.getVariablesAsString());
		}
	}
	private ComboBox<String> createCombo(List<String> options,int initIndex)
	{
		ObservableList<String> opts = FXCollections.observableArrayList();
		for(String option : options) opts.add(option);
		ComboBox<String> comboBox = new ComboBox<String>(opts);
		comboBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		comboBox.getSelectionModel().select(initIndex);
		
            
        return comboBox;
	}
	private void addCurrentWorkflowStatus()
	{
		
		Label lbl = new Label("Editor");
		lbl.setFont(Font.font (20));
		wfEditorBox.getChildren().addAll(lbl ,workFlowTools());
	}
	@SuppressWarnings("unchecked")
	private void addWorkflowList(TableView table)
	{
		Label title = new Label("Workflows");
		title.setFont(Font.font (20));
		Button addWorkflowBtn = new Button("Create new workflow");
		addWorkflowBtn.setMaxWidth(Double.MAX_VALUE);
		addWorkflowBtn.setOnAction(this::handler);
		List<TableColumn> tabColumns = new ArrayList<TableColumn>();
		tabColumns.add(addColumn("name",250,"name"));
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null) {

                this.forAction = (Workflow) table.getSelectionModel().getSelectedItem();
                
        		//data.setAll(forAction);
        		//set the map foraction selected
        		this.selectionMap.setForAction(forAction);
        		updateUIWithWorkflow();
		    }
		});
		table.setItems(coordinator.monitorWorkflowData);
		table.getColumns().addAll(tabColumns);
	    wfSelectorBox.getChildren().addAll(title,addWorkflowBtn,workflowtable);
	}
	private void enableDisableNodes()
	{
		String status = statusLbl.getText();
		//POSSIBLE VALUES: "PENDING,CANCEL_PENDING, RUNNING,COMPLETED,CANCELLED,FAILED,INTERRUPTED"//ALSO INITIALISED 
		if(status.equals("INITIALISED")||status.equals("CANCELLED")||status.equals("FAILED")||status.equals("INTERRUPTED"))
		{
			//full editing permitted no results
			//disabled stop 2d map 3d map stats
			this.map2dBtn.setDisable(true);
			this.map3dBtn.setDisable(true);
			this.statsBtn.setDisable(true);
			this.stopBtn.setDisable(true);
			
			this.copyBtn.setDisable(false);
			this.saveBtn.setDisable(false);
			this.runBtn.setDisable(false);
			this.configTools.setDisable(false);
			this.mappingBox.setDisable(false);
		}
		if(status.equals("COMPLETED"))
		{
			//editing disabled
			this.configTools.setDisable(true);
			this.mappingBox.setDisable(true);
			//results enabled
			this.map2dBtn.setDisable(false);
			this.map3dBtn.setDisable(false);
			this.statsBtn.setDisable(false);
			this.stopBtn.setDisable(true);
			
			this.copyBtn.setDisable(false);
			this.saveBtn.setDisable(true);
			this.runBtn.setDisable(true);
		}
		if(status.equals("PENDING")||status.equals("CANCEL_PENDING")||status.equals("RUNNING"))
		{
			//only copy and stop
			this.configTools.setDisable(true);
			this.mappingBox.setDisable(true);
			
			this.map2dBtn.setDisable(true);
			this.map3dBtn.setDisable(true);
			this.statsBtn.setDisable(true);
			this.stopBtn.setDisable(false);
			
			this.copyBtn.setDisable(false);
			this.saveBtn.setDisable(true);
			this.runBtn.setDisable(true);
		}
		
	}
	private void updateUIWithWorkflow()
	{
		//event handlers to ignore changes
		this.newWorkflow = true;
		this.selectionMap.setDraw(false);
		wfNameTextField.setText(forAction.getName());
		statusLbl.setText(forAction.getStatus());
		enableDisableNodes();
		AnalysisParameters ap = forAction.getAnalysisParameters();
		this.datasetCB.getSelectionModel().select(ap.enumToStringDataset().indexOf(ap.getDataSet()));
		//data set should determine possible variables
		this.variableCB1.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(0)));
		this.variableCB2.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(1)));
		this.variableCB3.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(2)));
		this.variableCB4.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(3)));
		this.variableCB5.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(4)));
		this.variableCB6.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(5)));
		this.analysisMethodCB.getSelectionModel().select(ap.enumToStringAnalysisMethod().lastIndexOf(ap.getAnalysisMethod()));
		int kIndex = ap.getNClusters();
		if(kIndex>0) kIndex -=1;
		this.nClustersCB.getSelectionModel().select(kIndex);
		
		startdatePicker.setValue(ap.getStartDate());
		enddatePicker.setValue(ap.getEndDate());

		seasonStartMonth.getSelectionModel().select(ap.getSeasonStartMonth()-1);
		seasonEndMonth.getSelectionModel().select(ap.getSeasonEndMonth()-1);
		seasonStartDay.getSelectionModel().select(ap.getSeasonStartDay()-1);
		seasonEndDay.getSelectionModel().select(ap.getSeasonEndDay()-1);
		
		dayStartHour.getSelectionModel().select(ap.getDayStartHour()-1);
		dayEndHour.getSelectionModel().select(ap.getDayEndHour()-1);
		
		this.selectionMap.addMapShapeFromWorkflow();
		this.newWorkflow = false;
	}
	private void handler(ActionEvent event) {
		String cmd = ((Button)event.getSource()).getText();
		switch(cmd) {
		case "Create new workflow":
			Workflow wf = new Workflow();
			coordinator.addWorkflow(wf);
			this.workflowtable.getSelectionModel().select(wf);

			break;
		}
	}
	private boolean alertMessage(String text)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText(text);
		alert.setContentText("Proceed?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
		   return true;
		} else {
		    return false;
		}
	}
	private TableColumn addColumn(String name,int width,String property)
	{
		 TableColumn col = new TableColumn(name);
		 col.setMinWidth(width);
	     col.setCellValueFactory(new PropertyValueFactory<Workflow, String>(property));
	     return col;
	}
	
	
}
