package org.rolson.emr.emrcycle1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;



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
import netscape.javascript.JSObject;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.shapes.Rectangle;
import com.lynden.gmapsfx.shapes.RectangleOptions;

public class GUIWorkflowBuilder implements MapComponentInitializedListener {
	private ClusterCoordinator coordinator;
	private HBox mainBox;
	private VBox wfSelectorBox;
	private VBox wfEditorBox;
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
	ComboBox<String> analysisMethodCB;
	ComboBox<String> dayStartHour;
	ComboBox<String> dayEndHour;
	private boolean newWorkflow;
	private Label statusLbl;
	
	
	private Polygon selectionPolygon;
	private Rectangle selectionRectangle;
	private MVCArray selectionPointObs;
	private boolean drawRectangle;
	private boolean draw;
	private List<LatLong> selectionLatLongs;

	private GoogleMapView mapView; 
	private GoogleMap map;
	
	private TableView<Workflow> workflowtable = new TableView<Workflow>();
	private TableView<Workflow> currentworkflow = new TableView<Workflow>();
	private  ObservableList<Workflow> data = FXCollections.observableArrayList();
	private Workflow forAction;//currently editing this workflow
	int[] daysInMonths = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	List<String> months = Arrays.asList("January","February","March","April","May","June","July","August","September","October","November","December");

	List<String> hours;
	
	public GUIWorkflowBuilder(int index, String name,TabPane tabpane,ClusterCoordinator coord)
	{
		mapView = new GoogleMapView(null,"AIzaSyCWfTPB17ZBhMclyRk9j__M0BlayA_xUuA"); 
		
		mapView.addMapInializedListener(this);
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
		addCurrentWorkflowStatus();
		addWorkflowDefinitionTools();
		mainBox.getChildren().addAll(wfSelectorBox,wfEditorBox);
		return mainBox;
	}
	private void addWorkflowDefinitionTools()
	{
		HBox def = new HBox();
		
		BorderPane bp = new BorderPane();
		VBox mappingBox = new VBox();
		mappingBox.getChildren().addAll(mapTools(),mapView);
		bp.setRight(mappingBox);
		bp.setLeft(configTools());
		def.getChildren().setAll(bp);
		wfEditorBox.getChildren().add(def);
		
	}
	private HBox workFlowTools()
	{
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
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
			data.setAll(forAction);
			this.coordinator.updateWorkflowList();
		   }
		});
		tools.add(wfNameTextField, 1, 0);
		
		//copy button
		
		Button copyBtn = new Button("Copy");
		copyBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		copyBtn.setOnAction((event) -> {
		    // Button was clicked, do something...
			if(forAction!=null)
			{
			Workflow wf = new Workflow();
			coordinator.addWorkflow(wf);
			this.workflowtable.getSelectionModel().select(wf);
			}
		});
		tools.add(copyBtn,2,0);
		//save button
		Button saveBtn = new Button("Save");
		saveBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		saveBtn.setOnAction((event) -> {

			if(forAction!=null)
			{
			
			}
		});
		tools.add(saveBtn,3,0);
		//run button
		Button runBtn = new Button("Run");
		runBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		runBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(runBtn,4,0);
		//stop button
		Button stopBtn = new Button("Stop");
		stopBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		stopBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(stopBtn,5,0);
		//2d map
		Button map2dBtn = new Button("2d map");
		map2dBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		map2dBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(map2dBtn,3,1);
		//res lbl
		Label resLbl = new Label("Results: ");
		tools.add(resLbl,2,1);
		//3d map
		Button map3dBtn = new Button("3d map");
		map3dBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		map3dBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(map3dBtn,4,1);
		//stats
		Button statsBtn = new Button("Stats");
		statsBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		statsBtn.setOnAction((event) -> {
		    
			if(forAction!=null)
			{
			
			}
		});
		tools.add(statsBtn,5,1);
		//status label
				this.statusLbl = new Label("STATUS: ");
				tools.add(statusLbl,0,1);
				box.getChildren().add(tools);
		return box;
	}
	private VBox configTools()
	{
		VBox tools = new VBox();
		List<String> panels = Arrays.asList("Dataset","Variables", "Analysis method","Date range","Season range","Daily range");

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
				
				break;
			
			case "Dataset":
				
				this.datasetCB = createCombo( AnalysisParameters.enumToStringDataset(),0);
				toolpanel.getChildren().add(datasetCB);
				datasetCB.setOnAction((event) -> {
					if(forAction!=null&&!this.newWorkflow)
					{
				    String data = (String)datasetCB.getSelectionModel().getSelectedItem();
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
				variableCB1.setOnAction((event) -> {updateVariables(0,(String)variableCB1.getSelectionModel().getSelectedItem());});
				variableCB2.setOnAction((event) -> {updateVariables(1,(String)variableCB2.getSelectionModel().getSelectedItem());});
				variableCB3.setOnAction((event) -> {updateVariables(2,(String)variableCB3.getSelectionModel().getSelectedItem());});
				toolpanel.getChildren().addAll(variableCB1,variableCB2,variableCB3);		
				break;
			case "Analysis method":
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
	        //tools.getChildren().add(comboBox);
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
                wfNameTextField.setText(forAction.getName());
        		data.setAll(forAction);
        		updateUIWithWorkflow();
		    }
		});
		table.setItems(coordinator.monitorWorkflowData);
		table.getColumns().addAll(tabColumns);
	    wfSelectorBox.getChildren().addAll(title,addWorkflowBtn,workflowtable);
	}
	private void updateUIWithWorkflow()
	{
		//event handlers to ignore changes
		this.newWorkflow = true;
		this.draw =false;
		AnalysisParameters ap = forAction.getAnalysisParameters();
		this.datasetCB.getSelectionModel().select(ap.enumToStringDataset().indexOf(ap.getDataSet()));
		this.variableCB1.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(0)));
		this.variableCB2.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(1)));
		this.variableCB3.getSelectionModel().select(ap.enumToStringVariables().indexOf(ap.getVariablesAsString().get(2)));
		this.analysisMethodCB.getSelectionModel().select(ap.enumToStringAnalysisMethod().lastIndexOf(ap.getAnalysisMethod()));
		
		
		startdatePicker.setValue(ap.getStartDate());
		enddatePicker.setValue(ap.getEndDate());

		seasonStartMonth.getSelectionModel().select(ap.getSeasonStartMonth()-1);
		seasonEndMonth.getSelectionModel().select(ap.getSeasonEndMonth()-1);
		seasonStartDay.getSelectionModel().select(ap.getSeasonStartDay()-1);
		seasonEndDay.getSelectionModel().select(ap.getSeasonEndDay()-1);
		
		dayStartHour.getSelectionModel().select(ap.getDayStartHour()-1);
		dayEndHour.getSelectionModel().select(ap.getDayEndHour()-1);
		
		addMapShapeFromWorkflow();
		this.newWorkflow = false;
	}
	private void handler(ActionEvent event) {
		String cmd = ((Button)event.getSource()).getText();
		switch(cmd) {
		case "Create new workflow":
			Workflow wf = new Workflow();
			coordinator.addWorkflow(wf);
			this.workflowtable.getSelectionModel().select(wf);
			updateUIWithWorkflow();
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
	private void addMapShapeFromWorkflow()
	{
		clearSelection();
		this.selectionLatLongs = forAction.getAnalysisParameters().getSelectionCoords();
		for(LatLong ll : this.selectionLatLongs)
		{
			this.selectionPointObs.push(ll);
		}
		if(forAction.getAnalysisParameters().getSelectionShape().equals("polygon"))
		{
			drawPolygon();
		}
		else
		{
			drawRectangle(this.selectionLatLongs.get(0),this.selectionLatLongs.get(2));
		}
	}
	private HBox mapTools()
	{
		HBox mapControl = new HBox();
		mapControl.setSpacing(5);
		mapControl.setPadding(new Insets(10, 0, 0, 10));
		mapControl.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: rgb(220,220,220);");
		Button clearBtn = new Button("Clear selection");
		 clearBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		 clearBtn.setOnAction((event) -> {
			this.clearSelection();
		});
		 Button polyBtn = new Button("Selection polygon");
		 polyBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		 polyBtn.setOnAction((event) -> {
			 clearSelection();
			 this.draw =false;
			 drawPolygon();
			 
		});
		 Button polyDrawBtn = new Button("Draw polygon");
		 polyDrawBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		 polyDrawBtn.setOnAction((event) -> {
			 clearSelection();
			this.drawRectangle =false;
			this.draw =true;
		});
		 Button rectBtn = new Button("Selection rectangle");
		 rectBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		 rectBtn.setOnAction((event) -> {
			 this.draw =false;
			 clearSelection();
			 setUpMaxRectangle();
		     
		});
		 Button rectDrawBtn = new Button("Draw rectangle");
		 rectDrawBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		 rectDrawBtn.setOnAction((event) -> {
			 clearSelection();
			 this.drawRectangle =true;
			 this.draw =true;
		});
		 mapControl.getChildren().addAll(polyBtn,polyDrawBtn,rectBtn,rectDrawBtn,clearBtn);
		return mapControl;
	}
	private void clearSelection()
	{
		map.removeMapShape(this.selectionRectangle);
		map.removeMapShape(this.selectionPolygon);
		this.selectionPointObs.clear();
		this.selectionLatLongs.clear();
		this.selectionRectangle = new Rectangle();
		this.selectionPolygon = new Polygon();
	}
	private void setUpMaxRectangle()
	{
		LatLong topleft = new LatLong(11.422499, -78.362942);
		LatLong bottomright = new LatLong(-4.533734, -65.735365);
		drawRectangle(topleft, bottomright);
	}
	private void drawPolygon()
	{
		if(!this.draw)
		{
			setupMaxPolygonPoints();
		}
		else
		{
			//point data has been collected by map events
		}
		PolygonOptions options = new PolygonOptions()
				   .paths(this.selectionPointObs)
				   .strokeColor("black")
	                .strokeWeight(2)
	                .fillColor("null")
	                .editable(true).draggable(true);
		   
		   this.selectionPolygon =new Polygon(options);
		   map.addMapShape(selectionPolygon);
		 //if not in draw mode update from the shape data
	        if(!this.draw) updateSelectionFromShape();
	}
	private void drawRectangle(LatLong topleft, LatLong bottomright)
	{
		LatLongBounds llb = new LatLongBounds( topleft,bottomright);
        RectangleOptions rOpts = new RectangleOptions()
                .bounds(llb)
                .strokeColor("black")
                .strokeWeight(2)
                .fillColor("null")
                .editable(true).draggable(true);

        this.selectionRectangle = new Rectangle(rOpts);
        map.addMapShape(this.selectionRectangle);
        //if not in draw mode update from the shape data
        if(!this.draw) updateSelectionFromShape();
        //convertRectangleToPolygon(topleft,bottomright);
        
	}
	private void convertRectangleToPolygon(LatLong topleft, LatLong bottomright)
	{
		LatLong topright =new LatLong(topleft.getLatitude(),bottomright.getLongitude());
		LatLong bottomleft = new LatLong(bottomright.getLatitude(),topleft.getLongitude());
        this.selectionLatLongs.clear();
        
        this.selectionLatLongs.add(topleft);
        this.selectionLatLongs.add(topright);
        this.selectionLatLongs.add(bottomright);
        this.selectionLatLongs.add(bottomleft);
        this.selectionLatLongs.add(topleft);
        //test the rectangle as a polygon
//        this.selectionPointObs.clear();
//        this.selectionPointObs.push(topleft);
//        this.selectionPointObs.push(topright);
//        this.selectionPointObs.push(bottomright);
//        this.selectionPointObs.push(bottomleft);
//        this.selectionPointObs.push(topleft);
//        //test as polygon
//        PolygonOptions options = new PolygonOptions()
//				   .paths(this.selectionPointObs)
//				   .strokeColor("black")
//	                .strokeWeight(2)
//	                .fillColor("null")
//	                .editable(true).draggable(true);
//        this.selectionPolygon =new Polygon(options);
//		   map.addMapShape(this.selectionPolygon);
//        updateSelection();
	}
	private void setupMaxPolygonPoints()
	{
		LatLong p1 = new LatLong(8.766635, -78.221568);
		LatLong p2 = new LatLong(1.024341, -79.778153);
		LatLong p3 = new LatLong(-4.519759, -69.824647);
		LatLong p4 = new LatLong(1.114824, -66.675034);
		LatLong p5 = new LatLong(6.280859, -67.190206);
		LatLong p6 = new LatLong(13.615007, -71.219707);
		this.selectionPointObs.clear();
		this.selectionPointObs.push(p1);
		this.selectionPointObs.push(p2);
		this.selectionPointObs.push(p3);
		this.selectionPointObs.push(p4);
		this.selectionPointObs.push(p5);
		this.selectionPointObs.push(p6);
		this.selectionLatLongs.clear();
		this.selectionLatLongs.add(p1);
		this.selectionLatLongs.add(p2);
		this.selectionLatLongs.add(p3);
		this.selectionLatLongs.add(p4);
		this.selectionLatLongs.add(p5);
		this.selectionLatLongs.add(p6);
		
		
	}
	private void updateSelection(String shapetype)
	{
		
		if(forAction!=null) {
			forAction.getAnalysisParameters().setSelectionCoords(this.selectionLatLongs);
			forAction.getAnalysisParameters().setSelectionShape(shapetype);
			System.out.println("selection boundary updated");
		}
	}
	private void updateSelectionFromShape()
	{
		if(forAction!=null) {
			JSObject points;
			points = this.selectionPolygon.getPath().getArray();
			String pString = points.toString();
			LatLongBounds llb = this.selectionRectangle.getBounds();
			String shape="";
			if(!points.toString().equals(""))
			{
				//polygon is defined
				shape="polygon";
				this.selectionLatLongs.clear();
				double lat=0;
				double lon =0;
				pString = pString.replace("(", "");
				pString = pString.replace(")", "");
				String[] c = pString.split(",");
				for(int s=0;s<c.length;s++) {
					if(s%2==0) {
						lat= Double.parseDouble(c[s]);
						
					}
					else {
						lon= Double.parseDouble(c[s]);
						this.selectionLatLongs.add(new  LatLong (lat,lon));
					}
				}
				
			}
			if(llb.getJSObject()!=null)
			{
				//rectangle exists
				shape="rectangle";
				LatLong ne =llb.getNorthEast();
				LatLong sw = llb.getSouthWest();
				LatLong nw = new LatLong(ne.getLatitude(),sw.getLongitude());
				LatLong se = new LatLong(sw.getLatitude(),ne.getLongitude());
				this.selectionLatLongs.clear();
				this.selectionLatLongs.add(nw);
				this.selectionLatLongs.add(ne);
				this.selectionLatLongs.add(se);
				this.selectionLatLongs.add(sw);
				this.selectionLatLongs.add(nw);
			}
		updateSelection(shape);
		}
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
        this.selectionRectangle = new Rectangle();
        this.selectionPolygon = new Polygon();
        this.selectionPointObs = new MVCArray();
        this.drawRectangle = true;
        this.draw =false;
        this.selectionLatLongs = new ArrayList<LatLong>();
        map = mapView.createMap(mapOptions);
        
        mapView.addEventHandler(MouseEvent.MOUSE_CLICKED, 
                new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mEvent) { 
            	updateSelectionFromShape();
            	System.out.println("mouse click detected! " + mEvent.getSource());
            	
            };
        });
        
        
        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
        	if(this.draw) {
        	   LatLong latLong = event.getLatLong();
        	   this.selectionPointObs.push(latLong);
        	   this.selectionLatLongs.add(latLong);
        	   //todo add custom markers? 
        	   if(this.drawRectangle)
        	   {
        		   if(this.selectionPointObs.getLength()==2)
        		   {
        			   	LatLong topleft =new LatLong(this.selectionLatLongs.get(0).getLatitude(),this.selectionLatLongs.get(0).getLongitude());
        				LatLong bottomright = new LatLong(this.selectionLatLongs.get(1).getLatitude(),this.selectionLatLongs.get(1).getLongitude());
        				drawRectangle(topleft,bottomright);
        		   }
        	   }
        	   else
        	   {
        		   //draw a polygon
        		   if(this.selectionPointObs.getLength()>2)
        		   {
        			  map.removeMapShape(selectionPolygon);
        			  drawPolygon();
        			  
        		   }
        	   }
        	}
        	
        	});
        
    }  
	
}
