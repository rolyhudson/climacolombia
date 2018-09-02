package org.rolson.emr.emrcycle1;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
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
import com.lynden.gmapsfx.util.MarkerImageFactory;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import netscape.javascript.JSObject;


public class SelectionMap implements MapComponentInitializedListener{
	private Workflow forAction;
	
	private Polygon selectionPolygon;
	private Rectangle selectionRectangle;
	private MVCArray selectionPointObs;
	private boolean drawRectangle;
	private boolean draw;
	
	private List<LatLong> selectionLatLongs;
	private List<Marker> mapMarkers;

	private GoogleMapView mapView; 
	private GoogleMap map;
	
	public SelectionMap()
	{
		
		mapView = new GoogleMapView("en","AIzaSyAj9C3s1dVtL3WA8BRsSWKoutIFYdJlfBc");
		mapView.addMapInializedListener(this);
	}
	
	public GoogleMapView getMapView()
	{
		
		return mapView;
	}
	public void setDraw(boolean drawOnOff)
	{
		this.draw = drawOnOff;
	}
	public void setForAction(Workflow action)
	{
		this.forAction = action;
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
        this.mapMarkers = new ArrayList<Marker>();
        map = mapView.createMap(mapOptions);
        drawGridMarkers();
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
	public void addMapShapeFromWorkflow()
	{
		clearSelection();
		this.selectionLatLongs = forAction.getAnalysisParameters().getSelectionCoordsLatLon();
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
	public VBox mapTools()
	{
		VBox box = new VBox();
		Label nameLbl = new Label("Selection");
		nameLbl.setFont(Font.font (15));
		box.getChildren().add(nameLbl);
		box.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
		        + "-fx-border-width: 1;" + "-fx-border-insets: 2;"
		        + "-fx-border-color: rgb(220,220,220);");
		HBox mapControl = new HBox();
		
		
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
			 setupMaxPolygonPoints();
			 
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
		 box.getChildren().add(mapControl);
		return box;
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
		
		PolygonOptions options = new PolygonOptions()
				   .paths(this.selectionPointObs)
				   .strokeColor("black")
	                .strokeWeight(2)
	                .fillColor("null")
	                .editable(true).draggable(true);
		   
		   this.selectionPolygon =new Polygon(options);
		   map.addMapShape(selectionPolygon);
		 //if not in draw mode update from the shape data
	     updateSelectionFromShape();
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
        updateSelectionFromShape();
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
		drawPolygon();
		
	}
	private void addMarker(LatLong location,InfoWindow myInfoWindow)
	{
		MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(location);
       
        markerOptions1.icon("http://rolson.org/geometry/icons/tinyPoint.png");
        Marker marker = new Marker(markerOptions1);
        this.mapMarkers.add(marker);
        map.addMarker(marker);
        map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> {
            myInfoWindow.open(map,marker);
        });
	}
	private InfoWindow makeInfoWindow(String name, String lat, String lon,String ele) {
		InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
        infoWindowOptions.content(name+"<br>"
                                + "lat: "+lat+"<br>"
                                + "lon: "+lon+"<br>"
                                + "ele: "+ele);

        InfoWindow infoWindow = new InfoWindow(infoWindowOptions);
        return infoWindow;
	}
	public void drawGridMarkers()
	{
		removeMarkers();
		String data = getFileContents("src/resources/colombiaGrid.csv");
		String lines[] = data.split("\\r?\\n");
		for(String line : lines) {
			String parts[] = line.split(",");
			addMarker(new LatLong(Double.parseDouble(parts[1]),Double.parseDouble(parts[0])),
					makeInfoWindow("grid point", parts[1], parts[0],parts[2]));
		}
	}
	public void drawCityMarkers()
	{
		removeMarkers();
		String data = getFileContents("src/resources/cities.csv");
		String lines[] = data.split("\\r?\\n");
		for(String line : lines) {
			String parts[] = line.split(",");
			addMarker(new LatLong(Double.parseDouble(parts[1]),Double.parseDouble(parts[2])),
					makeInfoWindow(parts[0], parts[1], parts[2],parts[3]));
		}
	}
	public void removeMarkers()
	{
		for(Marker m : this.mapMarkers) {
			map.removeMarker(m);
		}
	}
	private String getFileContents(String path)
	{
		try(FileInputStream inputStream = new FileInputStream(path)) {     
		    String everything = IOUtils.toString(inputStream);
		    return everything;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
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
}
