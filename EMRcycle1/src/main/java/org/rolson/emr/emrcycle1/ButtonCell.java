package org.rolson.emr.emrcycle1;

import java.util.Optional;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;

class ButtonCell extends TableCell<Workflow, Boolean> {
    final Button cellButton;
 ClusterCoordinator coord;
    ButtonCell(final TableView tblView,String name,ClusterCoordinator cc){
    	
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
                			if(alertMessage(forAction.getName()))
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
                			if(alertMessage(forAction.getName()))
                			{
                				//cancel step
                				//coord.stopWorkflow(forAction);
                			}
                		}
                		 break;
                	case "map":
                		if(forAction.getStatus().equals("COMPLETED"))
                		{
                			if(alertMessage(forAction.getName()))
        					{
            					//run vis
                				//coord.visualGeoWorkflow(forAction);
        					}
                		}
                		
                		break;
                	case "stats":
                		if(forAction.getStatus().equals("COMPLETED"))
                		{
                			if(alertMessage(forAction.getName()))
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
}
