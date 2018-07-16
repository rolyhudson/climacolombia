package org.rolson.emr.emrcycle1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUIFX extends Application {

	
	public void start(Stage stage) {

		WorkflowCoordinator coordinator = new WorkflowCoordinator();
        GUILayout layout = new GUILayout(coordinator,stage);
		//initUI(stage);
    }

    

    public static void main(String[] args) {
        launch(args);
    }
}
