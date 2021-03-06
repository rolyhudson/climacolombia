package Coordination;

import javafx.scene.control.TableCell;
import java.util.Optional;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;

public class ClusterActionCell extends TableCell<Cluster, Boolean> {
	final Button cellButton;
	 ClusterCoordinator coord;
	 public ClusterActionCell(final TableView tblView,String name,ClusterCoordinator cc){
	    	
	    	cellButton = new Button(name);
	    	coord = cc;
	        cellButton.setOnAction(new EventHandler<ActionEvent>(){
	        	
	            @Override
	            public void handle(ActionEvent t) {
	            	String cmd = ((Button)t.getSource()).getText();
	                int selectedIndex = getTableRow().getIndex();
	                Cluster forAction = (Cluster) tblView.getItems().get(selectedIndex);
	                switch(cmd)
	                {
	                	
	                	case "stop":
	                		//STARTING, BOOTSTRAPPING, RUNNING, WAITING, TERMINATING, TERMINATED, and TERMINATED_WITH_ERRORS
	                		if(forAction.getStatus().equals("STARTING")
	                				||forAction.getStatus().equals("BOOTSTRAPPING")
	                				||forAction.getStatus().equals("RUNNING")
	                				||forAction.getStatus().equals("WAITING"))
	                		{
	                			if(alertMessage(forAction.getName(),"stop"))
	                			{
	                				//terminate
	                				coord.stopCluster(forAction);
	                			}
	                		}
	                		 break;
	                	
	                	case "stats":
	                		if(forAction.getStatus().equals("COMPLETED"))
	                		{
	                			if(alertMessage(forAction.getName(),"generate stats page for"))
	        					{
	            					//run stats
	                				//coord.visualStatsWorkflow(forAction);
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
	    
	    private boolean alertMessage(String text, String action)
		{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("About to "+action+ " :"+ text);
			alert.setContentText("Proceed?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
			   return true;
			} else {
			    return false;
			}
		}
}
