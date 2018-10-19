package org.rolson.emr.emrcycle1;


import Coordination.ClusterCoordinator;
import javafx.animation.Animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.util.Duration;

public class GUIFX extends Application {

	ClusterCoordinator coordinator = new ClusterCoordinator();
	public void start(Stage stage) {

		
        GUILayout gui = new GUILayout(coordinator,stage);
    }
    public static void main(String[] args) {

        launch(args);
    }
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        // Save file
        coordinator.saveWorkflows();
    }
}
