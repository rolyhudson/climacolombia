package org.rolson.emr.emrcycle1;

import javafx.application.Application;

import javafx.stage.Stage;

public class GUIFX extends Application {
	public void start(Stage stage) {

		ClusterCoordinator coordinator = new ClusterCoordinator();
        new GUILayout(coordinator,stage);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
