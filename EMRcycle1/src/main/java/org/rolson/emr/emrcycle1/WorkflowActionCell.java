package org.rolson.emr.emrcycle1;

import javafx.scene.control.TableCell;
import java.util.Optional;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;

public class WorkflowActionCell extends TableCell<Workflow, Boolean> {
	final Button cellButton;
	 ClusterCoordinator coord;
	 WorkflowActionCell(final TableView tblView,String name,ClusterCoordinator cc){
	    	
	    	cellButton = new Button(name);
	    	coord = cc;
	        cellButton.setOnAction(new EventHandler<ActionEvent>(){
	        	
	            @Override
	            public void handle(ActionEvent t) {
	            	String cmd = ((Button)t.getSource()).getText();
	                int selectedIndex = getTableRow().getIndex();
	                Workflow forAction = (Workflow) tblView.getItems().get(selectedIndex);
	                switch(cmd)
	                {
	                	case "run":
	                		//check status 
	                		if(forAction.getStatus().equals("INITIALISED")
	                				||forAction.getStatus().equals("CANCELLED")
	                				||forAction.getStatus().equals("COMPLETED")
	                				||forAction.getStatus().equals("INTERUPTED")
	                				||forAction.getStatus().equals("FAILED"))
	                		{
	                			if(alertMessage("Run workflow: "+forAction.getName()))
	                			{
	                				//run or rerun step
	                				coord.runWorkflow(forAction);
	                			}
	                		}
	                		break;
	                	case "stop":
	                		if(forAction.getStatus().equals("PENDING")
	                				||forAction.getStatus().equals("RUNNING"))
	                		{
	                			if(alertMessage("Stopping workflow: "+forAction.getName()))
	                			{
	                				//cancel step
	                				//coord.stopWorkflow(forAction);
	                			}
	                		}
	                		 break;
	                	
	                	case "stats":
	                		if(forAction.getStatus().equals("COMPLETED"))
	                		{
	                			if(alertMessage("Generate statisitcal visualisation for: "+forAction.getName()))
	        					{
	            					//run stats
	                				//coord.visualStatsWorkflow(forAction);
	        					}
	                		}
	                		break;
	                	case "edit":
	                		if(forAction.getStatus().equals("INITIALISED")
	                				||forAction.getStatus().equals("CANCELLED")
	                				||forAction.getStatus().equals("COMPLETED")
	                				||forAction.getStatus().equals("INTERUPTED")
	                				||forAction.getStatus().equals("FAILED"))
	                		{
	                			if(alertMessage(forAction.getName()))
	        					{
	            					//edit
	                				//coord.editWorkflow(forAction);
	        					}
	                		}
	                		break;
	                	case "copy":
	                		if(forAction.getStatus().equals("INITIALISED")
	                				||forAction.getStatus().equals("CANCELLED")
	                				||forAction.getStatus().equals("COMPLETED")
	                				||forAction.getStatus().equals("INTERUPTED")
	                				||forAction.getStatus().equals("FAILED"))
	                		{
	                			if(alertMessage(forAction.getName()))
	        					{
	                				//edit
	                				//coord.copyWorkflow(forAction);
	        					}
	                		}
	                		break;
	                }
	            }
	            });
	    }
	 
	    //Display button if the row is not empty
	    @Override
	    protected void updateItem(Boolean t, boolean empty) {
	        super.updateItem(t, empty);
	        if(!empty){
	            setGraphic(cellButton);
	        }
	    }
	    private void visResults(String uri)
	    {
	    	Alert alert = new Alert(AlertType.INFORMATION);
	    	alert.setTitle("GeoVis");
	    	alert.setHeaderText("Visualisation available");
	    	alert.setContentText(uri);

	    	alert.showAndWait();
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

}
