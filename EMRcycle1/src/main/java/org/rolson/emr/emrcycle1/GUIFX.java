package org.rolson.emr.emrcycle1;


import javafx.animation.Animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.util.Duration;

public class GUIFX extends Application {

	
	public void start(Stage stage) {

		ClusterCoordinator coordinator = new ClusterCoordinator();
        GUILayout gui = new GUILayout(coordinator,stage);
    }
    public static void main(String[] args) {

        launch(args);
    }
    
}
