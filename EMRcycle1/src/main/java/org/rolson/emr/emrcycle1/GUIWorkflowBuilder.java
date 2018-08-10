package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;

public class GUIWorkflowBuilder implements MapComponentInitializedListener {
	private ClusterCoordinator coordinator;
	private HBox mainBox;
	private VBox wfSelectorBox;
	private VBox wfEditorBox;

	private GoogleMapView mapView; 
	private GoogleMap map;
	private TableView<Workflow> workflowtable = new TableView<Workflow>();
	private TableView<Workflow> currentworkflow = new TableView<Workflow>();
	private  ObservableList<Workflow> data = FXCollections.observableArrayList();
	private Workflow forAction;//currently editing this workflow
	int[] daysInMonths = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	List<String> days;
	List<String> hours;
	
	public GUIWorkflowBuilder(int index, String name,TabPane tabpane,ClusterCoordinator coord)
	{
		mapView = new GoogleMapView(); 
		mapView.addMapInializedListener(this);
		coordinator = coord;
		setDaysHours();
		Tab tab = new Tab();
		tab.setText(name);
		tab.setContent(setLayout());
		tabpane.getTabs().add(index, tab);
		
		
	}
	private void setDaysHours()
	{
		days = new ArrayList<String>();
		for(int i=1;i<32;i++)days.add(Integer.toString(i));
		
		hours = new ArrayList<String>();
		for(int i=1;i<25;i++)hours.add(Integer.toString(i));
	}
	private HBox setLayout()
	{
		mainBox = new HBox();
		wfSelectorBox = new VBox();
		wfSelectorBox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: grey;");
		mainBox.setSpacing(5);
		mainBox.setPadding(new Insets(10, 0, 0, 10));
		addWorkflowList(this.workflowtable);
		
		wfEditorBox = new VBox();
		wfEditorBox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: grey;");
		addCurrentWorkflowStatus(this.currentworkflow);
		addWorkflowDefinitionTools();
		mainBox.getChildren().addAll(wfSelectorBox,wfEditorBox);
		return mainBox;
	}
	private void addWorkflowDefinitionTools()
	{
		HBox def = new HBox();
		
		BorderPane bp = new BorderPane();
		bp.setRight(mapView);
		bp.setLeft(configTools());
		def.getChildren().setAll(bp);
		wfEditorBox.getChildren().add(def);
		
	}
	private VBox configTools()
	{
		VBox tools = new VBox();
		List<String> panels = Arrays.asList("Workflow name","Copy" ,"Dataset","Variables", "Analysis method","Date range","Season range","Daily range");

		for(String p : panels)
		{
			VBox toolpanel = new VBox();
			toolpanel.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
			        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
			        + "-fx-border-color: rgb(220,220,220);");
			if(!p.equals("Copy")) {
			Label nameLbl = new Label(p);
			nameLbl.setFont(Font.font (20));
			toolpanel.getChildren().add(nameLbl);}
			
			switch(p)
			{
			case "Copy":	
				Button copyBtn = new Button("Copy");
				copyBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
				copyBtn.setOnAction((event) -> {
				    // Button was clicked, do something...
					Workflow wf = new Workflow(forAction.getName()+"copy");
					coordinator.addWorkflow(wf);
					this.workflowtable.getSelectionModel().select(wf);
				});
				toolpanel.getChildren().add(copyBtn);
				break;
			case "Workflow name":
				TextField textField = new TextField ();
				toolpanel.getChildren().add(textField);
				textField.textProperty().addListener((obs, oldText, newText) -> {
				   // System.out.println("Text changed from "+oldText+" to "+newText);
					forAction.setName(newText);
					data.setAll(forAction);
					this.coordinator.updateWorkflowList();
				});
				//need event on text change
				break;
			case "Dataset":
				List<String> opts = Arrays.asList("monthly grid","hourly cities");
				toolpanel.getChildren().add(createCombo( AnalysisParameters.enumToStringDataset(),0));
				break;
			case "Variables":
				List<String> vars = Arrays.asList("temperature","relative humidity","windspeed");
				//add vertical
				toolpanel.getChildren().addAll(createCombo(AnalysisParameters.enumToStringVariables(),0),
						createCombo(AnalysisParameters.enumToStringVariables(),1),
						createCombo(AnalysisParameters.enumToStringVariables(),2));
				break;
			case "Analysis method":
				List<String> meths = Arrays.asList("k means","bi k means","power iteration clustering","guassian mixture");
				toolpanel.getChildren().add(createCombo(AnalysisParameters.enumToStringAnalysisMethod(),0));
				break;
			case "Date range":	
				Label stLbl = new Label("Start: ");
				Label enLbl = new Label("End: ");
				DatePicker startdatePicker = new DatePicker();
				DatePicker enddatePicker = new DatePicker();
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
				break;
			case "Season range":	
				List<String> months = Arrays.asList("January","February","March","April","May","June","July","August","September","October","November","December");
				GridPane gridPane = new GridPane();
				Label startLbl = new Label("Start: ");
				Label endLbl = new Label("End: ");
				gridPane.add(startLbl, 0, 0);
				gridPane.add(createCombo(months,0), 1, 0);
				gridPane.add(createCombo(days,0), 2, 0);
				gridPane.add(endLbl, 0, 1);
				gridPane.add(createCombo(months,11), 1, 1);
				gridPane.add(createCombo(days,30), 2, 1);
				toolpanel.getChildren().addAll(gridPane);
				break;
			case "Daily range":
				GridPane dailyGridPane = new GridPane();
				Label sLbl = new Label("Start hour: ");
				Label eLbl = new Label("End hour: ");
				dailyGridPane.add(sLbl, 0, 0);
				dailyGridPane.add(createCombo(hours,0), 1, 0);
				dailyGridPane.add(eLbl, 0, 1);
				dailyGridPane.add(createCombo(hours,23), 1, 1);
				toolpanel.getChildren().add(dailyGridPane);
				break;
			}
			tools.getChildren().add(toolpanel);
	        //tools.getChildren().add(comboBox);
		}
		return tools;
	}
	
	private ComboBox createCombo(List<String> options,int initIndex)
	{
		ObservableList<String> opts = FXCollections.observableArrayList();
		for(String option : options) opts.add(option);
		ComboBox comboBox = new ComboBox(opts);
		comboBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		comboBox.getSelectionModel().select(initIndex);
		comboBox.setOnAction(this::defintitionUpdate);
            
        return comboBox;
	}
	private void addCurrentWorkflowStatus(TableView table)
	{
		//table.maxHeight(100);
		table.minHeight(100);
		Label statusLbl = new Label("Editor");
		statusLbl.setFont(Font.font (20));
		List<TableColumn> tabColumns = new ArrayList<TableColumn>();
		List<String> cols = Arrays.asList("name", "status" ,"creationDate", "awsID","appType");
        for(String col : cols)
        {
        	tabColumns.add(addColumn(col,150,col));
        }
        
        table.setItems(data);
		table.getColumns().addAll(tabColumns);
		
		wfEditorBox.getChildren().addAll(statusLbl ,table);
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
        		data.setAll(forAction);
		    }
		});
		table.setItems(coordinator.monitorWorkflowData);
		table.getColumns().addAll(tabColumns);
	    wfSelectorBox.getChildren().addAll(title,addWorkflowBtn,workflowtable);
	}
	private void defintitionUpdate(Event event) {
		ComboBox cb = (ComboBox)event.getSource();
		
		
	}
	private void handler(ActionEvent event) {
		String cmd = ((Button)event.getSource()).getText();
		switch(cmd) {
		case "Create new workflow":
			Workflow wf = new Workflow("new workflow");
			
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
	@Override
    public void mapInitialized() {

        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();
        
        mapOptions.center(new LatLong(3.791898,-74.1868241))
        		.mapType(MapTypeIdEnum.TERRAIN)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(5);
                   
        map = mapView.createMap(mapOptions);

    }  
}
